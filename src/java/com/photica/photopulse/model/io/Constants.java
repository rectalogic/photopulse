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
 
package com.photica.photopulse.model.io;

public class Constants {
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    /// XML TAGS
    static final String XML_FILE_FORMAT_VERSION="1.2"; // our version
    static final String XML_FILE_FORMAT_VERSION_1_1="1.1"; // old version - 1.0
    static final String XML_FILE_FORMAT_VERSION_1_0="1.0"; // old version - 1.0

    static final String XML_ATTR_FILE_FORMAT_VERSION="fileVersion";

    static final String XML_TAG_SHOW = "show";
    static final String XML_ATTR_BACKGROUND_COLOR = "bgColor";
    static final String XML_ATTR_FRAMERATE = "frameRate";
    static final String XML_ATTR_DEFAULT_EFFECT_DURATION = "defaultEffectDuration";
    static final String XML_ATTR_DEFAULT_TRANSITION_DURATION = "defaultTransitionDuration";
    static final String XML_ATTR_MP3_MODE = "MP3Preference";
    static final String XML_TAG_BEGIN_TRANS_LIST   = "beginTransitionList";
    static final String XML_TAG_EFFECT_LIST   = "effectList";
    static final String XML_TAG_END_TRANS_LIST   = "endTransitionList";
    static final String XML_TAG_SELECTION   ="selection";
    static final String XML_ATTR_EFFECT_OR_TRANS_NAME   ="name";
    static final String XML_ATTR_SHOW_TYPE = "showType";
    static final String XML_TAG_PHOTOEFFECTS_LIST = "photoEffects";
    static final String XML_TAG_PHOTOEFFECT = "photoEffect";
    static final String XML_TAG_EVENT_ARG = "eventArg";
    static final String XML_TAG_PHOTO = "photo";
    static final String XML_ATTR_PHOTO_SCALE = "photoScale";
    static final String XML_ATTR_FILE = "file";
    static final String XML_ATTR_BASE_EFFECT_KEY = "key";
    static final String XML_ATTR_DURATION = "duration";
    static final String XML_ATTR_BEGIN_TRANS_DURATION = "beginTransDuration";
    static final String XML_ATTR_END_TRANS_DURATION = "endTransDuration";
    static final String XML_ATTR_EFFECT_DURATION = "effectTransDuration";
    static final String XML_ATTR_END_TRANS_ON_TOP = "endTransOnTop";
    static final String XML_ATTR_LOCKED = "locked";
    static final String XML_TAG_BEGIN_TRANSITION = "beginTransition";
    static final String XML_TAG_END_TRANSITION = "endTransition";
    static final String XML_TAG_EFFECT = "effect";
    static final String XML_TAG_KEYFRAME = "keyFrame";
    static final String XML_ATTR_START_TIME= "startTime";
    static final String XML_ATTR_ROTATION = "rotation";
    static final String XML_ATTR_SCALE = "scale";
    static final String XML_ATTR_TRANSLATE_X = "translateX";
    static final String XML_ATTR_TRANSLATE_Y = "translateY";
    static final String XML_ATTR_EASING = "easing";
    static final String XML_ATTR_LINEAR = "linear";
    static final String XML_ATTR_ENDSHOW_MODE = "endShowMode";

    static final String XML_ATTR_STUFF = "stuff";
    static final String XML_ATTR_DIRECTION = "direction";

    static final String XML_ATTR_TONE = "tone";

    static final String XML_ATTR_MP3_DIRECTORY = "MP3Dir";
    static final String XML_ATTR_PHOTO_DIRECTORY = "photoDir";
    static final String XML_ATTR_EXPORT_DIRECTORY = "exportDir";
    static final String XML_ATTR_EXPORT_FILE = "exportFile";

    static final String XML_ATTR_WIDTH = "width";
    static final String XML_ATTR_HEIGHT = "height";

    static final String XML_TAG_SKIN = "skin";
    static final String XML_ATTR_URI = "uri";

    static final String XML_TAG_MP3_LIST = "mp3List";
    static final String XML_TAG_MP3 = "mp3";


}
