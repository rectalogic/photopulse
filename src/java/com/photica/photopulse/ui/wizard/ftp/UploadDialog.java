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

package com.photica.photopulse.ui.wizard.ftp;

import com.photica.photopulse.ui.wizard.DisposeHandler;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.HyperLabel;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.ui.LabelTextArea;
import com.photica.ui.ToolButton;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.GridLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class UploadDialog extends JDialog {
    private static final String PANEL_PROGRESS = "progress";
    private static final String PANEL_SUCCESS = "success";

    private UploadSettings settings;
    private File htmlFile;

    private boolean isCanceled = false;

    private JLabel progressLabel;
    private JProgressBar progressBar;

    private UploadDialog(Frame parent, UploadSettings settings, File htmlFile) {
        super(parent, UIMessages.getMessage(UIMessages.UI_FTPU_TITLE));
        this.settings = settings;
        this.htmlFile = htmlFile;

        buildUI();

        setLocationRelativeTo(parent);
        setVisible(true);

        // Pass a clone of settings in case user changes while we are uploading
        new UploadWorker(this, (UploadSettings)settings.clone(), htmlFile).start();
    }

    public static void showDialog(Frame parent, File htmlFile, UploadSettings settings) {
        // Show settings dialog if we don't have everything we need
        showDialog(parent, htmlFile, !settings.isValid(), settings);
    }

    private static void showDialog(Frame parent, File htmlFile, boolean showSettings, UploadSettings settings) {
        // Get valid settings if needed
        if (showSettings) {
            UploadSettingsDialog settingsDialog = new UploadSettingsDialog(settings);
            if (!settingsDialog.showDialog(parent))
                return;
        }

        new UploadDialog(parent, settings, htmlFile);
    }

    void onSetProgressMax(int max) {
        progressBar.setMaximum(max);
    }

    void onReportProgress(File uploadFile, int progress) {
        if (uploadFile != null) {
            // Display subdirectory for associated files
            String fileName = uploadFile.getName();
            if (uploadFile != htmlFile) {
                File parentFile = uploadFile.getParentFile();
                if (parentFile != null)
                    fileName = parentFile.getName() + File.separator + fileName;
            }

            progressLabel.setText(fileName);
            progressLabel.setToolTipText(uploadFile.toString());
        }
        progressBar.setValue(progress);
    }

    // Called after successful upload
    void onUploadSuccess() {
        // Ignore if user canceled
        if (isCanceled()) {
            dispose();
            return;
        }

        // Show success panel
        Container panel = getContentPane();
        ((CardLayout)panel.getLayout()).show(panel, PANEL_SUCCESS);

        // Now it is safe to allow user to close
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        toFront();
    }

    // Called when upload failed
    void onUploadFail(String messages) {
        toFront();
        int result = ErrorDialog.showErrorConfirmDialog(this, UIMessages.getMessage(UIMessages.ERR_FTP_FAILED, messages));
        dispose();

        // Try again, first displaying settings so user can correct any problems.
        // Use original settings passed to showDialog()
        if (result == ErrorDialog.YES_OPTION)
            showDialog((Frame)getOwner(), htmlFile, true, settings);
    }

    // Can be invoked from non-Swing threads
    boolean isCanceled() {
        return isCanceled;
    }

    private void buildUI() {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        Container panel = getContentPane();
        panel.setLayout(new CardLayout());

        panel.add(buildProgressUI(), PANEL_PROGRESS);
        panel.add(buildSuccessUI(), PANEL_SUCCESS);

        pack();
    }

    private Component buildProgressUI() {
        progressLabel = new JLabel(" ");
        progressBar = new JProgressBar();

        JButton cancelButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_CANCEL));
        cancelButton.addActionListener(new CancelHandler());

        // Lay it out
        JPanel panel = new JPanel(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.insets.left = gbc.insets.right = gbc.insets.top = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPU_INFO,
                new Object[] { htmlFile.getName(), settings.getPreset().getHostname() })),
            gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets.left = gbc.insets.right = gbc.insets.top = 10;
        panel.add(progressLabel, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.left = gbc.insets.right = 10;
        gbc.insets.top = 5;
        panel.add(progressBar, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets.top = gbc.insets.bottom = 10;
        panel.add(cancelButton, gbc);

        return panel;
    }

    private Component buildSuccessUI() {
        // Build URL to uploaded show
        String showFilename = htmlFile.getName();
        Preset preset = settings.getPreset();

        String viewShowURL = preset.populateURLTemplate(preset.getViewURLTemplate(), settings.getLogin(), showFilename);
        String manageShowURL = preset.populateURLTemplate(preset.getManageURLTemplate(), settings.getLogin(), showFilename);

        WizardGBC gbc = new WizardGBC();

        // Build success message
        Component message = null;
        if (viewShowURL == null && manageShowURL == null) {
            message = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_FTPSUCCESS_NOURL, showFilename));
        }
        else {
            JLabel messageLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_FTPSUCCESS, showFilename));

            // Allow copying view or manage url, with preference to view
            final String copyURL = viewShowURL != null ? viewShowURL : manageShowURL;

            // Textarea to display URL
            JTextArea urlText = new LabelTextArea(copyURL, 2, 30, true, messageLabel);
            urlText.setFont(new Font("monospaced", Font.PLAIN, urlText.getFont().getSize()));

            // Button to copy URL to clipboard
            class CopyURLHandler implements ActionListener {
                public void actionPerformed(ActionEvent e) {
                    try {
                        Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(copyURL), null);
                    } catch (IllegalStateException ex) {
                    }
                }
            }
            JButton copyButton = new ToolButton(new ResourceIcon("resources/copy.gif"));
            copyButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_TIP_FTPSUCCESS_COPYURL));
            copyButton.addActionListener(new CopyURLHandler());

            // Button to launch browser to URL.
            class LaunchHandler implements ActionListener {
                private String url;
                public LaunchHandler(String url) {
                    this.url = url;
                }
                public void actionPerformed(ActionEvent e) {
                    if (!HyperLabel.launchDocument(url)) {
                        ErrorDialog.showErrorDialog(UploadDialog.this,
                                UIMessages.getMessage(UIMessages.ERR_LAUNCH_FAILED, url));
                    }
                }
            }

            JButton viewShowButton = new JButton(UIMessages.getMessage(UIMessages.UI_LABEL_LAUNCH));
            if (viewShowURL != null)
                viewShowButton.addActionListener(new LaunchHandler(viewShowURL));
            else
                viewShowButton.setEnabled(false);

            JButton manageShowButton = new JButton(UIMessages.getMessage(UIMessages.UI_LABEL_MANAGE));
            if (manageShowURL != null)
                manageShowButton.addActionListener(new LaunchHandler(manageShowURL));
            else
                manageShowButton.setEnabled(false);

            JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
            buttonPanel.add(viewShowButton);
            buttonPanel.add(manageShowButton);

            JPanel successPanel = new JPanel(new GridBagLayout());

            // Lay it out

            gbc.reset();
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.insets.bottom = 5;
            successPanel.add(messageLabel, gbc);

            gbc.reset();
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.insets.left = gbc.insets.right = 5;
            successPanel.add(urlText, gbc);

            gbc.reset();
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            gbc.insets.right = 5;
            successPanel.add(copyButton, gbc);

            gbc.reset();
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets.top = 15;
            gbc.insets.left = gbc.insets.right = 5;
            successPanel.add(buttonPanel, gbc);

            message = successPanel;
        }


        // OK button to dismiss
        JButton okButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OK));
        okButton.addActionListener(new DisposeHandler(this));

        // Lay it all out
        JPanel panel = new JPanel(new GridBagLayout());

        gbc.reset();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(message, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.top = 10;
        panel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.top = gbc.insets.bottom = 10;
        panel.add(okButton, gbc);

        return panel;
    }

    /**
     * Handle canceling an upload
     */
    private class CancelHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (isCanceled)
                return;

            isCanceled = true;

            // Disable the button for feedback
            ((JComponent)e.getSource()).setEnabled(false);
        }
    }
}
