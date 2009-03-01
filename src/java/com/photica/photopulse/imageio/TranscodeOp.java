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

import java.awt.image.BufferedImage;

/**
 * Used to filter an image when transcoding - after decoding before encoding.
 */
public interface TranscodeOp {
    /**
     * Filter the image.
     * @param srcImage Image to filter, should not be modified
     * @return Filtered image, or srcImage if no processing needed
     */
    public BufferedImage filter(BufferedImage srcImage);

    /**
     * @return A unique name for this op (e.g. based on classname). Used for caching results.
     */
    public String getOpName();
}
