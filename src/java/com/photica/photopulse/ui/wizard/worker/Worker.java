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

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.ui.wizard.UIMessages;

import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class Worker extends Thread {
    private WorkerProgress progress;
    private ThreadPriorityAdjuster windowListener;
    private String errors;

    public Worker(String strName, WorkerProgress progress) {
        super(strName);
        this.progress = progress;

        this.windowListener = new ThreadPriorityAdjuster();
        progress.addWindowListener(this.windowListener);
    }

    public void run() {
        try {
            doWork();
        } catch (Throwable e) {
            PhotoPulse.logException(e);
            setErrorMessages(UIMessages.getMessage(UIMessages.ERR_INTERNAL_ERROR,
                    new Object[] { PhotoPulse.EXCEPTION_LOG.getAbsolutePath(), e.getMessage() }));
        } finally {
            SwingUtilities.invokeLater(new Worker.Finisher());
        }
    }

    // Invoked on worker thread
    protected abstract void doWork();

    // Invoked on Swing thread
    protected void doFinish() {
        progress.removeWindowListener(this.windowListener);
        progress.closeProgress();
    }

    public String getErrorMessages() {
        return errors;
    }

    public void setErrorMessages(String strMsg) {
        errors = strMsg;
    }

    private class Finisher implements Runnable {
        public void run() {
            doFinish();
        }
    }

    /**
     * Dynamically lower worker thread priority when window is deactivated.
     */
    private class ThreadPriorityAdjuster extends WindowAdapter {
        private int initialPriority = getPriority();

        public void windowActivated(WindowEvent e) {
            setThreadPriority(initialPriority);
        }

        public void windowDeactivated(WindowEvent e) {
            setThreadPriority(initialPriority/2);
        }

        private void setThreadPriority(int priority) {
            try {
                setPriority(priority);
            } catch (NullPointerException e) {
                // Ignore NPE. It is thrown by Thread.setPriority if the thread has exited.
                // XXX http://developer.java.sun.com/developer/bugParade/bugs/5037861.html
            }
        }
    }
}
