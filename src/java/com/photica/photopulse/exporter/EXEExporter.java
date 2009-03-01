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
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.model.ShowModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Saves shows to an EXE
 */
public class EXEExporter extends ShowExporterBase {
    private static final String RSRC_EXE_PLAYER = "/resources/FlashPlayer.exe";

    EXEExporter(File filSave, ShowModel.ShowType showType, boolean isHighQuality) {
        super(filSave, showType, isHighQuality);
    }

    public File[] getExtraExportFiles(ShowModel model) {
        if (model.getMP3() != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL)
            return new File[] { getExternalMP3FileName(MP3Data.computeFileName(getExportFile())) };
        else
            return null;
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IVException, IOException {
        // Open target projector EXE for writing
        RandomAccessFile raf = new RandomAccessFile(getExportFile(), "rw");
        raf.setLength(0);

        try {
            // Write FlashPla.exe
            byte[] abBuffer = new byte[1024*32];
            InputStream is = PhotoPulse.class.getResourceAsStream(RSRC_EXE_PLAYER);
            try {
                int len = 0;
                while ((len = is.read(abBuffer, 0, abBuffer.length)) != -1)
                    raf.write(abBuffer, 0, len);
            } finally {
                is.close();
            }

            // Insert version before SWF
            insertVersion(raf);

            // Append SWF to EXE after version
            int size = LazyGenerator.writeFlashOutput(lazyGenerator, swfShow, raf);

            // Append projector magic number
            raf.write(0x56);
            raf.write(0x34);
            raf.write(0x12);
            raf.write(0xFA);

            // Append 4 byte SWF size
            raf.write(size & 0x000000FF);
            raf.write(size>>>8 & 0x000000FF);
            raf.write(size>>>16 & 0x000000FF);
            raf.write(size>>>24 & 0x000000FF);
        } finally {
            raf.close();
        }

        // Write external mp3 swf
        if (swfMP3 != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL)
            exportSWF(swfMP3, getExternalMP3FileName(MP3Data.computeFileName(getExportFile())));
        return true;
    }

    private File getExternalMP3FileName(String strMP3) {
        return new File(getExportFile().getParentFile(), strMP3);
    }
}
