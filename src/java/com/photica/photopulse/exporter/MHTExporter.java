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
import com.photica.photopulse.mime.MHTML;
import com.photica.photopulse.model.ShowModel;

import java.awt.geom.Dimension2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Saves shows to an MHTML document
 */
public class MHTExporter extends HTMExporter {
    MHTExporter(File filSave, ShowModel.ShowType showType, boolean isHighQuality) {
        super(filSave, showType, isHighQuality);
    }

    public File[] getExtraExportFiles(ShowModel model) {
        return null;
    }
    
    public boolean isLazyGenerationSupported() {
        return false;
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IVException, IOException {
        // XXX We don't support using LazyGenerator yet, we need an OutputStream, it needs a RandomAccessFile.
        if (lazyGenerator != null)
            throw new IllegalArgumentException("Do not use LazyGenerator for MHT export");

        BufferedOutputStream osMHT = new BufferedOutputStream(new FileOutputStream(getExportFile()));
        try {
            MHTML mhtml = new MHTML(osMHT);

            // Get SWF size
            Dimension2D dimSize = model.getSkin().getSkinSizePixels();
            Integer nSWFWidth = new Integer((int)dimSize.getWidth());
            Integer nSWFHeight = new Integer((int)dimSize.getHeight());

            final String strSWFName = "file:///photopulse.swf";

            // Write host page template
            String strHostContents = populateHTMHostTemplate(strSWFName, nSWFWidth, nSWFHeight, model.getBackgroundColor());
            mhtml.writeHTMLPart(strHostContents, "file:///photopulse.htm");

            // Write Flash file
            OutputStream osBinary = mhtml.writeBinaryPartHeader(strSWFName, MHTML.MIMETYPE_SWF);
            exportSWF(swfShow, osBinary);

            // Write mp3 Flash file
            if (swfMP3 != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL) {
                OutputStream osBinaryMP3 = mhtml.writeBinaryPartHeader("file:///" + MP3Data.computeFileName(getExportFile()), MHTML.MIMETYPE_SWF);
                exportSWF(swfMP3, osBinaryMP3);
            }

            mhtml.writeFinish();
        } finally {
            osMHT.close();
        }

        return true;
    }
}
