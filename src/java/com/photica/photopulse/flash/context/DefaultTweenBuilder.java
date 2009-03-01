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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.flash.context;

import com.iv.flash.api.CXForm;
import com.iv.flash.api.FlashDef;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.shape.MorphShape;
import com.iv.flash.util.IVException;

import java.awt.geom.AffineTransform;

public class DefaultTweenBuilder implements TweenBuilder {
    public static final int TWEEN_INSTANCE_ADD = 1;
    public static final int TWEEN_INSTANCE_MODIFY = 2;
    public static final int TWEEN_INSTANCE_IGNORE = 3;

    private Script script;
    private FlashDef def;
    private int depth;
    private int tweenInst;

    public DefaultTweenBuilder(Script script, FlashDef def, int depth, int tweenInst) {
        this.script = script;
        this.depth = depth;
        this.def = def;
        this.tweenInst = tweenInst;
    }

    public boolean isMorphTween() {
        return def instanceof MorphShape;
    }

    public Instance createInitialInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        switch (tweenInst) {
        case TWEEN_INSTANCE_ADD:
            return script.getFrameAt(frame).addInstance(def, depth, atxTween, cxfTween);
        case TWEEN_INSTANCE_MODIFY:
            return script.getFrameAt(frame).addInstance(depth, atxTween, cxfTween);
        case TWEEN_INSTANCE_IGNORE:
            return null;
        default:
            throw new IllegalArgumentException("Invalid tween instance action");
        }
    }

    public Instance createInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        return script.getFrameAt(frame).addInstance(depth, atxTween, cxfTween);
    }
}
