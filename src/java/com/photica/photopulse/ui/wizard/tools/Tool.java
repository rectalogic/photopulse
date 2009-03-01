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

import java.io.File;

public interface Tool {
    public boolean invokeTool(ShowModel model);

    /**
     * Return a label indicating the tool action (should start with a verb, e.g. "Create something")
     */
    public String getToolLabel();

    /**
     * Return a label indicating the tool action appropriate for a menu item.
     */
    public String getToolMenuLabel();

    public Integer getToolMenuMnemonic();

    /**
     * Action used for Tools menu.
     */
    public ToolAction getToolAction();

    /**
     * Return true if this tool is supported for the given ShowType.
     */
    public boolean isShowTypeSupported(ShowModel.ShowType showType);
}
