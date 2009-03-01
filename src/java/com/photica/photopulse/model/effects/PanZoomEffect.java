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
 
package com.photica.photopulse.model.effects;


public class PanZoomEffect extends Effect {

    public static class Keyframe implements Cloneable {
        /** This scale factor means do not scale */
        public static final double UNSCALED = -1.0;

        private double startTime;
        private boolean isLinear = false;
        private boolean hasEasing = false;
        private double translateX = 0;
        private double translateY = 0;
        private double scale = UNSCALED;
        private double rotation = 0;

        /**
         * @param startTime Start time of keyframe as a percentage of containing effects duration 0.0 -> 1.0
         *   Each keyframe must have a startTime greater than the previous keyframe, first must be 0.
         * @param isLinear
         * @param hasEasing
         * @param translateX Unit coords - 0,0 is centerstage, -1,0 is stage left, 1,0 stage right
         * @param translateY Unit coords - 0,0 is centerstage, 0,1 is stage bottom, 0,-1 stage top
         * @param scale Percentage of scale - 1.0 means scale the photo to fit the stage, UNSCALED means do not scale
         * @param rotation Degrees
         */
        public Keyframe(double startTime, boolean isLinear, boolean hasEasing, double translateX, double translateY, double scale, double rotation) {
            this.startTime = startTime;
            this.isLinear = isLinear;
            this.hasEasing = hasEasing;
            this.translateX = translateX;
            this.translateY = translateY;
            this.scale = scale;
            this.rotation = rotation;
        }

        /**
         * Start time as percentage of effects duration 0.0 -> 1.0
         */
        public double getStartTime() {
            return startTime;
        }

        public boolean isLinear() {
            return isLinear;
        }

        public boolean hasEasing() {
            return hasEasing;
        }

        public double getTranslateX() {
            return translateX;
        }

        public double getTranslateY() {
            return translateY;
        }

        public double getScale() {
            return scale;
        }

        public double getRotation() {
            return rotation;
        }

        /**
         * Override hashcode to return a value based on contents.
         * @return int hash code
         */
        public int hashCode() {
            return (int)(startTime + translateX + translateY + scale + rotation + (isLinear ? 123.0 : 456.0) + (hasEasing ? 321.0 : 654));
        }

        /**
         * Compare keyframes for equality.
         * @param other KeyFrame instance
         * @return
         */
        public boolean equals(Object other) {
            boolean ret = false;
            if ((other != null) && (other.getClass().equals(this.getClass()))) {
                Keyframe kf = (Keyframe)other;
                return startTime == kf.startTime &&
                        isLinear == kf.isLinear &&
                        hasEasing == kf.hasEasing &&
                        translateX == kf.translateX &&
                        translateY == kf.translateY &&
                        scale == kf.scale &&
                        rotation == kf.rotation;
            }
            return ret;
        }
    }

    private Keyframe[] keyframes = new Keyframe[0];


    /**
     *
     * @return hashCode comprised of hashcodes from the keyframes.
     */
    public int hashCode() {
        int ret = 0;
        if (keyframes.length > 0) {
            for (int x = 0; x < keyframes.length; x++) {
                // or the keyframe hashcodes together and shift depending on position
                // this make the order matter some in generating the hash
                ret |= keyframes[x].hashCode() << x % 4;
            }
        }
        return ret;
    }

    /**
     * Determine equality by checking key frames.
     * @param other - another PanZoomEffect
     * @return
     */
    public boolean equals(Object other) {
        boolean ret = super.equals(other);
        if (ret) {
            PanZoomEffect pz = (PanZoomEffect)other;
            if (keyframes.length == pz.keyframes.length) {
                ret = true;
                for (int x = 0; x < keyframes.length; x++) {
                    if (!keyframes[x].equals(pz.keyframes[x])) {
                        ret = false;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    PanZoomEffect(String tag) {
        super(tag);
    }

    public PanZoomEffect clonePanZoomEffect(Keyframe[] keyframes) throws IllegalArgumentException {
        validateKeyframes(keyframes);

        PanZoomEffect clone = (PanZoomEffect)clone();
        if (keyframes == null)
            clone.keyframes = null;
        else
            clone.keyframes = (Keyframe[])keyframes.clone();
        return clone;
    }

    public Keyframe[] getKeyframes() {
        if (keyframes == null)
            return null;
        else
            return (Keyframe[])keyframes.clone();
    }

    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Ensure keyframe start times are in increasing order and 0 &lt;= time &lt;= 1.
     */
    private void validateKeyframes(Keyframe[] keyframes) throws IllegalArgumentException {
        double startTime = -1;
        for (int i = 0; i < keyframes.length; i++) {
            double frameTime = keyframes[i].getStartTime();
            if (frameTime < 0 || frameTime > 1 || frameTime < startTime)
                throw new IllegalArgumentException("invalid keyframe array");
            startTime = frameTime;
        }
    }
}

