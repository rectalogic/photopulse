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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.ui;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import java.text.ParseException;

/**
* This class adds methods that allow the user to ignore ChangeEvents when the value is programmatically modified.
 * http://developer.java.sun.com/developer/bugParade/bugs/4792192.html
 * Also allows "nothing" to be displayed (indeterminate/empty state)
 */
public class JSpinnerFix extends JSpinner {
    private boolean isSetValueQuiet = false;
    private boolean isDisplayEmpty = false;

    public JSpinnerFix(SpinnerNumberModel model) {
        super(model);
    }

    public void setValueQuiet(Object value) {
        isSetValueQuiet = true;
        try {
            setValue(value);
        } finally {
            isSetValueQuiet = false;
        }
    }

    // We can't override fireStateChanged() and ignore the event because the UI needs it.
    // So listeners can query whether they should ignore the event.
    public boolean isSetValueQuiet() {
        return isSetValueQuiet;
    }

    public void setDisplayEmpty(boolean isEmpty) {
        this.isDisplayEmpty = isEmpty;
        getEditor().setVisible(!isDisplayEmpty);
    }

    public boolean isDisplayEmpty() {
        return isDisplayEmpty;
    }

    // Attempt to handle invalid input by reverting to previous value.
    // This avoids BasicSpinnerUI.ArrowButtonHandler.actionPerformed() from just beeping
    // when invalid data is entered and arrow buttons or enter are pressed.
    public void commitEdit() throws ParseException {
        try {
            super.commitEdit();
        } catch (ParseException e) {
            // If the editor contains garbage, revert the display to the current model value
            JComponent editor = getEditor();
            if (editor instanceof DefaultEditor)
                ((DefaultEditor)editor).stateChanged(new ChangeEvent(this));
            else
                throw e;
        }
    }
}
