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

import java.io.File;
import java.io.IOException;
import java.awt.Rectangle;
import java.awt.image.AffineTransformOp;

public interface ImageTranscoder {
    // ImageIO format names (ImageReader.getFormatName)
    public static final String FORMAT_JPEG = "jpeg";
    public static final String FORMAT_PNG = "png";
    public static final String FORMAT_GIF = "gif";

    public static final int SCALE_SUBSAMPLE = 100 + AffineTransformOp.TYPE_NEAREST_NEIGHBOR + AffineTransformOp.TYPE_BILINEAR + AffineTransformOp.TYPE_BICUBIC;
    public static final int SCALE_NEAREST_NEIGHBOR = AffineTransformOp.TYPE_NEAREST_NEIGHBOR;
    public static final int SCALE_BILINEAR = AffineTransformOp.TYPE_BILINEAR;
    public static final int SCALE_BICUBIC = AffineTransformOp.TYPE_BICUBIC;

    /**
     * Transcode an image into the target format, applying cropping and scaling.
     * @param sourceImageFile
     * @param targetFormat One of the FORMAT_* constants.
     *   If null, then an appropriate format will be chosen based on the input format.
     * @param cropRect Region to crop from source image
     * @param scaleFactor Scaling factor greater than 0.0, less than or equal to 1.0
     * @param scaleType SCALE_SUBSAMPLE, SCALE_NEAREST_NEIGHBOR, SCALE_BILINEAR
     * @param op Operation to perform on image after decoding but before encoding
     * @param output Destination File, OutputStream etc.
     * @return Decoded source image, or null if transcode fails.
     */
    public DecodedImage transcodeImage(File sourceImageFile, String targetFormat,
            Rectangle cropRect, double scaleFactor, int scaleType, TranscodeOp op, Object output) throws IOException;
}
