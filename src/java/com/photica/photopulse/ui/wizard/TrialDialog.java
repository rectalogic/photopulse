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
import com.photica.photopulse.Util;
import com.photica.photopulse.Branding;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class TrialDialog extends JDialog {

    /**
     * Return true if trial expired and app should exit
     */
    public static boolean showDialog(WizardUI parent) {
        TrialDialog dlgTrial = new TrialDialog(parent);
        dlgTrial.setLocationRelativeTo(parent);
        dlgTrial.setVisible(true);

        return false;
    }

    private TrialDialog(final WizardUI parent) {
        super(parent, UIMessages.getMessage(UIMessages.UI_TRIAL_TITLE), true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String strMsg = null;
        try {
            strMsg = Util.readStream(UIMessages.class
                .getResourceAsStream(UIMessages.getMessage(UIMessages.RSRC_TRIAL)));
        } catch (IOException e) {
            strMsg = UIMessages.getMessage(UIMessages.ERR_TRIAL_LOAD);
        }

        // Label for trial info
        // The HTML must constrain the width, or the label lays it out too wide
        // XXX http://developer.java.sun.com/developer/bugParade/bugs/4348815.html
        JLabel lblMsg = new JLabel(strMsg);

        // Link to purchase
        HyperLabel lblPurchase = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_PURCHASE),
            UIMessages.getMessage(UIMessages.URL_PURCHASE));

        // Link to register
        HyperLabel lblRegister = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_REGISTER),
            parent.getRegistrationURL());

        // Button to enter license
        JButton btnLicense = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_TRIAL_LICENSE));
        btnLicense.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showLicenseDialog(TrialDialog.this);
            }
        });

        // OK button
        JButton btnOK = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OK));
        btnOK.addActionListener(new DisposeHandler(this));

        // Lay it all out
        Container panel = getContentPane();
        panel.setLayout(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(lblMsg, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = gbc.WEST;
        gbc.weightx = 1;
        gbc.insets.top = gbc.insets.left = 5;
        panel.add(lblRegister, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets.top = gbc.insets.right = 5;
        panel.add(lblPurchase, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets.top = gbc.insets.right = 5;
        panel.add(btnLicense, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.fill = gbc.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets.top = 10;
        panel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 4;
        gbc.anchor = gbc.CENTER;
        gbc.gridwidth = 2;
        gbc.insets.top = gbc.insets.bottom = 10;
        panel.add(btnOK, gbc);

        pack();
    }
}