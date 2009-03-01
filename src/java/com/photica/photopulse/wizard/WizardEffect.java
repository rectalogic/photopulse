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
 
package com.photica.photopulse.wizard;

import com.photica.photopulse.model.effects.BaseEffect;
import com.photica.photopulse.model.effects.EndTransition;
import com.photica.photopulse.model.effects.Effect;

import java.util.List;

/**
 * Wrapper for Wizard effects/transitions.
 */
public class WizardEffect<E extends BaseEffect> {
    private String key;
    private String displayName;
    private E effect;

    WizardEffect(E effect, String key) {
        this.effect = effect;
        this.key = key;
    }

    public E getEffect() {
        return effect;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        // Cache display name
        if (displayName == null)
            displayName = DisplayNames.getDisplayName(key);
        return displayName;
    }

    public String toString() {
        return "WizardEffect[" + key + "]";
    }
}
