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
 
package com.photica.photopulse.ui.wizard.expert.panzoom;

import com.photica.photopulse.ui.wizard.BaseDialog;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.skin.Skin;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

public class PanZoomEditorDialog extends BaseDialog {
    private PanZoomEditorDialog() {
        super(UIMessages.getMessage(UIMessages.UI_PZ_DIALOGTITLE), true);
    }

    /**
     * Display dialog to edit PanZoomEffect.
     * @return The new PhotoEffects, or null if dialog canceled
     */
    public static PhotoEffects showDialog(Component parent, PhotoEffects effects, Skin skin, Color backgroundColor) throws IOException {
        PanZoomEditor editor = new PanZoomEditor(effects, skin, backgroundColor);

        PanZoomEditorDialog dialog = new PanZoomEditorDialog();
        dialog.setLayout(new BorderLayout());
        dialog.add(editor, BorderLayout.CENTER);

        // If dialog not canceled, then build and return a new PhotoEffects
        if (dialog.showDialog(parent))
            return editor.buildPhotoEffects();
        else
            return null;
    }
}
