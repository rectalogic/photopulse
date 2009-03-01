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

import com.iv.flash.api.AlphaColor;
import com.iv.flash.api.FlashDef;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Frame;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.shape.FillStyle;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.commands.GenericCommand;
import com.iv.flash.context.Context;
import com.iv.flash.util.IVException;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.Util;
import com.photica.photopulse.model.effects.ToneEffect;
import com.photica.photopulse.flash.context.PhotoContext;
import com.photica.photopulse.imageio.ToneOp;
import com.photica.photopulse.imageio.TranscodeOp;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class InsertPhotoCommand extends GenericCommand {

    public void doCommand(FlashFile flashFile, Context context, Script parent, int frame) throws IVException {
        PhotoContext photoContext = PhotoContext.findContext(context);
        if (photoContext == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_PHOTOCONTEXT);

        String imagePath = getParameter(context, "image");
        if (imagePath == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_IMAGEPARAM);
        boolean isFlashPhoto = getBoolParameter(context, "isFlash", false);
        String cropSpec = getParameter(context, "crop", "");
        String scaleSpec = getParameter(context, "scale", "");
        String instanceName = getParameter(context, "instancename");
        String toneSpec = getParameter(context, "tone");

        Instance inst = getInstance();

        FlashDef photoDef = null;

        if (isFlashPhoto) {
            FlashFile photoFlashFile = photoContext.loadFlashPhoto(new File(imagePath));
            // If we fail, set photoDef to a shape to be used below
            if (photoFlashFile == null)
                photoDef = photoContext.createImageErrorShape();
            else {
                // Copy and process FlashFile main instScript if a template
                Script photoScript = photoFlashFile.getMainScript();
                if (photoFlashFile.isTemplate()) {
                    photoScript = photoScript.copyScript();
                    // Process using photoFlashFile so relative paths can be loaded
                    photoScript = photoFlashFile.processScript(photoScript, context);
                }

                Rectangle2D bounds = photoFlashFile.getFrameSize();
                double scaleFactor = Util.parseScale(scaleSpec);

                // Center and scale flash photo instance
                Script instScript = inst.copyScript();
                AffineTransform atx = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
                atx.translate(-bounds.getWidth()/2, -bounds.getHeight()/2);

                final int photoLayer = 3;
                final int maskLayer = 2;
                final int backgroundLayer = 1;

                // Shape to use as mask and to fill background
                Shape shape = createTransparentShape(bounds);

                Frame photoFrame = instScript.newFrame();
                // Add photo and mask it (in case it animates out of bounds)
                photoFrame.addInstance(photoScript, photoLayer, atx, null);
                Instance maskInst = photoFrame.addInstance(shape, maskLayer, atx, null);
                maskInst.clip = photoLayer;
                // Add invisible background so MC.width/height will be full size in ActionScript
                photoFrame.addInstance(shape, backgroundLayer, atx, null);

                inst.setScript(instScript);
            }
        }
        else {
            TranscodeOp op = null;
            if (ToneEffect.Tone.SEPIA.toString().equals(toneSpec))
                op = new TranscodeToneOp(true);
            else if (ToneEffect.Tone.GRAY.toString().equals(toneSpec))
                op = new TranscodeToneOp(false);
            photoDef = photoContext.loadPhoto(new File(imagePath), cropSpec, scaleSpec, op);
        }

        inst.name = instanceName;

        // If photoDef is set, this is either a simple bitmap shape or it was a flash photo
        // that failed and photoDef is an error shape.
        if (photoDef != null) {
            Instance instPhoto = inst;

            // Put photo in named MC if name specified.
            // ActionScript might be manipulating it by name, and only MCs can be manipulated by name.
            if (instanceName != null) {
                // New instance for photo
                instPhoto = new Instance();
                instPhoto.matrix = new AffineTransform();
                instPhoto.def = photoDef;

                Script script = inst.copyScript();
                script.newFrame().addInstance(instPhoto, 1);
                inst.setScript(script);
            }
            else {
                // Otherwise put photo directly in this instance (replace our def)
                inst.def = photoDef;
            }

            // Center photo instance
            Rectangle2D bounds = photoDef.getBounds();
            instPhoto.matrix.translate(-bounds.getWidth()/2, -bounds.getHeight()/2);
        }
    }

    private Shape createTransparentShape(Rectangle2D rect) {
        Shape shape = new Shape();
        shape.setFillStyle0(FillStyle.newSolid(new AlphaColor(0, 0, 0, 0)));
        shape.setFillStyle1(0);
        shape.drawRectangle(rect);
        shape.setBounds(rect);
        return shape;
    }

    private class TranscodeToneOp implements TranscodeOp {
        private boolean isSepia;
        private ToneOp op;

        public TranscodeToneOp(boolean isSepia) {
            this.isSepia = isSepia;
            op = new ToneOp(isSepia);
        }

        public BufferedImage filter(BufferedImage srcImage) {
            return op.filter(srcImage, null);
        }

        public String getOpName() {
            return getClass().getName() + (isSepia ? "sepia" : "gray");
        }
    }
}
