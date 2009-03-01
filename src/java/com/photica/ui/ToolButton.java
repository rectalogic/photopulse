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

import javax.swing.JButton;
import javax.swing.Action;
import javax.swing.Icon;
import java.awt.Insets;

/**
 * JButton configured to look like a toolbar button with icon.
 */
public class ToolButton extends JButton {
    public ToolButton(Action action) {
        super(action);
        init(false);
    }

    public ToolButton(String text) {
        super(text);
        init(false);
    }

    public ToolButton(Icon icon) {
        super(icon);
        init(false);
    }

    public ToolButton(Icon icon, boolean micro) {
        super(icon);
        init(micro);
    }

    public ToolButton(String text, Icon icon) {
        super(text, icon);
        init(false);
    }

    private void init(boolean micro) {
        setMargin(micro ? new Insets(0, 0, 0, 0) : new Insets(3, 3, 3, 3));
    }
}
