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

import com.photica.photopulse.flash.ShowGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public class PhotoPulse {

    public static final File EXCEPTION_LOG;

    public static final File INSTALLDIR;
    public static License LICENSE;


    static {
        // Locate the jar we reside in and construct our installation dir.
        String strInstallDir = System.getProperty("photopulse.home");
        if (strInstallDir == null) {
            INSTALLDIR = new File(".");
            System.out.println("Warning: PhotoPulse - photopulse.home not set, using " + INSTALLDIR);
        }
        else
            INSTALLDIR = new File(strInstallDir);

        // Clean up exception log if older than a week
        File logDir = new File(System.getProperty("user.home", INSTALLDIR.getPath()));
        EXCEPTION_LOG = new File(logDir, "photopulse-errors.log");
        long lModified = EXCEPTION_LOG.lastModified();
        if (lModified > 0
                && (System.currentTimeMillis() - lModified) > License.MILLIS_PER_WEEK)
            EXCEPTION_LOG.delete();

        // Load license key if any
        LICENSE = License.load();

        // Force JGenerator to initialize early. Use Class.forName so this does not get optimized out.
        try {
            Class.forName(ShowGenerator.class.getName());
        } catch (ClassNotFoundException e) {
        }
    }

    // Logs exceptions to a log file
    public static void logException(Throwable throwable) {
        PrintWriter pwLog = null;
        try {
            pwLog = new PrintWriter(new FileWriter(EXCEPTION_LOG, true));
            pwLog.println(new Date().toString() + " - " + getVersion());
            throwable.printStackTrace(pwLog);
        } catch (IOException e) {
        } finally {
            if (pwLog != null)
                pwLog.close();
        }
    }

    public static String getVersion() {
        String strVersion = "internal-build";
        Package pkg = PhotoPulse.class.getPackage();
        if (pkg != null) {
            String strPkgVersion = pkg.getImplementationVersion();
            if (strPkgVersion == null || strPkgVersion.length() == 0)
                return strVersion;
            return strPkgVersion;
        }
        return strVersion;
    }
}
