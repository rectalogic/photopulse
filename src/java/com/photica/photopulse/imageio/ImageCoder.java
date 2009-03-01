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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.imageio;

import EDU.oswego.cs.dl.util.concurrent.LinkedNode;
import EDU.oswego.cs.dl.util.concurrent.LinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.QueuedExecutor;
import EDU.oswego.cs.dl.util.concurrent.ThreadFactory;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.model.Photo;
import com.iv.flash.util.FlashBuffer;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImagingOpException;
import java.awt.image.RasterFormatException;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.InflaterInputStream;

public class ImageCoder implements ThumbnailDecoder, ImageTranscoder {

    public static final String CLASSPREFIX_JAI = "com.sun.media.imageioimpl";
    public static final String CLASSPREFIX_BUILTIN = "com.sun.imageio";

    private static final ColorModel RGB_COLORMODEL = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
    private static final int[] RGB_BANDS = {0, 1, 2};

    private static List<String> IMAGE_SUFFIXES;

    private QueuedExecutor threadPool;

    private static ImageCoder instance = new ImageCoder();

    /**
     * Create daemon threads for pool
     */
    private static class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable command) {
            Thread thread = new Thread(command, "ImageCoder");
            thread.setDaemon(true);
            return thread;
        }
    }

    /**
     * Implement a stack Channel - we want LIFO so the most recent image is thumbnailed first
     */
    private static class LinkedStack extends LinkedQueue {
        protected void insert(Object object) {
            synchronized (putLock_) {
                LinkedNode node = new LinkedNode(object);
                synchronized (head_) {
                    node.next = head_.next;
                    head_.next = node;
                }
                if (waitingForTake_ > 0)
                    putLock_.notify();
            }
        }
    }

    public static ImageCoder getInstance() {
        return instance;
    }

    private ImageCoder() {
        threadPool = new QueuedExecutor(new LinkedStack());
        threadPool.setThreadFactory(new DaemonThreadFactory());
        ImageIO.setUseCache(false);
    }

    /**
     * Decode the image file.
     * @param imageFile Image to decode
     * @param cropRect Region of image to decode, may be null
     * @param scaleFactor Scale factor (greater than 0.0, less than or equal to 1.0)
     * @param scaleWidth Size of bounding box to scale image to.
     * @param scaleHeight Size of bounding box to scale image to.
     *   If width and height are >0 then scaleFactor is ignored and size is used to compute scaling.
     * @param scaleType SCALE_SUBSAMPLE, SCALE_NEAREST_NEIGHBOR, SCALE_BILINEAR. Subsampling may result in a slightly smaller image.
     * @param listener May be null.
     * @return decoded image, never null
     * @throws IOException
     */
    public DecodedImage decodeImage(File imageFile, Rectangle cropRect,
            double scaleFactor, int scaleWidth, int scaleHeight, int scaleType, IIOReadListener listener) throws IOException {

        ImageReader reader = getImageReader(imageFile, CLASSPREFIX_BUILTIN, true, true);
        if (reader == null)
            throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, imageFile.getName()));

        BufferedImage image = null;
        String formatName;
        int imageWidth;
        int imageHeight;
        boolean needScaling;
        try {
            if (listener != null) {
                reader.addIIOReadProgressListener(listener);
                reader.addIIOReadUpdateListener(listener);
            }

            imageWidth = reader.getWidth(0);
            imageHeight = reader.getHeight(0);
            needScaling = (scaleFactor < 1.0 && scaleFactor > 0)
                    || (scaleWidth > 0 && scaleHeight > 0 && (scaleWidth < imageWidth || scaleHeight < imageHeight));

            formatName = reader.getFormatName().toLowerCase();

            // Read in the cropped/subsampled image
            ImageReadParam readParam = null;
            if ((scaleType == SCALE_SUBSAMPLE && needScaling) || cropRect != null) {
                readParam = reader.getDefaultReadParam();
                if (cropRect != null)
                    readParam.setSourceRegion(cropRect);
                if (scaleType == SCALE_SUBSAMPLE) {
                    if (scaleFactor < 1.0 && scaleFactor > 0) {
                        readParam.setSourceSubsampling((int)Math.ceil(imageWidth / (imageWidth * scaleFactor)),
                                (int)Math.ceil(imageHeight / (imageHeight * scaleFactor)), 0, 0);
                    }
                    else if (scaleWidth > 0 && scaleHeight > 0) {
                        // Use ceil so the image ends up the same size or smaller than the dimensions specified
                        int sample = Math.max((int)Math.ceil((float)imageWidth / scaleWidth),
                                (int)Math.ceil((float)imageHeight / scaleHeight));
                        readParam.setSourceSubsampling(sample, sample, 0, 0);
                    }
                }
            }
            image = reader.read(0, readParam);
        } catch (IllegalArgumentException e) {
            PhotoPulse.logException(e);
            // This can happen if we crop to 0x0 image
            throw (IOException)new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, imageFile.getName())).initCause(e);
        } finally {
            // XXX Workaround JDK ImageReader native leak
            dispose(reader);
            reader = null;
        }

        // Only scale if we did not already subsample above.
        // Don't scale up, only down
        if (scaleType != SCALE_SUBSAMPLE && needScaling) {
            // Override scaleFactor if width and height are specified
            if (scaleWidth > 0 && scaleHeight > 0)
                scaleFactor = Math.min((float)scaleWidth / imageWidth, (float)scaleHeight / imageHeight);

            // If the image is not "standard", draw it into an image that is.
            // Otherwise the scale below takes forever doing colorspace conversion.
            int type = image.getType();
            if (type != BufferedImage.TYPE_INT_RGB || type != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(),
                        image.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
                Graphics g = rgbImage.getGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = rgbImage;
            }

            // TYPE_BILINEAR scaling will introduce an alpha channel that corrupts encoding to JPEG if not removed
            // http://developer.java.sun.com/developer/bugParade/bugs/4503132.html
            AffineTransform atxScale = AffineTransform.getScaleInstance(scaleFactor, scaleFactor);
            // Since scaleType is not SCALE_SUBSAMPLE, it is one of the AffineTransformOp types
            AffineTransformOp op = null;
            try {
                op = new AffineTransformOp(atxScale, scaleType);
            } catch (ImagingOpException e) {
                PhotoPulse.logException(e);
                throw (IOException)new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, imageFile.getName())).initCause(e);
            }
            image = transformImage(op, image);
            if (image == null)
                throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, imageFile.getName()));
        }

        return new DecodedImage(imageFile, formatName, image);
    }

    public DecodedImage decodeThumbnail(Photo photo, int scaleWidth, int scaleHeight, IIOReadListener listener, boolean fastDecode) throws IOException {
        if (fastDecode)
            throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_IMAGE_DECODE, photo.getFile().getName()));
        return decodeImage(photo.getFile(), null, 0, scaleWidth, scaleHeight, SCALE_SUBSAMPLE, listener);
    }

    /**
     * Transform image.
     * @param op Operation
     * @param image Image to transform
     * @return transformed image or null if transform fails
     */
    public BufferedImage transformImage(BufferedImageOp op, BufferedImage image) {
        try {
            return op.filter(image, null);
        } catch (RasterFormatException e) {
            // Don't log, this is common when transformed width < 0 for AffineTransformOp
            return null;
        }
    }

    /**
     * Encode the DecodedImage into the output (File, OutputStream etc.) in the specified format.
     * @param dimage Image to encode
     * @param targetFormat Format name to encode to. If null, a suitable format will be chosen
     * @param output Target to encode to (File, RandomAccessFile, OutputStream, ImageOutputStream)
     * @throws IOException
     */
    public void encodeImage(DecodedImage dimage, String targetFormat, Object output) throws IIOException, IOException {
        // If target format name is specified, use it.
        // Otherwise write png/gif as png, everything else as jpeg
        // XXX what about TIFF and BMP now?
        if (targetFormat == null) {
            if (FORMAT_PNG.equalsIgnoreCase(dimage.getSourceFormat()) || FORMAT_GIF.equalsIgnoreCase(dimage.getSourceFormat()))
                targetFormat = FORMAT_PNG;
            else
                targetFormat = FORMAT_JPEG;
        }

        ImageOutputStream ios;
        if (output instanceof ImageOutputStream)
            ios = (ImageOutputStream)output;
        else {
            ios = ImageIO.createImageOutputStream(output);
            if (ios == null)
                throw new IIOException("Failed to create ImageOutputStream");
        }

        ImageWriter writer = getImageWriter(targetFormat, CLASSPREFIX_BUILTIN);
        if (writer == null)
            throw new IIOException("Failed to find ImageWriter");

        try {
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            BufferedImage sourceImage = dimage.getImage();

            if (FORMAT_JPEG.equals(targetFormat)) {
                // Strip alpha before encoding to JPEG
                // For ARGB, share the raster (efficient)
                ColorModel colorModel = sourceImage.getColorModel();
                if (sourceImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    WritableRaster raster = sourceImage.getRaster().createWritableChild(0, 0,
                            sourceImage.getWidth(), sourceImage.getHeight(), 0, 0, RGB_BANDS);
                    sourceImage = new BufferedImage(RGB_COLORMODEL, raster, false, null);
                }
                // For other incompatible image types, copy into a new image (inefficient)
                else if (colorModel.hasAlpha() || colorModel.getNumComponents() != 3 || colorModel.getPixelSize() != 24) {
                    int width = sourceImage.getWidth();
                    int height = sourceImage.getHeight();
                    BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = rgbImage.createGraphics();
                    g.drawImage(sourceImage, 0, 0, width, height, Color.WHITE, null);
                    g.dispose();
                    sourceImage = rgbImage;
                }

                // Flash can't handle progressive JPEGs.
                // Disable progressive.
                if (writeParam.canWriteProgressive()) {
                    try {
                        writeParam.setProgressiveMode(ImageWriteParam.MODE_DISABLED);
                    } catch (UnsupportedOperationException e) {
                        // Ignore this, we are trying to turn off progressive, so if it's not supported, fine
                    }
                }
            }

            // Set compression quality.
            if (writeParam.canWriteCompressed()) {
                try {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(0.75f);
                } catch (UnsupportedOperationException e) {
                    // Ignore, if we can't compress then never mind
                }
            }

            writer.setOutput(ios);
            writer.write(null, new IIOImage(sourceImage, null, null), writeParam);
        } finally {
            // XXX Workaround JDK ImageWriter leak
            dispose(writer);
        }
    }

    public DecodedImage transcodeImage(File sourceImageFile, String targetFormat,
            Rectangle cropRect, double scaleFactor, int scaleType, TranscodeOp op, Object output) throws IOException {

        DecodedImage dimage = decodeImage(sourceImageFile, cropRect, scaleFactor, 0, 0, scaleType, null);

        if (op != null) {
            BufferedImage filteredImage = op.filter(dimage.getImage());
            dimage = new DecodedImage(dimage.getSourceFile(), dimage.getSourceFormat(), filteredImage);
        }

        encodeImage(dimage, targetFormat, output);
        return dimage;
    }

    /**
     * Spawns a thread to load a subsampled thumbnail of the specified image file.
     * An Icon is returned which will render the image once loaded.
     * @param observer A Component that will be repainted as the image is loaded
     * @param decoder Used to decode the thumbnail. If null, the ImageCoder singleton will be used.
     */
    public DecodedImageIcon createImageIcon(Photo photo, int width, int height, ThumbnailDecoder decoder, Component observer) {
        if (decoder == null)
            decoder = this;

        // Try a fast decode first
        DecodedImage decodedImage = null;
        try {
            decodedImage = decoder.decodeThumbnail(photo, width, height, null, true);
        } catch (IOException e) {
        }
        if (decodedImage != null)
            return new DecodedImageIcon(photo, width, height, decodedImage);

        // Otherwise decode on background thread
        DecodedImageIcon icon = new DecodedImageIcon(photo, width, height, decoder, observer);
        try {
            threadPool.execute(icon);
        } catch (InterruptedException e) {
        }
        return icon;
    }

    /**
     * Compute scale factor to scale source to fit within dest, maintaining aspect
     */
    public double computeScaleFactor(double sourceWidth, double sourceHeight, double destWidth, double destHeight) {
        return Math.min(destWidth / sourceWidth, destHeight / sourceHeight);
    }

    /**
     * @param photoFiles Image files to valdate
     * @return List of Photo objects
     */
    public List<Photo> validatePhotoFiles(File[] photoFiles) {
        // Validate the photo files
        ArrayList<Photo> photoList = new ArrayList<Photo>(photoFiles.length);
        for (int i = 0; i < photoFiles.length; i++) {
            Photo photo = validatePhotoFile(photoFiles[i]);
            if (photo != null)
                photoList.add(photo);
        }

        return photoList;
    }

    /**
     * Return a Photo object for the file, or null if not a valid image
     */
    public Photo validatePhotoFile(File photoFile) {
        ImageReader reader = null;
        try {
            // Do not ignore metadata when pinging for size
            reader = getImageReader(photoFile, CLASSPREFIX_BUILTIN, false, false);
            if (reader == null)
                return validateFlashPhotoFile(photoFile);

            return new Photo(photoFile, reader.getWidth(0), reader.getHeight(0), false);
        } catch (IllegalArgumentException e) {
        } catch (IllegalStateException e) {
        } catch (IndexOutOfBoundsException e) {
        } catch (IOException e) {
        } finally {
            dispose(reader);
        }
        return null;
    }

    public Photo validateFlashPhotoFile(File photoFile) {
        try {
            InputStream stream = new FileInputStream(photoFile);

            byte[] headerBuf = new byte[3];
            int size = Util.readFully(stream, headerBuf);
            if (size < headerBuf.length
                    || (headerBuf[0] != (byte)'F' && headerBuf[0] != (byte)'C')
                        || headerBuf[1] != (byte)'W' || headerBuf[2] != (byte)'S')
                return null;

            // Compressed SWF
            if (headerBuf[0] == (byte)'C') {
                // Skip compressed file size
                if (stream.skip(4) != 4)
                    return null;
                stream = new InflaterInputStream(stream);
            }

            // Skip version byte and file size (4 bytes)
            if (stream.skip(5) != 5)
                return null;

            // Rect will be >2 and <9 bytes
            byte[] frameRectBuf = new byte[9];
            size = Util.readFully(stream, frameRectBuf);
            if (size < 2)
                return null;
            FlashBuffer fb = new FlashBuffer(frameRectBuf);
            Rectangle2D rect = fb.getRect();
            if (rect.getX() != 0 || rect.getY() != 0)
                return null;

            return new Photo(photoFile,
                    (int)(rect.getWidth() / ShowGenerator.TWIPS_PER_PIXEL),
                    (int)(rect.getHeight() / ShowGenerator.TWIPS_PER_PIXEL),
                    true);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Returns a sorted array of all supported image file suffixes
     */
    public List<String> getSupportedFileSuffixes() {
        if (IMAGE_SUFFIXES != null)
            return IMAGE_SUFFIXES;

        // Build a list of all supported file suffixes
        ArrayList<String> suffixList = new ArrayList<String>();
        Iterator<ImageReaderSpi> iter = IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, false);
        while (iter.hasNext()) {
            ImageReaderSpi spi = iter.next();
            String[] suffixes = spi.getFileSuffixes();
            for (int i = 0; i  < suffixes.length; i++) {
                if (suffixes[i].length() > 0)
                    suffixList.add("." + suffixes[i].toLowerCase());
            }
        }

        // Sort and remove duplicates
        Collections.sort(suffixList);
        String prev = null;
        ListIterator<String> iterSuffixes = suffixList.listIterator();
        while (iterSuffixes.hasNext()) {
            String suffix = iterSuffixes.next();
            if (suffix.equals(prev))
                iterSuffixes.remove();
            prev = suffix;
        }
        IMAGE_SUFFIXES = Collections.unmodifiableList(suffixList);
        return IMAGE_SUFFIXES;
    }

    /**
     * Dispose of the reader (required to release native resources).
     * Close the ImageInputStream associated with the reader
     * http://developer.java.sun.com/developer/bugParade/bugs/4697126.html
     */
    private void dispose(ImageReader reader) {
        if (reader == null)
            return;

        // Close the stream before calling dispose to free native resources
        Object input = reader.getInput();
        if (input != null && input instanceof ImageInputStream) {
            try {
                ((ImageInputStream)input).close();
            } catch (IOException e) {
                // Ignore
            }
        }

        reader.dispose();
    }

    /**
     * Dispose of the writer (required to release native resources).
     * Close the ImageOutputStream associated with the writer
     * http://developer.java.sun.com/developer/bugParade/bugs/4697126.html
     */
    private void dispose(ImageWriter writer) {
        if (writer == null)
            return;

        // Close the stream before calling dispose to free native resources
        Object output = writer.getOutput();
        if (output != null && output instanceof ImageOutputStream) {
            try {
                ((ImageOutputStream)output).close();
            } catch (IOException e) {
                // Ignore
            }
        }

        writer.dispose();
    }

    /**
     * Choose an ImageReader, giving preference to JAI readers.
     * Caller must close the ImageInputStream associated with the reader
     * @param classPrefix Implementation class prefix - null to use first available
     */
    private ImageReader getImageReader(File imageFile, String classPrefix, boolean seekForwardOnly, boolean ignoreMetadata) throws IOException {
        ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
        if (iis == null)
            return null;
        ImageReader reader = (ImageReader)getReaderWriter(ImageIO.getImageReaders(iis), classPrefix);
        if (reader == null)
            return null;
        reader.setInput(iis, seekForwardOnly, ignoreMetadata);
        return reader;
    }

    /**
     * Choose an ImageWriter, giving preference to JAI readers.
     */
    private ImageWriter getImageWriter(String formatName, String classPrefix) {
        return (ImageWriter)getReaderWriter(ImageIO.getImageWritersByFormatName(formatName), classPrefix);
    }

    private Object getReaderWriter(Iterator iter, String classPrefix) {
        Object firstRW = null;
        while (iter.hasNext()) {
            Object rw = iter.next();
            // Return the first one found if we don't care
            if (classPrefix == null)
                return rw;
            // Return it if we found what we wanted
            if (rw.getClass().getName().startsWith(classPrefix)) {
                // Dispose of the first one since we won't use it
                if (firstRW != null)
                    disposeReaderWriter(firstRW);
                return rw;
            }
            // Save the first one in case we don't find one we want
            if (firstRW == null)
                firstRW = rw;
            // If we aren't using this one, we must dispose of it
            else
                disposeReaderWriter(rw);
        }
        return firstRW;
    }

    private void disposeReaderWriter(Object rw) {
        if (rw instanceof ImageReader)
            ((ImageReader)rw).dispose();
        else
            ((ImageWriter)rw).dispose();
    }
}
