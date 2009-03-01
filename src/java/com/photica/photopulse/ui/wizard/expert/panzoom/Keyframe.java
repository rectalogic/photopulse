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

import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.imageio.ImageCoder;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.awt.Dimension;
import java.awt.geom.Dimension2D;

/**
 * Keyframe model - mutable data for a single keyframe.
 */
public class Keyframe implements Cloneable {
    public static final String TRANSLATE_X_PROPERTY = "translateX";
    public static final String TRANSLATE_Y_PROPERTY = "translateY";
    public static final String SCALE_FACTOR_PROPERTY = "scaleFactor";
    public static final String ROTATION_FACTOR_PROPERTY = "rotationFactor";

    /** X coord, origin is center of stage */
    private int translateX = 0;
    /** Y coord, origin is center of stage */
    private int translateY = 0;
    /** Scale factor (0-1). 1.0 is full photo size. */
    private double scaleFactor = 1.0;
    /** Rotation factor (0-1). In radians. */
    private double rotationFactor = 0.0;

    private boolean isLinear;
    private boolean hasEasing;

    private PropertyChangeSupport propertyChange;


    public Keyframe(PanZoomEffect.Keyframe keyframe, Dimension stageSize, Dimension imageSize) {
        translateX = (int)(keyframe.getTranslateX() * (stageSize.width/2.0));
        translateY = (int)(keyframe.getTranslateY() * (stageSize.height/2.0));

        double kfScale = keyframe.getScale();
        if (kfScale == PanZoomEffect.Keyframe.UNSCALED)
            scaleFactor = 1.0;
        else {
            // Scale is applied to stage.
            // Apply keyframe scale to the stage, then compute factor to scale the image to that size.
            scaleFactor = computeImageScale(imageSize, stageSize, kfScale);
        }

        rotationFactor = Math.toRadians(keyframe.getRotation());
        isLinear = keyframe.isLinear();
        hasEasing = keyframe.hasEasing();
    }

    public Keyframe(Dimension2D stageSize, Dimension2D imageSize) {
        scaleFactor = computeImageScale(imageSize, stageSize, 1);
    }

    public PanZoomEffect.Keyframe getKeyframe(double startTime, Dimension2D stageSize, Dimension2D imageSize) {
        double kfTranslateX = translateX / (stageSize.getWidth()/2.0);
        double kfTranslateY = translateY / (stageSize.getHeight()/2.0);

        // Scale is applied to stage.
        // Compute factor to scale stage to scaled image size.
        double kfScale = computeStageScale(stageSize, imageSize, scaleFactor);

        // Keyframe stores rotation as degrees
        double kfRotation = Math.toDegrees(rotationFactor);

        return new PanZoomEffect.Keyframe(startTime, isLinear, hasEasing,
                kfTranslateX, kfTranslateY, kfScale, kfRotation);
    }

    /**
     * Compute stage scale factor to fully enclose the scaled image.
     */
    private double computeStageScale(Dimension2D stageSize, Dimension2D imageSize, double imageScale) {
        return Math.max((imageSize.getWidth() * imageScale) / stageSize.getWidth(),
                (imageSize.getHeight() * imageScale) / stageSize.getHeight());
    }

    /**
     * Compute image scale factor to fully fit within the scaled stage (but pin image scale to max 1.0)
     */
    private double computeImageScale(Dimension2D imageSize, Dimension2D stageSize, double stageScale) {
        double scale = ImageCoder.getInstance().computeScaleFactor(imageSize.getWidth(), imageSize.getHeight(),
                    stageSize.getWidth() * stageScale, stageSize.getHeight() * stageScale);
        return scale > 1 ? 1 : scale;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null)
            return;
        if (propertyChange == null)
            propertyChange = new PropertyChangeSupport(this);
        propertyChange.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null || propertyChange == null)
            return;
        propertyChange.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, double oldValue, double newValue) {
        PropertyChangeSupport propertyChange = this.propertyChange;
        if (propertyChange == null || oldValue == newValue)
            return;
        propertyChange.firePropertyChange(propertyName, oldValue, newValue);
    }

    public int getTranslateX() {
        return translateX;
    }

    public void setTranslateX(int translateX) {
        double oldValue = this.translateX;
        this.translateX = translateX;
        firePropertyChange(TRANSLATE_X_PROPERTY, oldValue, translateX);
    }

    public int getTranslateY() {
        return translateY;
    }

    public void setTranslateY(int translateY) {
        double oldValue = this.translateY;
        this.translateY = translateY;
        firePropertyChange(TRANSLATE_Y_PROPERTY, oldValue, translateY);
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(double scaleFactor) {
        double oldValue = this.scaleFactor;
        this.scaleFactor = scaleFactor;
        firePropertyChange(SCALE_FACTOR_PROPERTY, oldValue, scaleFactor);
    }

    public double getRotationFactor() {
        return rotationFactor;
    }

    /**
     * @param rotationFactor Rotation in radians
     */
    public void setRotationFactor(double rotationFactor) {
        double oldValue = this.rotationFactor;
        this.rotationFactor = rotationFactor;
        firePropertyChange(ROTATION_FACTOR_PROPERTY, oldValue, rotationFactor);
    }

    public boolean isLinear() {
        return isLinear;
    }

    public void setLinear(boolean linear) {
        isLinear = linear;
    }

    public boolean hasEasing() {
        return hasEasing;
    }

    public void setEasing(boolean easing) {
        this.hasEasing = easing;
    }

    public Keyframe clone() {
        try {
            Keyframe clone = (Keyframe)super.clone();
            clone.propertyChange = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
