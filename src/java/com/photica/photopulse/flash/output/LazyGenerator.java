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
import com.iv.flash.util.IVException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;


/**
 * Manages generating a SWF that has many bitmaps in a memory efficient way.
 * First, during FlashFile.processFile(), our commands create LazyBitmap instances.
 * LazyGenerator provides a RAF that the LazyBitmaps can encode their images into.
 * So the image bits are not stored in memory in a Bitmap, but on disk.
 * The LazyBitmap references the bits on disk (RAF offsets).
 * <p>
 * Later, during FlashFile.generate() when LazyBitmap.write() is called, it does not write
 * SWF bits to the FlashOutput. It just remembers its offset in the FlashOutput,
 * generates a FlashDef ID, and adds itself to an ordered list of LazyBitmaps maintained by LazyGenerator.
 * <p>
 * At this point, we have a FlashOutput that contains all the SWF bits except the bitmap definitions,
 * and we have a temp file containing the encoded bitmap bits.
 * Now {@link #writeFlashOutput} must be used to combine the two into a proper SWF.
 * It iterates the ordered LazyBitmap list, writing out chunks of FlashOutput buffer
 * followed by generating the bitmap SWF definition, then seeking back and fixing the SWF size
 * bits in the file (requires a RAF). LazyBitmap generates the bitmap SWF definition from the temp
 * bitmap RAF by creating a concrete Bitmap subclass and forcing it to write into a temp FlashOutput.
 * <p>
 * {@link #writeFlashOutput} is static, if it is passed a non-null LazyGenerator instance, it uses
 * it to properly create the SWF. Otherwise it assumes the FlashOutput is complete and just writes it out.
 */
public class LazyGenerator {
    private BitmapRAF bitmapRAF;

    private List<LazyBitmap> lazyBitmapList;

    public LazyGenerator(File exportFile) throws IOException {
        File exportDir = exportFile;
        if (!exportDir.isDirectory())
            exportDir = exportFile.getParentFile();
        bitmapRAF = new BitmapRAF(File.createTempFile("lbm", ".tmp", exportDir));

        lazyBitmapList = new LinkedList<LazyBitmap>();
    }

    /**
     * Shared scratch file for LazyBitmaps to stash their encoded bitmap bits
     */
    RandomAccessFile getBitmapRAF() {
        return bitmapRAF;
    }

    /**
     * When LazyBitmap.write() is called, it adds itself to our list for deferred writing.
     */
    void addLazyBitmap(LazyBitmap bitmap) {
        lazyBitmapList.add(bitmap);
    }

    /**
     * Write swf output using lazyGenerator if not null.
     * @return Size of swf written
     */
    public static int writeFlashOutput(LazyGenerator lazyGenerator, FlashOutput fob, RandomAccessFile rafOutput) throws IOException, IVException {
        if (lazyGenerator != null)
            return lazyGenerator.writeFlashOutput(fob, rafOutput);
        else {
            int size = (int)fob.getSize();
            rafOutput.write(fob.getBuf(), 0, size);
            return size;
        }
    }

    private int writeFlashOutput(FlashOutput fob, RandomAccessFile rafOutput) throws IOException, IVException {
        long beginOutputOffset = rafOutput.getFilePointer();

        byte[] buf = fob.getBuf();
        int bufSize = fob.getSize();
        int bufOffset = 0;

        for (LazyBitmap lazyBitmap : lazyBitmapList) {
            // Write any FlashOutput bits before this Bitmaps offset
            int bitmapOffset = lazyBitmap.getFlashOutputOffset();
            if (bufOffset < bitmapOffset) {
                rafOutput.write(buf, bufOffset, bitmapOffset - bufOffset);
                bufOffset = bitmapOffset;
            }
            // Now generate the Bitmap and write its bits
            FlashOutput fobBitmap = lazyBitmap.lazyWrite();
            rafOutput.write(fobBitmap.getBuf(), 0, fobBitmap.getSize());
        }
        // Write any remaining FlashOutput bits after the last Bitmap
        if (bufOffset < bufSize)
            rafOutput.write(buf, bufOffset, bufSize - bufOffset);

        // Fixup the SWF size record.
        // First write into the buffer, then copy that to the RAF.
        // Fixup RAF position afterwords.
        long endOutputOffset = rafOutput.getFilePointer();
        int size = (int)(endOutputOffset - beginOutputOffset);
        fob.writeDWordAt(size, 4);
        rafOutput.seek(beginOutputOffset + 4);
        rafOutput.write(buf, 4, 4);
        rafOutput.seek(endOutputOffset);

        // Preemptively cleanup RAF
        cleanup();

        return size;
    }

    /**
     * This must be called after constructing a LazyGenerator to clean up temp files.
     */
    public void cleanup() {
        if (bitmapRAF != null) {
            try {
                bitmapRAF.cleanup();
            } catch (IOException e) {
            }
            bitmapRAF = null;
        }
    }
}

/**
 * RandomAccessFile that cannot be closed externally.
 * Exposes cleanup() method that closes and deletes the underlying file.
 * This allows us to keep the RAF open across multiple ImageIO encodings
 * (even though we may call ImageOutputStream.dispose)
 */
class BitmapRAF extends RandomAccessFile {
    private File file;

    public BitmapRAF(File file) throws FileNotFoundException {
        super(file, "rw");
        this.file = file;
    }

    public void close() throws IOException {
        // Do nothing
    }

    void cleanup() throws IOException {
        super.close();
        if (!file.delete())
            file.deleteOnExit();
    }
}