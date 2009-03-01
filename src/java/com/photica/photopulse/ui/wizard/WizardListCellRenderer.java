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

import com.photica.photopulse.wizard.WizardEffect;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/**
 * Render list cell as a checkbox
 */
public class WizardListCellRenderer extends JCheckBox implements ListCellRenderer {

    private boolean hasFocus;

    public WizardListCellRenderer() {
        setOpaque(false);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean selected, boolean hasFocus) {

        setComponentOrientation(list.getComponentOrientation());

        // Render checkbox selection based on wizard selection model, not on native JList selection
        setSelected(((WizardList)list).getShowSelectionModel().isSelectedIndex(index));

        setText(((WizardEffect)value).getDisplayName());
        setEnabled(list.isEnabled());
        setFont(list.getFont());
        this.hasFocus = hasFocus;

        return this;
    }

    // Overrides Component
    public boolean hasFocus() {
        // Force paint() to draw focus or not
        return hasFocus;
    }

    // Overridden for performance, see DefaultListCellRenderer docs

    public void invalidate() {}
    public void validate() {}
    public void revalidate() {}
    public void repaint(long tm, int x, int y, int width, int height) {}
    public void repaint(Rectangle r) {}
    public void repaint() {}
}