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

package com.photica.photopulse.commands;

import com.iv.flash.api.FlashDef;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.shape.MorphShape;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.commands.GenericCommand;
import com.iv.flash.context.Context;
import com.iv.flash.util.IVException;
import com.iv.flash.util.Resource;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.flash.context.PhotoContext;
import com.photica.photopulse.flash.context.TweenBuilder;
import com.photica.photopulse.flash.context.DefaultTweenBuilder;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Inserts the specified shape as a motion/morph tween
 */
public class InsertShapeTweenCommand extends GenericCommand {

    public void doCommand(FlashFile file, Context ctxCommand, Script scrParent, int nFrame) throws IVException {
        PhotoContext ctxPhoto = PhotoContext.findContext(ctxCommand);
        if (ctxPhoto == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_PHOTOCONTEXT);

        String strShapeName = getParameter(ctxCommand, "shape", "");
        String strShapeType = getParameter(ctxCommand, "type", "Shape");

        // Load the specified shape and get its bounds
        FlashDef defShape = null;
        Rectangle2D rectShapeBounds = null;
        if ("Shape".equals(strShapeType)) {
            Shape shape = ctxPhoto.loadShape(strShapeName);
            if (shape != null) {
                rectShapeBounds = shape.getBounds();
                defShape = shape;
            }
        }
        else {
            MorphShape shape = ctxPhoto.loadMorphShape(strShapeName);
            if (shape != null) {
                rectShapeBounds = shape.getBoundsStart();
                defShape = shape;
            }
        }
        if (defShape == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { strShapeName, getCommandName() });

        Instance inst = getInstance();

        inst.def = defShape;

        // Get initial shape size (to compute initial scale factor)
        double dblInitWidth = ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "initwidth", 0);
        if (dblInitWidth == 0)
            dblInitWidth = rectShapeBounds.getWidth();
        double dblInitHeight = ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "initheight", 0);
        if (dblInitHeight == 0)
            dblInitHeight = rectShapeBounds.getHeight();
        Rectangle2D rectDestBounds = new Rectangle2D.Double(0, 0, dblInitWidth, dblInitHeight);

        boolean bExpandRadius = getBoolParameter(ctxCommand, "expand", false);
        boolean bMaintainAspect = getBoolParameter(ctxCommand, "aspect", true);

        // Compute begin transform
        AffineTransform atxBegin = new AffineTransform(inst.matrix);
        atxBegin.translate(ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "xbegin", 0),
            ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "ybegin", 0));
        ctxPhoto.scaleSourceToDest(rectShapeBounds, rectDestBounds, atxBegin,
            bMaintainAspect, bExpandRadius);
        atxBegin.scale(getDoubleParameter(ctxCommand, "beginscalex", 0)/100,
            getDoubleParameter(ctxCommand, "beginscaley", 0)/100);

        int nDuration = ctxPhoto.convertDurationToFrames(getDoubleParameter(ctxCommand, "duration", 0.0));

        if (nDuration > 1) {
            // Insert needed frames into timeline
            if (getBoolParameter(ctxCommand, "insertframes", false)) {
                int nFramesNeeded = nDuration - (scrParent.getFrameCount() - nFrame);
                if (nFramesNeeded > 0)
                    scrParent.getTimeline().insertFrames(nFrame+1, nFramesNeeded);
            }

            // Compute end transform
            AffineTransform atxEnd = new AffineTransform(inst.matrix);
            atxEnd.translate(ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "xend", 0),
                ShowGenerator.TWIPS_PER_PIXEL * getDoubleParameter(ctxCommand, "yend", 0));
            ctxPhoto.scaleSourceToDest(rectShapeBounds, rectDestBounds, atxEnd,
                bMaintainAspect, bExpandRadius);
            atxEnd.scale(getDoubleParameter(ctxCommand, "endscalex", 0)/100,
                getDoubleParameter(ctxCommand, "endscaley", 0)/100);

            ctxPhoto.tween(scrParent, defShape, inst.depth,
                nFrame, nDuration,
                atxBegin, null,
                Math.toRadians(getDoubleParameter(ctxCommand, "beginrotation", 0)), 0, 0,
                atxEnd, null,
                Math.toRadians(getDoubleParameter(ctxCommand, "endrotation", 0)), 0, 0,
                DefaultTweenBuilder.TWEEN_INSTANCE_IGNORE);
        }

        // Now reset initial transform (whether or not we tweened)
        inst.matrix = atxBegin;
    }
}
