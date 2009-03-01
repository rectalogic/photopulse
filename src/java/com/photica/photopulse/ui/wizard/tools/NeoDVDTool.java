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

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.ui.wizard.UIMessages;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

public class NeoDVDTool implements Tool {
    private static final String NEODVDPATH;
    private static final String RSRC_NEODVD_INI = "/resources/neoDVD.ini";

    private Integer mnemonic = (Integer)UIMessages.getResource(UIMessages.I_UI_MN_TOOLS_CREATEDVD);
    private String label = UIMessages.getMessage(UIMessages.UI_TOOL_NEODVD_LABEL);
    private String menuLabel = UIMessages.getMessage(UIMessages.UI_TOOL_NEODVD_MENU_LABEL);
    private ToolAction action = new ToolAction(this);

    static {
        // Native launcher app sets this property from registry if NeoDVD installed
        String neoPath = System.getProperty("neodvd.path");
        if (neoPath != null) {
            File neoFile = new File(neoPath);
            // Don't use it if it doesn't exist
            if (!neoFile.exists())
                neoPath = null;
        }
        NEODVDPATH = neoPath;
    }

    public static boolean isNeoDVDAvailable() {
        return NEODVDPATH != null;
    }

    public boolean invokeTool(ShowModel model) {
        if (NEODVDPATH == null)
            return false;

        try {
            // Use basename as title
            String title = Util.getBaseName(model.getExportFile());

            // Populate neoDVD INI template
            String iniContents = MessageFormat.format(Util.readStream(PhotoPulse.class.getResourceAsStream(RSRC_NEODVD_INI)),
                new Object[] {
                    title,
                    model.getExportFile().getAbsolutePath(),
                    model.getEndShowMode() == ShowModel.EndShowMode.LOOP ? "1" : "0"
                });

            // Write template to temp file
            File iniFile = File.createTempFile("neoDVD", ".ini");
            iniFile.deleteOnExit();
            FileWriter iniWriter = new FileWriter(iniFile);
            try {
                iniWriter.write(iniContents);
            } finally {
                iniWriter.close();
            }

            Runtime.getRuntime().exec(new String[] {
                NEODVDPATH,
                iniFile.getAbsolutePath()
            });

            return true;
        } catch (IOException e) {
            PhotoPulse.logException(e);
            return false;
        }
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
        return showType == ShowModel.ShowType.WMV;
    }
}
