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

import java.awt.Component;
import java.awt.Container;

public class UIUtilities {
    /**
     * Recursively enable/disable a tree of Components
     * @param component Root Component to start with
     */
    public static void enableComponentTree(Component component, boolean enabled) {
        component.setEnabled(enabled);

        if (component instanceof Container) {
            Component[] children = ((Container)component).getComponents();
            if (children != null) {
                for (int i = 0; i < children.length; i++)
                    enableComponentTree(children[i], enabled);
            }
        }
    }
}
