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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.ui.wizard.tools;

import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.WizardFileFilter;
import com.photica.photopulse.ui.wizard.WizardUI;
import com.photica.photopulse.ui.wizard.HyperLabel;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.ftp.UploadSettings;
import com.photica.photopulse.ui.wizard.ftp.UploadSettingsDialog;
import com.photica.photopulse.ui.wizard.ftp.UploadDialog;
import com.photica.photopulse.ui.wizard.ftp.Preset;

import javax.swing.JFileChooser;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.File;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Upload a show via FTP.
 */
public class UploadTool implements Tool {
    private String label = UIMessages.getMessage(UIMessages.UI_TOOL_UPLOAD_LABEL);
    private String menuLabel = UIMessages.getMessage(UIMessages.UI_TOOL_UPLOAD_MENU_LABEL);
    private Integer mnemonic = (Integer)UIMessages.getResource(UIMessages.I_UI_MN_TOOLS_UPLOAD);

    private UploadSettings uploadSettings;
    private File uploadDirectory;
    private String uploadFileName;

    private Action manageShowAction;

    public UploadTool() {
        uploadSettings = new UploadSettings();
        uploadSettings.addPropertyChangeListener(new SettingsChangeHandler());
        manageShowAction = new ManageShowAction();
        enableManageShowAction();
    }

    public boolean invokeTool(ShowModel model) {
        // Upload without prompting for file via filechooser
        uploadShow(WizardUI.getInstance(), model.getExportFile());
        return true;
    }

    public String getToolLabel() {
        return label;
    }

    public String getToolMenuLabel() {
        return menuLabel;
    }

    public Integer getToolMenuMnemonic() {
        return mnemonic;
    }

    public ToolAction getToolAction() {
        // Special handling for this tool
        return null;
    }

    public boolean isShowTypeSupported(ShowModel.ShowType showType) {
        return showType == ShowModel.ShowType.HTM;
    }

    /**
     * Return an Action that will launch the browser to the FTP presets "manage show" URL.
     * This Action will be disabled if the current preset does not support that.
     */
    public Action getManageShowAction() {
        return manageShowAction;
    }

    private void enableManageShowAction() {
        manageShowAction.setEnabled(uploadSettings.getLogin() != null
                && uploadSettings.getPreset().getManageURLTemplate() != null);
    }

    public void showUploadDialog(Frame parent, File uploadFile) {
        JFileChooser uploadFileChooser = null;

        WizardFileFilter htmFilter = new WizardFileFilter(WizardFileFilter.FILTER_HTM);

        if (uploadFile != null && htmFilter.isValidExtension(uploadFile.getName())) {
            // Initially select the provided file
            uploadFileChooser = new JFileChooser(uploadFile);
            uploadFileChooser.setSelectedFile(uploadFile);
        }
        else
            uploadFileChooser = new JFileChooser(uploadDirectory);

        uploadFileChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_FTPUPLOADCHOOSER_TITLE));
        uploadFileChooser.setAcceptAllFileFilterUsed(false);
        uploadFileChooser.addChoosableFileFilter(htmFilter);

        if (uploadFileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return;

        // Save directory user chose for next time
        uploadDirectory = uploadFileChooser.getCurrentDirectory();

        uploadShow(parent, uploadFileChooser.getSelectedFile());
    }

    private void uploadShow(Frame parent, File uploadFile) {
        UploadDialog.showDialog(parent, uploadFile, uploadSettings);
        // Save filename for populating manage url
        this.uploadFileName = uploadFile.getName();
    }

    public void showUploadSettingsDialog(Component parent) {
        UploadSettingsDialog dlgSettings = new UploadSettingsDialog(uploadSettings);
        dlgSettings.showDialog(parent);
    }

    public void setUploadDirectory(File directory) {
        uploadDirectory = directory;
    }

    public File getUploadDirectory() {
        return uploadDirectory;
    }

    /**
     * Handle Preset change in UploadSettings
     */
    private class SettingsChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            enableManageShowAction();
        }
    }

    /**
     * Action to launch browser to "manage show" url
     */
    private class ManageShowAction extends AbstractAction {
        public ManageShowAction() {
            super(UIMessages.getMessage(UIMessages.UI_TOOL_MANAGE_SHOW_MENU_LABEL));
            putValue(MNEMONIC_KEY, UIMessages.getResource(UIMessages.I_UI_MN_TOOLS_MANAGE_SHOW));
        }

        public void actionPerformed(ActionEvent event) {
            Preset preset = uploadSettings.getPreset();
            String manageURL = preset.populateURLTemplate(preset.getManageURLTemplate(),
                    uploadSettings.getLogin(), uploadFileName);
            if (manageURL != null && !HyperLabel.launchDocument(manageURL)) {
                ErrorDialog.showErrorDialog(WizardUI.getInstance(),
                        UIMessages.getMessage(UIMessages.ERR_LAUNCH_FAILED, manageURL));
            }
        }
    }
}
