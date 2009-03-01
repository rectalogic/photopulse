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

package com.photica.photopulse.wizard;

import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.imageio.ImageCoder;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.List;

public class ExtensionFileFilter extends FileFilter implements FilenameFilter {
    public static final int FILTER_IMAGES = 0;

    private int filterType;
    private String description;

    private static final List<String> EXTENSIONS_IMAGES = ImageCoder.getInstance().getSupportedFileSuffixes();

    public ExtensionFileFilter(int filterType) {
        this.filterType = filterType;
    }

    // Overrides FileFilter
    public boolean accept(File file) {
        if (file.isDirectory())
            return true;

        return isValidExtension(file.getName());
    }

    // Implements FilenameFilter
    public boolean accept(File dir, String fileName) {
        return isValidExtension(fileName);
    }

    protected String initDescription() {
        if (getFilterType() == FILTER_IMAGES)
            return SystemMessages.getMessage(SystemMessages.UI_FILTER_IMAGES, formatExtensions());
        else
            return "";
    }

    protected String formatExtensions() {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = getExtensions().iterator();
        while (iter.hasNext()) {
            sb.append("*" + iter.next());
            if (iter.hasNext())
                sb.append(",");
        }
        return sb.toString();
    }

    // Overrides FileFilter
    public String getDescription() {
        if (description != null)
            return description;
        description = initDescription();
        return description;
    }

    private String getExtension(String fileName) {
        if (fileName == null)
            return null;
        int index = fileName.lastIndexOf('.');
        if (index == -1)
            return null;
        return fileName.substring(index);
    }

    private boolean checkValidExtension(String suffix) {
        List<String> extensions = getExtensions();
        if (extensions == null)
            return false;
        return extensions.contains(suffix.toLowerCase());
    }

    public boolean isValidExtension(String fileName) {
        String suffix = getExtension(fileName);
        if (suffix == null)
            return false;
        return checkValidExtension(suffix);
    }

    /**
     * If fileName has a valid extension, strip it and return the name.
     * Otherwise return fileName unmodified.
     */
    public String stripValidExtension(String fileName) {
        String suffix = getExtension(fileName);
        if (suffix != null && checkValidExtension(suffix))
            return fileName.substring(0, fileName.length() - suffix.length());
        else
            return fileName;
    }

    public List<String> getExtensions() {
        if (getFilterType() == FILTER_IMAGES)
            return EXTENSIONS_IMAGES;
        return null;
    }

    public int getFilterType() {
        return filterType;
    }
}
