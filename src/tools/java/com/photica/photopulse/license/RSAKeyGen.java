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
import java.math.BigInteger;
import java.security.SecureRandom;

public class RSAKeyGen {
    private static final int CERTAINTY = 100;

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: RSAKeyGen <bits>");
            System.exit(1);
        }
        int nBits = Integer.parseInt(args[0]);

        SecureRandom sr =  new SecureRandom();

        BigInteger d = null;
        BigInteger n = null;
        BigInteger e = null;

        while (true) {
            // Generate 2 prime numbers
            BigInteger p = new BigInteger(nBits, CERTAINTY, sr);
            BigInteger q = new BigInteger(nBits, CERTAINTY, sr);

            // Make sure they are different
            while (p.equals(q))
                q = new BigInteger(nBits, CERTAINTY, sr);

            n = p.multiply(q);
            BigInteger pm1 = p.subtract(BigInteger.ONE);
            BigInteger qm1 = q.subtract(BigInteger.ONE);
            BigInteger phi = pm1.multiply(qm1);

            // This will throw an exception if d does not exists,
            // catch the exception and go another round
            try {
                d = License.PUBLIC.modInverse(phi);
                break;
            } catch (ArithmeticException ex) {
            }
        }

        // Private key is d,n
        System.out.println("private key (d,n)");
        System.out.println("d\n  dec=" + d.toString(10) + "\n  base36=" + d.toString(36));
        System.out.println("n\n  dec=" + n.toString(10) + "\n  base36=" + n.toString(36));

        System.out.println();

        // Public key is e,n
        System.out.println("public key (e,n)");
        System.out.println("e\n  dec=" + License.PUBLIC.toString(10) + "\n  base36=" + License.PUBLIC.toString(36));
        System.out.println("n\n  dec=" + n.toString(10) + "\n  base36=" + n.toString(36));
    }
}