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
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.SystemMessages;
import com.photica.ui.FlashPlayerControl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

public class FlashPlayerPreviewExporter extends ShowExporterBase implements PreviewExporter {
    private FlashPlayerControl flashControl;

    FlashPlayerPreviewExporter() {
        super(null, null, false);
    }

    public ShowModel cloneShowModel(ShowModel model) {
        // Force internal MP3
        ShowModel.MP3Mode mode = model.getMP3Mode();
        if (mode == ShowModel.MP3Mode.EXTERNAL) {
            model = model.clone();
            model.setMP3Mode(ShowModel.MP3Mode.INTERNAL);
        }
        return model;
    }

    public File[] getExtraExportFiles(ShowModel model) {
        return null;
    }

    public boolean isLazyGenerationSupported() {
        return false;
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IOException, ExportException {
        // Never use LazyGenerator for previews
        if (lazyGenerator != null)
            throw new IllegalArgumentException("Do not use LazyGenerator for preview export");

        // Get size of movie in pixels
        Rectangle2D swfRect = swfShow.getFlashFile().getFrameSize();
        Dimension swfSize = new Dimension((int)(swfRect.getWidth() / ShowGenerator.TWIPS_PER_PIXEL),
                (int)(swfRect.getHeight() / ShowGenerator.TWIPS_PER_PIXEL));

        // Create player of proper size
        flashControl = new FlashPlayerControl(swfSize, swfShow.getBuf(), 0, swfShow.getSize(),
                SystemMessages.getMessage(SystemMessages.ERR_FLASHPLAYER));

        return true;
    }

    public Component getPreviewComponent() {
        return flashControl;
    }

    public void dispose() {
        if (flashControl == null)
            return;
        Container container = flashControl.getParent();
        if (container != null) {
            container.remove(flashControl);
            flashControl = null;
        }
    }
}
