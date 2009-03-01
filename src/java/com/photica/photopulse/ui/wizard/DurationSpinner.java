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
 
package com.photica.photopulse.ui.wizard;

import com.photica.ui.JSpinnerFix;
import com.photica.photopulse.flash.ShowGenerator;

import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.SpinnerModel;
import javax.swing.JSpinner;
import java.text.ParseException;

/**
 * JSpinner optimized for durations.
 */
public class DurationSpinner extends JSpinnerFix {
    public DurationSpinner() {
        // Limit duration to maxframes, assume 1fps minimum is reasonable.
        this(0.0, 0.0, ShowGenerator.MAXFRAMES, 0.1, 4);
    }

    public DurationSpinner(double value, double minimum, double maximum, double stepSize, int displayColumns) {
        super(new SpinnerNumberModel(value, minimum, maximum, stepSize));
        ((JSpinner.NumberEditor)getEditor()).getTextField().setColumns(displayColumns);
    }

    protected JComponent createEditor(SpinnerModel model) {
        // Change the default pattern of "#,##0.###"
        // Get rid of commas.
        return new JSpinner.NumberEditor(this, "#0.###");
    }

    /**
     * The underlying duration in the model is often different than the number displayed
     * in the editor - e.g. 1.2499999998 vs. 1.25.
     * So when we commit, we only want to change the model if the displayed value is different
     * from what would be displayed for the model value.
     * So in the above case if 1.25 was displayed, and 1.2499999998 would be displayed as 1.25,
     * then we would not commit because 1.25==1.25
     */
    public void commitEditIfChanged() {
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)getEditor();
        String modelValue = editor.getFormat().format(getDuration());
        // Commit if the rendered model value is different from the currently rendered editor value
        if (!modelValue.equals(editor.getTextField().getText())) {
            try {
                commitEdit();
            } catch (ParseException e) {
            }
        }
    }

    public double getDuration() {
        return ((Double)getValue()).doubleValue();
    }

    /**
     * Sets current duration with no callbacks.
     */
    public void setDuration(double duration) {
        setValueQuiet(new Double(duration));
    }
}
