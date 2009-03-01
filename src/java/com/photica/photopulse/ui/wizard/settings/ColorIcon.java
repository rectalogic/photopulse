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
 
package com.photica.photopulse.ui.wizard.settings;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


// Icon to render a color swatch
class ColorIcon implements Icon {
    private Color color;
    private static final int SIZE = 20;

    public ColorIcon(Color color) {
        this.color = color;
    }

    public ColorIcon() {
        this.color = Color.GRAY;
    }

    public void setColor(Color c) {
        color = c;
    }

    public Color getColor() {
        return color;
    }

    public int getIconHeight() {
        return SIZE;
    }

    public int getIconWidth() {
        return SIZE;
    }

    public void paintIcon(Component cmp, Graphics g, int x, int y) {
        g.setColor(color);
        g.fillRect(x, y, SIZE, SIZE);
    }
}