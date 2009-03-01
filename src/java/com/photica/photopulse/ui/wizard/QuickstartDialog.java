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

import com.photica.photopulse.Util;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.io.IOException;

public class QuickstartDialog extends JDialog {
    public QuickstartDialog(Frame parent) {
        super(parent, UIMessages.getMessage(UIMessages.UI_QUICKSTART_TITLE), true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        String strQuickstart = null;
        try {
            strQuickstart = Util.readStream(UIMessages.class
                .getResourceAsStream(UIMessages.getMessage(UIMessages.RSRC_QUICKSTART)));
        } catch (IOException e) {
            strQuickstart = UIMessages.getMessage(UIMessages.ERR_QUICKSTART_LOAD);
        }

        // Label for HTML quickstart guide
        // The HTML must constrain the width, or the label lays it out too wide
        // XXX http://developer.java.sun.com/developer/bugParade/bugs/4348815.html
        JLabel lblQuickstart = new JLabel();
        // Set base URL for images in HTML
        lblQuickstart.putClientProperty(BasicHTML.documentBaseKey,
            UIMessages.class.getResource("resources/"));
        lblQuickstart.setText(strQuickstart);

        // OK button
        JButton btnOK = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OK));
        btnOK.addActionListener(new DisposeHandler(this));
        getRootPane().setDefaultButton(btnOK);

        // Lay it all out
        Container panel = getContentPane();
        panel.setLayout(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(lblQuickstart, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.fill = gbc.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.top = 10;
        panel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = gbc.CENTER;
        gbc.insets.top = gbc.insets.bottom = 10;
        panel.add(btnOK, gbc);

        pack();
    }
}