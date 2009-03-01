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
import com.photica.ui.LabelTextArea;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;

public class AboutDialog extends JDialog {
    public AboutDialog(Frame parent) {
        super(parent, UIMessages.getMessage(UIMessages.UI_ABOUT_TITLE), true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Link to website
        HyperLabel lblWebLink = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_WEBSITE),
            UIMessages.getMessage(UIMessages.URL_PHOTOPULSE));

        // Product name
        JLabel lblProduct = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_ABOUT));
        Font fontProduct = lblProduct.getFont();
        lblProduct.setFont(fontProduct.deriveFont(Font.BOLD, fontProduct.getSize() + 3));

        // Copyright
        JLabel lblCopyright = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_COPYRIGHT));

        // Product version
        JLabel lblVersion = new JLabel();
        lblVersion.setText(UIMessages.getMessage(UIMessages.UI_LABEL_VERSION, PhotoPulse.getVersion()));

        // Max memory
        JLabel lblMemory = new JLabel();
        lblMemory.setText(UIMessages.getMessage(UIMessages.UI_LABEL_MEMORY,
             Runtime.getRuntime().maxMemory() / 1024));

        // Product serial
        String strSerialLabel;
        if (PhotoPulse.LICENSE != null) {
            strSerialLabel = UIMessages.getMessage(UIMessages.UI_LABEL_SERIAL_REG,
                PhotoPulse.LICENSE.getSerialString());
        }
        else
            strSerialLabel = UIMessages.getMessage(UIMessages.UI_LABEL_SERIAL_UNREG);
        JLabel lblSerial = new JLabel(strSerialLabel);

        // Purchase link (only if unlicensed)
        HyperLabel lblPurchase = null;
        if (PhotoPulse.LICENSE == null) {
            lblPurchase = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_PURCHASE),
                UIMessages.getMessage(UIMessages.URL_PURCHASE));
        }

        // Link to error log
        HyperLabel lblLog = null;
        lblLog = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_LOGLINK),
            PhotoPulse.EXCEPTION_LOG.exists() ? PhotoPulse.EXCEPTION_LOG.getAbsolutePath() : null);

        // Field for notices, looks like scrollable label.
        Font fontNotices = fontProduct.deriveFont(10.0f);
        JTextArea txtNotices = new LabelTextArea(UIMessages.getMessage(UIMessages.UI_NOTICES),
            8, 45, false, lblProduct);
        txtNotices.setFont(fontNotices);

        // Link to license file
        HyperLabel lblLicenseLink = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_VIEWLICENSE),
            new File(PhotoPulse.INSTALLDIR, "licenses").getAbsolutePath());

        // OK button
        JButton btnOK = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OK));
        btnOK.addActionListener(new DisposeHandler(this));
        getRootPane().setDefaultButton(btnOK);

        // Lay it all out
        Container panel = getContentPane();
        panel.setLayout(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(lblProduct, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblVersion, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblMemory, gbc);

        gbc.reset();
        gbc.gridy = 2;
        if (lblPurchase == null)
            gbc.gridwidth = 3;
        else
            gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblSerial, gbc);

        if (lblPurchase != null) {
            gbc.reset();
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets.top = 5;
            gbc.insets.left = 10;
            panel.add(lblPurchase, gbc);
        }

        gbc.reset();
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblCopyright, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblLog, gbc);

        gbc.reset();
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(txtNotices, gbc);

        gbc.reset();
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblLicenseLink, gbc);

        gbc.reset();
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1;
        gbc.insets.top = gbc.insets.right = 5;
        panel.add(lblWebLink, gbc);

        gbc.reset();
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.top = 10;
        panel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1;
        gbc.insets.top = gbc.insets.bottom = 10;
        panel.add(btnOK, gbc);

        pack();
    }
}