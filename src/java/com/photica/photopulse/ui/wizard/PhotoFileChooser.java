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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Manage a dialog for selecting photo files or a directory of photos
 */
public class PhotoFileChooser {

    public static class Transfer {
        private File directory;
        private File[] selectedFiles;

        public File getDirectory() {
            return directory;
        }

        public void setDirectory(File directory) {
            this.directory = directory;
        }

        // Not public
        void setSelectedFiles(File[] selectedFiles) {
            this.selectedFiles = selectedFiles;
        }

        public File[] getSelectedFiles() {
            return selectedFiles;
        }
    }


    private JFileChooser fileChooser;
    private ThumbnailAccessory thumbnail;
    private JCheckBox thumbnailToggle;

    public PhotoFileChooser() {
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_IMAGECHOOSER_TITLE));
        fileChooser.setMultiSelectionEnabled(true);
        WizardFileFilter imageFilter = new WizardFileFilter(WizardFileFilter.FILTER_IMAGES);
        fileChooser.addChoosableFileFilter(imageFilter);
        fileChooser.addChoosableFileFilter(new WizardFileFilter(WizardFileFilter.FILTER_FLASHPHOTOS));
        fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new FilterChangeHandler());
        fileChooser.setAccessory(buildOpenAccessoryUI());
        fileChooser.setFileFilter(imageFilter);
    }



    public boolean showModalDialog(Component parent, Transfer transfer) {

        fileChooser.setCurrentDirectory(transfer.getDirectory());

        // Dialog opens too wide if many files are in the textfield, so deselect
        fileChooser.setSelectedFiles(null);

        if (fileChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION)
            return false;

        File[] selectedFiles = fileChooser.getSelectedFiles();
        File directory = fileChooser.getCurrentDirectory();

        // If the user chose Open All, then selectedFiles will be null (set by OpenAllHandler)
        // So we choose all files in the directory
        if (selectedFiles == null || selectedFiles.length == 0) {
            selectedFiles = openAllFiles(fileChooser.getFileFilter(), directory);
        }
        // If only one file selected, prompt to select all in directory
        else if (selectedFiles.length == 1) {
            int nChoice = ErrorDialog.showConfirmDialog(parent,
                    UIMessages.getMessage(UIMessages.ERR_CONFIRM_ONE),
                    UIMessages.getMessage(UIMessages.UI_CONFIRMONE_TITLE),
                    ErrorDialog.YES_NO_OPTION);
            if (nChoice == ErrorDialog.YES_OPTION) {
                selectedFiles = openAllFiles(fileChooser.getFileFilter(), directory);
            }
        }

        // File.listFiles() returns null if invalid directory
        if (selectedFiles == null) {
            ErrorDialog.showErrorDialog(parent,
                com.photica.photopulse.SystemMessages.getMessage(com.photica.photopulse.SystemMessages.ERR_INVALIDDIRECTORY, directory.toString()));
            return false;
        }

        // File implements Comparable, so hopefully this sorts based on filesystem order
        Arrays.sort(selectedFiles);

        // Transfer data back to caller
        transfer.setSelectedFiles(selectedFiles);
        transfer.setDirectory(directory);

        return true;
    }

    // Build open filechooser accessory panel
    private JComponent buildOpenAccessoryUI() {
        // Image file chooser "open all" accessory and thumbnail viewer
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        // Instructions on how to perform multi-selection
        JLabel instructLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_SELECTION));

        // "Open all" button
        JButton openAllButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_OPENALL));
        openAllButton.addActionListener(new OpenAllHandler());

        // Image preview thumbnail
        thumbnail = new ThumbnailAccessory(fileChooser);
        thumbnail.setBorder(BorderFactory.createEtchedBorder());

        // Checkbox to enable/disable thumbnail
        thumbnailToggle = new JCheckBox(UIMessages.getMessage(UIMessages.UI_TOGGLE_THUMBNAIL), true);
        thumbnailToggle.addItemListener(new ThumbnailHandler());

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(instructLabel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = 5;
        panel.add(openAllButton, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.top = 15;
        panel.add(thumbnailToggle, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.insets.top = 2;
        gbc.weighty = 1;
        panel.add(thumbnail, gbc);

        return panel;
    }

    private File[] openAllFiles(FileFilter filter, File directory) {
        FilenameFilter ff = filter instanceof FilenameFilter ? (FilenameFilter)filter : new AllFilesFilter();
        return directory.listFiles(ff);
    }

    /**
     * Handle file filter change
     */
    private class FilterChangeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            Object filter = e.getNewValue();
            boolean enablePreview = filter instanceof WizardFileFilter && ((WizardFileFilter)filter).getFilterType() == WizardFileFilter.FILTER_IMAGES;
            thumbnail.setActive(enablePreview);
            thumbnailToggle.setEnabled(enablePreview);
            thumbnailToggle.setSelected(enablePreview);
        }
    }

    private class ThumbnailHandler implements ItemListener {
        public void itemStateChanged(ItemEvent e) {
            thumbnail.setActive(thumbnailToggle.isSelected());
        }
    }

    // Handle "Open All" button
    private class OpenAllHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Set selectedFiles to null so we know to select all files in directory
            fileChooser.setSelectedFiles(null);
            fileChooser.approveSelection();
        }
    }

    private static class AllFilesFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return new File(dir, name).isFile();
        }
    }
}
