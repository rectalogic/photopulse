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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.exporter;

import com.photica.photopulse.ui.wizard.ErrorDialog;

import java.awt.Component;

/**
 * ExportExceptions are responsible for rendering their own error message dialog.
 * SystemMessages used in ExportExceptions should be self contained, not rely on any additional message context.
 */
public class ExportException extends Exception {
    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Render exception message in a modal dialog.
     * ExportException subclasses my override with additional behavior (e.g. prompt user for further action).
     */
    public void showErrorDialog(Component parent) {
        ErrorDialog.showErrorDialog(parent, getMessage());
    }
}
