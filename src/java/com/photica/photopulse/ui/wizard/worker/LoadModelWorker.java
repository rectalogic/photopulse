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
 
package com.photica.photopulse.ui.wizard.worker;

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.io.InvalidModelException;
import com.photica.photopulse.model.io.ModelReader;
import com.photica.photopulse.ui.wizard.UIMessages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * Load and validate a model
 */
public class LoadModelWorker extends Worker {
    private File modelFile;
    private ShowModel model;
    private String warningMessages;

    public LoadModelWorker(WorkerProgress progress, File modelFile) {
        super("LoadModelWorker", progress);
        this.modelFile = modelFile;
    }

    protected void doWork() {
        try {
            ModelReader reader = new ModelReader();
            model = reader.process(modelFile);

            // Warn user if skin invalid
            String invalidSkinName = reader.getInvalidSkinName();
            if (invalidSkinName != null) {
                warningMessages = UIMessages.getMessage(UIMessages.ERR_LOAD_MODEL_INVALIDSKIN, invalidSkinName);
            }

            // Warn user if any photos were invalid
            Iterator iter = reader.invalidPhotoIterator();
            if (iter.hasNext()) {
                StringBuilder fileBuf = new StringBuilder();
                while (iter.hasNext()) {
                    File photoFile = (File)iter.next();
                    fileBuf.append(photoFile.getCanonicalPath());
                    fileBuf.append("\n");
                }
                String msg = UIMessages.getMessage(UIMessages.ERR_LOAD_MODEL_INVALIDPHOTOS, fileBuf.toString());
                if (warningMessages != null)
                    warningMessages += "\n" + msg;
                else
                    warningMessages = msg;
            }
        } catch (FileNotFoundException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_LOAD_MODEL_NOFILE, new Object[] { modelFile.getName(), e.getMessage() } ));
        } catch (IOException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_LOAD_MODEL_IO, new Object[] { modelFile.getName(), e.getMessage() } ));
        } catch (InvalidModelException e) {
            PhotoPulse.logException(e);
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_LOAD_MODEL_FAILED, modelFile.getName()));
        }
    }

    /**
     * @return Model that was loaded
     */
    public ShowModel getModel() {
        return model;
    }

    public String getWarningMessages() {
        return warningMessages;
    }
}
