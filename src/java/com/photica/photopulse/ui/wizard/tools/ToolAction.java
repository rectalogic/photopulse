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
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.WizardUI;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.File;

public class ToolAction extends AbstractAction {
    private Tool tool;
    private ShowModel model;

    public ToolAction(Tool tool) {
        super(tool.getToolMenuLabel());
        putValue(MNEMONIC_KEY, tool.getToolMenuMnemonic());
        this.tool = tool;
        setEnabled(false);
    }

    public void updateAction(ShowModel model) {
        this.model = model;

        if (model.getExportFile() == null) {
            setEnabled(false);
            return;
        }
        else
            setEnabled(tool.isShowTypeSupported(model.getShowType()));
    }

    public void actionPerformed(ActionEvent e) {
        if (model.getExportFile() != null) {
            if (!model.getExportFile().exists()) {
                ErrorDialog.showErrorDialog(WizardUI.getInstance(),
                        UIMessages.getMessage(UIMessages.ERR_TOOL_NOFILE, model.getExportFile().getName()));
            }
            else if (!tool.invokeTool(model)) {
                ErrorDialog.showErrorDialog(WizardUI.getInstance(),
                        UIMessages.getMessage(UIMessages.ERR_TOOL_FAILED, model.getExportFile().getName()));
            }
        }
    }
}
