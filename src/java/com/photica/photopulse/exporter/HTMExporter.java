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
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;

import java.awt.Color;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.MessageFormat;

/**
 * Saves show as HTML page, with associated content directory.
 */
public class HTMExporter extends ShowExporterBase {
    private static final String RSRC_HTM_LAUNCH = "/resources/launch.htm";
    private static final String RSRC_HTM_HOST = "/resources/host.htm";
    private static final String RSRC_THUMBNAIL = "/resources/thumbnail.jpg";

    // Suffix used for associated HTML file directory (e.g. foo.htm and foo_files)
    public static final String HTML_FILES_SUFFIX = "_files";

    HTMExporter(File filSave, ShowModel.ShowType showType, boolean isHighQuality) {
        super(filSave, showType, isHighQuality);
    }

    public File[] getExtraExportFiles(ShowModel model) {
        return new File[] { getContentDir() };
    }

    public boolean exportShow(ShowModel model, FlashOutput swfShow, LazyGenerator lazyGenerator, FlashOutput swfMP3, MP3Data mp3Data) throws IVException, IOException {
        File filHTMLaunch = getExportFile();

        // Get basename
        String strHTMBaseName = Util.getBaseName(filHTMLaunch);

        // Get SWF size
        Dimension2D dimSize = model.getSkin().getSkinSizePixels();
        Integer nSWFWidth = new Integer((int)dimSize.getWidth());
        Integer nSWFHeight = new Integer((int)dimSize.getHeight());

        // Make content files directory using HTML basename
        File filContentDir = getContentDir();
        if (!filContentDir.isDirectory() && !filContentDir.mkdirs())
            throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_MKDIRFAILED, filContentDir.getPath()));

        // Write SWF to content dir
        File filSWF = new File(filContentDir, strHTMBaseName + ".swf");
        RandomAccessFile raf = new RandomAccessFile(filSWF, "rw");
        raf.setLength(0);
        try {
            LazyGenerator.writeFlashOutput(lazyGenerator, swfShow, raf);
            insertVersion(raf);
        } finally {
            raf.close();
        }

        // Write external mp3 swf to content dir
        if (swfMP3 != null && model.getMP3Mode() == ShowModel.MP3Mode.EXTERNAL)
            exportSWF(swfMP3, new File(filContentDir, MP3Data.computeFileName(getExportFile())));

        // Write JPEG thumbnail to content dir
        File filThumbnail = new File(filContentDir, strHTMBaseName + ".jpg");
        writeThumbnail(filThumbnail, ((PhotoEffects)model.getPhotoEffectList().get(0)).getPhoto());

        // Write launch page template
        String strContentDir = filContentDir.getName();
        String strLaunchContents = populateHTMLaunchTemplate(
            Util.encodeURL(strContentDir + "/" + filHTMLaunch.getName()),
            Util.encodeURL(strContentDir + "/" + filThumbnail.getName()),
            nSWFWidth, nSWFHeight);
        FileWriter fwLaunch = new FileWriter(filHTMLaunch);
        try {
            fwLaunch.write(strLaunchContents);
        } finally {
            fwLaunch.close();
        }

        // Write host page template
        String strHostContents = populateHTMHostTemplate(Util.encodeURL(filSWF.getName()), nSWFWidth, nSWFHeight,
                model.getBackgroundColor());
        FileWriter fwHost = new FileWriter(new File(filContentDir, filHTMLaunch.getName()));
        try {
            fwHost.write(strHostContents);
        } finally {
            fwHost.close();
        }

        return true;
    }

    private File getContentDir() {
        File filSave = getExportFile();
        return new File(filSave.getParentFile(), Util.getBaseName(filSave) + HTML_FILES_SUFFIX);
    }

    private void writeThumbnail(File thumbFile, Photo photo) throws IOException {
        if (photo == null) {
            writeDefaultThumbnail(thumbFile);
            return;
        }

        // Scale and encode thumbnail to JPEG
        double scaleFactor = ImageCoder.getInstance().computeScaleFactor(photo.getWidth(), photo.getHeight(), ThumbnailCache.THUMBNAIL_WIDTH, ThumbnailCache.THUMBNAIL_HEIGHT);
        try {
            ThumbnailCache.getInstance().transcodeImage(photo.getFile(), ImageTranscoder.FORMAT_JPEG, null, scaleFactor, ImageTranscoder.SCALE_SUBSAMPLE, null, thumbFile);
        } catch (IOException e) {
            writeDefaultThumbnail(thumbFile);
        }
    }

    private void writeDefaultThumbnail(File thumbFile) throws IOException {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            // Copy default thumbnail image from resources
            byte[] abBuffer = new byte[1024*10];
            is = PhotoPulse.class.getResourceAsStream(RSRC_THUMBNAIL);
            fos = new FileOutputStream(thumbFile);
            int len = 0;
            while ((len = is.read(abBuffer, 0, abBuffer.length)) != -1)
                fos.write(abBuffer, 0, len);
        } finally {
            if (is != null)
                is.close();
            if (fos != null)
                fos.close();
        }
    }

    private String encodeColor(Color color) {
        if (color == null)
            return "#FFFFFF";

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        return "#"
            + (r < 0x0F ? "0" : "") + Integer.toHexString(r)
            + (g < 0x0F ? "0" : "") + Integer.toHexString(g)
            + (b < 0x0F ? "0" : "") + Integer.toHexString(b);
    }

    private String populateHTMLaunchTemplate(String strLaunchName,
            String strThumbnailName, Integer nWidth, Integer nHeight) throws IOException {
        return MessageFormat.format(Util.readStream(PhotoPulse.class.getResourceAsStream(RSRC_HTM_LAUNCH)),
            new Object[] {
                strLaunchName,
                nWidth, nHeight,
                strThumbnailName
            });
    }

    protected String populateHTMHostTemplate(String strSWFName,
            Integer nWidth, Integer nHeight, Color clrBackground) throws IOException {
        return MessageFormat.format(Util.readStream(PhotoPulse.class.getResourceAsStream(RSRC_HTM_HOST)),
            new Object[] {
                strSWFName,
                nWidth, nHeight,
                encodeColor(clrBackground)
            });
    }
}
