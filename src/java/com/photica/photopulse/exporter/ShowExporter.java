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

/**
 * Superclass for objects that know how to save a show to different file formats.
 */
public interface ShowExporter {

    public File getExportFile();

    public ShowModel.ShowType getShowType();

    /**
     * The exporter can clone and modify the ShowModel if needed (e.g. to change MP3 mode or EndShow mode).
     * It should return the clone, or return the original model if no changes are needed.
     */
    public ShowModel cloneShowModel(ShowModel model);


    /**
     * If the exporter takes time and reports progress, it should return it's portion of the overall progress.
     * @return Percentage of overall progress, 0.0->1.0
     */
    public float getProgressPortion();

    /**
     * Return files that will be created in the same directory as the main save file
     * (not including the save file itself). Return null if no additional files.
     */
    public File[] getExtraExportFiles(ShowModel model);

    /**
     * Return true if the show should be exported at the highest quality.
     */
    public boolean isHighQuality();

    /**
     * Return true if LazyGenerator is supported by {@link #exportShow}
     */
    public boolean isLazyGenerationSupported();

    /**
     * Implement show saving.
     * @param swfShow The show itself
     * @param lazyGenerator If not null, must be used to export swfShow
     * @param swfMP3 The MP3 or null
     * @return true if show exported, false if canceled
     */
    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws ExportException, IVException, IOException;
}
