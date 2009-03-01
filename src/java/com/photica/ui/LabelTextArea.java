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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.ui;

import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.Font;

/**
 * A JTextArea configured to look like a JLabel.
 */
public class LabelTextArea extends JTextArea {
    /**
     * @param label JLabel to match colors to
     */
    public LabelTextArea(String text, int rows, int columns, boolean isSelectable, JLabel label) {
        super(text, rows, columns);

        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createLoweredBevelBorder(),
            BorderFactory.createEmptyBorder(5,5,5,5));
        setBorder(border);

        if (!isSelectable)
            setHighlighter(null);

        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setForeground(label.getForeground());
        setBackground(label.getBackground());
    }
}
