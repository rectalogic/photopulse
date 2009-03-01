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
 
package com.photica.photopulse.ui.wizard.tools;

import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.ui.wizard.HyperLabel;
import com.photica.photopulse.ui.wizard.UIMessages;

import java.io.File;

/**
 * Launch native OS viewer associated with this file type.
 */
public class OSViewerTool implements Tool {
    private String label = UIMessages.getMessage(UIMessages.UI_TOOL_OSVIEWER_LABEL);
    private String menuLabel = UIMessages.getMessage(UIMessages.UI_TOOL_OSVIEWER_MENU_LABEL);
    private Integer mnemonic = (Integer)UIMessages.getResource(UIMessages.I_UI_MN_TOOLS_VIEWSHOW);
    private ToolAction action = new ToolAction(this);

    public boolean invokeTool(ShowModel model) {
        return HyperLabel.launchDocument(model.getExportFile().getAbsolutePath());
    }

    public String getToolLabel() {
        return label;
    }

    public String getToolMenuLabel() {
        return menuLabel;
    }

    public Integer getToolMenuMnemonic() {
        return mnemonic;
    }

    public ToolAction getToolAction() {
        return action;
    }

    public boolean isShowTypeSupported(ShowModel.ShowType showType) {
        return true;
    }
}
