/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Photica Photopulse.
 *
 * The Initial Developer of the Original Code is
 * Photica Inc.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.photica.photopulse;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.Preferences;

public final class License {

    /** License types */
    public enum Type {
        /**
         * Permanent license containing user specific data
         */
        USER,
        /**
         * Permanent anonymous license (e.g. for CDROM retail)
         */
        ANONYMOUS,
        /**
         * Interim license, contains a timestamp after which the license will not be accepted on input.
         * If the license was entered prior to expiration, it will continue to work.
         * Used as an interim license until the user gets a USER license.
         */
        INTERIM,
        /**
         * Temporary license. Contains a timestamp after which the license expires.
         * Used for 30day trial with all features enabled.
         */
        TIMESTAMPED,
    };

    // License version number (8 bits)
    public static final int VERSION = Branding.LICENSE_VERSION;

    // Time from which timestamped licenses count
    public static final long EPOCH = 1018929600000L;

    // Milliseconds per week
    public static final long MILLIS_PER_WEEK = 1000L * 60 * 60 * 24 * 7;

    // Shared RSA modulus (8 bit) for hash byte
    public static final BigInteger MODULUS_HASH = BigInteger.valueOf(27263);

    // Shared RSA modulus (512 bit) for timestamp and userdata wrapping
    public static final BigInteger MODULUS_WRAP = new BigInteger("s4c8z8jq19imm4xg0im5aw556gaxksv6jgo2dtdynpnmxkerb86nsetcgnrbmhcmmcix3sisthihf0lrzlo4znqepklqolnm86n4dgabga8cctyf3dtivp3yp4mdor1xi8z18pu88q1bnkdoze4xsiq2ay0spnb9c4gt4ztfff8j0mpi14kk1e5qwpgb5hf4ej9n65", 36);

    // Public RSA key for hash byte and timestamp wrapping
    public static final BigInteger PUBLIC = BigInteger.valueOf(65537);

    // License prefix to identify wrapped encrypted timestamped vs. normal
    // vs. user-data encrypted
    public static final String PREFIX_ANONYMOUS = "p";
    public static final String PREFIX_INTERIM = "w";
    public static final String PREFIX_TIMESTAMPED = "t";
    public static final String PREFIX_USER = "begin_license";
    public static final String POSTFIX_USER = "end_license";
    public static final String INFIX_USER = "*";

    private static final String PREF_LICENSE = "License";

    private Type type;
    private String licenseString;
    private long serialNumber;
    private String serialString;
    private int expirationTime;

    public static class InvalidLicenseException extends Exception {
        public static final int LIC_NULL = 100;
        public static final int LIC_ANONYMOUS_DECODE = 101;
        public static final int LIC_INTERIM_DECODE = 102;
        public static final int LIC_USER_NOINFIX = 103;
        public static final int LIC_USER_NOPOSTFIX = 104;
        public static final int LIC_USER_DECODE = 105;
        public static final int LIC_UNKNOWN_PREFIX = 106;
        public static final int LIC_NUMBERFORMAT = 107;
        public static final int LIC_TIMESTAMPED_DECODE = 109;
        public static final int LIC_TIMESTAMPED_EXPIRED = 110;

        private int errorCode;

        public InvalidLicenseException(int errorCode) {
            this.errorCode = errorCode;
        }
        public int getErrorCode() {
            return errorCode;
        }
    }

    // Parse encoded license
    // String must be whitespace/hyphen stripped and lowercase
    private License(String licenseString) throws InvalidLicenseException {
        if (licenseString == null || licenseString.length() == 0)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_NULL);

        type = decodeType(licenseString);
        if (type == null)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_UNKNOWN_PREFIX);

        try {
            switch (type) {
            case ANONYMOUS:
                decodeAnonymousLicense(licenseString);
                break;
            case INTERIM:
                decodeInterimLicense(licenseString);
                break;
            case TIMESTAMPED:
                decodeTimestampedLicense(licenseString);
                break;
            case USER:
                decodeUserLicense(licenseString);
                break;
            default:
                throw new InvalidLicenseException(InvalidLicenseException.LIC_UNKNOWN_PREFIX);
            }
        } catch (NumberFormatException e) {
            throw new InvalidLicenseException(InvalidLicenseException.LIC_NUMBERFORMAT);
        }

        this.licenseString = licenseString;
    }

    private void decodeAnonymousLicense(String licenseString) throws InvalidLicenseException {
        BigInteger licenseBI = new BigInteger(licenseString.substring(1), 36);
        serialNumber = decodeSerial(licenseBI);
        if (serialNumber == -1)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_ANONYMOUS_DECODE);
    }

    private void decodeInterimLicense(String licenseString) throws InvalidLicenseException {
        BigInteger licenseBI = new BigInteger(licenseString.substring(1), 36);

        // RSA decrypt
        licenseBI = licenseBI.modPow(PUBLIC, MODULUS_WRAP);

        // Extract timestamp from lower 16bits
        expirationTime = licenseBI.and(BigInteger.valueOf(0xffff)).intValue();

        // Now shift off timestamp and decode unwrapped license
        licenseBI = licenseBI.shiftRight(16);
        serialNumber = decodeSerial(licenseBI);
        if (serialNumber == -1)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_INTERIM_DECODE);
    }

    private void decodeTimestampedLicense(String licenseString) throws InvalidLicenseException {
        BigInteger licenseBI = new BigInteger(licenseString.substring(1), 36);

        // RSA decrypt
        licenseBI = licenseBI.modPow(PUBLIC, MODULUS_WRAP);

        // Extract timestamp from lower 16bits
        expirationTime = licenseBI.and(BigInteger.valueOf(0xffff)).intValue();

        // Now shift off timestamp and decode unwrapped license
        licenseBI = licenseBI.shiftRight(16).flipBit(0);
        serialNumber = decodeSerial(licenseBI);
        if (serialNumber == -1)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_TIMESTAMPED_DECODE);

        if (isExpired())
            throw new InvalidLicenseException(InvalidLicenseException.LIC_TIMESTAMPED_EXPIRED);
    }

    private void decodeUserLicense(String licenseString) throws InvalidLicenseException {
         if (!licenseString.endsWith(POSTFIX_USER))
            throw new InvalidLicenseException(InvalidLicenseException.LIC_USER_NOPOSTFIX);

        // Find the infix separator, search from end since we don't
        // know what userdata contains
        int infixIndex = licenseString.lastIndexOf(INFIX_USER);
        if (infixIndex == -1)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_USER_NOINFIX);

        // Decode the license between the infix and postfix markers
        BigInteger licenseBI = new BigInteger(licenseString.substring(infixIndex + 1,
            licenseString.length() - POSTFIX_USER.length()), 36);

        // RSA decrypt
        licenseBI = licenseBI.modPow(PUBLIC, MODULUS_WRAP);

        // Extract user data
        String userData = licenseString.substring(PREFIX_USER.length(), infixIndex);

        // Avoid negative hashcodes
        long userHash = userData.hashCode() & 0xFFFFFFFFL;

        // XOR user data hash with license
        licenseBI = licenseBI.xor(BigInteger.valueOf(userHash));

        // Decode license
        serialNumber = decodeSerial(licenseBI);
        if (serialNumber == -1)
            throw new InvalidLicenseException(InvalidLicenseException.LIC_USER_DECODE);
    }

    public static License parse(String licenseString) throws InvalidLicenseException {
        return new License(licenseString);
    }

    public static Type decodeType(String licenseString) {
        if (licenseString == null)
            return null;
        else if (licenseString.startsWith(PREFIX_ANONYMOUS))
            return Type.ANONYMOUS;
        else if (licenseString.startsWith(PREFIX_INTERIM))
            return Type.INTERIM;
        else if (licenseString.startsWith(PREFIX_TIMESTAMPED))
            return Type.TIMESTAMPED;
        else if (licenseString.startsWith(PREFIX_USER))
            return Type.USER;
        else
            return null;
    }

    // Strip whitespace and hyphens from license, convert to lower case
    public static String cleanString(String licenseString) {
        StringBuilder sb = new StringBuilder(licenseString);
        int length = licenseString.length();
        // Traverse buffer backwards removing hyphens and whitespace
        // and converting to lower case
        for (int i = length - 1; i >= 0; i--) {
            char ch = sb.charAt(i);
            if (ch == '-' || Character.isWhitespace(ch))
                sb.deleteCharAt(i);
            else
                sb.setCharAt(i, Character.toLowerCase(ch));
        }
        return sb.toString();
    }

    public String getSerialString() {
        if (serialString == null)
            serialString = BigInteger.valueOf(serialNumber).toString(36).toUpperCase();
        return serialString;
    }

    public long getSerial() {
        return serialNumber;
    }

    public boolean isExpired() {
        if (expirationTime == 0)
            return false;
        if (System.currentTimeMillis() > EPOCH + expirationTime * MILLIS_PER_WEEK)
            return true;
        return false;
    }

    public Type getType() {
        return type;
    }

    private static String loadPref() {
        Preferences prefs = Preferences.systemNodeForPackage(License.class);
        String licenseString = prefs.get(PREF_LICENSE, null);
        // For backwards compatibility, check user node if not in system node
        if (licenseString == null) {
            prefs = Preferences.userNodeForPackage(License.class);
            return prefs.get(PREF_LICENSE, null);
        }
        else
            return licenseString;
    }

    private static void removePref() {
        try {
            Preferences prefs = Preferences.systemNodeForPackage(License.class);
            prefs.remove(PREF_LICENSE);
        } catch (IllegalStateException e) {
        }
    }

    public static License load() {
        try {
            // Normally we would load license from prefs, but for the open source app we'll hardcode a valid license.
            return parse("begin_licensephotopulse*8ef0fw1imjkbbh1j6dzwp9vzjx2gw19guaw1i2mzugx9buhslsb63bxu3ped08cpowf2e72tqdb1gab70xqlix7szec0l1wdo67r2au2ndwvxwqrh6hv843w1w1iatkcuztv464zmbbdgmrnfyv16t24ce8v8bvfbacwjo29eprwb3i84kgggt1wa8xzgrwmhiskyiend_license");
            //return parse(loadPref());
        } catch (InvalidLicenseException e) {
            // Remove invalid license from prefs
            removePref();
            return null;
        }
    }

    public void store() {
        // Store in system node.
        // This is normally not writeable by XP Limited users,
        // but the photopulse installer pre-creates the registry entries with proper permissions.
        Preferences prefs = Preferences.systemNodeForPackage(getClass());
        prefs.put(PREF_LICENSE, licenseString);
    }

    /**
     * Decode 64bit license plus RSA encrypted hash byte into 32 bit serial and 8 bit version
     * input: [RSA hash byte][scrambled 64bit license]
     * output: [32bit serial]
     * Return -1 if invalid.
     */
    private long decodeSerial(BigInteger licenseBI) {

        // Extract scrambled serial from lower 64 bits
        long scrambledSerial = licenseBI.and(BigInteger.valueOf(0xffffffffffffffffL)).longValue();

        // Extract encrypted hash byte from upper bytes
        BigInteger encryptedHashBI = licenseBI.shiftRight(64);

        // Decode 32 bit serial number from every even bit
        long serial = decodeBits(scrambledSerial, 0x02, 32);

        // Decode 8 bit version number from low order odd bits
        int version = (int)decodeBits(scrambledSerial, 0x01, 8);

        // Decode 24 bit hashcode from high order odd bits
        long hash = decodeBits(scrambledSerial, 0x010000, 24);

        // RSA decrypt extra hash byte and include it in high order hash bits
        hash |= encryptedHashBI.modPow(PUBLIC, MODULUS_HASH).intValue() << 24;

        // Check retrieved hash against computed hash
        long computedHash = hash(serial, version);
        if (computedHash == 0 || hash != computedHash)
            return -1;

        // Check version
        if (version != VERSION)
            return -1;

        return serial;
    }

    // The mask should have one bit set, it is shifted by 2 each time through the loop
    // and the corresponding scrambled bit is added to the result.
    private static long decodeBits(long scrambledBits, long scrambledBitMask, int bitCount) {
        long result = 0;
        long resultMask = 0x01;
        for (int i = 0; i < bitCount; i++) {
            if ((scrambledBits & scrambledBitMask) != 0)
                result |= resultMask;
            resultMask <<= 1;
            scrambledBitMask <<= 2;
        }
        return result;
    }

    /**
     * Compute 32 bit hash for 32 bit serial and 8 bit version
     */
    public static long hash(long serial, int version) {
        MessageDigest md;
        try {
            // 16 byte digest length
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return 0;
        }

        md.update((byte)(serial & 0x0ff));
        md.update((byte)((serial>>>8) & 0x0ff));
        md.update((byte)((serial>>>16) & 0x0ff));
        md.update((byte)((serial>>>24) & 0x0ff));
        md.update((byte)(version & 0x0ff));

        byte[] digestBytes = md.digest();

        // Compose 4 bytes from the 16 bytes of digest
        return 0x0ffffffff &
            (((long)digestBytes[0] & 0xff)
            | ((long)(digestBytes[3] << 8) & 0xff00)
            | ((long)(digestBytes[5] << 16) & 0xff0000)
            | ((long)(digestBytes[7] << 24) & 0xff000000));
    }
}