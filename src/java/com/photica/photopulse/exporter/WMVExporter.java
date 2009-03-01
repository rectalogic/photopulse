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
 
package com.photica.photopulse.exporter;

import com.iv.flash.api.FlashFile;
import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.IVException;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.progress.ProgressReporter;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.Iterator;

public class WMVExporter extends ShowExporterBase {

    static {
        boolean initialized = false;
        try {
            System.loadLibrary("swf2wmv");
            initIDs();
            initialized = true;
        } catch (UnsatisfiedLinkError e) {
            PhotoPulse.logException(e);
        }
        INITIALIZED = initialized;
    }

    private static final boolean INITIALIZED;

    private static final String WMV_PROFILE_PATH =  new File(PhotoPulse.INSTALLDIR, "video" + File.separator + "wmvprofile.prx").getAbsolutePath();

    private int totalFrames;
    private ShowModel.MP3Mode originalMP3Mode;

    WMVExporter(File filSave, ShowModel.ShowType showType, boolean isHighQuality) throws ExportException {
        super(filSave, showType, isHighQuality);
        if (!INITIALIZED)
            throw new ExportException(SystemMessages.getMessage(SystemMessages.ERR_WMV_NOTINSTALLED));
    }

    public ShowModel cloneShowModel(ShowModel model) {
        ShowModel.MP3Mode mp3Mode = model.getMP3Mode();

        // Don't export MP3 as swf, we will deal with the MP3 when exporting the WMV.
        // Force EndShow mode to loop.
        if (model.getEndShowMode() != ShowModel.EndShowMode.LOOP
                || (model.getMP3() != null && mp3Mode != ShowModel.MP3Mode.NONE)) {
            originalMP3Mode = mp3Mode;
            model = model.clone();
            model.setMP3Mode(ShowModel.MP3Mode.NONE);
            model.setEndShowMode(ShowModel.EndShowMode.LOOP);
        }
        return model;
    }

    /**
     * Allocate 50% overall progress to exporting
     */
    public float getProgressPortion() {
        return 0.5f;
    }

    public File[] getExtraExportFiles(ShowModel model) {
        return null;
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IOException, IVException, ExportException {
        boolean deleteExportFile = false;
        File swfFile = null;
        try {
            String[] mp3Files = null;
            // Build list of MP3s
            if (originalMP3Mode != ShowModel.MP3Mode.NONE && mp3Data != null)
                mp3Files = loadMP3s(mp3Data);

            // We need to write the swf to disk in a temp file first via LazyGenerator.
            swfFile = File.createTempFile("wmvtmp", ".swf", getExportFile().getParentFile());
            RandomAccessFile rafSWF = new RandomAccessFile(swfFile, "rw");
            try {
                LazyGenerator.writeFlashOutput(lazyGenerator, swfShow, rafSWF);
            } finally {
                rafSWF.close();
            }

            FlashFile flashFile = swfShow.getFlashFile();

            // Get size of movie in pixels
            Rectangle2D swfRect = flashFile.getFrameSize();
            int swfWidth = (int)(swfRect.getWidth() / ShowGenerator.TWIPS_PER_PIXEL);
            int swfHeight = (int)(swfRect.getHeight() / ShowGenerator.TWIPS_PER_PIXEL);

            // Used for progress reporting
            totalFrames = Math.max(flashFile.getMainScript().getFrameCount(),
                    mp3Data != null ? mp3Data.getFrameCount() : 0);

            encodeWMV(swfFile.getAbsolutePath(), swfWidth, swfHeight, model.getFrameRate(), mp3Files,
                    WMV_PROFILE_PATH, getExportFile().getAbsolutePath());

            ProgressReporter.updateProgress(1.0f);
            deleteExportFile = ProgressReporter.isCanceled();

        } catch (InstantiationException e) {
            deleteExportFile = true;
            PhotoPulse.logException(e);
            throw new ExportException(SystemMessages.getMessage(SystemMessages.ERR_WMV_FLASHVERSION));
        } catch (WMVEncodeException e) {
            deleteExportFile = true;
            PhotoPulse.logException(e);
            // If the system is running low on memory, then get_TotalFrames() can fail with E_FAIL
            if ("IShockwaveFlash::get_TotalFrames".equals(e.getOperation()) && e.getHResult() == WMVEncodeException.E_FAIL) {
                throw new ExportException(SystemMessages.getMessage(SystemMessages.ERR_WMV_LOWVM));
            }
            else
                throw new ExportException(SystemMessages.getMessage(SystemMessages.ERR_WMV_EXPORT));
        } finally {
            // Delete temporary flash file
            if (swfFile != null) {
                if (!swfFile.delete())
                    swfFile.deleteOnExit();
            }

            // Delete video file if export canceled or failed
            if (deleteExportFile)
                getExportFile().delete();
        }

        return !ProgressReporter.isCanceled();
    }

    /**
     * Build array of MP3 absolute filenames
     */
    private String[] loadMP3s(MP3Data mp3Data) throws ExportException {
        String[] mp3Files = new String[mp3Data.getMP3Count()];
        Iterator<MP3Data.MP3Sound> iter = mp3Data.iterateMP3s();
        URI uri = null;
        int i = 0;
        while (iter.hasNext()) {
            try {
                uri = iter.next().getURI();
                File mp3File = new File(uri);
                mp3Files[i] = mp3File.getAbsolutePath();
                i++;
            } catch (IllegalArgumentException e) {
                // Illegal MP3 (e.g. http, not local file)
                throw new ExportException(SystemMessages.getMessage(SystemMessages.ERR_WMV_BADMP3, uri.toString()));
            }
        }
        return mp3Files;
    }

    /**
     * Called back from native code to report progress
     */
    private boolean updateProgress(int frameNum) {
        ProgressReporter.updateProgress((float)frameNum / totalFrames);
        return ProgressReporter.isCanceled();
    }

    private static native void initIDs() throws UnsatisfiedLinkError;

    /**
     * @throws InstantiationException Thrown if Flash is not installed or wrong version
     */
    private native void encodeWMV(String swfFile, int swfWidth, int swfHeight, float frameRate,
            String[] mp3Files, String prxProfileFile, String wmvOutputFile) throws WMVEncodeException, InstantiationException;

    /**
     * Thrown by native code if export fails
     */
    private static class WMVEncodeException extends Exception {
        public static final int E_FAIL = 0x80004005;

        private String operation;
        private int hResult;

        /**
         * @param operation Operation (COM method) that failed
         * @param hResult COM HRESULT failure code of method that failed
         * @param location File and line number location in native code where error occurred
         */
        public WMVEncodeException(String operation, int hResult, String location) {
            super("0x" + Integer.toHexString(hResult).toUpperCase() + ": " + operation + " [" + location + "]");
            this.operation = operation;
            this.hResult = hResult;
        }

        public int getHResult() {
            return hResult;
        }

        public String getOperation() {
            return operation;
        }
    }

    // Usage: WMVExporter <output.wmv> <input.swf> <width> <height>
    public static void main(String args[]) throws Exception {
        File exportFile = new File(args[0]);
        WMVExporter exporter = new WMVExporter(exportFile, ShowModel.ShowType.WMV, true);
        exporter.encodeWMV(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), 12, null,
                WMV_PROFILE_PATH, args[0]);
    }
}
