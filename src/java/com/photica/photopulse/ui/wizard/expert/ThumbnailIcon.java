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
 
package com.photica.photopulse.ui.wizard.expert;

import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.ui.wizard.ResourceIcon;

import javax.swing.Icon;
import java.awt.Component;
import java.awt.Graphics;

/**
 * Render a photo thumbnail with a locked icon adornment.
 * The icon size is a fixed thumbnail size, it centers it's thumbnail icon.
 */
public class ThumbnailIcon implements Icon {
    private static final Icon LOCK_ICON = new ResourceIcon("resources/lock.png");

    private Icon thumbnailIcon;
    private boolean isLocked;


    public Icon getThumbnailIcon() {
        return thumbnailIcon;
    }

    public void setThumbnailIcon(Icon thumbnailIcon) {
        this.thumbnailIcon = thumbnailIcon;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getIconHeight() {
        return ThumbnailCache.THUMBNAIL_HEIGHT;
    }

    public int getIconWidth() {
        return ThumbnailCache.THUMBNAIL_WIDTH;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (thumbnailIcon != null) {
            thumbnailIcon.paintIcon(c, g,
                    x + getIconWidth()/2 - thumbnailIcon.getIconWidth()/2,
                    y + getIconHeight()/2 - thumbnailIcon.getIconHeight()/2);
        }

        if (isLocked)
            LOCK_ICON.paintIcon(c, g, x + getIconWidth() - LOCK_ICON.getIconWidth(), y);
    }
}
