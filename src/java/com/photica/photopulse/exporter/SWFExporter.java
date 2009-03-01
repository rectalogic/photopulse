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

import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.IVException;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.model.ShowModel;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Saves shows to a standalone SWF
 */
public class SWFExporter extends ShowExporterBase {
    SWFExporter(File filSave, ShowModel.ShowType showType, boolean isHighQuality) {
        super(filSave, showType, isHighQuality);
    }

    public File[] getExtraExportFiles(ShowModel model) {
        if (model.getMP3() != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL)
            return new File[] { getExternalMP3FileName(MP3Data.computeFileName(getExportFile())) };
        else
            return null;
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IVException, IOException {
        RandomAccessFile raf = new RandomAccessFile(getExportFile(), "rw");
        raf.setLength(0);
        try {
            LazyGenerator.writeFlashOutput(lazyGenerator, swfShow, raf);
            insertVersion(raf);
        } finally {
            raf.close();
        }
        if (swfMP3 != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL)
            exportSWF(swfMP3, getExternalMP3FileName(MP3Data.computeFileName(getExportFile())));
        return true;
    }

    private File getExternalMP3FileName(String strMP3) {
        return new File(getExportFile().getParentFile(), strMP3);
    }
}
