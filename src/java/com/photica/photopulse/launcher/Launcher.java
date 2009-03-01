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

package com.photica.photopulse.launcher;

import com.photica.photopulse.Branding;

import javax.swing.JOptionPane;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Launches photopulse using a custom protocol handler (jax:) that decodes the jar files.
 * This class is not encoded - so don't put anything critical (licensing) here.
 */
public class Launcher {
    private static ClassLoader LOADER;

    /**
     * Initialize custom ClassLoader to load jax encrypted classes
     */
    private static synchronized void initLoader() throws MalformedURLException {
        if (LOADER != null)
            return;

        // Construct our installation dir
        File installDir = null;
        String homePath = System.getProperty("photopulse.home");
        if (homePath == null) {
            installDir = new File(".");
            System.out.println("Warning: Launcher - photopulse.home not set, using " + installDir);
        }
        else
            installDir = new File(homePath);

        File mainJarFile = new File(installDir, "lib" + File.separator + "ui-wizard.jar");
        URL mainJarURL = new URL("jax:" + mainJarFile.getAbsolutePath());

        // Create classloader with the jar.
        // It will use the manifest Class-Path to find everything else
        LOADER = new URLClassLoader(new URL[] { mainJarURL });

        // This ensures that Class.forName etc. works on AWT thread
        Thread.currentThread().setContextClassLoader(LOADER);
    }

    /**
     * Called from native code to access jax encrypted classes.
     * Private for extra obscurity.
     */
    private static Class loadClass(String strClassName) throws ClassNotFoundException {
        try {
            initLoader();
            return LOADER.loadClass(strClassName);
        } catch (MalformedURLException e) {
            throw new ClassNotFoundException("Failed to initialize ClassLoader", e);
        }
    }

    /**
     * This is called from the native launcher EXE, make it private for extra obscurity
     */
    private static void main(String args[]) {
        try {
            // Handler for exceptions on AWT thread
            System.setProperty("sun.awt.exception.handler",
                "com.photica.photopulse.ui.wizard.AWTExceptionHandler");

            // Load the main class
            // XXX get Main-Class attr from jar manifest
            Class mainClass = loadClass("com.photica.photopulse.ui.wizard.Main");

            // Invoke its main()
            mainClass.getMethod("main", new Class[] { String[].class })
                .invoke(null, new Object[] { args });
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            JOptionPane.showMessageDialog(null,
                        "Internal error, exiting.\n" + e.toString()
                        + (cause != null ? "\n" + cause.toString() : ""),
                    Branding.PRODUCT_NAME + " Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
