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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.skin;

import com.iv.flash.api.FlashFile;

import java.net.URL;

/**
 * Represents a single external reference in a skin (e.g. Flash font *.fft file)
 */
public class SkinExternal {
    // Used as key into skin cache
    private URL externalURL;

    // Loaded skin external
    private Object external;

    public SkinExternal(URL externalURL) {
        this.externalURL = externalURL;
    }

    public void load() throws SkinException {
        if (externalURL != null)
            external = SkinCache.getSkin(externalURL);
    }

    public void unload() {
        external = null;
    }

    // Only valid on a load()ed external
    public void addExternal(FlashFile flashFile) {
        flashFile.addExternalMedia(externalURL.toExternalForm(), external);
    }
}

