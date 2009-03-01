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

import com.photica.photopulse.ui.wizard.BaseDialog;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.HyperLabel;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.ui.JComboBoxFix;
import com.photica.ui.JSpinnerFix;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// Manages FTP settings
public class UploadSettingsDialog extends BaseDialog {
    private static final String PANEL_CUSTOM = "custom";
    private static final String PANEL_INFO = "info";

    private UploadSettings settings;

    private Preset customPreset;

    private JComboBoxFix presetCombo;
    private JButton signupButton;
    private JPanel customPanel;

    private JTextField hostnameText;
    private JSpinner portSpinner;
    private JTextField ftpDirectoryText;
    private JTextField urlTemplateText;

    private JTextField loginText;
    private JCheckBox savePasswordCheckbox;
    private JTextField passwordText;

    // The UploadSettings object will be modified directly
    public UploadSettingsDialog(UploadSettings settings) {
        super(UIMessages.getMessage(UIMessages.UI_FTPS_TITLE), false);
        this.settings = settings;
        initCustomPreset(settings);
        buildUI();
    }

    private void initCustomPreset(UploadSettings settings) {
        Preset preset = settings.getPreset();
        if (preset.isCustom())
            customPreset = preset;
        else
            customPreset = new Preset();
    }

    public boolean showDialog(Component parent) {
        return super.showDialog(parent);
    }

    protected boolean confirmOK() {
        Preset preset = (Preset)presetCombo.getSelectedItem();

        // Login/passwd must be set.
        // Host must be set unless using a preset.
        if (loginText.getDocument().getLength() == 0
                || passwordText.getDocument().getLength() == 0
                || (preset == customPreset && hostnameText.getDocument().getLength() == 0)) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_FTPS_REQUIRED));
            return false;
        }

        // If custom preset selected, replace it with a custom one built from the UI
        if (preset == customPreset) {
            String ftpDirectory = ftpDirectoryText.getDocument().getLength() == 0 ? null : ftpDirectoryText.getText();
            String urlTemplate = urlTemplateText.getDocument().getLength() == 0 ? null : urlTemplateText.getText();
            preset = new Preset(hostnameText.getText(), ((Integer)portSpinner.getValue()).intValue(),
                    ftpDirectory, urlTemplate);
        }
        settings.setPreset(preset);

        settings.setLogin(loginText.getText());
        settings.setPassword(passwordText.getText());
        settings.setSavePassword(savePasswordCheckbox.isSelected());

        // Save in prefs
        settings.storePrefs();

        return true;
    }

    private void selectInitialPreset() {
        Preset preset = settings.getPreset();

        // If custom but unset, then find a default preset to select
        if (preset.isCustom() && !preset.isDefault()) {
            int count = presetCombo.getItemCount();
            for (int i = 0; i < count; i++) {
                Preset item = (Preset)presetCombo.getItemAt(i);
                if (item.isDefault()) {
                    preset = item;
                    break;
                }
            }
        }

        // Don't invoke combo listener, it will reset login/password fields
        if (preset == null) {
            presetCombo.setSelectedIndexQuiet(0);
            preset = (Preset)presetCombo.getItemAt(0);
        }
        else
            presetCombo.setSelectedItemQuiet(preset);

        showCustomPanel(preset);
    }

    private void buildUI() {
        presetCombo = new JComboBoxFix(PresetManager.getPresets());
        // Add custom preset to end of list
        presetCombo.addItem(customPreset);
        presetCombo.setMaximumRowCount(4);
        presetCombo.setPrototypeDisplayValue("mmmmmmmmmmmmmm");
        presetCombo.addActionListener(new PresetHandler());

        signupButton = new JButton(UIMessages.getMessage(UIMessages.UI_FTPS_SIGNUP));
        signupButton.addActionListener(new SignupHandler());

        loginText = new JTextField(settings.getLogin(), 5);
        passwordText = new JPasswordField(settings.getPassword(), 5);
        savePasswordCheckbox = new JCheckBox(UIMessages.getMessage(UIMessages.UI_FTPS_SAVEPW),
            settings.isSavePassword());

        customPanel = new JPanel(new CardLayout());
        customPanel.add(buildCustomPanel(), PANEL_CUSTOM);
        JLabel infoLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_INFO));
        // Let custom panel set the overall preferred size, label will reflow to fit
        infoLabel.setPreferredSize(new Dimension(1, 1));
        infoLabel.setVerticalAlignment(SwingConstants.TOP);
        customPanel.add(infoLabel, PANEL_INFO);

        // Lay everything out
        WizardGBC gbc = new WizardGBC();
        this.setLayout(new GridBagLayout());

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_LOGIN)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(loginText, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_PW)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(passwordText, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        this.add(savePasswordCheckbox, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets.left = gbc.insets.bottom = 5;
        gbc.insets.top = 15;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_PROVIDER)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.left = gbc.insets.bottom = 5;
        gbc.insets.top = 15;
        this.add(presetCombo, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.bottom = gbc.insets.right = 5;
        gbc.insets.top = 15;
        this.add(signupButton, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        gbc.insets.top = 15;
        this.add(customPanel, gbc);

        // Set initial state
        selectInitialPreset();
    }

    private JPanel buildCustomPanel() {
        hostnameText = new JTextField(customPreset.getHostname(), 5);

        portSpinner = new JSpinnerFix(new SpinnerNumberModel(customPreset.getPort(), 1, 65535, 1));
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)portSpinner.getEditor();
        editor.getTextField().setColumns(2);
        // Get rid of commas in numbers
        editor.getFormat().applyPattern("#");

        ftpDirectoryText = new JTextField(customPreset.getDirectory(), 5);
        urlTemplateText = new JTextField(customPreset.getViewURLTemplate(), 5);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_FTPS_CUSTOMSETTINGS)));

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_HOSTNAME)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        panel.add(hostnameText, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_PORT)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        panel.add(portSpinner, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_DIR)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        panel.add(ftpDirectoryText, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FTPS_URL)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        panel.add(urlTemplateText, gbc);

        return panel;
    }

    private void showCustomPanel(Preset preset) {
        // Only enable signup if we have a preset with a url
        signupButton.setEnabled(preset.getSignupURL() != null);
        signupButton.setToolTipText(preset.getSignupURL());

        if (preset == customPreset)
            ((CardLayout)customPanel.getLayout()).show(customPanel, PANEL_CUSTOM);
        else
            ((CardLayout)customPanel.getLayout()).show(customPanel, PANEL_INFO);
    }

    /**
     * Handle preset combo selection
     */
    private class PresetHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Preset preset = (Preset)presetCombo.getSelectedItem();

            // Clear login/password when preset changed.
            // If it changes to the original preset, use original login/pass
            if (preset == settings.getPreset()) {
                loginText.setText(settings.getLogin());
                passwordText.setText(settings.getPassword());
            }
            else {
                loginText.setText(null);
                passwordText.setText(null);
            }

            // Show custom panel if custom preset
            showCustomPanel(preset);
        }
    }

    /**
     * Launch signup URL for selected Preset
     */
    private class SignupHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Preset preset = (Preset)presetCombo.getSelectedItem();
            String url = preset.getSignupURL();
            if (url != null)
                HyperLabel.launchDocument(url);
        }
    }

}