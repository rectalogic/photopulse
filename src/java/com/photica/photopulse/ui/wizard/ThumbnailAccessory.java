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

import com.photica.photopulse.imageio.DecodedImageIcon;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.cache.SoftCache;
import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.model.Photo;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * JFileChooser accessory to render image thumbnail
 */
public class ThumbnailAccessory extends JLabel implements PropertyChangeListener {

    private JFileChooser fileChooser;
    private boolean isActive = true;

    // Cache mapping Files to thumbnail DecodedImageIcons, uses SoftReferences
    private SoftCache<File,DecodedImageIcon> thumbnailCache = new SoftCache<File,DecodedImageIcon>();

    public ThumbnailAccessory(JFileChooser fc) {
        fileChooser = fc;
        fc.addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
    }

    public Dimension getPreferredSize() {
        Insets insets = getInsets();
        return new Dimension(ThumbnailCache.THUMBNAIL_WIDTH + insets.left + insets.right,
                ThumbnailCache.THUMBNAIL_HEIGHT + insets.top + insets.bottom);
    }

    public void setImageFile(File imageFile) {
        if (imageFile != null) {
            DecodedImageIcon icon = thumbnailCache.get(imageFile);
            if (icon == null) {
                Photo photo = ImageCoder.getInstance().validatePhotoFile(imageFile);
                if (photo != null) {
                    icon = ImageCoder.getInstance().createImageIcon(photo, ThumbnailCache.THUMBNAIL_WIDTH, ThumbnailCache.THUMBNAIL_HEIGHT,
                            ThumbnailCache.getInstance(), this);
                    thumbnailCache.put(imageFile, icon);
                }
            }
            setIcon(icon);
        }
        else
            setIcon(null);
    }

    public void setActive(boolean active) {
        if (isActive && !active) {
            fileChooser.removePropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
            setImageFile(null);
            setEnabled(false);
        }
        else if (!isActive && active) {
            fileChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY, this);
            setEnabled(true);
            setImageFile(fileChooser.getSelectedFile());
        }

        isActive = active;
    }

    // Implements PropertyChangeListener
    public void propertyChange(PropertyChangeEvent e) {
        if (!isActive)
            return;

        File[] files = (File[])e.getNewValue();

        // Nothing selected now
        if (files == null || files.length == 0)
            setImageFile(null);
        // Single file selected
        else if (files.length == 1)
            setImageFile(files[0]);
        // Multiple files selected
        else {
            File[] oldFiles = (File[])e.getOldValue();
            // If there wasn't anything selected before, use one of the new files
            if (oldFiles == null || oldFiles.length == 0)
                setImageFile(files[0]);
            // A set of files was selected before, and a new set is selected now
            else {
                // Need an ArrayList since it is mutable
                List<File> listNewFiles = new ArrayList<File>(Arrays.asList(files));
                List<File> listOldFiles = Arrays.asList(oldFiles);

                // Remove all the old files from the new list,
                // and use one of the remaining new files
                listNewFiles.removeAll(listOldFiles);
                if (!listNewFiles.isEmpty())
                    setImageFile(listNewFiles.get(0));
            }
        }
    }
}
