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

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.Action;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToolLauncher {
    /**
     * Return a list of available ToolActions
     */
    public static List<ToolAction> createToolActionList() {
        List<Tool> toolList = ToolRegistry.getTools();
        List<ToolAction> actionList = new ArrayList<ToolAction>(toolList.size());
        for (Tool tool : toolList) {
            ToolAction action = tool.getToolAction();
            if (action != null)
                actionList.add(action);
        }
        return actionList;
    }

    /**
     * Display dialog with checkboxes to launch applicable tools. Launch checked tool on OK.
     */
    public static void showDialog(Component parent, String message, ShowModel model) {
        List<Tool> toolList = createToolList(model.getShowType());
        Box buttonBox = Box.createVerticalBox();
        List<JToggleButton> buttonList = createButtonList(buttonBox, toolList);

        int choice = ErrorDialog.showDialog(parent, ErrorDialog.INFORMATION_MESSAGE,
                new Object[] {
                    message,
                    buttonBox
                },
                UIMessages.getMessage(UIMessages.UI_TOOL_DIALOG_TITLE),
                ErrorDialog.OK_CANCEL_OPTION);

        if (choice == ErrorDialog.OK_OPTION) {
            Tool tool = findSelectedTool(toolList, buttonList);
            if (tool != null) {
                if (!tool.invokeTool(model)) {
                    ErrorDialog.showErrorDialog(parent,
                        UIMessages.getMessage(UIMessages.ERR_TOOL_FAILED, model.getExportFile().getName()));
                }
            }
        }
    }

    private static List<Tool> createToolList(ShowModel.ShowType showType) {
        List<Tool> toolList = ToolRegistry.getTools();
        List<Tool> list = new ArrayList<Tool>(toolList.size());
        for (Tool tool : toolList) {
            if (tool.isShowTypeSupported(showType))
                list.add(tool);
        }
        return list;
    }

    /**
     * Create a list of JToggleButtons, one for each tool. Add them to the parent container.
     */
    private static List<JToggleButton> createButtonList(Container parent, List<Tool> toolList) {
        int size = toolList.size();
        List<JToggleButton> buttonList = new ArrayList<JToggleButton>(size);
        if (size == 1) {
            JCheckBox checkBox = new JCheckBox((toolList.get(0)).getToolLabel(), true);
            parent.add(checkBox);
            buttonList.add(checkBox);
        }
        else {
            ButtonGroup buttonGroup = new ButtonGroup();
            for (int i = 0; i < size; i++) {
                JRadioButton radioButton = new JRadioButton((toolList.get(i)).getToolLabel(), i == 0);
                buttonGroup.add(radioButton);
                parent.add(radioButton);
                buttonList.add(radioButton);
            }
        }
        return buttonList;
    }

    private static Tool findSelectedTool(List<Tool> toolList, List<JToggleButton> buttonList) {
        int size = buttonList.size();
        for (int i = 0; i < size; i++) {
            if (buttonList.get(i).isSelected())
                return toolList.get(i);
        }
        return null;
    }
}
