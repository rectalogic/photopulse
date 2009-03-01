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

import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.model.effects.EndTransition;

public class PhotoEffects {
    private Photo photo = null;
    private String eventArg = null;
    private double photoScale = ShowModel.DEFAULT_PHOTO_SCALE;
    private BeginTransition beginTransition = EffectRegistry.BEGINTRANS_None;
    private Effect effect = EffectRegistry.EFFECT_None;
    private EndTransition endTransition = EffectRegistry.ENDTRANS_None;
    private double beginTransitionDuration;
    private double effectDuration;
    private double endTransitionDuration;
    private boolean isEndTransitionTopLayer = false;
    private boolean isLocked = false;

    public PhotoEffects(Photo photo) {
        this.photo = photo;
    }

    public PhotoEffects(Photo photo,
            double beginTransitionDuration,
            double effectDuration,
            double endTransitionDuration) {
        this.photo = photo;
        this.beginTransitionDuration = beginTransitionDuration;
        this.effectDuration = effectDuration;
        this.endTransitionDuration = endTransitionDuration;
    }

    public PhotoEffects(Photo photo, String eventArg, double photoScale,
            BeginTransition beginTransition, double beginTransitionDuration,
            Effect effect, double effectDuration,
            EndTransition endTransition, double endTransitionDuration, boolean isEndTransitionTopLayer, boolean isLocked) {
        this.photo = photo;
        this.eventArg = eventArg;
        this.photoScale = photoScale;
        this.beginTransition = beginTransition;
        this.beginTransitionDuration = beginTransitionDuration;
        this.effect = effect;
        this.effectDuration = effectDuration;
        this.endTransition = endTransition;
        this.endTransitionDuration = endTransitionDuration;
        this.isEndTransitionTopLayer = isEndTransitionTopLayer;
        this.isLocked = isLocked;
    }

    public PhotoEffects(PhotoEffects effects,
            double beginTransitionDuration,
            double effectDuration,
            double endTransitionDuration) {
        this(effects.photo, effects.eventArg, effects.photoScale,
                effects.beginTransition, beginTransitionDuration,
                effects.effect, effectDuration,
                effects.endTransition, endTransitionDuration, effects.isEndTransitionTopLayer, effects.isLocked);
    }

    public BeginTransition getBeginTransition() {
        return beginTransition;
    }

    public double getBeginTransitionDuration() {
        return beginTransitionDuration;
    }

    public Effect getEffect() {
        return effect;
    }

    public double getEffectDuration() {
        return effectDuration;
    }

    public EndTransition getEndTransition() {
        return endTransition;
    }

    public double getEndTransitionDuration() {
        return endTransitionDuration;
    }

    public boolean isEndTransitionTopLayer() {
        return isEndTransitionTopLayer;
    }

    public Photo getPhoto() {
        return photo;
    }

    /**
     * If locked, then the effect and transitions should not be modified.
     */
    public boolean isLocked() {
        return isLocked;
    }

    /**
     * Event argument associated with this photo.
     * This is passed to the Skins eventHandler if any.
     * Typically this is the title of the photo, but it could be a javascript fragment etc.
     */
    public String getEventArg() {
        return eventArg;
    }

    /**
     * Scale factor for photo as a percentage of stage size, 1.0 means fit the stage.
     * This scales the actual bits of the photo, not just a visual transform.
     * The photo will not be scaled up.
     */
    public double getPhotoScale() {
        return photoScale;
    }

    public String toString() {
        if (photo != null)
            return photo.getFile().getName();
        else
            return "";
    }
}

