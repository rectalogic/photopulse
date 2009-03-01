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

import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.UIManager;
import java.awt.Font;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/**
 * Fixes TitledBorder to render itself disabled when its Component is disabled.
 * http://developer.java.sun.com/developer/bugParade/bugs/4129681.html
 */
public class TitledBorderFix extends TitledBorder {
    public TitledBorderFix(String title) {
        super(title);
    }

    public TitledBorderFix(Border border) {
        super(border);
    }

    public TitledBorderFix(Border border, String title) {
        super(border, title);
    }

    public TitledBorderFix(Border border, String title, int titleJustification, int titlePosition) {
        super(border, title, titleJustification, titlePosition);
    }

    public TitledBorderFix(Border border, String title, int titleJustification, int titlePosition, Font titleFont) {
        super(border, title, titleJustification, titlePosition, titleFont);
    }

    public TitledBorderFix(Border border, String title, int titleJustification, int titlePosition, Font titleFont,
            Color titleColor) {
        super(border, title, titleJustification, titlePosition, titleFont, titleColor);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c.isEnabled())
            super.paintBorder(c, g, x, y, width, height);
        else {
            Color color = getTitleColor();
            // This is not LAF independent
            setTitleColor(c.getBackground().darker());
            super.paintBorder(c, g, x, y, width, height);
            setTitleColor(color);
        }
    }
}

