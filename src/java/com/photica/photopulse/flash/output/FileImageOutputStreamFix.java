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

import javax.imageio.stream.FileImageOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;

/**
 * FileImageOutputStream subclass that properly initializes streamPos to the
 * current position in the RAF it is handed.
 * XXX http://developer.java.sun.com/developer/bugParade/bugs/5043343.html
 */
public class FileImageOutputStreamFix extends FileImageOutputStream {
    public FileImageOutputStreamFix(RandomAccessFile raf) throws IOException {
        super(raf);
        streamPos = raf.getFilePointer();
    }
}
