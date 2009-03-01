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
 
package com.photica.photopulse.ui.wizard.viewer;

import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.DecodedImageIcon;
import com.photica.photopulse.imageio.IIOReadListener;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.ThumbnailDecoder;
import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.ui.wizard.EmptyIcon;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.photopulse.SystemMessages;
import com.photica.ui.DragList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Icon;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class PhotoViewer extends JPanel {
    private static NearestNeighborThumbnailDecoder thumbnailDecoder = null;
    private static final int PHOTO_WIDTH = ThumbnailCache.THUMBNAIL_WIDTH * 4;
    private static final int PHOTO_HEIGHT = ThumbnailCache.THUMBNAIL_HEIGHT * 4;
    private static final Icon EMPTY_ICON = new EmptyIcon(PHOTO_WIDTH, PHOTO_HEIGHT);

    private ShowList showList;

    private int photoIndex = 0;
    private PhotoCache photoCache;

    private JLabel photoLabel = null;

    private JButton prevPhotoButton;
    private JButton nextPhotoButton;
    private JButton deletePhotoButton;


    /**
     * Show photo in a screenshot dialog.
     */
    public static void showDialog(DragList<ShowList,PhotoEffects> list, int index) {
        PhotoViewer viewer = new PhotoViewer(list.getModel(), index);
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(list), viewer, UIMessages.getMessage(UIMessages.UI_VIEWER_TITLE),
                JOptionPane.PLAIN_MESSAGE);
        viewer.dispose();
    }

    private PhotoViewer(ShowList showList, int index) {
        super(new GridBagLayout());

        this.showList = showList;

        photoLabel = new JLabel();
        photoLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        photoLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        photoLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        Insets margin = new Insets(3, 3, 3, 3);
        prevPhotoButton = new JButton(new ResourceIcon("resources/left.gif"));
        prevPhotoButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_LABEL_PREV));
        prevPhotoButton.setMargin(margin);
        prevPhotoButton.addActionListener(new NavigatePhotoHandler(-1));
        nextPhotoButton = new JButton(new ResourceIcon("resources/right.gif"));
        nextPhotoButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_LABEL_NEXT));
        nextPhotoButton.setMargin(margin);
        nextPhotoButton.addActionListener(new NavigatePhotoHandler(1));
        deletePhotoButton = new JButton(new ResourceIcon("resources/clear.gif"));
        deletePhotoButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_LABEL_DELETE));
        deletePhotoButton.setMargin(margin);
        deletePhotoButton.addActionListener(new DeletePhotoHandler());

        photoCache = new PhotoCache();

        updatePhoto(index);

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 3;
        gbc.fill = WizardGBC.BOTH;
        gbc.insets.bottom = 5;
        add(photoLabel, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        add(prevPhotoButton, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.WEST;
        add(nextPhotoButton, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.left = 5;
        add(deletePhotoButton, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.top = 5;
        add(new JSeparator(), gbc);
    }

    // return resources
    private void dispose() {
        photoCache.clear();
    }

    private void updatePhoto(int index) {
        int size = showList.size();

        if (index < 0)
            index = 0;
        else if (index >= size)
            index = size - 1;

        this.photoIndex = index;

        if (size == 0) {
            photoLabel.setIcon(EMPTY_ICON);
            photoLabel.setText(" ");
            updateButtonState();
            return;
        }

        Photo photo = showList.get(photoIndex).getPhoto();
        photoLabel.setIcon(photo.isFlashPhoto() ? EMPTY_ICON : photoCache.getCachedIcon(photo));

        String title = UIMessages.getMessage(UIMessages.UI_VIEWER_LABEL,
                photo.getFile().getName(), String.valueOf(photo.getWidth()), String.valueOf(photo.getHeight()));
        photoLabel.setText(title);

        updateButtonState();
    }

    private void updateButtonState() {
        int size = showList.size();
        nextPhotoButton.setEnabled(size > photoIndex + 1);
        prevPhotoButton.setEnabled(photoIndex > 0);
        deletePhotoButton.setEnabled(photoIndex >= 0 && photoIndex < size);
    }

    private class NavigatePhotoHandler implements ActionListener {
        private int increment;

        public NavigatePhotoHandler(int increment) {
            this.increment = increment;
        }

        public void actionPerformed(ActionEvent e) {
            updatePhoto(photoIndex + increment);
        }
    }

    private class DeletePhotoHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showList.remove(photoIndex);
            // This will fix photoIndex if we just deleted the last photo
            updatePhoto(photoIndex);
        }
    }

    // Simple cache that holds a limited number of items.
    private class PhotoCache extends LinkedHashMap<Photo,DecodedImageIcon> {
        private static final int CACHE_MAX = 10;
        private PhotoCache() {
            // cache the last 10 image
            super(CACHE_MAX);
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            boolean shouldRemove = size() >= CACHE_MAX;
            return shouldRemove;
        }

        public Icon getCachedIcon(Photo photo) {
            // XXX use different strategy if performance increase is desired
            // Always remove from cache - remove/replace makes it most recently used
            DecodedImageIcon photoIcon = this.remove(photo);

            // Get the photo if not in cache
            if (photoIcon == null) {
                if (thumbnailDecoder == null)
                    thumbnailDecoder = new NearestNeighborThumbnailDecoder();
                photoIcon = ImageCoder.getInstance().createImageIcon(photo, PHOTO_WIDTH, PHOTO_HEIGHT,
                        thumbnailDecoder, photoLabel);
            }

            // Always place in cache to make it "new"
            this.put(photo, photoIcon);
            return photoIcon;
        }
    }

    // implement ThumbnailDecoder with nearest neighbor scaling
    private static class NearestNeighborThumbnailDecoder implements ThumbnailDecoder {
        public DecodedImage decodeThumbnail(Photo photo, int scaleWidth, int scaleHeight, IIOReadListener listener, boolean fastDecode) throws IOException {
            if (fastDecode)
                throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, photo.getFile().getName()));
            return ImageCoder.getInstance().decodeImage(photo.getFile(), null, 0.0, scaleWidth, scaleHeight,
                    ImageTranscoder.SCALE_NEAREST_NEIGHBOR, listener);
        }
    }
}
