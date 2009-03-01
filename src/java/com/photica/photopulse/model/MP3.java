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
 
package com.photica.photopulse.model;

import java.io.File;

/**
 * Utility Class to store info about an MP3 File
 * This class is desinged to be immutable. As an immutable class all
 * changes to the list of mp3 files can be easily tracked.
 */

public final class MP3 {
    public MP3(File file, double duration) {
        this.file = file;
        this.duration = duration;
    }

    public File getFile() {
        return file;
    }

    public double getDuration() {
        return duration;
    }

    // See http://www.javaworld.com/javaworld/jw-01-1999/jw-01-object-p2.html
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if ((other != null) && (other.getClass().equals(this.getClass()))) {
            return duration == ((MP3)other).duration && file.equals(((MP3)other).file);
        }
        return false;
    }

    // If we override equals, we must override hashCode
    public int hashCode() {
        return file.hashCode() ^ (int)duration;
    }

    private File file = null;
    private double duration = 0;
}
