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

package com.photica.photopulse;

import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SystemMessages extends ListResourceBundle {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(SystemMessages.class.getName());

    // Message IDs.
    // Using the same name of the identifier as the string saves space
    // in the class file.
    public static final String ERR_MKDIRFAILED = "ERR_MKDIRFAILED";
    public static final String ERR_INVALIDDIRECTORY = "ERR_INVALIDDIRECTORY";
    public static final String ERR_NO_PHOTOCONTEXT = "ERR_NO_PHOTOCONTEXT";
    public static final String ERR_NO_GRAPHCONTEXT = "ERR_NO_GRAPHCONTEXT";
    public static final String ERR_NO_IMAGEPARAM = "ERR_NO_IMAGEPARAM";
    public static final String ERR_INVALID_TRANSITION = "ERR_INVALID_TRANSITION";
    public static final String ERR_NO_PHOTOS = "ERR_NO_PHOTOS";
    public static final String ERR_OOM_PHOTO = "ERR_OOM_PHOTO";
    public static final String ERR_BADSKIN = "ERR_BADSKIN";
    public static final String ERR_LOADSKIN = "ERR_LOADSKIN";
    public static final String ERR_MAXFRAMES = "ERR_MAXFRAMES";
    public static final String ERR_MP3LENGTH = "ERR_MP3LENGTH";
    public static final String ERR_WMV_NOTINSTALLED = "ERR_WMV_NOTINSTALLED";
    public static final String ERR_WMV_FLASHVERSION = "ERR_WMV_FLASHVERSION";
    public static final String ERR_WMV_EXPORT = "ERR_WMV_EXPORT";
    public static final String ERR_WMV_LOWVM = "ERR_WMV_LOWVM";
    public static final String ERR_WMV_BADMP3 = "ERR_WMV_BADMP3";
    public static final String ERR_IMAGE_DECODE = "ERR_IMAGE_DECODE";
    public static final String ERR_FLASHPLAYER = "ERR_FLASHPLAYER";

    public static final String UI_FILTER_IMAGES = "UI_FILTER_IMAGES";
    public static final String UI_BUILTIN_SKINSET = "UI_BUILTIN_SKINSET";
    public static final String UI_BUILTIN_SKINSET_DESC = "UI_BUILTIN_SKINSET_DESC";
    public static final String UI_SKIN_NAME_SMALL = "UI_SKIN_NAME_SMALL";
    public static final String UI_SKIN_NAME_LARGE = "UI_SKIN_NAME_LARGE";
    public static final String UI_SKIN_NAME_SMALL_PORTRAIT = "UI_SKIN_NAME_SMALL_PORTRAIT";
    public static final String UI_SKIN_NAME_LARGE_PORTRAIT = "UI_SKIN_NAME_LARGE_PORTRAIT";
    public static final String UI_SKIN_NAME_SMALL_LANDSCAPE = "UI_SKIN_NAME_SMALL_LANDSCAPE";
    public static final String UI_SKIN_NAME_LARGE_LANDSCAPE = "UI_SKIN_NAME_LARGE_LANDSCAPE";

    public static final String SWF_SNIFF_URL = "SWF_SNIFF_URL";

    private static final Object[][] contents = {
        { ERR_MKDIRFAILED,
            "Failed to create directory {0}." },
        { ERR_INVALIDDIRECTORY,
            "Invalid directory {0}." },
        { ERR_NO_PHOTOCONTEXT,
            "Unable to find PhotoContext." },
        { ERR_NO_GRAPHCONTEXT,
            "Unable to find GraphContext." },
        { ERR_NO_IMAGEPARAM,
            "Image parameter not specified." },
        { ERR_INVALID_TRANSITION,
            "Invalid transition - {0}." },
        { ERR_NO_PHOTOS,
            "No photos specified." },
        { ERR_OOM_PHOTO,
            "The photo file \"{0}\" is too large to load into memory. It may be possible to load the photo if it is resized to smaller dimensions in an image editor." },
        { ERR_BADSKIN,
            "The specified skin is not in a valid skin format ({0})." },
        { ERR_LOADSKIN,
            "Unable to read the specified skin file ({0})." },
        { ERR_MAXFRAMES,
            "The show duration is too long ({0} frames, maximum is {1})." },
        { ERR_MP3LENGTH,
            "The MP3 is too long ({0} minutes, maximum length is {1} minutes). Reducing the frame rate of your show may allow longer MP3s to be used." },
        { ERR_IMAGE_DECODE,
            "Failed to decode image {0}." },
        { ERR_FLASHPLAYER,
            "Failed to load Flash preview player." },

        { ERR_WMV_NOTINSTALLED,
            "Microsoft Windows Media version 9 or higher must be installed to use this feature." },
        { ERR_WMV_FLASHVERSION,
            "Macromedia Flash ActiveX control version 5 or higher must be installed to use this feature." },
        { ERR_WMV_EXPORT,
            "Failed to create the show due to a Windows Media error." },
        { ERR_WMV_LOWVM,
            "Failed to create the show due to a Windows Media error. Increasing your systems virtual memory may help prevent this error." },
        { ERR_WMV_BADMP3,
            "MP3s used to create a video must be local files. This MP3 cannot be used:\n{0}" },

        { UI_FILTER_IMAGES,
            "Image ({0})" },
        { UI_BUILTIN_SKINSET,
            "None" },
        { UI_BUILTIN_SKINSET_DESC,
            "No theme. Provides a set of common sizes, along with the ability to specify your own custom size." },

        { UI_SKIN_NAME_SMALL,
            "Small ({0,number,#} X {1,number,#})" },
        { UI_SKIN_NAME_LARGE,
            "Large ({0,number,#} X {1,number,#})" },
        { UI_SKIN_NAME_SMALL_PORTRAIT,
            "Small portrait ({0,number,#} X {1,number,#})" },
        { UI_SKIN_NAME_LARGE_PORTRAIT,
            "Large portrait ({0,number,#} X {1,number,#})" },
        { UI_SKIN_NAME_SMALL_LANDSCAPE,
            "Small landscape ({0,number,#} X {1,number,#})" },
        { UI_SKIN_NAME_LARGE_LANDSCAPE,
            "Large landscape ({0,number,#} X {1,number,#})" },

        { SWF_SNIFF_URL,
            "javascript:\"Flash 4 or higher required. <a href='http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash' target='_blank'>Download now</a>.\"" },

    };

    // Overrides ListResourceBundle
    protected Object[][] getContents() {
        return contents;
    }

    public static ResourceBundle getBundle() {
        return RESOURCE_BUNDLE;
    }

    public static String getMessage(String key) throws MissingResourceException {
        return RESOURCE_BUNDLE.getString(key);
    }

    public static String getMessage(String key, Object ... arguments) throws MissingResourceException {
        return MessageFormat.format(getMessage(key), arguments);
    }
}
