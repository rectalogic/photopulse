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

import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.model.ShowModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.plaf.FileChooserUI;
import javax.swing.plaf.basic.BasicFileChooserUI;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;



public class ExportFileChooser {

    public static class Transfer {
        private File directory;
        private File selectedFile;
        private ShowModel.ShowType showType = ShowModel.ShowType.HTM;
        private boolean isHighQuality;

        public File getDirectory() {
            return directory;
        }

        public void setDirectory(File directory) {
            this.directory = directory;
        }

        // Not public
        void setSelectedFile(File selectedFile) {
            this.selectedFile = selectedFile;
        }

        public File getSelectedFile() {
            return selectedFile;
        }

        public ShowModel.ShowType getShowType() {
            return showType;
        }

        public void setShowType(ShowModel.ShowType showType) {
            this.showType = showType;
        }

        public boolean isHighQuality() {
            return isHighQuality;
        }

        public void setHighQuality(boolean highQuality) {
            isHighQuality = highQuality;
        }
    }

    /**
     * Map a file filter to a show type to an (optional) JRadioButton.
     */
    private static class ShowTypeFileFilter extends WizardFileFilter {
        private ShowModel.ShowType showType;
        private JRadioButton radioButton;

        public ShowTypeFileFilter(int filterType, ShowModel.ShowType showType, JRadioButton radioButton) {
            super(filterType);
            this.showType = showType;
            this.radioButton = radioButton;
        }

        public ShowModel.ShowType getShowType() {
            return showType;
        }

        public JRadioButton getRadioButton() {
            return radioButton;
        }
    }

    private ShowTypeFileFilter[] fileFilters = new ShowTypeFileFilter[] {
        new ShowTypeFileFilter(WizardFileFilter.FILTER_HTM, ShowModel.ShowType.HTM,
                new JRadioButton(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_HTM_TOGGLE))),
        new ShowTypeFileFilter(WizardFileFilter.FILTER_WMV, ShowModel.ShowType.WMV,
                new JRadioButton(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_WMV_TOGGLE))),
        new ShowTypeFileFilter(WizardFileFilter.FILTER_EXE, ShowModel.ShowType.EXE,
                new JRadioButton(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_EXE_TOGGLE))),
        new ShowTypeFileFilter(WizardFileFilter.FILTER_SWF, ShowModel.ShowType.SWF, null)
    };

    private JFileChooser fileChooser;
    private JCheckBox highQualityToggle;

    private JRadioButton hiddenRadio;

    public ExportFileChooser() {
        // Export file chooser
        fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_TITLE));
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Select this button to deselect all others in the group
        hiddenRadio = new JRadioButton();

        ButtonGroup group = new ButtonGroup();

        ActionListener radioHandler = new ShowTypeRadioHandler();
        for (int i = 0; i < fileFilters.length; i++) {
            fileChooser.addChoosableFileFilter(fileFilters[i]);
            JRadioButton button = fileFilters[i].getRadioButton();
            if (button != null) {
                button.addActionListener(radioHandler);
                group.add(button);
            }
        }
        group.add(hiddenRadio);

        fileChooser.addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new FileFilterHandler());

        // High quality toggle button
        highQualityToggle = new JCheckBox(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_HIQUALITY));
        highQualityToggle.setToolTipText(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_HIQUALITY_INFO));

        // Layout show type radio buttons
        Box radioPanel = Box.createVerticalBox();
        radioPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_EXPORTCHOOSER_TOGGLE_TITLE)));
        for (int i = 0; i < fileFilters.length; i++) {
            JRadioButton button = fileFilters[i].getRadioButton();
            if (button != null) {
                // Put toggle button in northwest
                button.setHorizontalTextPosition(SwingConstants.RIGHT);
                button.setVerticalTextPosition(SwingConstants.TOP);
                radioPanel.add(button);
            }
        }

        JPanel panel = new JPanel(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weighty = 1;
        gbc.insets.left = gbc.insets.right = 5;
        panel.add(radioPanel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        panel.add(highQualityToggle, gbc);

        fileChooser.setAccessory(panel);
    }


    public boolean showModalDialog(Component parent, Transfer transfer) {

        // If we have an export file then set it as the selected file
        // otherwise use the project directory
        if (transfer.getSelectedFile() != null)
            fileChooser.setSelectedFile(transfer.getSelectedFile());
        else
            fileChooser.setCurrentDirectory(transfer.getDirectory());

        // Set selected file filter based on show type
        ShowModel.ShowType showType = transfer.getShowType();
        for (int i = 0; i < fileFilters.length; i++) {
            if (fileFilters[i].getShowType() == showType) {
                // If filter is new, set it which will invoke listener.
                // Otherwise invoke listener method to sync rest of UI.
                if (fileFilters[i] != fileChooser.getFileFilter())
                    fileChooser.setFileFilter(fileFilters[i]);
                else
                    handleNewFilter(fileFilters[i]);
                break;
            }
        }

        if (fileChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION)
            return false;

        transfer.setDirectory(fileChooser.getCurrentDirectory());

        ShowTypeFileFilter filter = (ShowTypeFileFilter)fileChooser.getFileFilter();

        // Append filename suffix if needed
        transfer.setSelectedFile(filter.applyExtension(fileChooser.getSelectedFile()));

        transfer.setShowType(filter.getShowType());

        // Get users quality choice
        transfer.setHighQuality(highQualityToggle.isSelected());

        return true;
    }

    private void handleNewFilter(ShowTypeFileFilter filter) {
        // Set default value of "high quality" toggle appropriately.
        highQualityToggle.setSelected(ExporterFactory.isExporterHighQuality(filter.getShowType()));

        // Select corresponding radio
        JRadioButton button = filter.getRadioButton();
        if (button != null)
            button.setSelected(true);
        else
            hiddenRadio.setSelected(true);
    }

    /**
     * Handle changing file filter
     */
    private class FileFilterHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // When user selects a filter, the filename field is cleared due to a bug.
            // But not when the filter is selected via the radio buttons.
            // So for the case where it is not cleared, we strip the previous filters
            // suffix and store the basename back in the field.
            // XXX http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4678049

            ShowTypeFileFilter oldFilter = (ShowTypeFileFilter)e.getOldValue();
            if (oldFilter != null) {
                FileChooserUI ui = ((JFileChooser)e.getSource()).getUI();
                if (ui instanceof BasicFileChooserUI) {
                    BasicFileChooserUI bui = (BasicFileChooserUI)ui;
                    String fileName = bui.getFileName();
                    String strippedFileName = oldFilter.stripValidExtension(fileName);
                    if (strippedFileName != fileName)
                        bui.setFileName(strippedFileName);
                }
            }

            handleNewFilter((ShowTypeFileFilter)e.getNewValue());
        }
    }

    /**
     * Handle selection of show type radio buttons
     */
    private class ShowTypeRadioHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Set corresponding filter for this radio button
            for (int i = 0; i < fileFilters.length; i++) {
                if (fileFilters[i].getRadioButton() == e.getSource()) {
                    fileChooser.setFileFilter(fileFilters[i]);
                    break;
                }
            }
        }
    }
}
