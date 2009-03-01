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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.imageio;

import java.awt.image.BufferedImageOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.DataBuffer;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Converts an image to either graytone or sepiatone.
 */
public class ToneOp implements BufferedImageOp {
    private boolean isSepia;

    public ToneOp(boolean isSepia) {
        this.isSepia = isSepia;
    }

    public boolean isSepia() {
        return isSepia;
    }

    /**
     * @param srcImage Image to be toned
     * @param destImage It is most efficient to pass null for dest and let the filter create it
     * @return Toned image
     */
    public BufferedImage filter(BufferedImage srcImage, BufferedImage destImage) {
        // Convert to byte gray if not already
        if(srcImage.getType() != BufferedImage.TYPE_BYTE_GRAY){
          BufferedImage tmp = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
          copyImage(srcImage, tmp);
          srcImage = tmp;
        }

        BufferedImage tonedImage;

        // Source is now grayscale byte, so we can use its raster with the sepia colormodel
        if (isSepia)
            tonedImage = new BufferedImage(SEPIA_COLORMODEL, srcImage.getRaster(), true, null);
        else
            tonedImage = srcImage;

        if (destImage != null)
            copyImage(tonedImage, destImage);
        else
            destImage = tonedImage;

        return destImage;
    }

    private void copyImage(BufferedImage srcImage, BufferedImage destImage) {
        Graphics2D g = destImage.createGraphics();
        g.drawImage(srcImage, 0, 0, null);
        g.dispose();
    }

    public Rectangle2D getBounds2D(BufferedImage srcImage) {
        return new Rectangle(0, 0, srcImage.getWidth(), srcImage.getHeight());
    }

    public BufferedImage createCompatibleDestImage(BufferedImage srcImage, ColorModel destCM) {
        BufferedImage destImage = null;
        if (destCM == null)
            destCM = srcImage.getColorModel();

        destImage = new BufferedImage(destCM, destCM.createCompatibleWritableRaster(srcImage.getWidth(), srcImage.getHeight()),
                destCM.isAlphaPremultiplied(), null);

        return destImage;
    }

    public Point2D getPoint2D(Point2D srcPt, Point2D destPt) {
        if (destPt == null)
            destPt = new Point2D.Double();
        destPt.setLocation(srcPt.getX(), srcPt.getY());
        return destPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

    /**
     * Tritone sepia colormap generated using PhotoShop.
     * Create a grayscale gradient image of width 256 (one pixel per graytone, 0-255).
     * In PhotoShop, choose Image|Mode|Grayscale. Choose Image|Mode|Duotone...
     * Load "BMY Sepia 2.ado" tritone preset.
     * C:\Program Files\Adobe\Photoshop CS\Presets\Duotones\TRITONE\Process Tritones\BMY Sepia 2.ado
     * Choose Image|Mode|Indexed Color. Save As... PNG.
     * Load into PaintShopPro, Image|Palette|Save Palette...
     * The saved .PspPalette is a text file with 256 RGB levels.
     */
    private static final int[] SEPIA_COLORMAP = new int[] {
        0xff200202,
        0xff200202,
        0xff210302,
        0xff220403,
        0xff230403,
        0xff240503,
        0xff240503,
        0xff250603,
        0xff260603,
        0xff270703,
        0xff290804,
        0xff2a0904,
        0xff2b0905,
        0xff2c0a05,
        0xff2d0b05,
        0xff2f0b06,
        0xff2f0d06,
        0xff310e07,
        0xff320f08,
        0xff331008,
        0xff341109,
        0xff36130a,
        0xff38140b,
        0xff39140b,
        0xff3b160c,
        0xff3c170d,
        0xff3d180e,
        0xff3e190f,
        0xff3f1a0f,
        0xff401b10,
        0xff411c11,
        0xff421d12,
        0xff431e13,
        0xff442014,
        0xff462115,
        0xff472316,
        0xff482316,
        0xff4a2518,
        0xff4b2619,
        0xff4c281a,
        0xff4d291b,
        0xff4e2a1b,
        0xff4f2b1c,
        0xff512c1d,
        0xff522d1e,
        0xff532e1f,
        0xff543020,
        0xff563122,
        0xff573323,
        0xff583424,
        0xff5a3626,
        0xff5b3727,
        0xff5c3828,
        0xff5d3929,
        0xff5f3b2b,
        0xff603d2c,
        0xff613e2d,
        0xff623f2f,
        0xff634030,
        0xff644231,
        0xff654332,
        0xff664433,
        0xff674534,
        0xff684736,
        0xff694837,
        0xff6a4938,
        0xff6c4b3a,
        0xff6e4d3b,
        0xff6f4e3d,
        0xff71503e,
        0xff725240,
        0xff735341,
        0xff745443,
        0xff755644,
        0xff765745,
        0xff775947,
        0xff785a48,
        0xff795b49,
        0xff7a5c4a,
        0xff7b5e4c,
        0xff7c5f4d,
        0xff7d604e,
        0xff7e6250,
        0xff7f6351,
        0xff806452,
        0xff816654,
        0xff836856,
        0xff846957,
        0xff856b59,
        0xff866c5b,
        0xff876d5c,
        0xff886e5d,
        0xff89705e,
        0xff8a7160,
        0xff8b7361,
        0xff8c7463,
        0xff8c7564,
        0xff8d7665,
        0xff8e7867,
        0xff8f7969,
        0xff907b6a,
        0xff917c6c,
        0xff927d6d,
        0xff937e6e,
        0xff948070,
        0xff958171,
        0xff968273,
        0xff978474,
        0xff988576,
        0xff998677,
        0xff9a8879,
        0xff9b897a,
        0xff9b897a,
        0xff9c8a7c,
        0xff9d8c7d,
        0xff9e8d7f,
        0xff9f8e80,
        0xffa09082,
        0xffa19183,
        0xffa19284,
        0xffa29385,
        0xffa39487,
        0xffa49689,
        0xffa5978a,
        0xffa6998c,
        0xffa7998c,
        0xffa89b8e,
        0xffa99c90,
        0xffaa9e91,
        0xffaa9e91,
        0xffab9f93,
        0xffaba094,
        0xffaca195,
        0xffaca195,
        0xffada397,
        0xffaea499,
        0xffafa599,
        0xffb0a69b,
        0xffb1a89d,
        0xffb2a89d,
        0xffb3aa9f,
        0xffb3aba0,
        0xffb4aba1,
        0xffb4aca2,
        0xffb5ada3,
        0xffb6aea5,
        0xffb6afa5,
        0xffb7b0a6,
        0xffb8b1a8,
        0xffb9b2a9,
        0xffbab3ab,
        0xffbab3ab,
        0xffbab5ac,
        0xffbbb6ad,
        0xffbcb7ae,
        0xffbcb7af,
        0xffbdb8b0,
        0xffbeb9b1,
        0xffbeb9b2,
        0xffbfbab3,
        0xffc0bbb4,
        0xffc0bcb5,
        0xffc0bdb5,
        0xffc1bdb6,
        0xffc2beb7,
        0xffc2bfb8,
        0xffc3c0b9,
        0xffc4c1ba,
        0xffc5c2bb,
        0xffc5c3bd,
        0xffc5c3bd,
        0xffc6c4bf,
        0xffc7c5bf,
        0xffc8c6c0,
        0xffc9c7c2,
        0xffcac8c3,
        0xffcac8c3,
        0xffcac9c4,
        0xffcbcac5,
        0xffcccbc6,
        0xffcccbc6,
        0xffcdccc7,
        0xffceccc8,
        0xffcecdc9,
        0xffcecdc9,
        0xffcfceca,
        0xffd0cfca,
        0xffd0cfcb,
        0xffd1d0cc,
        0xffd2d1cd,
        0xffd2d1cd,
        0xffd2d1ce,
        0xffd3d2cf,
        0xffd3d2cf,
        0xffd4d3d0,
        0xffd5d4d1,
        0xffd6d5d2,
        0xffd6d5d2,
        0xffd6d6d3,
        0xffd7d6d3,
        0xffd8d7d4,
        0xffd9d8d6,
        0xffd9d8d6,
        0xffd9d9d6,
        0xffdadad7,
        0xffdbdad8,
        0xffdcdbd9,
        0xffdddcda,
        0xffdddddb,
        0xffdededc,
        0xffdfdedd,
        0xffe0dfdd,
        0xffe0dfdd,
        0xffe1e0de,
        0xffe1e1e0,
        0xffe2e2e0,
        0xffe2e2e0,
        0xffe3e2e1,
        0xffe4e3e2,
        0xffe4e4e3,
        0xffe5e5e4,
        0xffe6e6e4,
        0xffe7e6e5,
        0xffe7e7e6,
        0xffe8e8e7,
        0xffe9e9e7,
        0xffeaeae9,
        0xffeaeae9,
        0xffebeaea,
        0xffebebea,
        0xffececeb,
        0xffededec,
        0xffeeeded,
        0xffeeeeee,
        0xffefefee,
        0xfff0f0ef,
        0xfff1f0f0,
        0xfff1f1f1,
        0xfff2f2f1,
        0xfff3f3f2,
        0xfff4f4f3,
        0xfff5f4f4,
        0xfff5f5f4,
        0xfff6f6f5,
        0xfff7f7f6,
        0xfff8f7f7,
        0xfff7f7f7,
        0xfff8f8f8,
        0xfff9f9f9,
        0xfffafafa,
        0xfffafafa,
        0xfffbfbfb,
        0xfffdfdfd,
        0xfffdfdfd,
        0xfffefefe,
        0xffffffff,
    };

    private static final ColorModel SEPIA_COLORMODEL =
        new IndexColorModel(8, // 8 bits per pixel
                SEPIA_COLORMAP.length,
                SEPIA_COLORMAP,
                0, // Start index in the color map
                false, // No alpha
                -1, // No fully transparent pixel
                DataBuffer.TYPE_BYTE);
}
