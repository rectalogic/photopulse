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

import com.iv.flash.api.Instance;
import com.iv.flash.api.Frame;
import com.iv.flash.api.CXForm;
import com.iv.flash.util.IVException;

import java.awt.geom.AffineTransform;

/**
 * Interface for creating Flash Instances when tweening
 */
public interface TweenBuilder {

    /**
     * Returns true if tween is a morph
     */
    public boolean isMorphTween();

    /**
     * Return initial Instance - may return null
     */
    public Instance createInitialInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException;

    /**
     * Called for all subsequent instances - may return null
     */
    public Instance createInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException;
}
