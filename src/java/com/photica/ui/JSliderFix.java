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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.ui;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

/**
 * This class adds methods that allow the user to ignore ChangeEvents when the value is programmatically modified.
 * http://developer.java.sun.com/developer/bugParade/bugs/4792192.html
 */
public class JSliderFix extends JSlider {
    private boolean isSetValueQuiet = false;

    public JSliderFix(int min, int max) {
        super(min, max);
    }

    public void setValueQuiet(int value) {
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
}
