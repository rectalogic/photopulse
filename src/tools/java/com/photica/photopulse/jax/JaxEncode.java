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

package com.photica.photopulse.jax;

import com.photica.photopulse.launcher.jax.JaxInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Encode jar files - as an alternative to obfuscation.
 * Prepends a header indicating encoded or not.
 */
public class JaxEncode {

    // Takes an output directory and a list of jar files, encodes them into output dir
    public static void main(String args[]) throws IOException {
        if (args.length < 2)
            usage();

        System.err.println("JaxEncoding " + (args.length-1) + " jar files");

        File filOutputDir = new File(args[0]);

        byte[] abData = new byte[4096];


        for (int i = 1; i < args.length; i++) {
            File filJar = new File(args[i]);
            InputStream is = new JaxInputStream(new FileInputStream(filJar));
            OutputStream os = new FileOutputStream(new File(filOutputDir, filJar.getName()));

            // Prefix with single byte indicating encoding
            os.write(JaxInputStream.JAX_PREFIX);

            int len = 0;
            while ((len = is.read(abData, 0, abData.length)) != -1)
                os.write(abData, 0, len);

            is.close();
            os.close();
        }
    }

    private static void usage() {
        System.err.println("Usage: JaxEncode <output-dir> <jarfile> [<jarfile> ...]");
        System.exit(1);
    }
}
