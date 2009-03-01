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

import javax.swing.JComboBox;
import javax.swing.ComboBoxModel;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;
import java.awt.Rectangle;
import java.util.Vector;

/**
 * JComboBox that renders it's popup list at it's preferred width (the width of the longest item in the list).
 * This class uses some Swing internals and is dependent on the Metal LAF.
 * http://developer.java.sun.com/developer/bugParade/bugs/4177322.html
 *
 * This class also adds methods that do not fire an ActionEvent when the selection is programmatically modified.
 * http://developer.java.sun.com/developer/bugParade/bugs/4792192.html
 */
public class JComboBoxFix extends JComboBox {
    private boolean isSettingSelection = false;

    public JComboBoxFix(ComboBoxModel aModel) {
        super(aModel);
        init();
    }

    public JComboBoxFix(Vector items) {
        super(items);
        init();
    }

    public JComboBoxFix() {
        init();
    }

    public JComboBoxFix(Object[] items) {
        super(items);
        init();
    }

    private void init() {
        setUI(new MetalComboBoxUIFix());
    }

    public void setSelectedIndexQuiet(int index) {
        isSettingSelection = true;
        try {
            setSelectedIndex(index);
        } finally {
            isSettingSelection = false;
        }
    }

    public void setSelectedItemQuiet(Object item) {
        isSettingSelection = true;
        try {
            setSelectedItem(item);
        } finally {
            isSettingSelection = false;
        }
    }

    protected void fireActionEvent() {
        // Don't fire events if we are programmatically setting the selection
        if (!isSettingSelection)
            super.fireActionEvent();
    }
}

class BasicComboPopupFix extends BasicComboPopup {
    public BasicComboPopupFix(JComboBox combo) {
        super(combo);
    }

    protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
        // Use list preferred width if it is wider than the combo, otherwise use combos width.
        // So when combo is stretched, we stretch the list.
        // When combo is scrunched, we keep the list wider at its preferred width.
        scroller.setPreferredSize(null);
        getList().setVisibleRowCount(comboBox.getMaximumRowCount());
        int listWidth = scroller.getPreferredSize().width;
        return super.computePopupBounds(px, py, listWidth > pw ? listWidth : pw, ph);
    }
}

class MetalComboBoxUIFix extends MetalComboBoxUI {
    protected ComboPopup createPopup() {
        BasicComboPopup popup =  new BasicComboPopupFix(comboBox);
        popup.getAccessibleContext().setAccessibleParent(comboBox);
        return popup;
    }
}


