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

import com.photica.photopulse.progress.ProgressIndicator;
import com.photica.photopulse.progress.ProgressReporter;
import com.photica.photopulse.ui.wizard.worker.WorkerProgress;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.text.MessageFormat;

public class WizardProgressIndicator implements WorkerProgress, ProgressIndicator, ActionListener {

    private static final MessageFormat FORMAT = new MessageFormat(UIMessages.getMessage(UIMessages.UI_PROGRESS_PERCENT));

    private static final int PROGRESS_MAX = 1000;
    private static final int PROGRESS_SIZE = 300;

    private JDialog progressDialog;
    private JProgressBar progressBar;
    private boolean isCanceled = false;

    public WizardProgressIndicator(JFrame parent) {
        isCanceled = false;

        progressBar = new JProgressBar(0, PROGRESS_MAX);
        progressBar.setStringPainted(true);
        // XXX AlloyProgressBarUI does not draw the string if value==0, leave this in case they fix it
        setProgress(0);
        Dimension size = progressBar.getPreferredSize();
        size.width = PROGRESS_SIZE;
        progressBar.setPreferredSize(size);
        
        Object oMessages = new Object[] {
            new JLabel(UIMessages.getMessage(UIMessages.UI_PROGRESS_LABEL)),
            progressBar
        };

        JButton btnCancel = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_CANCEL));
        btnCancel.addActionListener(this);

        Object[] aoOptions = new Object[] {
            btnCancel
        };

        JOptionPane opProgress = new JOptionPane(oMessages,
            JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION,
            null, aoOptions, null);

        progressDialog = new JDialog(parent,
            UIMessages.getMessage(UIMessages.UI_PROGRESS_TITLE), true);
        Container paneContent = progressDialog.getContentPane();
        paneContent.setLayout(new BorderLayout());
        paneContent.add(opProgress, BorderLayout.CENTER);
        progressDialog.pack();
        progressDialog.setLocationRelativeTo(parent);

        // Make sure the user can't close the dialog - it should only be closed
        // using closeProgress()
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // Install ourself
        ProgressReporter.setProgressIndicator(this);
    }

    /**
     * Display progress dialog, must be invoked from Swing thread.
     * This method blocks until closeProgress() is called
     */
     // Implements WorkerProgress
    public void showProgress() {
        // Avoid race condition in case closeProgress was already called
        if (progressDialog.isDisplayable())
            progressDialog.setVisible(true);
    }

    /**
     * Unblock and dispose the progress dialog.
     * Must be invoked from Swing thread.
     */
     // Implements WorkerProgress
    public void closeProgress() {
        // Dispose so isDisplayable returns false
        progressDialog.dispose();
        // Uninstall ourself
        ProgressReporter.setProgressIndicator(null);
    }

    public void addWindowListener(WindowListener l) {
        progressDialog.addWindowListener(l);
    }

    public void removeWindowListener(WindowListener l) {
        progressDialog.removeWindowListener(l);
    }

    // Invoked when Cancel button pressed
    // Implements ActionListener
    public void actionPerformed(ActionEvent e) {
        if (isCanceled)
            return;

        // Do not close dialog - just set flag
        isCanceled = true;
        // Disable the button for feedback
        ((Component)e.getSource()).setEnabled(false);
    }

    // Implements ProgressIndicator
    public void updateProgress(final float progress) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setProgress(progress);
            }
        });
    }

    // Must be called on Swing thread.
    private void setProgress(float progress) {
        progressBar.setValue((int)(progress * PROGRESS_MAX));
        progressBar.setString(FORMAT.format(new Object[] { new Float(progress) }));
    }

    // Implements ProgressIndicator
    // Can be called from any thread
    public boolean isCanceled() {
        return isCanceled;
    }
}
