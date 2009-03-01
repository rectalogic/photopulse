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
 
package com.photica.photopulse.flash;

import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Frame;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.SetBackgroundColor;
import com.iv.flash.api.FlashObject;
import com.iv.flash.api.action.DoAction;
import com.iv.flash.api.action.Program;
import com.iv.flash.commands.GenericCommand;
import com.iv.flash.context.StandardContext;
import com.iv.flash.parser.UnparsedTag;
import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.GeomHelper;
import com.iv.flash.util.IVException;
import com.iv.flash.util.Log;
import com.iv.flash.util.PropertyManager;
import com.iv.flash.util.Tag;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.License;
import com.photica.photopulse.commands.InsertPhotosCommand;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.PreviewExporter;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.context.PhotoPulseContext;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.progress.ProgressReporter;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinException;
import org.apache.log4j.Logger;
import org.apache.log4j.WriterAppender;
import org.w3c.dom.Document;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

public class ShowGenerator {

    // A Flash movie can't have more than 16000 frames
    // http://www.macromedia.com/support/flash/ts/documents/bigflash.htm
    public static final int MAXFRAMES = 16000;

    public static final int TWIPS_PER_PIXEL = 20;

    private static final String RSRC_SWT_LIBRARY_ROOT = "/swt/";

    // Set to true to insert SWF ENABLEDEBUGGER tag into generated SWF.
    private static final boolean ENABLEDEBUGGER = false;

    static {
        com.iv.flash.util.Util.init(new File(PhotoPulse.INSTALLDIR, "lib" + File.separator + "jgenerator").getAbsolutePath());

        Log.setWarnLevel();
        Log.setLogToConsole();
    }

    /**
     * Max allowed SWF duration at a given frameRate
     */
    public static double computeMaxDuration(float frameRate) {
        return MAXFRAMES / frameRate;
    }

    /**
     * The model Skin will be loaded and unloaded.
     * @param model Model of show being exported
     * @param mp3Data Parsed MP3, may be null
     * @param exporter Object that knows how and where to save a show.
     * @param transcoder Used to crop/scale photos in the show
     * @param swLogErrors Filled with error log messages
     * @return true if output generated, false if no output generated (check swLogErrors)
     */
    public static boolean generate(ShowModel model, MP3Data mp3Data,
            ShowExporter exporter, ImageTranscoder transcoder, StringWriter swLogErrors) throws IVException, SkinException, IOException, ExportException {

        // Bury a check for an expired license here, in case the user is playing with their system clock.
        if (PhotoPulse.LICENSE != null
                && PhotoPulse.LICENSE.getType() == License.Type.TIMESTAMPED
                && PhotoPulse.LICENSE.isExpired()) {
            PhotoPulse.LICENSE = null;
        }

        // Collect messages logged
        Logger logger = Log.getLogger();
        logger.removeAllAppenders();
        logger.addAppender(new WriterAppender(new MessageLayout(), swLogErrors));

        // Let the exporter clone and modify the model if it needs to
        model = exporter.cloneShowModel(model);

        // Load the skin
        Skin skin = model.getSkin();
        skin.load();

        LazyGenerator lazyGenerator = null;

        try {
            Document document = new ShowDocumentBuilder().buildDocument(model);

            // Set the progress portion to whatever is left by the exporter
            ProgressReporter.pushProgressPortion(1.0f - exporter.getProgressPortion());

            ShowModel.MP3Mode mp3Mode = model.getMP3Mode();

            // Get external MP3 filename and create external MP3 swf if required.
            String mp3FileName = null;
            FlashOutput swfMP3 = null;
            if (mp3Mode == ShowModel.MP3Mode.EXTERNAL && mp3Data != null) {
                mp3FileName = MP3Data.computeFileName(exporter.getExportFile());
                swfMP3 = mp3Data.createExternalSWF();
            }

            // Create FlashFile template from skin
            FlashFile swt = createSkinSWT(skin, model.getFrameRate(), model.getBackgroundColor());

            // Set up context
            StandardContext ctxMain = new StandardContext();
            ctxMain.setValue("framerate", String.valueOf(model.getFrameRate()));
            Dimension2D skinSize = skin.getSkinSizePixels();
            ctxMain.setValue("skinwidth", String.valueOf((int)skinSize.getWidth()));
            ctxMain.setValue("skinheight", String.valueOf((int)skinSize.getHeight()));

            // Set up skin params
            Iterator<Map.Entry<String,String>> iter = skin.iterateSkinParams();
            if (iter != null) {
                while (iter.hasNext()) {
                    Map.Entry<String,String> entry = iter.next();
                    ctxMain.setValue(entry.getKey(), entry.getValue());
                }
            }

            if (exporter.isLazyGenerationSupported())
                lazyGenerator = new LazyGenerator(exporter.getExportFile());

            // Nested XML context for DOM Document
            // Apply branding if unlicensed and this is not a preview export
            PhotoPulseContext ctxPhotoPulse = new PhotoPulseContext(ctxMain, document,
                    skin.getEventHandler(),
                    (mp3Mode == ShowModel.MP3Mode.INTERNAL ? mp3Data : null),
                    mp3FileName, swfMP3 == null ? 0 : swfMP3.getFlashFile().getMainScript().getFrameCount(),
                    transcoder, lazyGenerator,
                    PhotoPulse.LICENSE == null ? !(exporter instanceof PreviewExporter) : false,
                    exporter.isHighQuality());

            // Process SWT template
            swt.processFile(ctxPhotoPulse);
            addEndShowMode(model.getEndShowMode(), exporter.getShowType(), swt);
            FlashOutput swf = swt.generate();

            // Bail before writing anything if user canceled
            if (ProgressReporter.isCanceled())
                return false;

            // Fail if jgenerator messages logged
            if (swLogErrors.getBuffer().length() > 0)
                return false;

            // Validate movies are not too long
            int nShowFrames = swf.getFlashFile().getMainScript().getFrameCount();
            int nMP3Frames = swfMP3 != null ? swfMP3.getFlashFile().getMainScript().getFrameCount() : 0;
            if (nShowFrames > MAXFRAMES || nMP3Frames > MAXFRAMES) {
                throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_MAXFRAMES,
                        new Object[] { new Integer(nShowFrames > MAXFRAMES ? nShowFrames : nMP3Frames), new Integer(MAXFRAMES) });
            }

            // Update progress portion with the exporters piece
            ProgressReporter.popProgressPortion();
            ProgressReporter.pushProgressPortion(exporter.getProgressPortion());

            // Save the shows files
            boolean result = exporter.exportShow(model, swf, lazyGenerator, swfMP3, mp3Data);
            ProgressReporter.popProgressPortion();
            return result;
        } finally {
            if (lazyGenerator != null)
                lazyGenerator.cleanup();
            skin.unload();
        }
    }

    private static FlashFile createSkinSWT(Skin skin, float frameRate, Color backgroundColor) {
        // Create empty swt FlashFile of proper size
        FlashFile swtSkin = new FlashFile();
        swtSkin.setVersion(4);
        Dimension2D dimSkin = skin.getSkinSize();
        swtSkin.setFrameSize(GeomHelper.newRectangle(0, 0,
            (int)dimSkin.getWidth(), (int)dimSkin.getHeight()));

        // Framerate is 8:8 fixed point - convert float to fixed point
        swtSkin.setFrameRate(Util.convertFrameRate(frameRate));

        swtSkin.setFileSize(100);
        swtSkin.setEncoding(PropertyManager.defaultEncoding);
        swtSkin.setTemplate(true);
        // Set these so FlashFile.getFullName() doesn't crash
        swtSkin.setFileName("skin.swt");
        swtSkin.setFileDir("");

        // Add main single frame script
        Script script = new Script(1);
        script.setMain();
        swtSkin.setMainScript(script);

        // Add SWF debugger tag.
        // Run generated SWF in debug Flash player, right click and choose Debugger to attach debugger.
        if (ENABLEDEBUGGER) {
            // Data extracted from SWF generated by Flash authoring tool. No password.
            byte[] debugger = new byte[] {
                (byte)0x9f, (byte)0x0e, (byte)0x00, (byte)0x00,
                (byte)0x24, (byte)0x31, (byte)0x24, (byte)0x2e,
                (byte)0x65, (byte)0x24, (byte)0x37, (byte)0x63,
                (byte)0x58, (byte)0x54, (byte)0x44, (byte)0x65,
                (byte)0x76, (byte)0x35, (byte)0x4d, (byte)0x6f,
                (byte)0x6f, (byte)0x50, (byte)0x76, (byte)0x33,
                (byte)0x76, (byte)0x6f, (byte)0x56, (byte)0x6e,
                (byte)0x4f, (byte)0x4d, (byte)0x58, (byte)0x31, (byte)0x00};
            script.getFrameAt(0).addFlashObject(new UnparsedTag(Tag.ENABLEDEBUGGER, debugger, 0, debugger.length));
        }

        if (backgroundColor != null) {
            script.setBackgroundColor(new SetBackgroundColor(
                    new com.iv.flash.api.Color(backgroundColor.getRed(),
                        backgroundColor.getGreen(), backgroundColor.getBlue())));
        }

        final int nCommandDepth = 2;

        // Create photos command
        InsertPhotosCommand cmd = new InsertPhotosCommand();
        cmd.setType(GenericCommand.TYPE_MOVIE);
        cmd.setDepth(nCommandDepth);
        cmd.setFrameNum(0);

        // Add command params
        cmd.addParameter(InsertPhotosCommand.PARAM_SELECT, "*/*");
        // Where to find other SWTs from resources
        cmd.addParameter(InsertPhotosCommand.PARAM_LIBRARY, ShowGenerator.class.getResource(RSRC_SWT_LIBRARY_ROOT).toString());

        // Add command to first frame in proper location
        Rectangle2D rectStage = skin.getStageBounds();
        AffineTransform atxStage = new AffineTransform();
        double dblWidth = rectStage.getWidth();
        double dblHeight = rectStage.getHeight();
        atxStage.translate(rectStage.getX() + dblWidth/2, rectStage.getY() + dblHeight/2);
        atxStage.scale(dblWidth / InsertPhotosCommand.COMMAND_WIDTH_TWIPS,
            dblHeight / InsertPhotosCommand.COMMAND_HEIGHT_TWIPS);
        Frame frame = script.newFrame();

        // Add an Instance for the command
        Instance instCmd = frame.addInstance(new Script(1), nCommandDepth, atxStage, null);
        cmd.setInstance(instCmd);
        instCmd.setCommand(cmd);

        // Add external non-visual skin elements (e.g. *.fft fonts)
        skin.addExternalElements(swtSkin);

        // Layer skins above and below the command
        Instance instForeground = skin.getForegroundSkin();
        if (instForeground != null)
            frame.addInstance(instForeground, nCommandDepth + 1);
        Instance instBackground = skin.getBackgroundSkin();
        if (instBackground != null)
            frame.addInstance(instBackground, nCommandDepth - 1);

        return swtSkin;
    }

    private static void addEndShowMode(ShowModel.EndShowMode endShowMode, ShowModel.ShowType showType, FlashFile swt) {
        switch (endShowMode) {
        // Close hosting browser window or quit hosting projector.
        // Equivalent ActionScript:
        //
        // getURL("javascript:void(top.close())", "_self"); OR getURL("FSCommand:quit", "");
        // stop();
        case CLOSE: {
            Program prog = new Program();
            // We don't want to invoke javascript URL in projector - it launches the browser
            if (showType == ShowModel.ShowType.EXE)
                prog.getURL("FSCommand:quit", "", swt);
            else
                prog.getURL("javascript:void(top.close())", "_self", swt);
            prog.stop();
            prog.none();
            swt.getMainScript().getLastFrame().addFlashObject(new DoAction(prog));
        } break;

        // Pause player at end of show, displaying last photo (with endtrans forced to None).
        // Invokes controller, moving it to the pause frame which cleanly pauses effects etc.
        // Equivalent ActionScript:
        //
        // tellTarget("controller") {
        //   gotoAndStop("pause");
        // }
        case PAUSE: {
            Program prog = new Program();
            prog.setTarget("controller", swt);
            prog.push("pause", swt);
            prog.gotoFrameAndStop();
            prog.none();

            // Put pause script on next to last frame - last frame removes all objects.
            // If the MP3 is too long, the photos will be gone on next to last too, oh well.
            // Also extract script from last frame and move it to the next to last frame.
            // This is the "showEnd" event handler invocation script.
            Script script = swt.getMainScript();
            int frameCount = script.getFrameCount();
            Frame lastFrame = script.getFrameAt(frameCount - 1);
            int frameSize = lastFrame.size();
            DoAction lastAction = null;
            for (int i = 0; i < frameSize; i++) {
                FlashObject fo = lastFrame.getFlashObjectAt(i);
                if (fo instanceof DoAction) {
                    lastAction = (DoAction)fo;
                    lastFrame.removeFlashObjectAt(i);
                    break;
                }
            }
            Frame nextLastFrame = script.getFrameAt(frameCount > 1 ? frameCount - 2 : 0);
            nextLastFrame.addFlashObject(new DoAction(prog));
            if (lastAction != null)
                nextLastFrame.addFlashObject(lastAction);
        } break;

        // Stop player at end of show, displaying background.
        // Equivalent ActionScript:
        //
        // stop();
        case STOP: {
            Program prog = new Program();
            prog.stop();
            prog.none();
            swt.getMainScript().getLastFrame().addFlashObject(new DoAction(prog));
        } break;

        // Do nothing for LOOP, looping is the default
        case LOOP:
        default:
        }
    }
}
