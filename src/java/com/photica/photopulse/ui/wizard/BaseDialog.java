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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base OK/Cancel dialog superclass
 */
public abstract class BaseDialog extends JComponent {

    // True if user clicked OK
    private boolean isConfirmed = false;

    private String title;
    private boolean isResizable;
    private JPanel contentsPanel;
    private JButton okButton;
    private JDialog dialog;

    public BaseDialog(String title, boolean isResizable) {
        this.title = title;
        this.isResizable = isResizable;

        // Panel to contain ourself - subclass should populate 'this' with components
        contentsPanel = new JPanel(new GridBagLayout());

        // Grid to make dialog buttons same size
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        // OK button, confirm and close dialog
        okButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OK));
        okButton.addActionListener(new ConfirmOKHandler());
        buttonPanel.add(okButton);

        // Cancel button, just close dialog
        JButton cancelButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_CANCEL));
        cancelButton.addActionListener(new CancelHandler());
        buttonPanel.add(cancelButton);

        // Lay it out
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contentsPanel.add(this, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.top = 10;
        contentsPanel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets.top = gbc.insets.bottom = gbc.insets.left = gbc.insets.right = 10;
        contentsPanel.add(buttonPanel, gbc);
    }

    /**
     * Display a dialog containing this instance.
     * We need to dynamically create a dialog each time since the dialog parent changes.
     * @return true if user chose OK
     */
    protected boolean showDialog(Component parent) {
        Window window;
        if (parent instanceof Window)
            window = (Window)parent;
        else
            window = SwingUtilities.getWindowAncestor(parent);

        if (window instanceof JDialog)
            dialog = new JDialog((Dialog)window, title, true);
        else
            dialog = new JDialog((Frame)window, title, true);

        dialog.setResizable(isResizable);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getRootPane().setDefaultButton(okButton);

        Container pane = dialog.getContentPane();
        pane.setLayout(new BorderLayout());
        pane.add(contentsPanel, BorderLayout.CENTER);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        isConfirmed = false;
        dialog.setVisible(true);

        dialog = null;

        return isConfirmed;
    }

    /**
     * Subclasses can override to cancel an OK
     */
    protected boolean confirmOK() {
        return true;
    }

    protected JDialog getDialog() {
        return dialog;
    }
    
    private class ConfirmOKHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!confirmOK())
                return;

            isConfirmed = true;
            dialog.dispose();
        }
    }

    private class CancelHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }
}
