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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.ui.wizard.ftp;

import com.photica.photopulse.ui.wizard.WizardFileFilter;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.Util;
import com.photica.photopulse.exporter.HTMExporter;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import sun.net.ftp.FtpLoginException;


// Separate worker thread to do the upload
class UploadWorker extends Thread {
    private UploadDialog uploadDialog;
    private UploadSettings settings;
    private String errorMessages;

    // Root HTML file (foo.htm)
    private File htmlFile;
    // Name of associated foo_files directory
    private String htmlDirectory;
    // Associated HTML files in foo_files directory
    private File[] htmlDirectoryFiles;

    private byte[] bufChunk = new byte[1024 * 3];
    private long totalBytes;
    private long uploadedBytes;

    private static final int MAXPROGRESS = 1000;

    public UploadWorker(UploadDialog uploadDialog, UploadSettings settings, File htmlFile) {
        super("FTPUploadWorker");
        this.uploadDialog = uploadDialog;
        this.settings = settings;

        // Get HTML file and associated foo_files
        this.htmlFile = htmlFile;
        File directory = getDirectory(htmlFile);
        if (directory != null) {
            htmlDirectoryFiles = directory.listFiles();
            htmlDirectory = directory.getName();
        }

        // Total number of bytes to upload
        totalBytes = this.htmlFile.length();
        if (htmlDirectoryFiles != null) {
            for (int i = 0; i < htmlDirectoryFiles.length; i++)
                totalBytes += htmlDirectoryFiles[i].length();
        }

        this.uploadDialog.onSetProgressMax(MAXPROGRESS);
    }

    // Returns the corresponding foo_files directory for foo.htm, if it exists
    private File getDirectory(File htmlFile) {
        // If file ends with a valid HTML suffix, strip the suffix and
        // append "_files" to basename
        if (new WizardFileFilter(WizardFileFilter.FILTER_HTM).accept(htmlFile)) {
            String basename = Util.getBaseName(htmlFile);
            File directory = new File(htmlFile.getParent(), basename + HTMExporter.HTML_FILES_SUFFIX);
            if (directory.isDirectory())
                return directory;
            else
                return null;
        }
        return null;
    }

    // Overrides Thread
    public void run() {
        try {
            doUpload();
            //XXX issue http GET for settings.getPreset().getHookURLTemplate() (subst vars) if specified && errorMessages==null
            //XXX need threadsafe Authenticator too
        } finally {
            class Finisher implements Runnable {
                public void run() {
                    if (errorMessages != null)
                        uploadDialog.onUploadFail(errorMessages);
                    else
                        uploadDialog.onUploadSuccess();
                }
            }
            SwingUtilities.invokeLater(new Finisher());
        }
    }

    // Invoked on worker thread
    private void doUpload() {
        FtpClient ftp = null;
        try {
            try {
                ftp = new FtpClient(settings.getPreset().getHostname(), settings.getPreset().getPort());
                ftp.login(settings.getLogin(), settings.getPassword());
                ftp.binary();
            } catch (ConnectException e) {
                errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_PORT, e.getMessage().trim());
                return;
            } catch (UnknownHostException e) {
                errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_HOST, e.getMessage().trim());
                return;
            } catch (FtpLoginException e) {
                errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_LOGIN, e.getMessage().trim());
                return;
            } catch (IOException e) {
                errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_CONNECT, e.getMessage().trim());
                return;
            }

            // Change to users ftpDirectory
            // Loop over subdirs, creating as needed
            String ftpDirectory = settings.getPreset().populateDirectory(settings.getLogin());
            if (ftpDirectory != null) {
                StringTokenizer st = new StringTokenizer(ftpDirectory, "/");
                while (st.hasMoreTokens()) {
                    String subdir = st.nextToken();
                    try {
                        ftp.cd(subdir);
                    } catch (IOException e) {
                        try {
                            ftp.mkdir(subdir);
                            ftp.cd(subdir);
                        } catch (IOException e2) {
                            errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_CD,
                                new Object[] { ftpDirectory, e2.getMessage().trim() });
                            return;
                        }
                    }
                }
            }

            try {
                if (!uploadFile(ftp, htmlFile))
                    return;
            } catch (IOException e) {
                errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_FILE,
                    new Object[] { htmlFile.getName(), e.getMessage().trim() });
                return;
            }

            // Make HTML files subdirectory and upload into it
            if (htmlDirectory != null && htmlDirectoryFiles != null) {
                try {
                    try {
                        ftp.mkdir(htmlDirectory);
                    } catch (IOException e) {
                        // Ignore error, fails with FileNotFoundException if ftpDirectory already exists
                    }
                    ftp.cd(htmlDirectory);
                } catch (IOException e) {
                    errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_CD,
                        new Object[] { htmlDirectory, e.getMessage().trim() });
                    return;
                }

                for (int i = 0; i < htmlDirectoryFiles.length; i++) {
                    try {
                        if (!uploadFile(ftp, htmlDirectoryFiles[i]))
                            return;
                    } catch (IOException e) {
                        // Report filename with subdirectory
                        String fileName = htmlDirectoryFiles[i].getName();
                        File parentFile = htmlDirectoryFiles[i].getParentFile();
                        if (parentFile != null)
                            fileName = parentFile.getName() + File.separator + fileName;
                        errorMessages = UIMessages.getMessage(UIMessages.ERR_FTPEX_FILE,
                            new Object[] { fileName, e.getMessage().trim() });
                        return;
                    }
                }
            }
        } finally {
            if (ftp != null) {
                try {
                    ftp.closeServer();
                } catch (IOException e) {
                }
            }
        }
    }

    // Upload a file, return false if canceled
    private boolean uploadFile(FtpClient ftp, File uploadFile) throws IOException {
        OutputStream os = null;
        InputStream is = null;
        try {
            // Report the filename
            reportProgress(uploadFile, uploadedBytes);
            os = ftp.put(uploadFile.getName());
            is = new BufferedInputStream(new FileInputStream(uploadFile));
            int byteCount;
            while ((byteCount = is.read(bufChunk)) != -1) {
                os.write(bufChunk, 0, byteCount);
                if (uploadDialog.isCanceled())
                    return false;
                uploadedBytes += byteCount;
                // Report progress for current file
                reportProgress(null, uploadedBytes);
            }
        } finally {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
        }
        return true;
    }

    private void reportProgress(final File uploadFile, long uploadedBytes) {
        final int nProgress = (int)(((double)uploadedBytes / totalBytes) * MAXPROGRESS);
        class Progress implements Runnable {
            public void run() {
                uploadDialog.onReportProgress(uploadFile, nProgress);
            }
        }
        SwingUtilities.invokeLater(new Progress());
    }
}

class FtpClient extends sun.net.ftp.FtpClient {
    public FtpClient(String host, int port) throws IOException {
        super(host, port);
    }
    public void mkdir(String directory) throws IOException {
        issueCommandCheck("MKD " + directory);
    }
}
