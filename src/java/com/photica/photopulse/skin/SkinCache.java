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

package com.photica.photopulse.skin;

import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Script;
import com.iv.flash.api.image.Bitmap;
import com.iv.flash.util.FlashBuffer;
import com.iv.flash.util.IVException;
import com.iv.flash.util.Resource;
import com.photica.photopulse.SystemMessages;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;


public class SkinCache {

    private static HashMap<URL,SoftReference<Object>> CACHE = new HashMap<URL, SoftReference<Object>>();

    /**
     * @return A Bitmap or FlashFile skin
     */
    public static synchronized Object getSkin(URL urlKey) throws SkinException {
        // XXX SoftReferences are very aggressively cleared - they rarely cache anything.
        // We need a different strategy here. Seems to work OK as of JDK1.4.2_01.
        // http://java.sun.com/docs/hotspot/PerformanceFAQ.html#175

        Object objSkin;
        SoftReference<Object> ref = CACHE.get(urlKey);
        if (ref != null) {
            objSkin = ref.get();
            if (objSkin != null)
                return objSkin;
            else
                CACHE.remove(ref);
        }

        try {
            objSkin = loadSkin(urlKey);
            CACHE.put(urlKey, new SoftReference<Object>(objSkin));
            return objSkin;
        } catch (IOException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_LOADSKIN, e.getMessage()));
        } catch (IVException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, e.getMessage()));
        }
    }

    private static Object loadSkin(URL urlKey) throws IOException, IVException {
        InputStream is = null;
        FlashBuffer fb;
        try {
            is = urlKey.openStream();
            fb = new FlashBuffer(is);
        } finally {
            if (is != null)
                is.close();
        }

        // If SWF, parse into FlashFile object
        if ((fb.getUByteAt(0) == 'F' || fb.getUByteAt(0) == 'C')
                && fb.getUByteAt(1) == 'W' && fb.getUByteAt(2) == 'S') {
            FlashFile file = FlashFile.parse(".", fb);
            Script script = file.getMainScript();
            script.resetMain();
            if (file.isTemplate())
                script.removeFileDepGlobalCommands();
            return file;
        }
        else {
            Bitmap bitmap = Bitmap.newBitmap(fb);
            if (bitmap == null)
                throw new IVException(Resource.UNSUPMEDIA, new Object[] { urlKey.getFile() });
            return bitmap;
        }
    }
}