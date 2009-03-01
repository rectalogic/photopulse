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
 
package com.photica.photopulse.ui.wizard;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.awt.BorderLayout;

public class MP3FileChooser extends JFileChooser {

    private HyperLabel listenLink;

    public MP3FileChooser() {
        setDialogTitle(UIMessages.getMessage(UIMessages.UI_SOUNDCHOOSER_TITLE));
        addChoosableFileFilter(new WizardFileFilter(WizardFileFilter.FILTER_MP3));
        buildAccessory();
    }

    private void buildAccessory() {
        // Display link to play selected file
        listenLink = new HyperLabel(UIMessages.getMessage(UIMessages.UI_SOUNDCHOOSER_LISTENLINK));
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        panel.add(listenLink, BorderLayout.SOUTH);
        setAccessory(panel);
        addPropertyChangeListener(new FileChangeHandler());
    }

    private class FileChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // For multi selection, only respond to selected files property
            if (!MP3FileChooser.this.isMultiSelectionEnabled() && JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                File file = getSelectedFile();
                if (file == null)
                    listenLink.setLink(null);
                else
                    listenLink.setLink(file.getAbsolutePath());
            }
            else if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(e.getPropertyName())) {
                File[] files = getSelectedFiles();
                if (files == null || files.length == 0 || files.length > 1)
                    listenLink.setLink(null);
                else
                    listenLink.setLink(files[0].getAbsolutePath());
            }
        }
    }
}
