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

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.Deflater;


/**
 * Helper class for LazyBitmap.
 * Manages encoding highQuality bitmaps. Directly encodes a BufferedImage to a zlib
 * compressed RGB[A] stream in the bitmap RAF.
 * Also knows how to encode those bits into SWF.
 * This class should be used for bitmap encoding whenever highQuality mode is used.
 * It is much faster than encoding to PNG and avoids QuickTime SWF color issues.
 */
class ZlibBitmap implements BitmapDelegate {
    // SWF format ID for 32 bit pixels
    private static final int FORMAT_32 = 5;

    private static final float ALPHA_SCALE = 1.0f / 0xff;

    public ZlibBitmap(DecodedImage dimage, RandomAccessFile bitmapRAF) throws IOException {
        BufferedImage image = dimage.getImage();
        ColorModel colorModel = image.getColorModel();

        boolean hasAlpha = colorModel.hasAlpha();

        final int scanlineCount = 30;
        int scanlineSize = scanlineCount * image.getWidth() * 4;

        // Set up for compression
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        byte zlibData[] = new byte[scanlineSize];

        // Set up for [A]RGB image decoding
        byte scanlineBytes[] = new byte[scanlineSize];
        int width = image.getWidth();
        int height = image.getHeight();
        Raster raster = image.getRaster();
        Object dataEl = null;

        for (int y = 0; y < height; y++) {
            int scanX = 0;
            int scanCount = (y + scanlineCount < height) ? scanlineCount : height - y;
            for (int scanY = 0; scanY < scanCount; scanY++) {
                // Populate scanlines with [A]RGB data
                for (int x = 0; x < width; x++) {
                    // Let raster create dataEl the first time, reuse on subsequent calls
                    int pixel = colorModel.getRGB((dataEl = raster.getDataElements(x, y + scanY, dataEl)));

                    // Store as ARGB, pre-multiplied alpha. Optimize 0x0 and 0xff cases
                    byte alpha = hasAlpha ? (byte)(pixel >> 24) : (byte)0xff;
                    if (alpha == (byte)0x0) {
                        scanlineBytes[scanX++] = (byte)0x00;
                        scanlineBytes[scanX++] = (byte)0x00;
                        scanlineBytes[scanX++] = (byte)0x00;
                        scanlineBytes[scanX++] = (byte)0x00;
                    }
                    else if (alpha == (byte)0xff) {
                        scanlineBytes[scanX++] = (byte)alpha;
                        scanlineBytes[scanX++] = (byte)(pixel >> 16);
                        scanlineBytes[scanX++] = (byte)(pixel >> 8);
                        scanlineBytes[scanX++] = (byte)(pixel);
                    }
                    else {
                        float normAlpha = (alpha & 0xff) * ALPHA_SCALE;
                        scanlineBytes[scanX++] = alpha;
                        scanlineBytes[scanX++] = (byte)(normAlpha * ((pixel >>> 16) & 0xff) + 0.5f);
                        scanlineBytes[scanX++] = (byte)(normAlpha * ((pixel >>> 8) & 0xff) + 0.5f);
                        scanlineBytes[scanX++] = (byte)(normAlpha * ((pixel) & 0xff) + 0.5f);
                    }
                }
            }
            y += scanCount - 1;

            // Compress and write scanline to RAF
            deflater.setInput(scanlineBytes, 0, scanX);
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

    // See com.iv.flash.api.image.LLBitmap.write()
    public void write(FlashOutput fob, int id, Rectangle2D bounds, byte[] imageBytes) {
        int tagSize = 2 + 1 + 4 + imageBytes.length;
        // We never use Tag.DEFINEBITSLOSSLESS - flash player does not composite other
        // elements with alpha properly if underlying image is not Tag.DEFINEBITSLOSSLESS2
        fob.writeLongTag(Tag.DEFINEBITSLOSSLESS2, tagSize);
        fob.writeWord(id);
        fob.writeByte(FORMAT_32);
        fob.writeWord((int)bounds.getWidth());
        fob.writeWord((int)bounds.getHeight());
        fob.writeArray(imageBytes, 0, imageBytes.length);
    }
}
