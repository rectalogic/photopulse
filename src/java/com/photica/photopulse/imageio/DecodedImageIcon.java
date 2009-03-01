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
 
package com.photica.photopulse.imageio;

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.model.Photo;

import javax.imageio.ImageReader;
import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Loads a subsampled thumbnail on a background thread.
 * Renders a progress bar while loading.
 * Renders a broken image if loading fails.
 * Renders the image once loaded.
 */
public class DecodedImageIcon implements Runnable, Icon {
    private DecodedImage decodedImage;

    private Photo photo;
    private int thumbnailWidth;
    private int thumbnailHeight;

    private boolean isLoaded = false;
    private ThumbnailDecoder decoder;
    private Component observer;
    private float percentLoaded = 0;

    DecodedImageIcon(Photo photo, int width, int height, ThumbnailDecoder decoder, Component observer) {
        this.photo = photo;
        this.thumbnailWidth = width;
        this.thumbnailHeight = height;
        this.decoder = decoder;
        this.observer = observer;
    }

    DecodedImageIcon(Photo photo, int width, int height, DecodedImage decodedImage) {
        this.photo = photo;
        this.thumbnailWidth = width;
        this.thumbnailHeight = height;
        this.decodedImage = decodedImage;
        this.isLoaded = true;
    }

    /**
     * Image file that is the source of this thumbnail
     */
    public Photo getPhoto() {
        return photo;
    }

    /**
     * Return the image, or null if not yet loaded or failed to load
     */
    public DecodedImage getDecodedImage() {
        return decodedImage;
    }

    /**
     * Return true if the image is loaded or failed to load.
     * Return false if still loading
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        BufferedImage tImage;
        boolean tIsLoaded;
        float tPercentLoaded;
        synchronized (this) {
            tImage = this.decodedImage != null ? this.decodedImage.getImage() : null;
            tIsLoaded = this.isLoaded;
            tPercentLoaded = this.percentLoaded;
        }

        if (tIsLoaded) {
            if (tImage == null) {
                // Image failed to load - draw an X
                Graphics2D g2d = (Graphics2D)g;
                g.setColor(Color.RED.darker());
                int offset = thumbnailWidth/20;
                g2d.setStroke(new BasicStroke(offset, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL));
                g.drawLine(x + offset, y + offset, x + thumbnailWidth - offset, y + thumbnailHeight - offset);
                g.drawLine(x + thumbnailWidth - offset, y + offset, x + offset, y + thumbnailHeight - offset);
            }
            else {
                // Image is loaded - render it centered
                g.drawImage(tImage, x + thumbnailWidth/2 - tImage.getWidth()/2,
                        y + thumbnailHeight/2 - tImage.getHeight()/2, null);
            }
        }
        // Image not loaded yet
        else {
            int size = thumbnailHeight/20;
            // Draw progress border
            g.setColor(c.getForeground());
            g.drawRect(x + size - 1, y + (thumbnailHeight/2) - (size/2) - 1, thumbnailWidth - (size*2), size);

            // Draw progress. Percentage is 0->1.0
            if (tPercentLoaded > 0) {
                g.setColor(UIManager.getColor("ProgressBar.foreground"));
                g.fillRect(x + size, y + (thumbnailHeight/2) - (size/2),
                        (int)((thumbnailWidth - (size*2)) * tPercentLoaded), size);
            }
        }
    }

    public int getIconWidth() {
        return thumbnailWidth;
    }

    public int getIconHeight() {
        return thumbnailHeight;
    }

    public void run() {
        // Just in case we were constructed with an image, but someone still stuck us on a thread
        if (isLoaded)
            return;

        DecodedImage dimage = null;
        try {
            dimage = decoder.decodeThumbnail(photo, thumbnailWidth, thumbnailHeight, new ImageReadListener(), false);
        } catch (IOException e) {
            return;
        } catch (Throwable e) {
            PhotoPulse.logException(e);
            return;
        } finally {
            synchronized (this) {
                decodedImage = dimage;
                isLoaded = true;
            }
            decoder = null;

            // Repaint the observer to draw the new state
            if (observer != null)
                observer.repaint();
        }
    }

    private class ImageReadListener implements IIOReadListener {
        public void sequenceStarted(ImageReader reader, int minIndex) {}
        public void sequenceComplete(ImageReader reader) {}

        public void imageStarted(ImageReader reader, int index) {
            if (observer != null)
                observer.repaint();
        }

        // XXX The JAI ImageIO plugin does not report progress very frequently
        public synchronized void imageProgress(ImageReader reader, float percent) {
            // Convert to 0->1.0 percentage
            percentLoaded = percent / 100.0f;
            if (observer != null)
                observer.repaint();
        }

        public void imageComplete(ImageReader reader) {}
        public void thumbnailStarted(ImageReader reader, int imageIndex, int thumbnailIndex) {}
        public void thumbnailProgress(ImageReader reader, float percent) {}
        public void thumbnailComplete(ImageReader reader) {}
        public void readAborted(ImageReader reader) {}
        public void passStarted(ImageReader reader, BufferedImage image, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {}
        public void imageUpdate(ImageReader reader, BufferedImage image, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
        public void passComplete(ImageReader reader, BufferedImage image) {}
        public void thumbnailPassStarted(ImageReader reader, BufferedImage thumbnail, int pass, int minPass, int maxPass, int minX, int minY, int periodX, int periodY, int[] bands) {}
        public void thumbnailUpdate(ImageReader reader, BufferedImage thumbnail, int minX, int minY, int width, int height, int periodX, int periodY, int[] bands) {}
        public void thumbnailPassComplete(ImageReader reader, BufferedImage thumbnail) {}
    }
}
