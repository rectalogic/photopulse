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
import com.iv.flash.api.Color;
import com.iv.flash.api.FlashDef;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.FlashObject;
import com.iv.flash.api.Frame;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.AlphaColor;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.api.shape.FillStyle;
import com.iv.flash.api.shape.LineStyle;
import com.iv.flash.api.shape.MorphShape;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.context.Context;
import com.iv.flash.context.StandardContext;
import com.iv.flash.util.GeomHelper;
import com.iv.flash.util.IVException;
import com.iv.flash.util.FlashBuffer;
import com.photica.photopulse.Util;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.output.LazyBitmap;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.TranscodeOp;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * Special shared Context for sub-commands.
 * Exposes Shape/Bitmap cache and tweening functionality.
 */
public class PhotoContext extends StandardContext {

    public static final String SHAPE_Circle = "Shapes/Circle";
    public static final String SHAPE_LawBadge = "Shapes/LawBadge";
    public static final String SHAPE_Star = "Shapes/Star";
    public static final String SHAPE_StarBurst = "Shapes/StarBurst";
    public static final String SHAPE_Square = "Shapes/Square";
    public static final String SHAPE_TriangleTopLeft = "Shapes/TriangleTopLeft";
    public static final String SHAPE_TriangleTopRight = "Shapes/TriangleTopRight";
    public static final String SHAPE_TriangleBottomLeft = "Shapes/TriangleBottomLeft";
    public static final String SHAPE_TriangleBottomRight = "Shapes/TriangleBottomRight";
    public static final String SHAPE_Heart = "Shapes/Heart";
    public static final String MORPH_SquarePlus = "Morphs/SquarePlus";
    public static final String MORPH_PlusSquare = "Morphs/PlusSquare";
    public static final String MORPH_MeltOff = "Morphs/MeltOff";
    public static final String MORPH_MeltOn = "Morphs/MeltOn";
    public static final String MORPH_VenetianOpen = "Morphs/VenetianOpen";
    public static final String MORPH_VenetianClose = "Morphs/VenetianClose";

    // Shapes define a standard 100x100 bounds region which can be used to sensibly scale them
    public static final int SHAPE_BOUNDS = 100 * ShowGenerator.TWIPS_PER_PIXEL;

    private static final int MAXMORPH = 65535;

    private FlashFile flashFile;

    // May be null if not lazy generation
    private LazyGenerator lazyGenerator;

    private ImageTranscoder transcoder;

    private Dimension stageSize;

    private boolean isHighQuality;

    // Map processed image URL to Bitmap Shape FlashDef or FlashFile
    private HashMap<String,Object> photoCache = new HashMap<String, Object>();

    // Stores last photo stored/retrieved from the cache
    private Bitmap lastBitmap;

    public PhotoContext(FlashFile flashFile, ImageTranscoder transcoder, boolean isHighQuality, LazyGenerator lazyGenerator) {
        this.flashFile = flashFile;
        this.transcoder = transcoder;
        this.isHighQuality = isHighQuality;
        this.lazyGenerator = lazyGenerator;
    }

    public static PhotoContext findContext(Context ctx) {
        // Search for parent PhotoContext
        while (ctx != null && !(ctx instanceof PhotoContext))
            ctx = ctx.getParent();
        return (PhotoContext)ctx;
    }

    /**
     * None of the AffineTransforms should contain a rotation/skew
     * factor - these do not interpolate linearly.
     *
     * @param atxP1 p1 in catmullrom, or null for linear interpolation. Only translation is used.
     * @param atxBegin p2 in catmullrom, or begin transform for linear interpolation. May be null if only doing color transform.
     * @param atxEnd p3 in catmullrom, or begin transform for linear interpolation. May be null if only doing color transform.
     * @param atxP4 p1 in catmullrom, or null for linear interpolation. Only translation is used.
     */
    public Instance tween(TweenBuilder tweenBuilder, int frameNum, int duration,
            AffineTransform atxP1,
            AffineTransform atxBegin /*P2*/, CXForm cxfBegin,
            double dblBeginRotate, double dblBeginSkewX, double dblBeginSkewY,
            AffineTransform atxEnd /*P3*/, CXForm cxfEnd,
            double dblEndRotate, double dblEndSkewX, double dblEndSkewY,
            AffineTransform atxP4,
            boolean isEasing) throws IVException {

        boolean isMorph = tweenBuilder.isMorphTween();

        AffineTransform atxRotateInit = null;
        CXForm cxfInit = null;

        // If duration is 0 or 1, then initialize with end transforms, otherwise initialize with begin transforms
        if (duration <= 1) {
            atxRotateInit = initialTransform(dblEndRotate, dblEndSkewX, dblEndSkewY, atxEnd);
            cxfInit = cxfEnd;
        }
        else {
            atxRotateInit = initialTransform(dblBeginRotate, dblBeginSkewX, dblBeginSkewY, atxBegin);
            cxfInit = cxfBegin;
        }

        // Create initial Instance - may be null
        Instance instFirst = tweenBuilder.createInitialInstance(frameNum, atxRotateInit, cxfInit);

        // If duration is 0 or 1, set morph to full and return the initial end transformed instance
        if (duration <= 1) {
            if (isMorph && instFirst != null)
                instFirst.ratio = MAXMORPH;
            return instFirst;
        }

        int endFrame = frameNum + duration - 1;

        for (int i = 1; i < duration - 1; i++) {
            double t = (double)i / (duration - 1);

            // Easing equation from:
            // http://groups.google.com/groups?selm=199512011750.MAA15775%40silky.cs.indiana.edu
            if (isEasing)
                t = 0.5 - 0.5 * Math.cos(Math.PI * t);

            // Transformation tween
            AffineTransform atxTween = null;
            if (atxBegin != null && atxEnd != null) {
                atxTween = interpolateTransforms(t, atxP1, atxBegin, atxEnd, atxP4);
                double t1 = 1.0 - t;
                composeTransforms(atxTween,
                    dblBeginRotate * t1 + dblEndRotate * t,
                    dblBeginSkewX * t1 + dblEndSkewX * t,
                    dblBeginSkewY * t1 + dblEndSkewY * t);
            }

            // Color tween
            CXForm cxfTween = null;
            if (cxfBegin != null && cxfEnd != null)
                cxfTween = CXForm.interpolate(t, cxfBegin, cxfEnd);

            Instance inst = tweenBuilder.createInstance(frameNum + i, atxTween, cxfTween);

            // Morph tween
            if (isMorph && inst != null)
                inst.ratio = (i * MAXMORPH) / (duration - 1);
        }

        if (frameNum != endFrame) {
            AffineTransform atxEndRotate = initialTransform(dblEndRotate, dblEndSkewX, dblEndSkewY, atxEnd);
            Instance inst = tweenBuilder.createInstance(endFrame, atxEndRotate, cxfEnd);
            if (isMorph && inst != null)
                inst.ratio = MAXMORPH;
        }

        return instFirst;
    }

    public Instance tween(Script script, FlashDef def, int depth, int frameNum, int duration,
            AffineTransform atxBegin, CXForm cxfBegin,
            double dblBeginRotate, double dblBeginSkewX, double dblBeginSkewY,
            AffineTransform atxEnd, CXForm cxfEnd,
            double dblEndRotate, double dblEndSkewX, double dblEndSkewY,
            int tweenInst) throws IVException {
        return tween(new DefaultTweenBuilder(script, def, depth, tweenInst), frameNum, duration,
            null,
            atxBegin, cxfBegin, dblBeginRotate, dblBeginSkewX, dblBeginSkewY,
            atxEnd, cxfEnd, dblEndRotate, dblEndSkewX, dblEndSkewY,
            null,
            false);
    }

    private AffineTransform initialTransform(double dblRotate, double dblSkewX, double dblSkewY, AffineTransform atx) {
        AffineTransform atxRotate = atx;
        if (dblRotate != 0 || dblSkewX != 0 || dblSkewY != 0) {
            atxRotate = new AffineTransform(atx);
            composeTransforms(atxRotate, dblRotate, dblSkewX, dblSkewY);
        }
        return atxRotate;
    }

    // Interpolate translation/scale - do not use for rotation
    private AffineTransform interpolateTransforms(double t, AffineTransform atxP1, AffineTransform atxBegin, AffineTransform atxEnd, AffineTransform atxP4) {
        double t1 = 1.0 - t;

        double m00 = atxBegin.getScaleX()*t1 + atxEnd.getScaleX()*t;
        double m11 = atxBegin.getScaleY()*t1 + atxEnd.getScaleY()*t;
        double m01 = atxBegin.getShearX()*t1 + atxEnd.getShearX()*t;
        double m10 = atxBegin.getShearY()*t1 + atxEnd.getShearY()*t;
        double m02 = 0;
        double m12 = 0;

        // Catmull-Rom spline translation interpolation
        if (atxP1 != null && atxP4 != null) {
            m02 = catmullrom(t, atxP1.getTranslateX(), atxBegin.getTranslateX(), atxEnd.getTranslateX(), atxP4.getTranslateX());
            m12 = catmullrom(t, atxP1.getTranslateY(), atxBegin.getTranslateY(), atxEnd.getTranslateY(), atxP4.getTranslateY());
        }
        // Linear translation interpolation
        else {
            m02 = atxBegin.getTranslateX()*t1 + atxEnd.getTranslateX()*t;
            m12 = atxBegin.getTranslateY()*t1 + atxEnd.getTranslateY()*t;
        }
        return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }

    private void composeTransforms(AffineTransform atxTween,
            double dblRotate, double dblSkewX, double dblSkewY) {
        if (dblSkewX != 0 || dblSkewY != 0) {
            // XXX shear/scale isn't the same
            //atx.shear(Math.sin(dblSkewY), -Math.sin(dblSkewX));
            //atx.scale(Math.cos(dblSkewY), Math.cos(dblSkewX));
            AffineTransform atxShear = new AffineTransform(Math.cos(dblSkewY), Math.sin(dblSkewY),
                    -Math.sin(dblSkewX), Math.cos(dblSkewX), 0, 0);
            atxTween.concatenate(atxShear);
        }
        if (dblRotate != 0)
            atxTween.rotate(dblRotate);
    }

    // Catmull-Rom spline interpolation
    // http://research.microsoft.com/~hollasch/cgindex/curves/catmull-rom.html
    private double catmullrom(double t, double p1, double p2, double p3, double p4) {
        return 0.5 * ((-p1 + 3*p2 -3*p3 + p4)*t*t*t
            + (2*p1 -5*p2 + 4*p3 - p4)*t*t
            + (-p1+p3)*t
            + 2*p2);
    }

    public void scaleSourceToDest(Rectangle2D sourceRect, Rectangle2D destRect,
            AffineTransform atx, boolean maintainAspect, boolean expandRadius) {
        scaleSourceToDest(sourceRect.getWidth(), sourceRect.getHeight(),
            destRect.getWidth(), destRect.getHeight(),
            atx, maintainAspect, expandRadius);
    }

    /**
     * Scale source rect to fit dest rect.
     * @param maintainAspect Maintain source aspect ratio
     * @param expandRadius Expand source circles radius to enclose dest.
     */
    public void scaleSourceToDest(double dblSourceWidth, double dblSourceHeight,
            double dblDestWidth, double dblDestHeight,
            AffineTransform atx, boolean maintainAspect, boolean expandRadius) {

        if (expandRadius) {
            // Expand rect so the circles radius will fully enclose the dest.
            // c^2 = a^2 + b^2
            // newradius^2 = halfwidth^2 + halfheight^2
            double dblDestWidth2 = dblDestWidth / 2;
            double dblDestHeight2 = dblDestHeight / 2;
            dblDestWidth = dblDestHeight =
                2 * Math.sqrt(dblDestWidth2*dblDestWidth2
                    + dblDestHeight2*dblDestHeight2);
        }

        double dblScaleX, dblScaleY;
        if (maintainAspect)
            dblScaleX = dblScaleY = Math.max(dblDestWidth, dblDestHeight)
                / Math.max(dblSourceWidth, dblSourceHeight);
        else {
            dblScaleX = dblDestWidth / dblSourceWidth;
            dblScaleY = dblDestHeight / dblSourceHeight;
        }

        atx.scale(dblScaleX, dblScaleY);
    }

    public MorphShape loadMorphShape(String name) {
        return (MorphShape)loadShapeClass(name, MorphShape.class);
    }

    public Shape loadShape(String name) {
        return (Shape)loadShapeClass(name, Shape.class);
    }

    private FlashDef loadShapeClass(String name, Class shapeClass) {
        // Shapes SWT must be parsed with full parsing to get the shapes
        // (otherwise they will be LazyShapes)

        Script script = flashFile.getScript(name);
        if (script == null)
            return null;
        Frame frame = script.getFrameAt(0);
        for (int i = 0; i < frame.size(); i++) {
            FlashObject fo = frame.getFlashObjectAt(i);
            if ((fo instanceof Instance) && shapeClass.isInstance(((Instance)fo).def))
                return ((Instance)fo).def;
        }
        return null;
    }

    public Bitmap getLastPhoto() {
        return lastBitmap;
    }
    public void resetLastPhoto() {
        lastBitmap = null;
    }

    public ImageTranscoder getTranscoder() {
        return transcoder;
    }

    public FlashFile loadFlashPhoto(File imageFile) {
        lastBitmap = null;

        // Check in cache first
        FlashFile flashFile = (FlashFile)photoCache.get(imageFile.toString());
        if (flashFile != null)
            return flashFile;

        // Load FlashFile from the file
        try {
            byte[] buffer = new byte[(int)imageFile.length()];
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(imageFile, "r");
                raf.readFully(buffer);
            } finally {
                if (raf != null)
                    raf.close();
            }
            FlashBuffer fb = new FlashBuffer(buffer);
            flashFile = FlashFile.parse(imageFile.getAbsolutePath(), fb);
        } catch (IOException e) {
            return null;
        } catch (IVException e) {
            return null;
        }

        // Reset script and tweak if template
        Script photoScript = flashFile.getMainScript();
        photoScript.resetMain();
        if (flashFile.isTemplate())
            photoScript.removeFileDepGlobalCommands();

        // Put in cache
        photoCache.put(imageFile.toString(), flashFile);

        return flashFile;
    }

    public Shape loadPhoto(File imageFile, String cropSpec, String scaleSpec, TranscodeOp op) {
        try {
            // Track last photo loaded from cache
            lastBitmap = null;
            Shape shape = loadPhotoInternal(imageFile, cropSpec, scaleSpec, op);
            if (shape != null) {
                // This is ugly, but Shape.getFillStyle1 will throw
                // ArrayIndexOutOfBoundsException if no style was set.
                if (shape.getShapeStyles().fillStyles.size() > 0) {
                    FillStyle fs = shape.getFillStyle1();
                    if (fs != null)
                        lastBitmap = fs.getBitmap();
                }
            }
            return shape;
        } catch (OutOfMemoryError e) {
            // Handle user attempting to load huge photos, rethrow with informative message.
            throw (OutOfMemoryError)new OutOfMemoryError(SystemMessages.getMessage(SystemMessages.ERR_OOM_PHOTO, imageFile.getName())).initCause(e);
        }
    }

    private Shape loadPhotoInternal(File imageFile, String cropSpec, String scaleSpec, TranscodeOp op) {
        if (cropSpec != null && cropSpec.length() == 0)
            cropSpec = null;
        if (scaleSpec != null && scaleSpec.length() == 0)
            scaleSpec = null;

        Rectangle cropRect = Util.parseRect(cropSpec);
        if (cropRect == null)
            cropSpec = null;
        double dblScale = Util.parseScale(scaleSpec);
        if (dblScale == 1.0)
            scaleSpec = null;

        // Check cache first
        String key = imageFile.toString()
            + (cropSpec != null ? "+" + cropSpec : "")
            + (scaleSpec != null ? "+" + scaleSpec : "")
            + (op != null ? "+" + op.getOpName() : "");
        Shape shape = (Shape)photoCache.get(key);
        if (shape != null)
            return shape;

        try {
            Bitmap bitmap = LazyBitmap.newBitmap(lazyGenerator, imageFile, transcoder, cropRect, dblScale, op);
            shape = Util.bitmapFill(bitmap);
        } catch (IVException e) {
            shape = null;
        } catch (IOException e) {
            shape = null;
        }

        if (shape == null)
            shape = createImageErrorShape();

        // Cache shape with bitmap
        photoCache.put(key, shape);
        return shape;
    }

    public Shape createImageErrorShape() {
        // XXX return shape with error message instead
        Shape shape = Shape.newShape1();
        Rectangle2D rect = GeomHelper.newRectangle(0, 0,
                stageSize.getWidth() * ShowGenerator.TWIPS_PER_PIXEL,
                stageSize.getHeight() * ShowGenerator.TWIPS_PER_PIXEL);
        shape.setFillStyle0(FillStyle.newSolid(new AlphaColor(0, 0, 255)));
        shape.drawRectangle(rect);
        shape.setBounds(rect);
        return shape;
    }

    // Convert duration to frames.
    // If <0, it specifies frames, otherwise it specifies seconds.
    public int convertDurationToFrames(double duration) {
        int frames = 0;
        if (duration < 0)
            frames = -(int)duration;
        else {
            // Seconds to frames
            frames = (int)(duration * Util.convertFrameRate(flashFile.getFrameRate()));
        }
        return frames;
    }

    public Dimension getStageSize() {
        return stageSize;
    }

    public void setStageSize(Dimension stageSize) {
        this.stageSize = stageSize;
    }

    public boolean isHighQuality() {
        return isHighQuality;
    }

    public LazyGenerator getLazyGenerator() {
        return lazyGenerator;
    }
}