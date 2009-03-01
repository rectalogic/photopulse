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

package com.photica.photopulse.ui.wizard;

import com.photica.photopulse.ui.wizard.worker.WorkerProgress;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class IndeterminateProgress extends JDialog implements WorkerProgress {

    public IndeterminateProgress(JDialog parent) {
        super(parent, UIMessages.getMessage(UIMessages.UI_PROGRESS_IND_TITLE), true);
        init(parent);
    }

    public IndeterminateProgress(JFrame parent) {
        super(parent, UIMessages.getMessage(UIMessages.UI_PROGRESS_IND_TITLE), true);
        init(parent);
    }

    private void init(Window parent) {
        // Make sure the user can't close the dialog
        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JProgressBar progress = new JProgressBar();
        Dimension dimSize = progress.getPreferredSize();
        dimSize.width = 200;
        progress.setPreferredSize(dimSize);
        progress.setIndeterminate(true);

        Container pane = this.getContentPane();
        pane.setLayout(new GridLayout(1, 1, 5, 5));
        pane.add(progress);
        this.pack();
        this.setLocationRelativeTo(parent);
    }

    /**
     * Display progress dialog, must be invoked from Swing thread.
     * This method blocks until closeProgress() is called
     */
    // Implements WorkerProgress
    public void showProgress() {
        // Avoid race condition in case closeProgress was already called
        if (this.isDisplayable())
            this.setVisible(true);
    }

    /**
     * Unblock and hide the progress dialog.
     * Must be invoked from Swing thread.
     */
    // Implements WorkerProgress
    public void closeProgress() {
        // Dispose so isDisplayable returns false
        this.dispose();
    }
}
