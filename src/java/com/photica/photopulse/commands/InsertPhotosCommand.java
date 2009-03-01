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
import com.iv.flash.api.FlashDef;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Frame;
import com.iv.flash.api.FreeCharacter;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.action.DoAction;
import com.iv.flash.api.action.Program;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.api.shape.MorphShape;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.cache.MediaCache;
import com.iv.flash.commands.GenericXMLCommand;
import com.iv.flash.context.Context;
import com.iv.flash.url.IVUrl;
import com.iv.flash.util.GeomHelper;
import com.iv.flash.util.IVException;
import com.iv.flash.util.Resource;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.Util;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.DefaultTweenBuilder;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.context.PhotoContext;
import com.photica.photopulse.flash.context.PhotoPulseContext;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.progress.ProgressReporter;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class InsertPhotosCommand extends GenericXMLCommand {

    // This is handled by GenericXMLCommand superclass
    public static final String PARAM_SELECT = "select";

    public static final String PARAM_LIBRARY = "library";


    // Flash makes commands 102.4 pixels width/height
    public static final int COMMAND_WIDTH_TWIPS = 2048;
    public static final int COMMAND_HEIGHT_TWIPS = 2048;

    private float frameRate;

    // Current depth and min/max reserved depths.
    // Each photo gets 2 layers - one for photo and one for optional transition mask.
    // Mask must go on layer below photo, so photos are always on the upper layer.
    private int currentDepth;
    private int maxDepth;
    private int minDepth;

    private FlashFile flashFile;

    private PhotoContext photoContext;

    // Parent script
    private Script parentScript;

    // Stage size
    private Rectangle2D stageRect;

    // Initial transform of command
    private AffineTransform atxPlaceholder;

    // XPath expressions
    private static final String XPATH_BeginTransClip = "@beginTransClip";
    private static final String XPATH_EndTransClip = "@endTransClip";
    private static final String XPATH_EndTransLayer = "@endTransLayer";
    private static final String XPATH_EffectClip = "@effectClip";
    private static final String XPATH_ImageFile = "@imageFile";
    private static final String XPATH_FlashPhoto = "@flashPhoto";
    private static final String XPATH_ImageCrop = "@imageCrop";
    private static final String XPATH_ImageScale = "@imageScale";
    private static final String XPATH_EffectDuration = "number(@effectDuration)";
    private static final String XPATH_BeginTransDuration = "number(@beginTransDuration)";
    private static final String XPATH_EndTransDuration = "number(@endTransDuration)";
    private static final String XPATH_EventArg = "eventArg";

    private static final String LOGO_CLIP = "Branding/Logo";
    private static final String SPLASH_CLIP = "Branding/Splash";
    private static final String CONTROLS_CLIP = "Controls/Control";
    private static final String PRELOADER_CLIP = "Preloader/Preloader";
    private static final String EFFECT_CLIP_PREFIX = "Effects/";

    private static final int BEGINSPLASH_SECONDS = 5;
    private static final int ENDSPLASH_SECONDS = 5;

    public void doCommand(FlashFile file, Context ctxCommand, Script scrParent, int nFrame) throws IVException {
        // Common XML params
        super.initParms(ctxCommand);

        flashFile = file;
        parentScript = scrParent;

        // Get info from parent PhotoPulseContext if one exists
        PhotoPulseContext ctxPhotoPulse = PhotoPulseContext.findContext(ctxCommand);
        MP3Data mp3Data = ctxPhotoPulse.getMP3Data();

        List listPhotos = ctxPhotoPulse.getValueList(select);
        if (listPhotos == null)
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_NO_PHOTOS);

        // Load libraries
        String strLibraryPath = getParameter(ctxCommand, "library", "");
        loadLibrary(strLibraryPath + "photopulse.swt", false);
        if (ctxPhotoPulse.isBranded())
            loadLibrary(strLibraryPath + "branding.swt", false);
        // Load shapes with full parsing, so Shapes instead of LazyShapes are loaded
        loadLibrary(strLibraryPath + "shapes.swt", true);

        photoContext = new PhotoContext(flashFile, ctxPhotoPulse.getTranscoder(), ctxPhotoPulse.isHighQuality(), ctxPhotoPulse.getLazyGenerator());

        Instance inst = getInstance();

        // Initialize stage size params
        stageRect = GeomHelper.getTransformedSize(inst.matrix,
            GeomHelper.newRectangle(-COMMAND_WIDTH_TWIPS/2, -COMMAND_HEIGHT_TWIPS/2,
                COMMAND_WIDTH_TWIPS, COMMAND_HEIGHT_TWIPS));
        int nStageWidth = (int)stageRect.getWidth()/ShowGenerator.TWIPS_PER_PIXEL;
        int nStageHeight = (int)stageRect.getHeight()/ShowGenerator.TWIPS_PER_PIXEL;
        photoContext.setValue("stagewidth", String.valueOf(nStageWidth));
        photoContext.setValue("stageheight", String.valueOf(nStageHeight));
        // Some commands want to scale things as a percentage of the min stage dimension
        photoContext.setValue("stagemin", String.valueOf(Math.min(nStageWidth, nStageHeight)));
        photoContext.setStageSize(new Dimension(nStageWidth, nStageHeight));

        // Save placeholder matrix for photos
        GeomHelper.deScaleMatrix(inst.matrix);
        atxPlaceholder = inst.matrix;

        // Initially reserve 200 layers (even number) and start in the middle
        final int RESERVE_LAYERS = 200;
        minDepth = inst.depth;
        // Add one because 2 layers per photo
        parentScript.reserveLayers(minDepth, RESERVE_LAYERS + 1);
        maxDepth = minDepth + RESERVE_LAYERS;
        currentDepth = minDepth + RESERVE_LAYERS/2;

        // Remove ourself
        NoopInstance noop = new NoopInstance();
        noop.replaceInstance(parentScript, nFrame, inst);

        // Initialize framerate param. Convert from 8:8 fixed point to float
        frameRate = Util.convertFrameRate(flashFile.getFrameRate());
        photoContext.setValue("framerate", String.valueOf(frameRate));

        // Skip first frame, we will add the preloader there
        nFrame++;
        int nStartFrame = nFrame;

        // First photo has no previous endtrans
        int nEndTransDurationFrames = 0;

        // Maps photo bitmap to last frame it was used on
        HashMap<FlashDef,Frame> hmLastPhoto = new HashMap<FlashDef, Frame>();

        String eventHandler = ctxPhotoPulse.getEventHandler();

        int totalPhotos = listPhotos.size();

        // Invoke event handler for show begin
        addInvokeEventHandler(nStartFrame, eventHandler, "showBegin", Integer.toString(totalPhotos));

        int nNameID = 0;
        int photoCount = 0;
        final float progressPortion = 1.0f / totalPhotos;
        Iterator iter = listPhotos.iterator();

        while (iter.hasNext()) {
            // Report progress and bail if canceled
            if (ProgressReporter.isCanceled())
                return;

            // Push progress portion, nested commands can report sub-progress relative to this
            ProgressReporter.pushProgressPortion(progressPortion);

            photoContext.setParent((Context)iter.next());

            // Get begintrans duration.
            int nBeginTransDurationFrames = getDurationFrames(evalDoubleParameter(photoContext, XPATH_BeginTransDuration, 0), 0);
            // Start begintrans duration later if it is smaller than previous endtrans duration
            if (nBeginTransDurationFrames < nEndTransDurationFrames)
                nFrame += nEndTransDurationFrames - nBeginTransDurationFrames;

            // Get effect duration.
            int nEffectDurationFrames = getDurationFrames(evalDoubleParameter(photoContext, XPATH_EffectDuration, 0), 0);

            // Get endtrans duration.
            // This is used to pad the next photos begintrans duration.
            nEndTransDurationFrames = getDurationFrames(evalDoubleParameter(photoContext, XPATH_EndTransDuration, 0), 0);

            // Don't do anything if the total duration is 0
            int nTotalDurationFrames = nBeginTransDurationFrames + nEffectDurationFrames + nEndTransDurationFrames;
            if (nTotalDurationFrames == 0)
                continue;

            // Effect duration includes transition times. Store in seconds.
            photoContext.setValue("effectDuration",
                String.valueOf((double)(nTotalDurationFrames) / frameRate));

            photoContext.resetLastPhoto();

            // Store image path in context
            String imagePath = evalStringParameter(photoContext, XPATH_ImageFile, "");
            photoContext.setValue("imagepath", imagePath);

            // Store whether this is a flash photo in context
            String isFlashPhoto = evalStringParameter(photoContext, XPATH_FlashPhoto, "false");
            photoContext.setValue("flashPhoto", isFlashPhoto);

            String strCrop = evalStringParameter(photoContext, XPATH_ImageCrop, null);
            String strScale = evalStringParameter(photoContext, XPATH_ImageScale, null);
            photoContext.setValue("crop", strCrop != null ? strCrop : "");
            photoContext.setValue("scale", strScale != null ? strScale : "");

            // Process effect now so it contains bitmap
            Script scrEffect = processClip(photoContext,
                evalStringParameter(photoContext, XPATH_EffectClip, EffectRegistry.EFFECT_None.getTag()));

            // Begin transition
            Instance instEffect = applyBeginTransition(scrEffect, nFrame,
                evalStringParameter(photoContext, XPATH_BeginTransClip, EffectRegistry.BEGINTRANS_None.getTag()),
                nBeginTransDurationFrames);

            // Invoke event handler for photo
            String eventArg = evalStringParameter(photoContext, XPATH_EventArg, null);
            addInvokeEventHandler(nFrame, eventHandler, "photo" + photoCount, eventArg);

            // Jump to end of begintrans + effect
            nFrame += nBeginTransDurationFrames + nEffectDurationFrames;

            // End transition
            applyEndTransition(scrEffect, nFrame,
                evalStringParameter(photoContext, XPATH_EndTransClip, EffectRegistry.ENDTRANS_None.getTag()),
                nEndTransDurationFrames);

            reserveLayers(evalStringParameter(photoContext, XPATH_EndTransLayer, "bottom"));

            // Store last frame photo bitmap was used on
            Bitmap bmLastPhoto = photoContext.getLastPhoto();
            if (bmLastPhoto != null)
                hmLastPhoto.put(bmLastPhoto, parentScript.getFrameAt(nFrame + nEndTransDurationFrames));

            // Name the effects "effect0", "effect1" and "effect2"
            // This is so the play/pause controls can programmatically access all running effects.
            // We need 3 names so names are unique since each effect overlaps 2 other
            // effects (at beginning and end trans)
            nNameID = (nNameID + 1) % 3;
            instEffect.name = "effect" + nNameID;

            // Popping updates progress to 100% for that portion
            ProgressReporter.popProgressPortion();

            photoCount++;
        }
        nFrame += nEndTransDurationFrames;

        // Free each photo on last frame it was used on
        Iterator<Map.Entry<FlashDef,Frame>> iterPhotoFrames = hmLastPhoto.entrySet().iterator();
        while (iterPhotoFrames.hasNext()) {
            Map.Entry<FlashDef,Frame> entry = iterPhotoFrames.next();
            Frame framePhoto = entry.getValue();
            framePhoto.addFlashObject(new FreeCharacter(entry.getKey()));
        }


        String strLoadMovieMP3 = null;
        int nEndMP3Frame = nStartFrame;
        // Add MP3 data to main timeline if not external
        if (mp3Data != null)
            nEndMP3Frame = mp3Data.addMP3Frames(parentScript, nStartFrame);
        // Get external swf filename for loadMovie, if any
        else {
            strLoadMovieMP3 = ctxPhotoPulse.getMP3FileName();
            // End frame is one after the end of the mp3 (counting from the start frame)
            nEndMP3Frame += ctxPhotoPulse.getMP3FrameCount() + 1;
            // Add frames to pad the show to the length of the external MP3
            parentScript.getFrameAt(nEndMP3Frame);
        }

        int nEndFrame = Math.max(nEndMP3Frame, nFrame);

        // Invoke event handler for photo
        addInvokeEventHandler(nEndFrame, eventHandler, "showEnd", null);

        // Add branding logos
        if (ctxPhotoPulse.isBranded())
            addBranding(nStartFrame, nEndFrame);

        // Add play/pause controls (above branding)
        addControls(nStartFrame, nEndFrame);

        // Add preloader to empty frame just before show starts and before splash
        // (skin will still be visible on this frame if we are licensed)
        addPreloader(nStartFrame - 1, nEndFrame, strLoadMovieMP3);

        // Add sniffing script to very first frame
        addSniffer();
    }

    private void addInvokeEventHandler(int frame, String eventHandler, String eventType, String eventArg) {
        if (eventHandler == null)
            return;

        // Equivalent ActionScript (when eventHandler is "/foreground/santa" and eventType is "photo1"):
        //
        // /foreground/santa:eventType = "photo1";
        // /foreground/santa:eventArg = "Title for this photo";
        // call ("/foreground/santa:eventHandler");

        Program prog = new Program();
        prog.push(eventHandler + ":eventType", flashFile);
        prog.push(eventType, flashFile);
        prog.setVar();
        prog.push(eventHandler + ":eventArg", flashFile);
        prog.push(eventArg, flashFile);
        prog.setVar();
        prog.push(eventHandler + ":eventHandler", flashFile);
        prog.callFrame();
        prog.none();

        parentScript.getFrameAt(frame).addFlashObject(new DoAction(prog));
    }

    private void addControls(int nFirstFrame, int nLastFrame) throws IVException {
        // Add controls at highest layer - above skins etc.
        int nControlDepth = parentScript.getMaxDepth() + 1;
        parentScript.reserveLayers(nControlDepth, 1);

        Script scrControls = flashFile.getScript(CONTROLS_CLIP);
        if (scrControls == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { CONTROLS_CLIP, getCommandName() });

        // Add controls to lower left
        Rectangle2D rectControls = scrControls.getBounds();
        AffineTransform atxControls = new AffineTransform(atxPlaceholder);
        atxControls.translate(-(stageRect.getWidth() / 2) + (rectControls.getWidth() / 2),
            (stageRect.getHeight() / 2) - (rectControls.getHeight() / 2));
        parentScript.getFrameAt(nFirstFrame).addInstance(scrControls, nControlDepth, atxControls, null, "controller");

        // Remove at end
        parentScript.getFrameAt(nLastFrame).removeInstance(nControlDepth);
    }

    private void addPreloader(int nFirstFrame, int nLastFrame, String strLoadMovieMP3) throws IVException {
        // Add preloader above max photo depth.
        // This puts it between background/foreground skins and above all photos.
        int nPreloaderDepth = maxDepth + 2;
        parentScript.reserveLayers(nPreloaderDepth, 1);

        Script scrPreloader = flashFile.getScript(PRELOADER_CLIP);
        if (scrPreloader == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { PRELOADER_CLIP, getCommandName() });

        // Preloader loads external MP3 if any
        if (strLoadMovieMP3 != null)
            photoContext.setValue("loadMP3", strLoadMovieMP3);

        // Process script so {framerate} and {beginsplashframes} etc. are processed
        scrPreloader = scrPreloader.copyScript();
        flashFile.processScript(scrPreloader, photoContext);

        Rectangle2D rectPreloader = scrPreloader.getBounds();
        AffineTransform atxPreloader = new AffineTransform(atxPlaceholder);

        // Scale to stage width if stage too small
        double dblScale = 1.0;
        if (rectPreloader.getWidth() > stageRect.getWidth())
            dblScale = stageRect.getWidth() / rectPreloader.getWidth();

        // Center on stage.
        // Name the instance so skins can manipulate it.
        atxPreloader.scale(dblScale, dblScale);
        parentScript.getFrameAt(nFirstFrame).addInstance(scrPreloader, nPreloaderDepth, atxPreloader, null, "preloader");

        // Remove at end
        parentScript.getFrameAt(nLastFrame).removeInstance(nPreloaderDepth);
    }

    private void addSniffer() {
        // ActionScript sniffer - bail if < Flash4
        // Do not play() the movie, this will break the preloader
        // Equivalent ActionScript
        //
        // if ("1") {
        // } else {
        //     getURL("", "");
        //     getURL("url", "_self");
        //     stop();
        // }
        //
        // The "if" statement was introduced in Flash4,
        // earlier versions will just execute the "else" portion

        Program prog = new Program();
        prog.push("1", flashFile);
        prog.logicalNot();
        prog.jumpIfTrue(5);
        String strURL = SystemMessages.getMessage(SystemMessages.SWF_SNIFF_URL);
        String strTarget = "_self";
        // getURL length is: byte(1) + word(2) + url.length+1 + target.length+1
        int nOffset = (1 + 2 + 1 + 1) + (1 + 2 + strURL.length()+1 + strTarget.length()+1) + 1;
        prog.jump(nOffset);
        // Need a bogus getURL to bump Flash2/3
        prog.getURL("", "", flashFile);
        prog.getURL(strURL, strTarget, flashFile);
        prog.stop();
        prog.none();

        parentScript.getFrameAt(0).addFlashObject(new DoAction(prog));
    }

    private void addBranding(int nFirstFrame, int nLastFrame) throws IVException {
        // Add branding at highest layer - above skins etc.
        int nLogoDepth = parentScript.getMaxDepth() + 1;
        parentScript.reserveLayers(nLogoDepth, 2);

        // Compute where logo goes, after begin splash and before end splash.
        // If no time for it, don't display it.
        int nStartLogoFrame = (int)(nFirstFrame + (frameRate * BEGINSPLASH_SECONDS));
        nStartLogoFrame = Math.min(nStartLogoFrame, nLastFrame);
        int nEndLogoFrame = (int)(nLastFrame - (frameRate * ENDSPLASH_SECONDS));
        nEndLogoFrame = Math.min(nEndLogoFrame, nLastFrame);
        if (nEndLogoFrame > nStartLogoFrame)
            addLogo(nLogoDepth, nStartLogoFrame, nEndLogoFrame);

        // Splash overlaps the show, 5sec at beginning and end
        addSplash(nLogoDepth+1,
                nFirstFrame, // Start beginsplash
                nStartLogoFrame, // End beginsplash
                nEndLogoFrame > nStartLogoFrame ? nEndLogoFrame : nStartLogoFrame // Start endsplash
        );
    }

    private void addLogo(int nDepth, int nStartFrame, int nEndFrame) throws IVException {
        Script scrLogo = flashFile.getScript(LOGO_CLIP);
        if (scrLogo == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { LOGO_CLIP, getCommandName() });

        // Scale logo to half stage width and position in upper left
        Rectangle2D rectLogo = scrLogo.getBounds();
        double dblScale = (stageRect.getWidth() / 2) / rectLogo.getWidth();
        AffineTransform atxLogo = new AffineTransform(atxPlaceholder);
        atxLogo.translate(-((stageRect.getWidth() / 2) - (rectLogo.getWidth() / 2) * dblScale),
            -((stageRect.getHeight() / 2) - (rectLogo.getHeight() / 2) * dblScale));
        atxLogo.scale(dblScale, dblScale);

        // Add the logo
        parentScript.getFrameAt(nStartFrame).addInstance(scrLogo, nDepth, atxLogo, null);

        // Remove the logo
        parentScript.getFrameAt(nEndFrame).removeInstance(nDepth);
    }

    private void addSplash(int nDepth, int nStartBeginSplash, int nEndBeginSplash, int nStartEndSplash) throws IVException {
        Script scrEndSplash = flashFile.getScript(SPLASH_CLIP);
        if (scrEndSplash == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { SPLASH_CLIP, getCommandName() });

        // Scale splash to stage width
        double dblScale = stageRect.getWidth() / scrEndSplash.getBounds().getWidth();
        AffineTransform atxSplash = new AffineTransform(atxPlaceholder);
        atxSplash.scale(dblScale, dblScale);

        // Begin splash
        parentScript.getFrameAt(nStartBeginSplash).addInstance(scrEndSplash, nDepth, atxSplash, null);
        if (nEndBeginSplash < nStartEndSplash)
            parentScript.getFrameAt(nEndBeginSplash).removeInstance(nDepth);

        // End splash - don't remove it
        if (nStartEndSplash > nEndBeginSplash)
            parentScript.getFrameAt(nStartEndSplash).addInstance(scrEndSplash, nDepth, atxSplash, null);
    }

    private Script processClip(Context ctxClip, String strClip) throws IVException {
        // Get specified MC
        strClip = EFFECT_CLIP_PREFIX + strClip;
        Script scrClip = flashFile.getScript(strClip);
        if (scrClip == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { strClip, getCommandName() });

        // Process MC so it will contain image symbol def stored in context
        scrClip = scrClip.copyScript();
        flashFile.processScript(scrClip, ctxClip);

        return scrClip;
    }

    private void reserveLayers(String strLayer) {
        // Reserve 2 layers per photo so each photo can have a transition mask

        // Next photo goes on top
        if ("bottom".equalsIgnoreCase(strLayer)) {
            if (currentDepth + 2 > maxDepth) {
                parentScript.reserveLayers(currentDepth + 2, 2);
                maxDepth = currentDepth + 2;
            }
            currentDepth = currentDepth + 2;
        }
        // Next photo goes on bottom
        else {
            if (currentDepth - 2 < minDepth) {
                // Move everything up and put the next photo on the current layer (now the bottom)
                parentScript.reserveLayers(currentDepth, 2);
                maxDepth +=  2;
            }
            else
                currentDepth = currentDepth - 2;
        }
    }

    private int getDurationFrames(double dblDurationSeconds, int nDefaultDurationFrames) {
        int nDuration = (int)(nDefaultDurationFrames * frameRate);
        if (dblDurationSeconds != 0)
            nDuration = (int)(dblDurationSeconds * frameRate);
        return nDuration;
    }

    private Instance addInstance(FlashDef def, int nDepth, int nFrame) {
        return parentScript.getFrameAt(nFrame).addInstance(def, nDepth, atxPlaceholder, null);
    }

    // Masks must be on the layer below the masked instance

    // For all transitions, directions should indicate the direction the motion is moving towards

    private Instance applyBeginTransition(FlashDef def, int nFrame,
            String strTransName, int nDuration) throws IVException {

        // Treat zero duration as None - just place the instance
        if (nDuration == 0)
            strTransName = EffectRegistry.BEGINTRANS_None.getTag();
        else
            strTransName = strTransName.intern();

        AffineTransform atxBegin = new AffineTransform(atxPlaceholder);

        if (EffectRegistry.BEGINTRANS_Fade.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newAlpha(0);
            CXForm cxfEnd = CXForm.newIdentity(true);
            return tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, true);
        }
        else if (EffectRegistry.BEGINTRANS_FadeBlack.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newIdentity(true);
            cxfBegin.setRedMul(0);
            cxfBegin.setGreenMul(0);
            cxfBegin.setBlueMul(0);
            CXForm cxfEnd = CXForm.newIdentity(true);
            return tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, true);
        }
        else if (EffectRegistry.BEGINTRANS_FadeWhite.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newIdentity(true);
            cxfBegin.setRedAdd(255);
            cxfBegin.setGreenAdd(255);
            cxfBegin.setBlueAdd(255);
            CXForm cxfEnd = CXForm.newIdentity(true);
            return tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, true);
        }
        else if (EffectRegistry.BEGINTRANS_FlipHorizontal.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.scale(0, 0);
            return tween(def, currentDepth+1, nFrame, nDuration,
                atxBegin, null, 0, 0, Math.toRadians(180),
                atxPlaceholder, null, 0, 0, 0, true);
        }
        else if (EffectRegistry.BEGINTRANS_FlipVertical.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.scale(0, 0);
            return tween(def, currentDepth+1, nFrame, nDuration,
                atxBegin, null, 0, Math.toRadians(180), 0,
                atxPlaceholder, null, 0, 0, 0, true);
        }
        else if (EffectRegistry.BEGINTRANS_Heart.getTag() == strTransName) {
            return buildShapeMaskBeginTrans(PhotoContext.SHAPE_Heart, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_Iris.getTag() == strTransName) {
            return buildShapeMaskBeginTrans(PhotoContext.SHAPE_Circle, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_LawBadge.getTag() == strTransName) {
            return buildShapeMaskBeginTrans(PhotoContext.SHAPE_LawBadge, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_Melt.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_MeltOn);
            scaleMaskToStage(shape.getBoundsEnd(), atxBegin);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxBegin);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_None.getTag() == strTransName) {
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_Plus.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_PlusSquare);
            scaleMaskToStage(shape, atxBegin);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxBegin);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_SkewLeft.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            return tween(def, currentDepth+1, nFrame, nDuration,
                atxBegin, null, 0, Math.toRadians(90), 0,
                atxPlaceholder, null, 0, 0, 0, true);
        }
        else if (EffectRegistry.BEGINTRANS_SkewRight.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            return tween(def, currentDepth+1, nFrame, nDuration,
                atxBegin, null, 0, Math.toRadians(-90), 0,
                atxPlaceholder, null, 0, 0, 0, true);
        }
        else if (EffectRegistry.BEGINTRANS_SlideDown.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.translate(0, -stageRect.getHeight());
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_SlideLeft.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.translate(stageRect.getWidth(), 0);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_SlideRight.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.translate(-stageRect.getWidth(), 0);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_SlideUp.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.translate(0, stageRect.getHeight());
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_Spin.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.scale(0, 0);
            return tween(def, currentDepth+1, nFrame, nDuration,
                atxBegin, null, Math.toRadians(-720), 0, 0,
                atxPlaceholder, null, 0, 0, 0, true);
        }
        else if (EffectRegistry.BEGINTRANS_Star.getTag() == strTransName) {
            return buildShapeMaskBeginTrans(PhotoContext.SHAPE_Star, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_StarBurst.getTag() == strTransName) {
            return buildShapeMaskBeginTrans(PhotoContext.SHAPE_StarBurst, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_StretchHorizontal.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.scale(2, 0);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_StretchVertical.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxBegin.scale(0, 2);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_VenetianHorizontal.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_VenetianOpen);
            atxBegin.rotate(Math.toRadians(90));
            scaleMaskToStage(shape.getBoundsEnd(), atxBegin);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxBegin);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_VenetianVertical.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_VenetianOpen);
            scaleMaskToStage(shape.getBoundsEnd(), atxBegin);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxBegin);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_WipeCenter.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            atxBegin.scale(0, 0);
            AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxEnd);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_WipeDiagonalBottomLeft.getTag() == strTransName) {
            return buildDiagonalWipeIn(stageRect.getWidth()/2, -stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleTopRight,
                    atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeDiagonalBottomRight.getTag() == strTransName) {
            return buildDiagonalWipeIn(-stageRect.getWidth()/2, -stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleTopLeft,
                    atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeDiagonalTopLeft.getTag() == strTransName) {
            return buildDiagonalWipeIn(stageRect.getWidth()/2, stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleBottomRight,
                    atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeDiagonalTopRight.getTag() == strTransName) {
            return buildDiagonalWipeIn(-stageRect.getWidth()/2, stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleBottomLeft,
                    atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeDown.getTag() == strTransName) {
            return buildWipeIn(0, -stageRect.getHeight(), atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeHorizontal.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            scaleMaskToStage(shape, atxBegin);
            atxBegin.scale(0, 1);
            AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxEnd);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_WipeLeft.getTag() == strTransName) {
            return buildWipeIn(stageRect.getWidth(), 0, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeRight.getTag() == strTransName) {
            return buildWipeIn(-stageRect.getWidth(), 0, atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeUp.getTag() == strTransName) {
            return buildWipeIn(0, stageRect.getHeight(), atxBegin, nFrame, nDuration, def);
        }
        else if (EffectRegistry.BEGINTRANS_WipeVertical.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            scaleMaskToStage(shape, atxBegin);
            atxBegin.scale(1, 0);
            AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxEnd);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
            return addInstance(def, currentDepth+1, nFrame);
        }
        else if (EffectRegistry.BEGINTRANS_ZoomBoth.getTag() == strTransName) {
            atxBegin.scale(0, 0);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_ZoomHorizontal.getTag() == strTransName) {
            atxBegin.scale(0, 1);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else if (EffectRegistry.BEGINTRANS_ZoomVertical.getTag() == strTransName) {
            atxBegin.scale(1, 0);
            return tween(def, currentDepth+1, nFrame, nDuration, atxBegin, atxPlaceholder, true);
        }
        else
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_INVALID_TRANSITION, new Object[] { strTransName });
    }

    private Instance buildWipeIn(double transX, double transY, AffineTransform atxBegin, int nFrame, int nDuration, FlashDef def) throws IVException {
        Shape shape = loadShape(PhotoContext.SHAPE_Square);
        atxBegin.translate(transX, transY);
        scaleMaskToStage(shape, atxBegin);
        AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
        scaleMaskToStage(shape, atxEnd);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        return addInstance(def, currentDepth+1, nFrame);
    }

    private Instance buildDiagonalWipeIn(double transX, double transY, String shapeName, AffineTransform atxBegin, int nFrame, int nDuration, FlashDef def) throws IVException {
        Shape shape = loadShape(shapeName);
        atxBegin.translate(transX, transY);
        atxBegin.scale(0, 0);
        AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
        atxEnd.translate(transX, transY);
        scaleMaskToDoubleStage(shape, atxEnd);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        return addInstance(def, currentDepth+1, nFrame);
    }

    private Instance buildShapeMaskBeginTrans(String shapeName, AffineTransform atxBegin, int nFrame, int nDuration, FlashDef def) throws IVException {
        atxBegin.scale(0, 0);
        Shape shape = loadShape(shapeName);
        AffineTransform atxEnd = new AffineTransform(atxPlaceholder);
        scaleStandardMaskToStage(atxEnd);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        return addInstance(def, currentDepth+1, nFrame);
    }

    // For all transitions, directions should indicate the direction the motion is moving towards

    private void applyEndTransition(FlashDef def, int nFrame,
            String strTransName, int nDuration) throws IVException {

        // Treat zero duration as None - do nothing
        if (nDuration == 0)
            strTransName = EffectRegistry.ENDTRANS_None.getTag();
        else
            strTransName = strTransName.intern();

        AffineTransform atxEnd = new AffineTransform(atxPlaceholder);

        if (EffectRegistry.ENDTRANS_Fade.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newIdentity(true);
            CXForm cxfEnd = CXForm.newAlpha(0);
            tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_FadeBlack.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newIdentity(true);
            CXForm cxfEnd = CXForm.newIdentity(true);
            cxfEnd.setRedMul(0);
            cxfEnd.setGreenMul(0);
            cxfEnd.setBlueMul(0);
            tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_FadeWhite.getTag() == strTransName) {
            CXForm cxfBegin = CXForm.newIdentity(true);
            CXForm cxfEnd = CXForm.newIdentity(true);
            cxfEnd.setRedAdd(255);
            cxfEnd.setGreenAdd(255);
            cxfEnd.setBlueAdd(255);
            tween(def, currentDepth+1, nFrame, nDuration, cxfBegin, cxfEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_FlipHorizontal.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.scale(0, 0);
            tween(def, currentDepth+1, nFrame, nDuration,
                atxPlaceholder, null, 0, 0, 0,
                atxEnd, null, 0, 0, Math.toRadians(180), false);
        }
        else if (EffectRegistry.ENDTRANS_FlipVertical.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.scale(0, 0);
            tween(def, currentDepth+1, nFrame, nDuration,
                atxPlaceholder, null, 0, 0, 0,
                atxEnd, null, 0, Math.toRadians(180), 0, false);
        }
        else if (EffectRegistry.ENDTRANS_Heart.getTag() == strTransName) {
            buildShapeMaskEndTrans(PhotoContext.SHAPE_Heart, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_Iris.getTag() == strTransName) {
            buildShapeMaskEndTrans(PhotoContext.SHAPE_Circle, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_LawBadge.getTag() == strTransName) {
            buildShapeMaskEndTrans(PhotoContext.SHAPE_LawBadge, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_Melt.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_MeltOff);
            scaleMaskToStage(shape.getBoundsEnd(), atxEnd);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_None.getTag() == strTransName) {
            // Do nothing
        }
        else if (EffectRegistry.ENDTRANS_Plus.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_SquarePlus);
            scaleMaskToStage(shape, atxEnd);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_SkewLeft.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            tween(def, currentDepth+1, nFrame, nDuration,
                atxPlaceholder, null, 0, 0, 0,
                atxEnd, null, 0, Math.toRadians(-90), 0, false);
        }
        else if (EffectRegistry.ENDTRANS_SkewRight.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            tween(def, currentDepth+1, nFrame, nDuration,
                atxPlaceholder, null, 0, 0, 0,
                atxEnd, null, 0, Math.toRadians(90), 0, false);
        }
        else if (EffectRegistry.ENDTRANS_SlideDown.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.translate(0, stageRect.getHeight());
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_SlideLeft.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.translate(-stageRect.getWidth(), 0);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_SlideRight.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.translate(stageRect.getWidth(), 0);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_SlideUp.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.translate(0, -stageRect.getHeight());
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_Spin.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.scale(0, 0);
            tween(def, currentDepth+1, nFrame, nDuration,
                atxPlaceholder, null, 0, 0, 0,
                atxEnd, null, Math.toRadians(-720), 0, 0, false);
        }
        else if (EffectRegistry.ENDTRANS_Star.getTag() == strTransName) {
            buildShapeMaskEndTrans(PhotoContext.SHAPE_Star, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_StarBurst.getTag() == strTransName) {
            buildShapeMaskEndTrans(PhotoContext.SHAPE_StarBurst, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_StretchHorizontal.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.scale(2, 0);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_StretchVertical.getTag() == strTransName) {
            addStageTransitionMask(nFrame, nDuration, currentDepth);
            atxEnd.scale(0, 2);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_VenetianHorizontal.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_VenetianClose);
            atxEnd.rotate(Math.toRadians(90));
            scaleMaskToStage(shape, atxEnd);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_VenetianVertical.getTag() == strTransName) {
            MorphShape shape = loadMorphShape(PhotoContext.MORPH_VenetianClose);
            scaleMaskToStage(shape, atxEnd);
            tweenMorphMask(shape, currentDepth, nFrame, nDuration, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_WipeCenter.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            atxEnd.scale(0, 0);
            AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxBegin);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_WipeDiagonalBottomLeft.getTag() == strTransName) {
            buildDiagonalWipeOut(-stageRect.getWidth()/2, stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleBottomLeft,
                    atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeDiagonalBottomRight.getTag() == strTransName) {
            buildDiagonalWipeOut(stageRect.getWidth()/2, stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleBottomRight,
                    atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeDiagonalTopLeft.getTag() == strTransName) {
            buildDiagonalWipeOut(-stageRect.getWidth()/2, -stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleTopLeft,
                    atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeDiagonalTopRight.getTag() == strTransName) {
            buildDiagonalWipeOut(stageRect.getWidth()/2, -stageRect.getHeight()/2, PhotoContext.SHAPE_TriangleTopRight,
                    atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeDown.getTag() == strTransName) {
            buildWipeOut(0, stageRect.getHeight(), atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeHorizontal.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            scaleMaskToStage(shape, atxEnd);
            atxEnd.scale(0, 1);
            AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxBegin);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_WipeLeft.getTag() == strTransName) {
            buildWipeOut(-stageRect.getWidth(), 0, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeRight.getTag() == strTransName) {
            buildWipeOut(stageRect.getWidth(), 0, atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeUp.getTag() == strTransName) {
            buildWipeOut(0, -stageRect.getHeight(), atxEnd, nFrame, nDuration);
        }
        else if (EffectRegistry.ENDTRANS_WipeVertical.getTag() == strTransName) {
            Shape shape = loadShape(PhotoContext.SHAPE_Square);
            scaleMaskToStage(shape, atxEnd);
            atxEnd.scale(1, 0);
            AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
            scaleMaskToStage(shape, atxBegin);
            tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
        }
        else if (EffectRegistry.ENDTRANS_ZoomBoth.getTag() == strTransName) {
            atxEnd.scale(0, 0);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_ZoomHorizontal.getTag() == strTransName) {
            atxEnd.scale(0, 1);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else if (EffectRegistry.ENDTRANS_ZoomVertical.getTag() == strTransName) {
            atxEnd.scale(1, 0);
            tween(def, currentDepth+1, nFrame, nDuration, atxPlaceholder, atxEnd, false);
        }
        else
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_INVALID_TRANSITION, new Object[] { strTransName });

        parentScript.getFrameAt(nFrame + nDuration).removeInstance(currentDepth+1);
    }

    private void buildWipeOut(double transX, double transY, AffineTransform atxEnd, int nFrame, int nDuration) throws IVException {
        Shape shape = loadShape(PhotoContext.SHAPE_Square);
        atxEnd.translate(transX, transY);
        scaleMaskToStage(shape, atxEnd);
        AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
        scaleMaskToStage(shape, atxBegin);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
    }

    private void buildDiagonalWipeOut(double transX, double transY, String shapeName, AffineTransform atxEnd, int nFrame, int nDuration) throws IVException {
        Shape shape = loadShape(shapeName);
        atxEnd.translate(transX, transY);
        atxEnd.scale(0, 0);
        AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
        atxBegin.translate(transX, transY);
        scaleMaskToDoubleStage(shape, atxBegin);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
    }

    private void buildShapeMaskEndTrans(String shapeName, AffineTransform atxEnd, int nFrame, int nDuration) throws IVException {
        atxEnd.scale(0, 0);
        Shape shape = loadShape(shapeName);
        AffineTransform atxBegin = new AffineTransform(atxPlaceholder);
        scaleStandardMaskToStage(atxBegin);
        tweenMask(shape, currentDepth, nFrame, nDuration, atxBegin, atxEnd);
    }

    /**
     * Add a mask to reveal only the stage.
     * Used to mask begin/end transitions that transform the effect outside stage bounds.
     */
    private void addStageTransitionMask(int nFrame, int nDuration, int nDepth) throws IVException {
        Shape shape = loadShape(PhotoContext.SHAPE_Square);
        AffineTransform atxMask = new AffineTransform(atxPlaceholder);
        scaleMaskToStage(shape, atxMask);
        Instance instMask = parentScript.getFrameAt(nFrame).addInstance(shape, nDepth, atxMask, null);
        instMask.clip = nDepth + 1;
        parentScript.getFrameAt(nFrame + nDuration).removeInstance(nDepth);
    }

    /**
     * Compute transform to scale the standard 100x100 shape bounds to the stage
     */
    private void scaleStandardMaskToStage(AffineTransform atx) {
        photoContext.scaleSourceToDest(PhotoContext.SHAPE_BOUNDS, PhotoContext.SHAPE_BOUNDS,
                stageRect.getWidth(), stageRect.getHeight(), atx, true, false);
    }

    private void scaleMaskToDoubleStage(Shape shape, AffineTransform atx) {
        Rectangle2D sourceRect = shape.getBounds();
        photoContext.scaleSourceToDest(sourceRect.getWidth(), sourceRect.getHeight(),
                stageRect.getWidth() * 2, stageRect.getHeight() * 2, atx, false, false);
    }

    private void scaleMaskToStage(Shape shape, AffineTransform atx) {
        photoContext.scaleSourceToDest(shape.getBounds(), stageRect, atx, false, false);
    }

    private void scaleMaskToStage(MorphShape shape, AffineTransform atx) {
        photoContext.scaleSourceToDest(shape.getBoundsStart(), stageRect, atx, true, false);
    }

    private void scaleMaskToStage(Rectangle2D rect, AffineTransform atx) {
        photoContext.scaleSourceToDest(rect, stageRect, atx, true, false);
    }

    private void tweenMask(FlashDef def, int nDepth, int nFrame, int nDuration,
            AffineTransform atxBegin, AffineTransform atxEnd) throws IVException {
        Instance inst = tween(def, nDepth, nFrame, nDuration, atxBegin, atxEnd, true);
        inst.clip = nDepth + 1;
        parentScript.getFrameAt(nFrame + nDuration).removeInstance(nDepth);
    }

    private void tweenMorphMask(MorphShape def, int nDepth, int nFrame, int nDuration,
            AffineTransform atxBegin) throws IVException {
        tweenMask(def, nDepth, nFrame, nDuration, atxBegin, null);
    }

    private Instance tween(FlashDef def, int nDepth, int nFrame, int nDuration,
            CXForm cxfBegin, CXForm cxfEnd, boolean bAddInst) throws IVException {
        return tween(def, nDepth, nFrame, nDuration,
            atxPlaceholder, cxfBegin, 0, 0, 0, null, cxfEnd, 0, 0, 0, bAddInst);
    }

    private Instance tween(FlashDef def, int nDepth, int nFrame, int nDuration,
            AffineTransform atxBegin, AffineTransform atxEnd, boolean bAddInst) throws IVException {
        return tween(def, nDepth, nFrame, nDuration,
            atxBegin, null, 0, 0, 0, atxEnd, null, 0, 0, 0, bAddInst);
    }

    private Instance tween(FlashDef def, int nDepth, int nFrame, int nDuration,
            AffineTransform atxBegin, CXForm cxfBegin,
            double dblBeginRotate, double dblBeginSkewX, double dblBeginSkewY,
            AffineTransform atxEnd, CXForm cxfEnd,
            double dblEndRotate, double dblEndSkewX, double dblEndSkewY,
            boolean bAddInst) throws IVException {
        return photoContext.tween(parentScript, def, nDepth, nFrame, nDuration,
            atxBegin, cxfBegin, dblBeginRotate, dblBeginSkewX, dblBeginSkewY,
            atxEnd, cxfEnd, dblEndRotate, dblEndSkewX, dblEndSkewY,
            bAddInst ? DefaultTweenBuilder.TWEEN_INSTANCE_ADD : DefaultTweenBuilder.TWEEN_INSTANCE_MODIFY);
    }

    private MorphShape loadMorphShape(String strName) throws IVException {
        MorphShape shape = photoContext.loadMorphShape(strName);
        if (shape == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { strName, getCommandName() });
        return shape;
    }

    private Shape loadShape(String strName) throws IVException {
        Shape shape = photoContext.loadShape(strName);
        if (shape == null)
            throw new IVException(Resource.CMDSCRIPTNOTFOUND, new Object[] { strName, getCommandName() });
        return shape;
    }

    private void loadLibrary(String strPath, boolean bFullParsing) throws IVException {
        try {
            IVUrl url = IVUrl.newUrl(strPath, flashFile);
            strPath = url.getName();
            FlashFile file = flashFile.getExternalFile(strPath);
            if (file != null)
                return;

            // Cache the parsed file
            file = (FlashFile)MediaCache.getMedia(strPath);
            if (file == null) {
                InputStream is = url.getInputStream();
                file = FlashFile.parse(strPath, is, bFullParsing, null);
                MediaCache.addMedia(url, file, file.getFileSize(), true);
            }

            flashFile.addExternalFile(strPath, file);
        } catch (IOException e) {
            throw new IVException(Resource.ERRREADINGFILE, new Object[] {strPath}, e);
        }
    }
}
