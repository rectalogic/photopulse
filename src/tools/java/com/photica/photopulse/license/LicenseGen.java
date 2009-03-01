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

package com.photica.photopulse.license;

import com.photica.photopulse.License;
import com.photica.photopulse.Branding;

import java.math.BigInteger;

public class LicenseGen {

    // Private RSA key (8 bit) for hash byte
    private static final BigInteger PRIVATE_HASH = BigInteger.valueOf(24353);

    // Private RSA key (512 bit) for timestamp and userdata wrapping
    private static final BigInteger PRIVATE_WRAP = new BigInteger("2klqm6ps1y0j4hmoekbh9gaejvdihyib70q7thx27g5zovtsyym9l58xmd1456gfziicw9blizm0b5to4f6m8tzl5729117otwho6xp0i588n9owldqk1kudggkvj02xvmpau7u6y9ly1yxdmkwpxfsc90c9x5nmhia2a5awrn9gsmo5cplzojub090mv26winl35t", 36);

    public static void main(String args[]) {
        System.err.println("LicenseGen for product " + Branding.PRODUCT_ID);

        if (args.length < 1)
            usage();
        // Decode a license key
        else if (args.length == 1) {
            try {
                long serialNumber = License.parse(License.cleanString(args[0])).getSerial();
                System.out.println("Serial: " + serialNumber);
                System.exit(0);
            } catch (License.InvalidLicenseException e) {
                System.err.println("Invalid license: " + e.getMessage());
                System.exit(1);
            }
        }

        License.Type licenseType = null;
        try {
            licenseType = Enum.valueOf(License.Type.class, args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            usage();
        }

        String licenseString = null;
        long serialNumber = 0;

        switch (licenseType) {
            case ANONYMOUS:
                if (args.length != 2)
                    usage();
                serialNumber = Long.parseLong(args[1]);
                licenseString = encodeAnonymousLicense(serialNumber);
                break;

            case INTERIM: {
                if (args.length != 3)
                    usage();

                serialNumber = Long.parseLong(args[1]);
                int expirationTime = 0;
                try {
                    expirationTime = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    usage();
                }

                // 16 bit expiration
                expirationTime = convertExpiration(expirationTime);
                licenseString = encodeInterimLicense(serialNumber, expirationTime);
            } break;

            case TIMESTAMPED: {
                if (args.length != 3)
                    usage();

                serialNumber = Long.parseLong(args[1]);
                int expirationTime = 0;
                try {
                    expirationTime = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    usage();
                }

                // 16 bit expiration
                expirationTime = convertExpiration(expirationTime);
                licenseString = encodeTimestampedLicense(serialNumber, expirationTime);
            } break;

            case USER: {
                if (args.length != 3)
                    usage();
                serialNumber = Long.parseLong(args[1]);
                licenseString = encodeUserLicense(serialNumber, args[2]);
            } break;

            default:
                usage();
        }

        System.out.println(licenseString);

        // Sanity check
        try {
            if (serialNumber != License.parse(License.cleanString(licenseString)).getSerial())
                System.err.println("ERROR - decoded serial does not match");
        } catch (License.InvalidLicenseException e) {
            System.err.println("ERROR - failed to decode license - " + e.getErrorCode());
        }
    }

    private static void usage() {
        System.err.println("Usage:");
        System.err.println("\tLicenseGen <licensekey>");
        System.err.println("\tLicenseGen anonymous <32bit serial>");
        System.err.println("\tLicenseGen interim <32bit serial> <expiration weeks from now>");
        System.err.println("\tLicenseGen timestamped <32bit serial> <expiration weeks from now>");
        System.err.println("\tLicenseGen user <32bit serial> <user data>");
        System.exit(1);
    }

    // "W" + RSA encrypted([crypto hash byte][license 64bit][timestamp 16bit])
    public static String encodeInterimLicense(long serialNumber, int expirationTime) {
        BigInteger licenseBI = encode(serialNumber, License.VERSION);

        // Encode expiration into low order 16 bits
        licenseBI = licenseBI.shiftLeft(16).or(BigInteger.valueOf(expirationTime));

        // RSA encrypt the whole thing
        licenseBI = licenseBI.modPow(PRIVATE_WRAP, License.MODULUS_WRAP);

        return (License.PREFIX_INTERIM + licenseBI.toString(36)).toUpperCase();
    }

    // "T" + RSA encrypted(flipBit0([crypto hash byte][license 64bit])[timestamp 16bit])
    public static String encodeTimestampedLicense(long serialNumber, int expirationTime) {
        BigInteger licenseBI = encode(serialNumber, License.VERSION).flipBit(0);

        // Encode expiration into low order 16 bits
        licenseBI = licenseBI.shiftLeft(16).or(BigInteger.valueOf(expirationTime));

        // RSA encrypt the whole thing
        licenseBI = licenseBI.modPow(PRIVATE_WRAP, License.MODULUS_WRAP);

        return (License.PREFIX_TIMESTAMPED + licenseBI.toString(36)).toUpperCase();
    }

    // "P" + [crypto hash byte][license 64bit]
    public static String encodeAnonymousLicense(long serialNumber) {
        return (License.PREFIX_ANONYMOUS + encode(serialNumber, License.VERSION).toString(36)).toUpperCase();
    }

    // "BEGIN LICENSE" + <userdata> + "*" + (RSA encrypted ([crypto hash byte][license 64bit])^<userdata-hash>) + "END LICENSE"
    public static String encodeUserLicense(long serialNumber, String userData) {
        BigInteger licenseBI = encode(serialNumber, License.VERSION);

        // Avoid negative hashcodes
        long lUserHash = License.cleanString(userData).hashCode() & 0xFFFFFFFFL;

        // XOR user data hash with license
        licenseBI = licenseBI.xor(BigInteger.valueOf(lUserHash));

        // RSA encrypt the whole thing
        licenseBI = licenseBI.modPow(PRIVATE_WRAP, License.MODULUS_WRAP);

        // Convert data to base36 and insert line breaks so it formats well for license dialog
        final int LINE_LENGTH = 35;
        StringBuilder sbLicense = new StringBuilder(licenseBI.toString(36).toUpperCase());
        int length = sbLicense.length();
        for (int i = LINE_LENGTH; i < length; i += LINE_LENGTH) {
            sbLicense.insert(i, '\n');
            i++;
        }

        return License.PREFIX_USER.toUpperCase() + "\n"
            + userData + "\n"
            + License.INFIX_USER + "\n"
            + sbLicense.toString() + "\n"
            + License.POSTFIX_USER.toUpperCase();
    }

    /**
     * Encode 32bit serial and 8bit version.
     * Return 64bit license key in low order bits and
     * RSA encrypted hash byte in high order bits.
     */
    public static BigInteger encode(long serialNumber, int version) {
        if (serialNumber != (serialNumber & 0xffffffffL)
                || version != (version & 0xff))
            throw new IllegalArgumentException("wrong number of bits in serial");

        // Compute 32bit hash
        long hash = License.hash(serialNumber, version);
        if (hash == 0)
            throw new IllegalArgumentException("invalid hashcode");

        // Encrypt high order hash byte separately
        BigInteger encryptedHashBI = BigInteger.valueOf((hash>>>24) & 0xff);
        encryptedHashBI = encryptedHashBI.modPow(PRIVATE_HASH, License.MODULUS_HASH);

        // Strip high order hash byte
        hash &= 0x00ffffff;

        long scrambledBits = 0;

        // Encode 32 bit serial number into every even bit
        scrambledBits |= encodeBits(serialNumber, 32) << 1;

        // Encode 8 bit version number into low order odd bits
        scrambledBits |= encodeBits(version, 8);

        // Encode 24 bit hashcode into high order odd bits
        scrambledBits |= encodeBits(hash, 24) << 16;

        // Return encrypted hash and 64bit license key
        return encryptedHashBI.shiftLeft(64).or(BigInteger.valueOf(scrambledBits));
    }

    private static long encodeBits(long bits, int bitCount) {
        long result = 0;
        long bitMask = 0x01;
        long resultMask = 0x01;
        for (int i = 0; i < bitCount; i++) {
            if ((bits & bitMask) != 0)
                result |= resultMask;
            resultMask <<= 2;
            bitMask <<= 1;
        }
        return result;
    }

    /**
     * Converts the expiration from number of weeks from today, to number of weeks from EPOCH.
     * @param expirationTime Number of weeks from now
     * @return Number of weeks from EPOCH
     */
    public static int convertExpiration(int expirationTime) {
        // Add weeks since epoch to expiration
        expirationTime = expirationTime
            + (int)((System.currentTimeMillis() - License.EPOCH) / License.MILLIS_PER_WEEK);

        if (expirationTime != (expirationTime & 0xffff))
            throw new IllegalArgumentException("wrong number of bits in expiration");

        return expirationTime;
    }

    //XXX debugging
    public static void dumpBits(String str, long bits, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.insert(0, (int)((bits>>i) & 0x01));
            if ((i+1) % 4 == 0)
                sb.insert(0, " ");
        }
        System.out.println(str);
        System.out.println(sb);
    }
}