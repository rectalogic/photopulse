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

import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.PhotoPulse;
import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.IVException;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.RandomAccessFile;

public abstract class ShowExporterBase implements ShowExporter {
    private ShowModel.ShowType showType;
    private boolean isHighQuality;
    private File exportFile;

    ShowExporterBase(File exportFile, ShowModel.ShowType showType, boolean isHighQuality) {
        this.exportFile = exportFile;
        this.showType = showType;
        this.isHighQuality = isHighQuality;
    }

    public File getExportFile() {
        return exportFile;
    }

    public ShowModel.ShowType getShowType() {
        return showType;
    }

    /**
     * Subclasses can clone and modify the model if desired
     */
    public ShowModel cloneShowModel(ShowModel model) {
        return model;
    }

    /**
     * If the exporter takes time and reports progress, it should return it's portion of the overall progress.
     * @return Percentage of overall progress, 0.0->1.0
     */
    public float getProgressPortion() {
        // Most exporters don't report progress
        return 0.0f;
    }

    /**
     * Subclasses must override and return files that will be created in the same directory as the main save file
     * (not including the save file itself). Return null if no additional files.
     */
    public abstract File[] getExtraExportFiles(ShowModel model);

    public boolean isHighQuality() {
        return isHighQuality;
    }

    public boolean isLazyGenerationSupported() {
        return true;
    }

    /**
     * Subclasses must override and implement show saving.
     * @return true if show exported, false if canceled
     */
    public abstract boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws ExportException, IVException, IOException;

    /**
     * This should only be used for non-lazy swf generation (e.g. MP3)
     * @param swf
     * @param outputFile
     * @throws IOException
     */
    protected void exportSWF(FlashOutput swf, File outputFile) throws IOException {
        // Write out SWF file
        FileOutputStream fos = new FileOutputStream(outputFile);
        try {
            exportSWF(swf, fos);
        } finally {
            fos.close();
        }
    }

    protected void exportSWF(FlashOutput swf, OutputStream os) throws IOException {
        // Write out SWF file
        os.write(swf.getBuf(), 0, swf.getSize());
    }

    // Insert version keyword, accessible via RCS ident command
    protected void insertVersion(RandomAccessFile raf) throws IOException {
        try {
            raf.write(("$PhotoPulse: " + PhotoPulse.getVersion() + " $").getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
        }
    }
}
