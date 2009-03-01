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

package com.photica.photopulse.skin;

import com.iv.flash.api.Color;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.api.shape.FillStyle;
import com.iv.flash.api.shape.Shape;
import com.photica.photopulse.Util;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;

// Represents a single foreground or background skin element
class SkinElement {
    // Instance name
    private String name;

    // Used as key into skin cache
    private URL elementURL;

    // Radians
    private double rotation;

    // Tile image
    private boolean isTiled;

    // Scale and position element to fit
    private Rectangle regionRect;

    // Region to mask (cut a hole in the element of this size)
    private Rectangle maskRect;

    // Loaded skin element
    private Object element;

    /**
     * @param regionRect Region in pixels
     */
    public SkinElement(String name, URL elementURL, Rectangle2D regionRect, Rectangle2D maskRect, double rotation, boolean isTiled) {
        this.name = name;
        this.elementURL = elementURL;
        this.rotation = rotation;
        this.isTiled = isTiled;

        // Convert pixels to twips
        if (regionRect != null)
            this.regionRect = new Rectangle((int)regionRect.getX()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)regionRect.getY()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)regionRect.getWidth()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)regionRect.getHeight()*ShowGenerator.TWIPS_PER_PIXEL);
        if (maskRect != null)
            this.maskRect = new Rectangle((int)maskRect.getX()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)maskRect.getY()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)maskRect.getWidth()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)maskRect.getHeight()*ShowGenerator.TWIPS_PER_PIXEL);
    }

    public SkinElement(String name, URL elementURL, Rectangle2D regionRect, boolean isTiled) {
        this(name, elementURL, regionRect, null, 0.0, isTiled);
    }

    public SkinElement(String name, URL elementURL) {
        this(name, elementURL, null, null, 0.0, false);
    }

    public void load() throws SkinException {
        if (elementURL != null)
            element = SkinCache.getSkin(elementURL);
    }

    public void unload() {
        element = null;
    }

    public URL getURL() {
        return elementURL;
    }

    public Rectangle2D getRegion() {
        return regionRect;
    }

    public double getRotation() {
        return rotation;
    }

    public boolean isTile() {
        return isTiled;
    }

    public Object getElement() {
        return element;
    }

    // Only valid on a load()ed element
    public Rectangle2D getNativeBounds() {
        if (element instanceof FlashFile) {
            return ((FlashFile)element).getFrameSize();
        }
        else {
            Bitmap bitmap = (Bitmap)element;
            return new Rectangle(0, 0,
                bitmap.getWidth()*ShowGenerator.TWIPS_PER_PIXEL,
                bitmap.getHeight()*ShowGenerator.TWIPS_PER_PIXEL);
        }
    }

    // Only valid on a load()ed element
    // rectRegion can have zero size, which means no scaling
    public Instance getInstance() {
        Script script;
        Rectangle2D nativeBounds = getNativeBounds();
        Rectangle2D objectRect = null;
        double rotationUsed = rotation;

        Rectangle2D regionRectUsed = regionRect;
        if (regionRectUsed == null)
            regionRectUsed = nativeBounds;

        if (element instanceof FlashFile) {
            FlashFile file = (FlashFile)element;
            script = file.getMainScript();
            // We have to copy the script to avoid processing the original, since these are cached
            if (file.isTemplate())
                script = script.copyScript();
            objectRect = nativeBounds;
        }
        else {
            Bitmap bitmap = (Bitmap)element;
            Shape shape = null;
            if (isTiled) {
                // Rotate the tile, not the instance
                shape = Util.bitmapTile(bitmap,
                    (int)regionRectUsed.getWidth(), (int)regionRectUsed.getHeight(),
                    rotationUsed);
                // Reset rotation so we don't rotate below
                rotationUsed = 0.0;
                objectRect = regionRectUsed;
            }
            else {
                shape = Util.bitmapFill(bitmap);
                objectRect = nativeBounds;
            }
            script = new Script(1);
            script.newFrame().addInstance(shape, 1, null, null);
        }


        double objectWidth = objectRect.getWidth();
        double objectHeight = objectRect.getHeight();
        double regionWidth = regionRectUsed.getWidth();
        double regionHeight = regionRectUsed.getHeight();

        AffineTransform atx = new AffineTransform();

        // If region is not at origin, translate
        if (regionRectUsed.getX() != 0 || regionRectUsed.getY() != 0)
            atx.translate(regionRectUsed.getX(), regionRectUsed.getY());

        // If skin object size does not match non-zero region size,
        // scale the Instance
        if ((regionWidth != 0 && regionHeight != 0)
            && (objectWidth != regionWidth
                || objectHeight != regionHeight)) {
            atx.scale(regionWidth / objectWidth,
                regionHeight / objectHeight);
        }

        // If rotation non-zero, rotate
        // Rotating non-square elements is not intuitive.
        // This page has a good explanation http://www.glyphic.com/transform/applet/3order.html
        if (rotationUsed != 0.0)
            atx.rotate(rotationUsed, objectWidth / 2, objectHeight / 2);

        Instance inst = new Instance();
        inst.name = name;
        inst.def = script;
        inst.matrix = atx;
        return inst;
    }

    // Return the elements mask, or null
    public Shape getMask() {
        if (maskRect == null)
            return null;

        Rectangle2D regionRectUsed = regionRect;
        if (regionRectUsed == null)
            regionRectUsed = getNativeBounds();
        // Zero size or negative size (flipping)
        else if (regionRectUsed.isEmpty()) {
            Rectangle2D bounds = getNativeBounds();
            regionRectUsed = new Rectangle((int)regionRectUsed.getX(), (int)regionRectUsed.getY(),
                (int)bounds.getWidth(), (int)bounds.getHeight());
        }

        Shape shapeMask = Shape.newShape1();
        shapeMask.setFillStyle0(FillStyle.newSolid(new Color(0, 0, 0)));
        shapeMask.drawRectangle(regionRectUsed);
        shapeMask.setFillStyle0(0);
        shapeMask.setFillStyle1(1);
        shapeMask.drawRectangle(maskRect);
        shapeMask.setBounds(regionRectUsed);

        return shapeMask;
    }
}
