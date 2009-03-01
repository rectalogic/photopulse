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

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Window;

/**
 * Handler to dispose of the specified Window
 */
public class DisposeHandler implements ActionListener {
    private Window window;

    public DisposeHandler(Window window) {
        this.window = window;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            window.dispose();
        } catch (IllegalStateException ex) {
            // Handle quicktime bug that causes
            // "java.lang.IllegalStateException: Can't dispose InputContext while it's active"
            // when dialog dismissed via keyboard.
            // XXX See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4289940
            PhotoPulse.logException(ex);
        }
    }
}
