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

import com.iv.flash.api.FlashDef;
import com.iv.flash.api.FlashItem;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.util.FlashBuffer;
import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.IVException;
import com.iv.flash.util.ScriptCopier;
import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.TranscodeOp;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class LazyBitmap extends Bitmap {
    private LazyGenerator lazyGenerator;

    // Bitmap size in pixels
    private Rectangle bitmapBounds;

    // Offsets of encoded bitmap in RAF
    private long rafBeginOffset;
    private long rafEndOffset;

    // Offset we need to write our SWF bits in FlashOutput
    private int fobOffset;

    private BitmapDelegate bitmapDelegate;

    private LazyBitmap() {
    }

    public LazyBitmap(LazyGenerator lazyGenerator, DecodedImage image, boolean isHighQuality) throws IOException {
        this.lazyGenerator = lazyGenerator;
        RandomAccessFile raf = lazyGenerator.getBitmapRAF();
        long pos = raf.getFilePointer();
        if (isHighQuality) {
            // Use ZlibBitmap for PNG, it is faster
            if (ImageTranscoder.FORMAT_PNG.equals(image.getSourceFormat()))
                bitmapDelegate = new ZlibBitmap(image, raf);
            // Use JPEG3Bitmap for JPEG, it handles alpha channel
            else
                bitmapDelegate = new JPEG3Bitmap(image, raf);
        }
        else
            ImageCoder.getInstance().encodeImage(image, null, new FileImageOutputStreamFix(raf));
        init(image.getImage(), pos, raf.getFilePointer());
    }

    LazyBitmap(LazyGenerator lazyGenerator, File imageFile, Rectangle cropRect, double scale, ImageTranscoder transcoder, TranscodeOp op) throws IOException {
        this.lazyGenerator = lazyGenerator;
        RandomAccessFile raf = lazyGenerator.getBitmapRAF();
        long pos = raf.getFilePointer();
        DecodedImage image;
        try {
            image = transcoder.transcodeImage(imageFile, null,
                    cropRect, scale, ImageTranscoder.SCALE_BILINEAR, op, new FileImageOutputStreamFix(raf));
        } catch (IOException e) {
            raf.seek(pos);
            throw e;
        }
        init(image.getImage(), pos, raf.getFilePointer());
    }

    private void init(BufferedImage image, long beginOffset, long endOffset) {
        this.rafBeginOffset = beginOffset;
        this.rafEndOffset = endOffset;
        this.bitmapBounds = new Rectangle(image.getWidth(), image.getHeight());
    }

    public static Bitmap newBitmap(LazyGenerator generator, File imageFile,
            ImageTranscoder transcoder, Rectangle cropRect, double scale,
            TranscodeOp op) throws IOException, IVException {

        // If we aren't generating lazily, then encode image into memory and return Bitmap
        if (generator == null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int)imageFile.length());
            transcoder.transcodeImage(imageFile, null, cropRect, scale, ImageTranscoder.SCALE_BILINEAR, op, bos);
            return Bitmap.newBitmap(new FlashBuffer(bos.toByteArray()));
        }
        // Transcode into RAF, return LazyBitmap with offsets
        else
            return new LazyBitmap(generator, imageFile, cropRect, scale, transcoder, op);
    }

    /**
     * Bitmap size in pixels
     */
    public Rectangle2D getBounds() {
        return bitmapBounds;
    }

    // I don't think this method is used in our scenario
    public int getSize() {
        return 0;
    }

    /**
     * Return offset in FlashOutput at which we should write our SWF bits
     */
    public int getFlashOutputOffset() {
        return fobOffset;
    }

    /**
     * We don't write any SWF bits, just save the offset in FlashOutput where we need to write them
     * and register ourselves with the LazyGenerator.
     */
    public void write(FlashOutput fob) {
        // Generate an ID for this instance and save it
        setID(fob.getDefID(this));

        fobOffset = fob.getPos();
        lazyGenerator.addLazyBitmap(this);
    }

    public FlashOutput lazyWrite() throws IOException, IVException {
        int bufSize = (int)(rafEndOffset - rafBeginOffset);

        FlashOutput fob = new LazyFlashOutput(bufSize + bufSize/4);

        // Read back our encoded bitmap bits
        RandomAccessFile bitmapRAF = lazyGenerator.getBitmapRAF();
        byte[] imageBytes = new byte[bufSize];
        bitmapRAF.seek(rafBeginOffset);
        bitmapRAF.readFully(imageBytes);

        if (bitmapDelegate != null) {
            bitmapDelegate.write(fob, getID(), bitmapBounds, imageBytes);
        }
        else {
            // Construct a real Bitmap using those bits
            Bitmap bitmap = Bitmap.newBitmap(new FlashBuffer(imageBytes));
            // Copy our generated ID into the new Bitmap. LazyFlashOutput will write this ID.
            bitmap.setID(getID());
            // Must call getBounds() since some Bitmaps defer initialization until it is called.
            bitmap.getBounds();

            // Make real Bitmap write the SWF bits
            bitmap.write(fob);
        }

        return fob;
    }

    protected FlashItem copyInto(FlashItem item, ScriptCopier copier) {
        super.copyInto(item, copier);
        ((LazyBitmap)item).lazyGenerator = lazyGenerator;
        ((LazyBitmap)item).bitmapBounds = (Rectangle)bitmapBounds.clone();
        ((LazyBitmap)item).rafBeginOffset = rafBeginOffset;
        ((LazyBitmap)item).rafEndOffset = rafEndOffset;
        ((LazyBitmap)item).fobOffset = fobOffset;
        return item;
    }

    public FlashItem getCopy(ScriptCopier copier) {
        return copyInto(new LazyBitmap(), copier);
    }
}

/**
 * FlashOutput that writes the def ID stored in a def, instead of generating one.
 */
class LazyFlashOutput extends FlashOutput {
    public LazyFlashOutput(int size) {
        super(size);
    }

    public void writeDefID(FlashDef def) {
        writeWord(def.getID());
    }
}
