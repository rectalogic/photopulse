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

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

// Error dialog that wraps message
public class ErrorDialog extends JOptionPane {

    public ErrorDialog(Object message, int messageType, int optionType) {
        super(message, messageType, optionType);
    }

    public int getMaxCharactersPerLineCount() {
        return 65;
    }

    public static void showErrorDialog(Component parent, Object message) {
        ErrorDialog ed = new ErrorDialog(message, ERROR_MESSAGE, DEFAULT_OPTION);
        JDialog dialog = ed.createDialog(parent, UIMessages.getMessage(UIMessages.UI_ERROR_TITLE));
        dialog.setVisible(true);
    }

    public static int showErrorConfirmDialog(Component parent, Object message) {
        return showDialog(parent, ERROR_MESSAGE, message,
                UIMessages.getMessage(UIMessages.UI_ERROR_TITLE), YES_NO_OPTION);
    }

    public static void showWarningDialog(Component parent, Object message) {
        ErrorDialog ed = new ErrorDialog(message, WARNING_MESSAGE, DEFAULT_OPTION);
        JDialog dialog = ed.createDialog(parent, UIMessages.getMessage(UIMessages.UI_ERROR_TITLE));
        dialog.setVisible(true);
    }

    public static void showInfoDialog(Component parent, Object message) {
        ErrorDialog ed = new ErrorDialog(message, INFORMATION_MESSAGE, DEFAULT_OPTION);
        JDialog dialog = ed.createDialog(parent, UIMessages.getMessage(UIMessages.UI_INFO_TITLE));
        dialog.setVisible(true);
    }

    public static int showConfirmDialog(Component parent, Object message, String title, int optionType) {
        return showDialog(parent, QUESTION_MESSAGE, message, title, optionType);
    }

    public static int showDialog(Component parent, int messageType, Object message, String title, int optionType) {
        ErrorDialog ed = new ErrorDialog(message, messageType, optionType);
        JDialog dialog = ed.createDialog(parent, title);
        dialog.setVisible(true);

        Object result = ed.getValue();
        if (result == null)
            return CLOSED_OPTION;
        if (result instanceof Integer)
            return ((Integer)result).intValue();
        return CLOSED_OPTION;
    }
}
