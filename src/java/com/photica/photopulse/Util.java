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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.photica.photopulse;

import com.iv.flash.api.image.Bitmap;
import com.iv.flash.api.shape.FillStyle;
import com.iv.flash.api.shape.Shape;
import com.iv.flash.util.GeomHelper;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

public class Util {
    /**
     * Parse x,y[,w,h] into Rectangle
     * Require w,h if bRequireSize true
     * Returns null if invalid
     */
    public static Rectangle parseRect(String strRect, boolean bRequireSize) {
        if (strRect == null || strRect.length() == 0)
            return null;
        StringTokenizer st = new StringTokenizer(strRect, ",");
        int nTokens = st.countTokens();
        if ((nTokens != 4 && bRequireSize) || (nTokens != 4 && nTokens != 2 && !bRequireSize))
            return null;
        Rectangle rect = new Rectangle();
        try {
            rect.x = Integer.parseInt(st.nextToken().trim());
            rect.y = Integer.parseInt(st.nextToken().trim());
            if (nTokens == 4) {
                rect.width = Integer.parseInt(st.nextToken().trim());
                rect.height = Integer.parseInt(st.nextToken().trim());
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return rect;
    }

    public static Rectangle parseRect(String strRect) {
        return parseRect(strRect, true);
    }

    /**
     * Parse w,h into Dimension
     * Returns null if invalid
     */
    public static Dimension parseSize(String size, String token) {
        if (size == null || size.length() == 0)
            return null;
        StringTokenizer st = new StringTokenizer(size, token);
        if (st.countTokens() != 2)
            return null;
        Dimension dim = new Dimension();
        try {
            dim.width = Integer.parseInt(st.nextToken().trim());
            dim.height = Integer.parseInt(st.nextToken().trim());
        } catch (NumberFormatException e) {
            return null;
        }
        return dim;
    }

    /**
     * Parse scale double 0->1.0.
     * Invalid scales parse to 1.0.
     */
    public static double parseScale(String strScale) {
        if (strScale == null || strScale.length() == 0)
            return 1.0;
        try {
            return Double.parseDouble(strScale.trim());
        } catch (NumberFormatException e) {
            return 1.0;
        }
    }

    public static String encodeURL(String strName) {
        if (strName == null)
            return null;
        try {
            // This seems to be the most reliable encoding, it's what File.toURI() uses
            return new URI(null, null, strName, null).getRawPath();
        } catch (URISyntaxException e) {
            return strName;
        }
    }

    public static String getBaseName(File file) {
        String strName = file.getName();
        int nSuffix = strName.lastIndexOf(".");
        if (nSuffix != -1)
            strName = strName.substring(0, nSuffix);
        return strName;
    }

    public static Shape bitmapTile(Bitmap bitmap, int nWidth, int nHeight, double dblRotation) {
        return bitmapFillOrTile(bitmap, nWidth, nHeight, dblRotation, FillStyle.TILED_BITMAP);
    }

    public static Shape bitmapFill(Bitmap bitmap) {
        return bitmapFillOrTile(bitmap,
            bitmap.getWidth() * ShowGenerator.TWIPS_PER_PIXEL,
            bitmap.getHeight() * ShowGenerator.TWIPS_PER_PIXEL,
            0.0,
            FillStyle.CLIPPED_BITMAP);
    }

    private static Shape bitmapFillOrTile(Bitmap bitmap, int nWidth, int nHeight, double dblRotation, int nFillStyle) {
        // Fill shape with bitmap
        Shape shape = new Shape();

        // Translate by 1/2 pixel to avoid Flash rendering with top/left pixels
        // duplicated and bottom right truncated.
        // This fix can mess up very small bitmaps (e.g. 1x2) that are scaled up.
        AffineTransform atx = AffineTransform.getTranslateInstance(-10, -10);

        // Rotate around the center of the bitmap
        if (dblRotation != 0.0)
            atx.rotate(dblRotation, bitmap.getWidth() / 2, bitmap.getHeight() / 2);

        // Scale from twips to pixels
        atx.scale(ShowGenerator.TWIPS_PER_PIXEL, ShowGenerator.TWIPS_PER_PIXEL);

        FillStyle fs = null;
        switch (nFillStyle) {
        case FillStyle.CLIPPED_BITMAP:
            fs = FillStyle.newClippedBitmap(bitmap, atx);
            break;
        case FillStyle.TILED_BITMAP:
            fs = FillStyle.newTiledBitmap(bitmap, atx);
            break;
        default:
            throw new InternalError("invalid fill style");
        }

        shape.setFillStyle1(fs);
        shape.drawRectangle(0, 0, nWidth, nHeight);
        shape.setBounds(GeomHelper.newRectangle(0, 0, nWidth, nHeight));

        return shape;
    }


    public static String readStream(InputStream is) throws IOException {
        if (is == null)
            throw new IOException("Attempt to read null InputStream");
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        StringBuilder sb = new StringBuilder();
        char[] ach = new char[512];
        int nCount;
        while ((nCount = isr.read(ach)) != -1)
            sb.append(ach, 0, nCount);
        isr.close();

        return sb.toString();
    }

    /**
     * Attempt to fill buf from stream.
     * If EOF encountered, buf may not be filled.
     * @return Number of bytes read
     */
    public static int readFully(InputStream stream, byte buf[]) throws IOException {
        int n = 0;
        while (n < buf.length) {
            int count = stream.read(buf, n, buf.length - n);
            if (count < 0)
                return n;
            n += count;
        }
        return n;
    }

    public static String formatDuration(double dblSeconds) {
        int nMinutes = (int)(dblSeconds / 60);
        int nSeconds = (int)dblSeconds - (nMinutes * 60);
        int nHundredths = (int)((dblSeconds  - (nMinutes * 60) - nSeconds) * 100);
        return String.valueOf(nMinutes)
                + ":" + (nSeconds <= 9 ? ("0"  + String.valueOf(nSeconds)) : String.valueOf(nSeconds))
                + "." + (nHundredths <= 9 ? ("0"  + String.valueOf(nHundredths)) : String.valueOf(nHundredths));
    }

    /**
     * Convert from 8:8 fixed point framerate to float.
     */
    public static float convertFrameRate(int frameRate) {
        return (float)frameRate / (1<<8);
    }

    /**
     * Convert from float framerate to 8:8 fixed point.
     */
    public static int convertFrameRate(float frameRate) {
        return (int)(frameRate * (1<<8));
    }
}