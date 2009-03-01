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

package com.photica.photopulse.imageio.cache;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.DataOutput;

class MetaData {

        private byte version = 1;
        private long srcLastModified = 0L;
        private long thumbLastAccessed = 0L;


        private static final byte currentVersion = 1;
        private static final long sizeofByte = 1;
        private static final long sizeofLong = 8;
        private static final long versionOffset = sizeofByte;
        private static final long lastAccessedOffset = versionOffset + sizeofLong;
        private static final long srcLastModifiedOffset = lastAccessedOffset + sizeofLong;


        long getSrcLastModified() {
            return srcLastModified;
        }
        public String toString() {
            return
                    "Version:"+ version +
                    " srcLastModified:" + srcLastModified +
                    " thumbLastAccessed:" + thumbLastAccessed;
        }


        MetaData loadMetaData(RandomAccessFile raf ) throws MetaDataVersionException, IOException {
            // check the version
            long sz = raf.length();
            raf.seek(sz-versionOffset);
            byte fileVersion = (byte)raf.read();
            if( currentVersion != fileVersion) throw new MetaDataVersionException("Incompatible Metadata Version expect " + this.version + " got " + fileVersion);
            raf.seek(sz - srcLastModifiedOffset);
            long srcLastModified = raf.readLong();
            long thumbLastAccessed = raf.readLong();

            return new MetaData( srcLastModified, thumbLastAccessed);

        }
        public MetaData (long srcLastModified, long thumbLastAccessed) {
            this.srcLastModified = srcLastModified;
            this.thumbLastAccessed = thumbLastAccessed;
        }
        public void writeMetaData(RandomAccessFile raf) throws IOException {
            long sz = raf.length();
            raf.seek(sz-srcLastModifiedOffset);
            appendMetaData(raf);
        }
        public void appendMetaData (DataOutput dout )throws IOException {
            dout.writeLong(srcLastModified);
            dout.writeLong(thumbLastAccessed);
            dout.write(version); // last - well defined location....
        }
        void setThumbLastAccessed(RandomAccessFile raf) throws IOException {
            long  sz = raf.length();
            raf.seek(sz - versionOffset);
            byte version = (byte)raf.read();
            if( this.version != version) {
                throw new IOException("Incompatible MetaData Version");
            }
            raf.seek(sz-lastAccessedOffset);
            raf.writeLong(System.currentTimeMillis());
        }

    }