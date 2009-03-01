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
 
package com.photica.photopulse.ui.wizard.worker;

import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.iv.flash.util.IVException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BuildMP3DataWorker extends Worker {
    private File mp3File;
    private float frameRate;
    private MP3Data mp3Data;

    public BuildMP3DataWorker(WorkerProgress progress, File mp3File, float frameRate) {
        super("BuildMP3DataWorker", progress);
        this.mp3File = mp3File;
        this.frameRate = frameRate;
    }

    protected void doWork() {
        try {
            mp3Data = new MP3Data(frameRate, mp3File);
        } catch (IOException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_IO_MP3, e.getMessage()));
            return;
        } catch (URISyntaxException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_INVALID_MP3, e.getMessage()));
            return;
        } catch (IVException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_INVALID_MP3, e.getMessage()));
            return;
        }
    }

    public MP3Data getMP3Data() {
        return mp3Data;
    }
}
