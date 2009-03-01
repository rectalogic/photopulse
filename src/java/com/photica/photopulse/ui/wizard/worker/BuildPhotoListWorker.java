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

import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.model.Photo;

import java.io.File;
import java.util.List;

public class BuildPhotoListWorker extends Worker {
    private File[] photoFiles;
    private List<Photo> photoList;

    public BuildPhotoListWorker(WorkerProgress progress, File[] photoFiles) {
        super("BuildPhotoListWorker", progress);
        this.photoFiles = photoFiles;
    }

    protected void doWork() {
        photoList = ImageCoder.getInstance().validatePhotoFiles(photoFiles);

        if (photoList.size() == 0) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_INVALID_IMAGES));
            return;
        }
    }

    /**
     * @return List of Photo objects
     */
    public List<Photo> getPhotoList() {
        return photoList;
    }
}
