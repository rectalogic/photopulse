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
 
package com.photica.photopulse.wizard;

import com.photica.photopulse.model.effects.Effect;

public class WizardEffectEffect extends WizardEffect<Effect> {
    private double photoScale = 1.0;

    WizardEffectEffect(Effect effect, String key) {
        super(effect, key);
    }

    WizardEffectEffect(Effect effect, String key, double photoScale) {
        super(effect, key);
        this.photoScale = photoScale;
    }

    /**
     * Scale factor to be passed through to the containing PhotoEffects for this effect.
     * Only valid for effects, not begin/end trans.
     * @see com.photica.photopulse.model.PhotoEffects#getPhotoScale
     */
    public double getPhotoScale() {
        return photoScale;
    }
}
