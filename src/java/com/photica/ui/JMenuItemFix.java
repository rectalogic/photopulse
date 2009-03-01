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

package com.photica.ui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;


/**
 * JMenuItem that aligns it's text properly when it doesn't have an icon.
 * This is so all items in a JMenu align - with or without icons.
 * Icons are assumed to be 16x16.
 */
public class JMenuItemFix extends JMenuItem implements Icon {
    static final int ICON_SIZE = 16;

    public JMenuItemFix(Action action) {
        super(action);
        init();
    }

    public JMenuItemFix(String strText) {
        super(strText);
        init();
    }

    private void init() {
        if (getIcon() == null)
            setIcon(this);
    }

    /**
     * Treat mnemonic case sensitive when determining which character to underline.
     */
    /*XXX
    public void setMnemonic(int mnemonic) {
        int index = getText().indexOf(mnemonic);
        // Apparently Alloy doesn't support setDisplayedMnemonicIndex (not all LAF do)
        if (index != -1)
            setDisplayedMnemonicIndex(index);
        super.setMnemonic(mnemonic);
    }
    */

    // Implements Icon
    public int getIconHeight() {
        return ICON_SIZE;
    }

    // Implements Icon
    public int getIconWidth() {
        return ICON_SIZE;
    }

    // Implements Icon
    public void paintIcon(Component c, Graphics g, int x, int y) {
        // Paint nothing
    }

}