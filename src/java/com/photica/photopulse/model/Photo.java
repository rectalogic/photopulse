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
 
package com.photica.photopulse.model;

import javax.swing.Icon;
import java.io.File;

/**
 * This class is immutable, except for the Icon property.
 * The Icon is ignored and is just a place for the app to hang an icon.
 */
public class Photo {

    private boolean isFlashPhoto;
    private int width;
    private int height;
    private File file = null;
    private Icon icon;

    /**
     *
     * @param file - file - thows NullPointerException if null
     * @param width
     * @param height
     * @param isFlashPhoto true if this is a Flash file instead of an image
     */
    public Photo(File file, int width, int height, boolean isFlashPhoto) {
        if (file == null)
            throw new NullPointerException("null file not allowed");
        this.file = file;
        this.width = width;
        this.height= height;
        this.isFlashPhoto = isFlashPhoto;
    }

    public File getFile() {
        return file;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Clears photo icon.
     * For flash photos, icon is not cleared.
     */
    public void clearIcon() {
        if (isFlashPhoto)
            return;
        else
            setIcon(null);
    }

    public int hashCode() {
        return file.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof Photo))
            return false;
        Photo p = (Photo)o;
        return file.equals(p.file) && width == p.width && height == p.height && isFlashPhoto == p.isFlashPhoto;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFlashPhoto() {
        return isFlashPhoto;
    }

    public String toString() {
        return "Photo[" + file.toString() + "]";
    }
}
