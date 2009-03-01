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
 
package com.photica.photopulse.ui.wizard.expert;

import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.ui.wizard.EmptyIcon;
import com.photica.photopulse.ui.wizard.ResourceIcon;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.SwingConstants;
import java.awt.Component;

/**
 * ListCellRenderer that can render PhotoEffects objects as thumbnails.
 */
public class PhotoListCellRenderer extends DefaultListCellRenderer {

    /**
     * This object can be used as the prototype cell value
     */
    public static final EmptyIcon EMPTY_ICON = new EmptyIcon(ThumbnailCache.THUMBNAIL_WIDTH, ThumbnailCache.THUMBNAIL_HEIGHT);
    public static final ResourceIcon FLASH_ICON = new ResourceIcon("resources/flashphoto.png");

    private ThumbnailIcon thumbnailIcon = new ThumbnailIcon();

    public PhotoListCellRenderer() {
        setHorizontalAlignment(SwingConstants.CENTER);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        Icon icon = null;
        Photo photo = null;


        if (value == EMPTY_ICON) {
            icon = EMPTY_ICON;
        }
        // Store hard references to Icons in the Photo.
        // These may all be cleared when generating a show.
        else {
            PhotoEffects effects = (PhotoEffects)value;
            photo = effects.getPhoto();
            icon = photo.getIcon();
            if (icon == null) {
                if (photo.isFlashPhoto()) {
                    icon = FLASH_ICON;
                }
                else {
                    icon = ImageCoder.getInstance().createImageIcon(photo,
                        ThumbnailCache.THUMBNAIL_WIDTH, ThumbnailCache.THUMBNAIL_HEIGHT,
                        ThumbnailCache.getInstance(), list);
                }
                photo.setIcon(icon);
            }
            thumbnailIcon.setThumbnailIcon(icon);
            thumbnailIcon.setLocked(effects.isLocked());
            icon = thumbnailIcon;
        }


        Component c = super.getListCellRendererComponent(list, icon, index, isSelected, cellHasFocus);
        setText(photo == null ? " " : photo.getFile().getName());
        return c;
    }
}
