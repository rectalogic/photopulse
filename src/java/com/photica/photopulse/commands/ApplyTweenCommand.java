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

import com.iv.flash.api.CXForm;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Frame;
import com.iv.flash.api.FreeCharacter;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.commands.GenericXMLCommand;
import com.iv.flash.context.Context;
import com.iv.flash.context.GraphContext;
import com.iv.flash.util.IVException;
import com.iv.flash.util.Util;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.progress.ProgressReporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.PhotoContext;
import com.photica.photopulse.flash.context.TweenBuilder;
import com.photica.photopulse.flash.output.LazyBitmap;
import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * Apply spline based tween to an instance
 */
 /*XXX
 keyframe duration is a percentage of overall effectDuration (which includes begin/end transitions)

 keyframe attrs must not match photo attrs - because parent photo context is searched
 for missing keyframe attrs

 <photo ...>
    <keyframe duration="(0.0-1.0)" linear="true/false" easing="true/false" translateX="" translateY="" scale="(0.0-1.0)" rotate="(degrees)"/>
    <keyframe duration="" translateX="" translateY="" scale="" rotate=""/>
    <keyframe (no duration on last keyframe) translateX="" translateY="" scale="" rotate=""/>
 </photo>

 */
public class ApplyTweenCommand extends GenericXMLCommand {

    private static class Keyframe {
        public int duration;
        private double translateX;
        private double translateY;
        public double rotation;
        public double scale;
        public boolean hasEasing;
        public boolean isLinear;
        public AffineTransform atx;
    }

    public void doCommand(FlashFile file, Context ctxCommand, Script scrParent, int frameNum) throws IVException {
        // Common XML params
        super.initParms(ctxCommand);

        // Params for this command
        String strPathDuration = getParameter(ctxCommand, "duration", "@duration");
        String strPathEasing = getParameter(ctxCommand, "easing", "@easing");
        String strPathLinear = getParameter(ctxCommand, "linear", "@linear");
        String strPathTranslateX = getParameter(ctxCommand, "translateX", "@translateX");
        String strPathTranslateY = getParameter(ctxCommand, "translateY", "@translateY");
        String strPathScale = getParameter(ctxCommand, "scale", "@scale");
        String strPathRotate = getParameter(ctxCommand, "rotate", "@rotate");
        String strImagePath = getParameter(ctxCommand, "imagepath", null);
        String strImageCrop = getParameter(ctxCommand, "imagecrop", null);
        String strImageScale = getParameter(ctxCommand, "imagescale", null);
        boolean isFlashPhoto = getBoolParameter(ctxCommand, "isFlash", false);

        PhotoContext ctxPhoto = PhotoContext.findContext(ctxCommand);
        if (ctxPhoto == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_PHOTOCONTEXT);

        GraphContext ctxGraph = retrieveGraphContext(ctxPhoto);
        if (ctxGraph == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_GRAPHCONTEXT);

        Instance inst = getCommandInstance(file, ctxCommand, scrParent, frameNum);

        List<GraphContext> contextList = (List<GraphContext>)ctxGraph.getValueList(select);
        if (contextList == null || contextList.isEmpty()) {
            processFlashDef(inst, file, ctxCommand);
            return;
        }

        // Total duration seconds including begintrans, effect and endtrans
        double effectDurationSeconds = Util.toDouble(ctxCommand.getValue("effectDuration"), 5);

        int contextSize = contextList.size();
        ArrayList<Keyframe> keyframeList = new ArrayList<Keyframe>(contextSize);

        // Collect scales and rotations.
        // If they are constant, then do not use high quality - only need it when scaling or rotating.
        int nTotalDuration = 0;
        boolean needsHighQuality = false;
        for (int i = 0; i < contextSize; i++) {
            GraphContext ctxKeyframe = contextList.get(i);

            int duration = ctxPhoto.convertDurationToFrames(effectDurationSeconds * evalDoubleParameter(ctxKeyframe, strPathDuration, 0.1));
            // Skip if zero duration (unless last frame) - use next overlapping keyframe
            if (duration == 0 && i < contextSize - 1)
                continue;
            nTotalDuration += duration;

            Keyframe keyframe = new Keyframe();
            keyframe.duration = duration;
            keyframe.hasEasing = evalBoolParameter(ctxKeyframe, strPathEasing, false);
            // Linear interpolation instead of spline based
            keyframe.isLinear = evalBoolParameter(ctxKeyframe, strPathLinear, false);
            keyframe.scale = evalDoubleParameter(ctxKeyframe, strPathScale, 1.0);
            keyframe.rotation = Math.toRadians(evalDoubleParameter(ctxKeyframe, strPathRotate, 0));
            keyframe.translateX = evalDoubleParameter(ctxKeyframe, strPathTranslateX, 0);
            keyframe.translateY = evalDoubleParameter(ctxKeyframe, strPathTranslateY, 0);

            keyframeList.add(keyframe);

            Keyframe firstKeyframe = keyframeList.get(0);
            if (i > 0 && (keyframe.scale != firstKeyframe.scale || keyframe.rotation != firstKeyframe.rotation))
                needsHighQuality = true;
        }

        Keyframe[] keyframes = new Keyframe[keyframeList.size()];
        keyframeList.toArray(keyframes);

        boolean isHighQuality = !isFlashPhoto && ctxPhoto.isHighQuality() && ctxPhoto.getLazyGenerator() != null && needsHighQuality;

        AffineTransform atxBase;
        if (isHighQuality)
            atxBase = new AffineTransform();
        else
            atxBase = new AffineTransform(inst.matrix);

        // Create scale/translate matrices for each keyframe
        for (int i = 0; i < keyframes.length; i++) {
            // First translate, then scale
            AffineTransform atx = new AffineTransform(atxBase);
            // Use pixels if hiquality, otherwise twips
            atx.translate((isHighQuality ? 1 : ShowGenerator.TWIPS_PER_PIXEL) * keyframes[i].translateX,
                (isHighQuality ? 1 : ShowGenerator.TWIPS_PER_PIXEL) * keyframes[i].translateY);
            atx.scale(keyframes[i].scale, keyframes[i].scale);
            keyframes[i].atx = atx;
        }

        TweenBuilder tweenBuilder = null;
        if (isHighQuality) {
            try {
                // Load base image to be tweened
                DecodedImage image = ImageCoder.getInstance().decodeImage(new File(strImagePath),
                        com.photica.photopulse.Util.parseRect(strImageCrop),
                        com.photica.photopulse.Util.parseScale(strImageScale), 0, 0,
                        ImageTranscoder.SCALE_BILINEAR, null);
                if (image != null)
                    tweenBuilder = new HighQualityTweenBuilder(ctxPhoto, scrParent, inst, image, frameNum, nTotalDuration);
            } catch (IOException e) {
                // Fall through and do low-quality
            }
        }

        if (tweenBuilder == null)
            tweenBuilder = new NormalQualityTweenBuilder(scrParent, inst);

        // Add 2 for doubled first/last points
        int nSegments = (keyframes.length + 2) - 4 + 1;

        try {
            for (int i = 0; i < nSegments; i++) {
                // Get 4 catmull points, p2 and p3 will be interpolated.
                // Double up first and last keyframe.
                AffineTransform atxP1 = i-1<0 ? null : keyframes[i-1].atx;
                // p2 is the current keyframe
                AffineTransform atxP2 = keyframes[i].atx;
                AffineTransform atxP3 = keyframes[i+1].atx;
                AffineTransform atxP4 = i+2>=keyframes.length ? null : keyframes[i+2].atx;

                // Pad duration so this keyframe overlaps the next by one frame.
                // (since those keyframes will be the same transform and we ignore the first instance in each tween)
                ctxPhoto.tween(tweenBuilder,
                    frameNum, keyframes[i].duration + 1,
                    keyframes[i].isLinear ? null : atxP1,
                    atxP2, null,
                    keyframes[i].rotation, 0, 0,
                    atxP3, null,
                    keyframes[i+1].rotation, 0, 0,
                    keyframes[i].isLinear ? null : atxP4,
                    keyframes[i].hasEasing);

                frameNum += keyframes[i].duration;
            }
        } catch (CancelException e) {
            // Return immediately if user cancelled - InsertPhotosCommand will see user canceled and bail
            return;
        }

        // Stop on the last frame
        scrParent.getFrameAt(frameNum).addStopAction();

        processFlashDef(inst, file, ctxCommand);
    }
}

class NormalQualityTweenBuilder implements TweenBuilder {
    private Script script;
    private int depth;
    private Instance initialInst;

    public NormalQualityTweenBuilder(Script script, Instance initialInst) {
        this.script = script;
        this.depth = initialInst.depth;
        this.initialInst = initialInst;
    }

    public boolean isMorphTween() {
        return false;
    }

    public Instance createInitialInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        // Reset the transform of the initial existing command instance on the first frame of the first tween segment
        // Subsequent segment first frames are ignored since the last frame of the previous segment overlaps.
        if (initialInst != null) {
            initialInst.matrix = atxTween;
            initialInst.cxform = cxfTween;
            initialInst = null;
        }
        return null;
    }

    public Instance createInstance(int frame, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        return script.getFrameAt(frame).addInstance(depth, atxTween, cxfTween);
    }
}

class HighQualityTweenBuilder implements TweenBuilder {
    private Instance initialInst;
    private final PhotoContext context;
    private final Script script;
    private final int depth;
    private final DecodedImage baseImage;
    private final AffineTransform atxStage;

    private int startFrame;
    private int frameCount;

    private final int stageWidth;
    private final int stageHeight;
    private final int imageWidth;
    private final int imageHeight;
    private final float cropX;
    private final float cropY;

    // Set as we build each frames instance
    private Bitmap bitmap;

    public HighQualityTweenBuilder(PhotoContext context, Script script, Instance initialInst,
            DecodedImage baseImage, int startFrame, int frameCount) {
        this.context = context;
        this.script = script;
        this.depth = initialInst.depth;
        this.initialInst = initialInst;

        this.startFrame = startFrame;
        this.frameCount = frameCount;

        Dimension stage = context.getStageSize();
        this.stageWidth = (int)stage.getWidth();
        this.stageHeight = (int)stage.getHeight();

        this.baseImage = baseImage;

        this.imageWidth = baseImage.getImage().getWidth();
        this.imageHeight = baseImage.getImage().getHeight();

        // Crop out a stage sized region around the base image center point.
        // If the image is smaller than the stage, these will be negative,
        // in which case we must also translate the image.
        this.cropX = (imageWidth - stageWidth)/2.0f;
        this.cropY = (imageHeight - stageHeight)/2.0f;

        // Translate stage so center is at upper left - because origin of image is upper left,
        // and we want it in upper left of stage.
        this.atxStage = AffineTransform.getTranslateInstance(
                -(stageWidth / 2.0) * ShowGenerator.TWIPS_PER_PIXEL,
                -(stageHeight / 2.0) * ShowGenerator.TWIPS_PER_PIXEL);
    }

    public boolean isMorphTween() {
        return false;
    }

    public Instance createInitialInstance(int frameNum, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        // Handle the first frame of the first tween segment - use the existing command instance.
        // Subsequent segment first frames are ignored since the last frame of the previous segment overlaps.
        if (initialInst != null) {
            ProgressReporter.updateProgress((float)(frameNum - startFrame) / frameCount);

            Shape shape = createTweenShape(atxTween);
            initialInst.def = shape == null ? Shape.newEmptyShape1() : shape;
            initialInst.matrix = atxStage;
            initialInst.cxform = cxfTween;
            initialInst = null;
        }
        // Ignore first instance in each tween
        return null;
    }

    public Instance createInstance(int frameNum, AffineTransform atxTween, CXForm cxfTween) throws IVException {
        ProgressReporter.updateProgress((float)(frameNum - startFrame) / frameCount);

        Frame frame = script.getFrameAt(frameNum);

        // Remove previous bitmap and instance if there was one
        if (bitmap != null) {
            frame.addFlashObject(new FreeCharacter(bitmap));
            frame.removeInstance(depth);
        }

        Shape shape = createTweenShape(atxTween);
        if (shape == null)
            return null;
        return frame.addInstance(shape, depth, atxStage, cxfTween);
    }

    public Shape createTweenShape(AffineTransform atxTween) throws IVException {
        // High quality takes a long time, so check for cancellation on each frame
        if (ProgressReporter.isCanceled())
            throw new CancelException();

        bitmap = null;

        // If image is smaller than stage (negative crop),
        // translate to prevent transform from losing negative image bits.
        AffineTransform atxImage = AffineTransform.getTranslateInstance(cropX < 0 ? -cropX : 0, cropY < 0 ? -cropY : 0);

        // Transform around center of base image
        atxImage.translate(imageWidth/2.0, imageHeight/2.0);
        atxImage.concatenate(atxTween);
        atxImage.translate(-imageWidth/2.0, -imageHeight/2.0);

        ImageCoder coder = ImageCoder.getInstance();
        AffineTransformOp op = null;
        try {
            op = new AffineTransformOp(atxImage, AffineTransformOp.TYPE_BILINEAR);
        } catch (ImagingOpException e) {
            return null;
        }

        // Return nothing if stage does not intersect transformed image.
        // Treat negative crop as positive since we translated by crop above.
        Rectangle2D transformedBounds = op.getBounds2D(baseImage.getImage());
        if (!transformedBounds.intersects(Math.abs(cropX), Math.abs(cropY), stageWidth, stageHeight))
            return null;

        // Apply the transform to the image
        BufferedImage transformedImage = coder.transformImage(op, baseImage.getImage());
        if (transformedImage == null)
            return null;

        int x = (int)Math.max(0, cropX);
        int y = (int)Math.max(0, cropY);
        BufferedImage croppedImage = transformedImage.getSubimage(x, y,
                (int)Math.min(stageWidth, transformedImage.getWidth() - x),
                (int)Math.min(stageHeight, transformedImage.getHeight() - y));

        try {
            Bitmap bm = new LazyBitmap(context.getLazyGenerator(),
                    new DecodedImage(baseImage.getSourceFile(), baseImage.getSourceFormat(), croppedImage),
                    true);
            Shape shape = com.photica.photopulse.Util.bitmapFill(bm);
            bitmap = bm;
            return shape;
        } catch (IOException e) {
            return context.createImageErrorShape();
        }
    }
}

/**
 * Thrown by {@link HighQualityTweenBuilder} if it detects user canceled.
 * This will then be thrown up through {@link PhotoContext#tween} and handled in {@link ApplyTweenCommand#doCommand}
 */
class CancelException extends IVException {
}
