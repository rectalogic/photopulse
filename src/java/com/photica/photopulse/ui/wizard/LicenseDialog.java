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

import com.photica.photopulse.License;
import com.photica.photopulse.PhotoPulse;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class LicenseDialog extends BaseDialog {

    private JTextArea licenseText;

    public LicenseDialog() {
        super(UIMessages.getMessage(UIMessages.UI_LICENSE_TITLE), true);
        buildUI();
    }

    private void buildUI() {
        // Toolbar
        JToolBar editToolbar = new JToolBar();
        editToolbar.setFloatable(false);

        // Text actions for toolbar
        Action pasteAction = new DefaultEditorKit.PasteAction();
        pasteAction.putValue(Action.SMALL_ICON, new ResourceIcon("resources/paste.gif"));
        String pasteMessage = UIMessages.getMessage(UIMessages.UI_LABEL_PASTE);
        pasteAction.putValue(Action.SHORT_DESCRIPTION, pasteMessage);
        // Implement our own clear action since DefaultEditorKit does not define one
        class ClearAction extends TextAction {
            public ClearAction() {
                super("clear-text");
            }
            public void actionPerformed(ActionEvent e) {
                JTextComponent targetText = getTextComponent(e);
                if (targetText != null)
                    targetText.setText(null);
            }
        };
        Action clearAction = new ClearAction();
        clearAction.putValue(Action.SMALL_ICON, new ResourceIcon("resources/clear.gif"));
        String clearMessage = UIMessages.getMessage(UIMessages.UI_LABEL_CLEAR);
        clearAction.putValue(Action.SHORT_DESCRIPTION, clearMessage);

        editToolbar.add(pasteAction);
        editToolbar.add(clearAction);

        // Link to web site
        JLabel purchaseLink = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_PURCHASE),
            UIMessages.getMessage(UIMessages.URL_PURCHASE));

        // Text field for license
        licenseText = new JTextArea(11, 35);
        licenseText.setLineWrap(true);
        licenseText.setFont(new Font("monospaced", Font.PLAIN, licenseText.getFont().getSize()));

        // Context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem pasteMenu = new JMenuItem(pasteMessage);
        pasteMenu.addActionListener(pasteAction);
        popup.add(pasteMenu);
        JMenuItem clearMenu = new JMenuItem(clearMessage);
        clearMenu.addActionListener(clearAction);
        popup.add(clearMenu);

        licenseText.setComponentPopupMenu(popup);

        // Lay it out
        this.setLayout(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_LICENSE)), gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets.left = gbc.insets.top = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_LICENSEKEY)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        this.add(new JScrollPane(licenseText), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.left = 5;
        this.add(editToolbar, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets.right = 5;
        this.add(purchaseLink, gbc);
    }

    public boolean showDialog(Component parent) {
        return super.showDialog(parent);
    }

    // Overrides BaseDialog
    protected boolean confirmOK() {
        String licenseData = licenseText.getText();
        License license = null;
        try {
            license = License.parse(License.cleanString(licenseData));
        } catch (License.InvalidLicenseException e) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_INVALID_LICENSE, new Integer(e.getErrorCode())));
            return false;
        }

        if (license.isExpired()) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_EXPIRED_LICENSE));
            return false;
        }

        // Store in preferences
        license.store();

        PhotoPulse.LICENSE = license;

        return true;
    }
}
