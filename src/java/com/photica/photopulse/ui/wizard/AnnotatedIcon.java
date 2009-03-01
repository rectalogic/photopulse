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
 
package com.photica.photopulse.ui.wizard;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;

/**
 * Icon that draws a main icon with an annotation icon in the corner.
 */
public class AnnotatedIcon implements Icon {
    private Icon icon;
    private Icon annotationIcon;

    public AnnotatedIcon(Icon icon, Icon annotation) {
        this.icon = icon;
        this.annotationIcon = annotation;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        icon.paintIcon(c, g, x, y);
        // Paint annotation in upper right corner inset 1 pixel
        annotationIcon.paintIcon(c, g, x + icon.getIconWidth() - annotationIcon.getIconWidth() - 1, y + 1);
    }

    public int getIconWidth() {
        return icon.getIconWidth();
    }

    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
