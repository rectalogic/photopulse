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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.ui.wizard.expert.panzoom;

import javax.swing.JComponent;
import javax.swing.JRootPane;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Stage extends JComponent {
    /** Percentage of overall UI occupied by actual stage */
    private static final double STAGE_AREA = 0.85;

    // Translucent stage borders
    private static final Color STAGE_COLOR = new Color(0xdd, 0xdd, 0xdd, 0xdd);

    // Scale preview rect
    private static final Stroke SCALE_PREVIEW_STROKE = new BasicStroke();
    private static final Stroke SCALE_PREVIEW_STROKE_WIDE = new BasicStroke(3);

    /** Overall UI size, must be square */
    private Dimension uiSize;

    /** Stage bounds within UI */
    private Dimension stageSize;

    private Keyframe keyframe;
    private KeyframeHandler keyframeHandler = new KeyframeHandler();

    /** Ignore translation property notifies when this is set */
    private boolean ignoreTranslateEvents;

    private BufferedImage image;

    private int scaledWidth;
    private int scaledHeight;
    private BufferedImage scaledImage;

    private AffineTransform atxImage = new AffineTransform();
    private AffineTransform atxScalePreview;

    public Stage(BufferedImage image, int size, double aspectRatio) {
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

        // All computations relative to a square stage.
        // If we are resized, that is OK - keep stage constant.
        uiSize = new Dimension(size, size);
        stageSize = computeStageSize(uiSize, aspectRatio);

        setOpaque(true);

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);

        this.image = image;
    }

    /**
     * Compute stage bounds for a stage of the specified aspectRatio, centered in the UI.
     */
    private Dimension computeStageSize(Dimension size, double aspectRatio) {
        double width;
        double height;

        // Portrait
        if (aspectRatio <= 1.0) {
            height = size.height * STAGE_AREA;
            width = height * aspectRatio;
        }
        // Landscape
        else {
            width = size.width * STAGE_AREA;
            height = width * 1/aspectRatio;
        }

        Dimension s = new Dimension();
        s.setSize(width, height);
        return s;
    }

    public void setKeyframe(Keyframe keyframe) {
        Keyframe oldKeyframe = this.keyframe;
        if (oldKeyframe != null)
            oldKeyframe.removePropertyChangeListener(keyframeHandler);
        this.keyframe = keyframe;

        handleScaleChange(oldKeyframe != null ? oldKeyframe.getScaleFactor() : 0, keyframe.getScaleFactor());

        keyframe.addPropertyChangeListener(keyframeHandler);
    }

    /**
     * Preview scaling - use prior to setting the scaleFactor on the Keyframe itself.
     * Preview must be reset by passing -1.
     */
    public void setScalePreview(double scaleFactor) {
        if (scaleFactor == -1) {
            atxScalePreview = null;
            repaint();
            return;
        }

        if (atxScalePreview == null)
            atxScalePreview = new AffineTransform();
        scaledWidth = (int)(image.getWidth() * scaleFactor);
        scaledHeight = (int)(image.getHeight() * scaleFactor);

        updateTransform(atxScalePreview);

        repaint();
    }

    private void handleScaleChange(double oldScale, double newScale) {
        // Stop previewing
        atxScalePreview = null;

        if (newScale != oldScale) {
            // If we are scaling down, use the current scaled image as the source
            BufferedImage sourceImage = image;
            if (newScale < oldScale)
                sourceImage = scaledImage;

            scaledWidth = (int)(newScale * image.getWidth());
            scaledHeight = (int)(newScale * image.getHeight());
            if (scaledWidth == 0 || scaledHeight == 0)
                scaledImage = null;
            else {
                JRootPane root = this.getRootPane();
                try {
                    // Display busy cursor while scaling image
                    if (root != null) {
                        root.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        root.getGlassPane().setVisible(true);
                    }
                    GraphicsConfiguration gc = this.getGraphicsConfiguration();
                    if (gc == null)
                        gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                    scaledImage = gc.createCompatibleImage(scaledWidth, scaledHeight, sourceImage.getTransparency());
                    Graphics g = scaledImage.getGraphics();
                    g.drawImage(sourceImage, 0, 0, scaledWidth, scaledHeight, null);
                    g.dispose();
                } finally {
                    if (root != null) {
                        root.getGlassPane().setVisible(false);
                        root.getGlassPane().setCursor(null);
                    }
                }
            }
        }

        updateTransform(atxImage);
    }

    private void addTranslation(int translateX, int translateY) {
        keyframe.setTranslateX(keyframe.getTranslateX() + translateX);
        keyframe.setTranslateY(keyframe.getTranslateY() + translateY);

        updateTransform(atxImage);
    }

    private void updateTransform(AffineTransform atx) {
        // Treat center stage as 0,0 and center of scaled image as 0,0

        atx.setToIdentity();
        atx.translate(-scaledWidth/2.0 + keyframe.getTranslateX(), -scaledHeight/2.0 + keyframe.getTranslateY());
        atx.rotate(keyframe.getRotationFactor(), scaledWidth/2.0, scaledHeight/2.0);

        repaint();
    }

    public Dimension getImageSize() {
        return new Dimension(image.getWidth(), image.getHeight());
    }
    
    public Dimension getStageSize() {
        return stageSize;
    }

    public Dimension getMaximumSize() {
        return uiSize;
    }

    public Dimension getMinimumSize() {
        return uiSize;
    }

    public Dimension getPreferredSize() {
        return uiSize;
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        // Background
        g2.setBackground(Color.GRAY);
        g2.clearRect(0, 0, getWidth(), getHeight());

        if (scaledImage != null || atxScalePreview != null) {
            // Treat center stage as 0,0 and center of scaled image as 0,0
            g2.translate(getWidth()/2, getHeight()/2);

            // Draw image
            if (scaledImage != null) {
                AffineTransform atxSave = g2.getTransform();
                g2.transform(atxImage);
                g2.drawImage(scaledImage, 0, 0, this);
                g2.setTransform(atxSave);
            }

            // Draw scale preview
            if (atxScalePreview != null) {
                AffineTransform atxSave = g2.getTransform();
                g2.transform(atxScalePreview);

                g2.setColor(Color.BLACK);
                g2.setStroke(SCALE_PREVIEW_STROKE_WIDE);
                g2.drawRect(0, 0, scaledWidth, scaledHeight);
                g2.drawLine(0, 0, scaledWidth, scaledHeight);
                g2.drawLine(0, scaledHeight, scaledWidth, 0);

                g2.setColor(Color.WHITE);
                g2.setStroke(SCALE_PREVIEW_STROKE);
                g2.drawRect(0, 0, scaledWidth, scaledHeight);
                g2.drawLine(0, 0, scaledWidth, scaledHeight);
                g2.drawLine(0, scaledHeight, scaledWidth, 0);

                g2.setTransform(atxSave);
            }

            g2.translate(-getWidth()/2, -getHeight()/2);
        }

        // Draw stage bounds
        g2.setColor(STAGE_COLOR);
        int stageX = (getWidth() - stageSize.width) / 2;
        int stageY = (getHeight() - stageSize.height) / 2;
        // Top
        g2.fillRect(0, 0, getWidth(), stageY);
        // Bottom
        g2.fillRect(0, stageY + stageSize.height, getWidth(), getHeight() - stageY + stageSize.height);
        // Left
        g2.fillRect(0, stageY, stageX, stageSize.height);
        // Right
        g2.fillRect(stageX + stageSize.width, stageY, getWidth() - stageX + stageSize.width, stageSize.height);
    }

    /**
     * Handle changes to underlying Keyframe model
     */
    private class KeyframeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            if (Keyframe.SCALE_FACTOR_PROPERTY == e.getPropertyName())
                handleScaleChange((Double)e.getOldValue(), (Double)e.getNewValue());
            else if (Keyframe.ROTATION_FACTOR_PROPERTY == e.getPropertyName())
                updateTransform(atxImage);
            else if (!ignoreTranslateEvents && (Keyframe.TRANSLATE_X_PROPERTY == e.getPropertyName() || Keyframe.TRANSLATE_Y_PROPERTY == e.getPropertyName()))
                updateTransform(atxImage);
        }
    }

    private class MouseHandler implements MouseListener, MouseMotionListener {
        private int offsetX;
        private int offsetY;

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {
            // Ignore translation changes to Keyframe since we are the ones making the change
            ignoreTranslateEvents = true;
            offsetX = e.getX();
            offsetY = e.getY();
        }

        public void mouseReleased(MouseEvent e) {
            ignoreTranslateEvents = false;
            addTranslation(e.getX() - offsetX, e.getY() - offsetY);
            offsetX = e.getX();
            offsetY = e.getY();
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
            addTranslation(e.getX() - offsetX, e.getY() - offsetY);
            offsetX = e.getX();
            offsetY = e.getY();
        }

        public void mouseMoved(MouseEvent e) {}
    }
}
