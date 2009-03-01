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

package com.photica.photopulse.ui.console;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ConsoleMessages extends ListResourceBundle {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(ConsoleMessages.class.getName());

    // Message IDs.
    // Using the same name of the identifier as the string saves space
    // in the class file.
    public static final String USAGE = "USAGE";
    public static final String INVALID_ARG = "INVALID_ARG";
    public static final String NO_OUTPUT = "NO_OUTPUT";
    public static final String IVEXCEPTION = "IVEXCEPTION";
    public static final String IOEXCEPTION = "IOEXCEPTION";
    public static final String LOGMESSAGES = "LOGMESSAGES";
    public static final String INVALID_MP3 = "INVALID_MP3";
    public static final String INVALID_MODEL = "INVALID_MODEL";
    public static final String NO_IMAGEDIR = "NO_IMAGEDIR";
    public static final String NO_IMAGES = "NO_IMAGES";
    public static final String INVALID_SKIN = "INVALID_SKIN";

    public static final String UIERRORTITLE = "UIERRORTITLE";

    private static final Object[][] contents = {
        { USAGE,
            "Usage: PhotoPulse [-model <model.xml>] [-dump <dump.xml>] (-exe <file.exe> | -swf <file.swf> | -htm <file.htm> | -mht <file.mht>) -imagedir <directory> [-mp3 <file.mp3>] [-framerate <integer>] [-bgcolor <color>] [-skin <uri>] [-effectSpeed <double>] [-transSpeed <double>]" },
        { INVALID_ARG,
            "Invalid argument, {0}" },
        { NO_OUTPUT,
            "No output format was specified" },
        { IVEXCEPTION,
            "Flash generation failed:\n{0}" },
        { IOEXCEPTION,
            "File access error:\n{0}" },
        { LOGMESSAGES,
            "Flash conversion failed:\n{0}" },
        { INVALID_MP3,
            "Invalid MP3 sound file:\n{0}" },
        { INVALID_MODEL,
            "Failed to process specified model file {0}:\n{1}" },
        { NO_IMAGEDIR,
            "Must specify an image directory to use XML Wizard" },
        { NO_IMAGES,
            "No valid images in directory" },
        { INVALID_SKIN,
            "Failed to load skin.\n{0}" },

        { UIERRORTITLE,
            "PhotoPulse Error" },

    };

    // Overrides ListResourceBundle
    protected Object[][] getContents() {
        return contents;
    }

    public static String getMessage(String key) throws MissingResourceException {
        return RESOURCE_BUNDLE.getString(key);
    }

    public static String getMessage(String key, Object ... arguments) throws MissingResourceException {
        return MessageFormat.format(getMessage(key), arguments);
    }
}
