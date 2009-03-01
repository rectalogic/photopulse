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

import com.photica.photopulse.wizard.ExtensionFileFilter;
import com.photica.photopulse.flash.context.MP3Data;

import java.io.File;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

public class WizardFileFilter extends ExtensionFileFilter {
    // Do not overlap with superclass IDs
    public static final int FILTER_HTM = 1;
    public static final int FILTER_EXE = 2;
    public static final int FILTER_SWF = 3;
    public static final int FILTER_MP3 = 4;
    public static final int FILTER_MHT = 5;
    public static final int FILTER_PPP = 8;
    public static final int FILTER_M3U = 9;
    public static final int FILTER_FLASHPHOTOS = 10;
    public static final int FILTER_WMV = 11;

    private static final List<String> EXTENSIONS_HTM = Collections.unmodifiableList(Arrays.asList(new String[] { ".htm", ".html" }));
    private static final List<String> EXTENSIONS_EXE = Collections.unmodifiableList(Arrays.asList(new String[] { ".exe" }));
    private static final List<String> EXTENSIONS_SWF = Collections.unmodifiableList(Arrays.asList(new String[] { ".swf" }));
    private static final List<String> EXTENSIONS_MP3 = Collections.unmodifiableList(Arrays.asList(new String[] { ".mp3", MP3Data.EXTENSION_M3U }));
    private static final List<String> EXTENSIONS_MHT = Collections.unmodifiableList(Arrays.asList(new String[] { ".mht", ".mhtml" }));
    private static final List<String> EXTENSIONS_WMV = Collections.unmodifiableList(Arrays.asList(new String[] { ".wmv" }));
    private static final List<String> EXTENSIONS_PPP = Collections.unmodifiableList(Arrays.asList(new String[] { ".ppp" }));
    private static final List<String> EXTENSIONS_M3U = Collections.unmodifiableList(Arrays.asList(new String[] { MP3Data.EXTENSION_M3U }));
    private static final List<String> EXTENSIONS_FLASHPHOTOS = Collections.unmodifiableList(Arrays.asList(new String[] { ".swf", ".swt" }));

    public WizardFileFilter(int filterType) {
        super(filterType);
    }

    // Overrides FileFilter
    protected String initDescription() {
        switch (getFilterType()) {
        case FILTER_HTM:
            return UIMessages.getMessage(UIMessages.UI_FILTER_HTM, formatExtensions());
        case FILTER_EXE:
            return UIMessages.getMessage(UIMessages.UI_FILTER_EXE, formatExtensions());
        case FILTER_SWF:
            return UIMessages.getMessage(UIMessages.UI_FILTER_SWF, formatExtensions());
        case FILTER_MP3:
            return UIMessages.getMessage(UIMessages.UI_FILTER_MP3, formatExtensions());
        case FILTER_MHT:
            return UIMessages.getMessage(UIMessages.UI_FILTER_MHT, formatExtensions());
        case FILTER_WMV:
            return UIMessages.getMessage(UIMessages.UI_FILTER_WMV, formatExtensions());
        case FILTER_PPP:
            return UIMessages.getMessage(UIMessages.UI_FILTER_PPP, formatExtensions());
        case FILTER_M3U:
            return UIMessages.getMessage(UIMessages.UI_FILTER_M3U, formatExtensions());
        case FILTER_FLASHPHOTOS:
            // Reuse SWF message, with extra extensions
            return UIMessages.getMessage(UIMessages.UI_FILTER_SWF, formatExtensions());
        default:
            return super.initDescription();
        }
    }

    public List<String> getExtensions() {
        switch (getFilterType()) {
        case FILTER_HTM:
            return EXTENSIONS_HTM;
        case FILTER_EXE:
            return EXTENSIONS_EXE;
        case FILTER_SWF:
            return EXTENSIONS_SWF;
        case FILTER_MP3:
            return EXTENSIONS_MP3;
        case FILTER_MHT:
            return EXTENSIONS_MHT;
        case FILTER_WMV:
            return EXTENSIONS_WMV;
        case FILTER_PPP:
            return EXTENSIONS_PPP;
        case FILTER_M3U:
            return EXTENSIONS_M3U;
        case FILTER_FLASHPHOTOS:
            return EXTENSIONS_FLASHPHOTOS;
        default:
            return super.getExtensions();
        }
    }

    // Append the proper extension to file if needed
    public File applyExtension(File file) {
        if (!isValidExtension(file.getName())) {
            List<String> extensions = getExtensions();
            String extension = extensions.get(extensions.size() - 1);
            file = new File(file.getParent(), file.getName() + extension);
        }
        return file;
    }
}
