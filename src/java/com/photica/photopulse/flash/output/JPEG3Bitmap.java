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
 
package com.photica.photopulse.flash.output;

import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.Tag;
import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.ImageCoder;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.Deflater;

/**
 * Helper class for LazyBitmap.
 * Manages encoding highQuality bitmaps. Directly encodes a BufferedImage to a JPEG
 * and zlib compressed alpha channel in the bitmap RAF.
 * Also knows how to encode those bits into SWF as a DefineBitsJPEG3.
 * This class should be used for bitmap encoding whenever transparent regions are introduced via image transformation
 * (e.g. hiQuality mode panzooms).
 */
public class JPEG3Bitmap implements BitmapDelegate {

    private int jpegSize;

    public JPEG3Bitmap(DecodedImage dimage, RandomAccessFile bitmapRAF) throws IOException {
        BufferedImage image = dimage.getImage();

        long offset = bitmapRAF.getFilePointer();
        ImageCoder.getInstance().encodeImage(dimage, ImageCoder.FORMAT_JPEG, new FileImageOutputStreamFix(bitmapRAF));
        jpegSize = (int)(bitmapRAF.getFilePointer() - offset);

        ColorModel colorModel = image.getColorModel();
        int width = image.getWidth();
        int height = image.getHeight();
        Raster raster = image.getRaster();
        Object dataEl = null;

        // If the image has alpha, check for any non-opaque pixels.
        // If all pixels are opaque, then don't write any zlib alpha channel.
        // Flash interprets this as fully opaque.
        boolean needsAlpha = false;
        if (colorModel.hasAlpha()) {
            outerLoop:
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Let raster create dataEl the first time, reuse on subsequent calls
                    dataEl = raster.getDataElements(x, y, dataEl);
                    if ((byte)0xff != (byte)colorModel.getAlpha(dataEl)) {
                        needsAlpha = true;
                        break outerLoop;
                    }
                }
            }
        }
        if (!needsAlpha)
            return;

        // Set up for compression
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        byte zlibData[] = new byte[width];

        byte scanlineBytes[] = new byte[width];

        for (int y = 0; y < height; y++) {
            // Populate scanline with alpha data
            for (int x = 0; x < width; x++) {
                // Let raster create dataEl the first time, reuse on subsequent calls
                dataEl = raster.getDataElements(x, y, dataEl);
                scanlineBytes[x] = (byte)colorModel.getAlpha(dataEl);
            }

            // Compress and write scanline to RAF
            deflater.setInput(scanlineBytes, 0, width);
            if (y == height - 1)
                deflater.finish();
            while (true) {
                int compressedSize = deflater.deflate(zlibData);
                if (compressedSize == 0)
                    break;
                bitmapRAF.write(zlibData, 0, compressedSize);
            }
        }
    }

    // imageBytes will contain jpeg and alpha zlib data together
    public void write(FlashOutput fob, int id, Rectangle2D bounds, byte[] imageBytes) {
        int tagSize = 2 + 4 + imageBytes.length;
        fob.writeTag(Tag.DEFINEBITSJPEG3, tagSize);
        fob.writeWord(id);
        fob.writeDWord(jpegSize);
        // Image and zlib alpha bytes
        fob.writeArray(imageBytes, 0, imageBytes.length);
    }
}
