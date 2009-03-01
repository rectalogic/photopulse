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

package com.photica.photopulse.ui.wizard;

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.wizard.ExtensionFileFilter;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    private static final long MILLISPERMONTH = 2592000000L;
    // Drop dead date - in milliseconds.
    // Set to 0 to disable completely.
    private static final long DROPDEADTIME = 0L;

    static {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (javax.swing.UnsupportedLookAndFeelException e) {
        }
    }

    public static void main(final String args[]) {
        // Exit if a drop dead date was specified and we are past it
        if (DROPDEADTIME != 0) {
            if (DROPDEADTIME <= System.currentTimeMillis()) {
                ErrorDialog.showErrorDialog(null,
                    UIMessages.getMessage(UIMessages.ERR_DROPDEAD));
                System.exit(1);
            }
        }

        class WizardLauncher implements Runnable {
            public void run() {
                // Windows shell may invoke us with -project (project filename).
                // Also support -photos (file containing list of photo files).
                if (args.length == 2) {
                    if ("-project".equals(args[0]))
                        WizardUI.init(new File(args[1]));
                    else if ("-photos".equals(args[0]))
                        WizardUI.init(parsePhotoFileList(new File(args[1])));
                }
                else
                    WizardUI.init((File)null);
            }
        }
        try {
            // Invoke synchronously so the launcher will display the splash until we are done
            SwingUtilities.invokeAndWait(new WizardLauncher());
        } catch (InterruptedException e) {
            PhotoPulse.logException(e);
        } catch (InvocationTargetException e) {
            PhotoPulse.logException(e);
        }
    }

    // Called from native code.
    // External processes can pulse a named Win32 event which causes photopulse.exe to call this method to raise our window.
    public static void raisePhotoPulse() {
        class RaiseUI implements Runnable {
            public void run() {
                WizardUI.getInstance().raiseWindow();
            }
        };
        try {
            SwingUtilities.invokeAndWait(new RaiseUI());
        } catch (InterruptedException e) {
        } catch (InvocationTargetException e) {
        }
    }

    // Read list of files/directories from the specified file, one filename per line
    public static File[] parsePhotoFileList(File photoListFile) {
        BufferedReader br = null;
        try {
            try {
                ArrayList<File> fileList = new ArrayList<File>();
                ExtensionFileFilter ffImages = new ExtensionFileFilter(ExtensionFileFilter.FILTER_IMAGES);
                br = new BufferedReader(new FileReader(photoListFile));
                String fileName;
                while ((fileName = br.readLine()) != null) {
                    File photoFile = new File(fileName);
                    // If it's a directory, add it's image contents
                    if (photoFile.isDirectory()) {
                        File[] photoFiles = photoFile.listFiles(ffImages);
                        if (photoFiles != null) {
                            Arrays.sort(photoFiles);
                            fileList.addAll(Arrays.asList(photoFiles));
                        }
                    }
                    else
                        fileList.add(photoFile);
                }

                if (fileList.isEmpty())
                    return null;
                return fileList.toArray(new File[fileList.size()]);
            } finally {
                if (br != null)
                    br.close();
            }
        } catch (IOException e) {
            return null;
        }
    }
}
