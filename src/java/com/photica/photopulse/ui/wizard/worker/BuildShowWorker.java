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

import com.iv.flash.util.IVException;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.progress.ProgressReporter;
import com.photica.photopulse.skin.SkinException;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.UIMessages;

import java.awt.Component;
import java.io.IOException;
import java.io.StringWriter;


public class BuildShowWorker extends Worker {
    private ShowModel model;
    private ShowExporter exporter;
    private ImageTranscoder transcoder;
    private MP3Data mp3Data;

    // Special handling for ExportExceptions, they render their own error message
    private ExportException exportException;

    private boolean isCanceled;

    public BuildShowWorker(WorkerProgress progress, ShowModel model,
            ShowExporter exporter, ImageTranscoder transcoder, MP3Data mp3Data) {
        super("BuildShowWorker", progress);

        this.model = model;
        this.exporter = exporter;
        this.transcoder = transcoder;
        this.mp3Data = mp3Data;
    }

    protected void doWork() {
        try {
            StringWriter swLogErrors = new StringWriter();
            if (!ShowGenerator.generate(model, mp3Data, exporter, transcoder, swLogErrors)) {
                String errors = swLogErrors.toString();
                isCanceled = ProgressReporter.isCanceled();
                if (!isCanceled && errors.length() > 0) {
                    setErrorMessages(UIMessages.getMessage(UIMessages.ERR_LOG_MESSAGES, errors));
                    return;
                }
            }
        } catch (SkinException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_BADSKIN, e.getMessage()));
            return;
        } catch (ExportException e) {
            PhotoPulse.logException(e);
            exportException = e;
            return;
        } catch (IVException e) {
            PhotoPulse.logException(e);
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_LOG_MESSAGES, e.getMessage()));
            return;
        } catch (IOException e) {
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_IO_GENERATE, e.getMessage()));
            return;
        } catch (OutOfMemoryError e) {
            System.gc();
            PhotoPulse.logException(e);
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_NOMEMORY, e.getMessage()));
            return;
        }
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public boolean isError() {
        return getErrorMessages() != null || exportException != null;
    }

    public void showErrorDialog(Component parent) {
        if (exportException != null)
            exportException.showErrorDialog(parent);
        else {
            String message = getErrorMessages();
            if (message != null)
                ErrorDialog.showErrorDialog(parent, message);
        }
    }
}
