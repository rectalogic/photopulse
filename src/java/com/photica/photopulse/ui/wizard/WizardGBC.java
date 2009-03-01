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

import java.awt.GridBagConstraints;
import java.awt.Insets;

// Resettable GridBagConstraints that exposes member fields as bean properties
public class WizardGBC extends GridBagConstraints {
    public void reset() {
        gridx = RELATIVE;
        gridy = RELATIVE;
        gridwidth = 1;
        gridheight = 1;
        weightx = 0;
        weighty = 0;
        anchor = CENTER;
        fill = NONE;
        insets.bottom = insets.left = insets.right = insets.top = 0;
        ipadx = 0;
        ipady = 0;
    }

    public void setGridX(int x) {
        gridx = x;
    }
    public void setGridY(int y) {
        gridy = y;
    }
    public void setGridWidth(int w) {
        gridwidth = w;
    }
    public void setGridHeight(int h) {
        gridheight = h;
    }
    public void setWeightX(int x) {
        weightx = x;
    }
    public void setWeightY(int y) {
        weighty = y;
    }
    public void setAnchor(int a) {
        anchor = a;
    }
    public void setFill(int f) {
        fill = f;
    }

    public void setInsets(Insets i) {
        insets = i;
    }
    public void setInsets(int t, int l, int b, int r) {
        insets.top = t;
        insets.left = l;
        insets.bottom = b;
        insets.right = r;
    }

    public void setInsetsTop(int t) {
        insets.top = t;
    }
    public void setInsetsBottom(int b) {
        insets.bottom = b;
    }
    public void setInsetsLeft(int l) {
        insets.left = l;
    }
    public void setInsetsRight(int r) {
        insets.right = r;
    }

    public void setIPadX(int x) {
        ipadx = x;
    }
    public void setIPadY(int y) {
        ipady = y;
    }
}
