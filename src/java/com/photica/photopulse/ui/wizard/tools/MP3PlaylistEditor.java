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
 
package com.photica.photopulse.ui.wizard.tools;

import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.ui.wizard.BaseDialog;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.MP3FileChooser;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.WizardFileFilter;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.ui.ToolButton;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * MP3 playlist (M3U) editor tool.
 */
public class MP3PlaylistEditor extends BaseDialog {
    private WizardFileFilter m3uFilter = new WizardFileFilter(WizardFileFilter.FILTER_M3U);

    private File mp3Directory;
    private File playlistFile;
    private JFileChooser playlistLoadChooser;
    private JFileChooser playlistSaveChooser;
    private JFileChooser mp3Chooser;
    private JList playList;
    private boolean isDirty = false;

    private JLabel playlistLabel;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;

    public MP3PlaylistEditor(ShowModel model) {
        super(UIMessages.getMessage(UIMessages.UI_M3U_EDITOR_TITLE), true);

        MP3 mp3 = model.getMP3();
        File m3uFile = mp3 != null ? mp3.getFile() : null;
        this.mp3Directory = model.getMP3Directory();

        this.setLayout(new GridBagLayout());

        playlistLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_M3U_NEW));

        playList = new JList(new DefaultListModel());
        playList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        playList.setVisibleRowCount(10);
        playList.setCellRenderer(new CellRenderer());
        // Lock down preferred size so it doesn't vary based on contents
        JScrollPane playListScroll = new JScrollPane(playList);
        final String prototypeCellLabel = "mmmmmmmmmmmmmmmmmmmmmmmm";
        Dimension cellSize = playList.getCellRenderer().getListCellRendererComponent(playList, prototypeCellLabel, 0, false, false).getPreferredSize();
        Dimension listSize = new Dimension(cellSize.width, playList.getPreferredScrollableViewportSize().height);
        playListScroll.setPreferredSize(listSize);

        // Attempt to load m3u if specified
        if (m3uFile != null && m3uFilter.isValidExtension(m3uFile.getName())) {
            try {
                readPlaylist(m3uFile);
            } catch (IOException e) {
            } catch (URISyntaxException e) {
            }
        }

        UIStateHandler stateHandler = new UIStateHandler();
        playList.addListSelectionListener(stateHandler);
        playList.addPropertyChangeListener("model", stateHandler);
        playList.getModel().addListDataListener(stateHandler);

        JPanel controlButtonPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        JButton addButton = new JButton(UIMessages.getMessage(UIMessages.UI_M3U_ADD));
        addButton.addActionListener(new AddMP3Handler());
        controlButtonPanel.add(addButton);
        removeButton = new JButton(UIMessages.getMessage(UIMessages.UI_M3U_REMOVE));
        removeButton.addActionListener(new RemoveMP3Handler());
        controlButtonPanel.add(removeButton);

        JPanel moveButtonPanel = new JPanel(new GridLayout(2, 1, 0, 5));

        moveUpButton = new ToolButton(UIMessages.getMessage(UIMessages.UI_M3U_MOVEUP), new ResourceIcon("resources/up.gif"));
        moveUpButton.setHorizontalAlignment(JButton.LEFT);
        moveUpButton.addActionListener(new MoveMP3UpHandler());
        moveButtonPanel.add(moveUpButton);
        moveDownButton = new ToolButton(UIMessages.getMessage(UIMessages.UI_M3U_MOVEDOWN), new ResourceIcon("resources/down.gif"));
        moveDownButton.setHorizontalAlignment(JButton.LEFT);
        moveDownButton.addActionListener(new MoveMP3DownHandler());
        moveButtonPanel.add(moveDownButton);

        JPanel fileButtonPanel = new JPanel(new GridLayout(1, 3, 5, 0));

        JButton loadButton = new JButton(UIMessages.getMessage(UIMessages.UI_M3U_LOAD));
        loadButton.addActionListener(new OpenPlaylistHandler());
        fileButtonPanel.add(loadButton);
        JButton saveButton = new JButton(UIMessages.getMessage(UIMessages.UI_M3U_SAVE));
        saveButton.addActionListener(new SavePlaylistHandler());
        fileButtonPanel.add(saveButton);

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.left = gbc.insets.top = gbc.insets.right = 5;
        this.add(playlistLabel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridheight = 2;
        gbc.insets.left = gbc.insets.top = gbc.insets.bottom = gbc.insets.right = 5;
        this.add(playListScroll, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = gbc.insets.right = gbc.insets.bottom = 5;
        this.add(controlButtonPanel, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets.top = gbc.insets.right = gbc.insets.bottom = 5;
        this.add(moveButtonPanel, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weightx = 1;
        gbc.insets.left = gbc.insets.right = 5;
        this.add(fileButtonPanel, gbc);

        updateUIState();
    }

    /**
     * m3u may contain relative or absolute URIs to mp3s or other m3us
     * M3U spec see http://hanna.pyxidis.org/tech/m3u.html
     * http://forums.winamp.com/showthread.php?s=dbec47f3a05d10a3a77959f17926d39c&threadid=65772
     * @see com.photica.photopulse.flash.context.MP3Data
     */
    private void readPlaylist(File m3uFile) throws IOException, URISyntaxException {
        DefaultListModel model = new DefaultListModel();

        File m3uDirectory = m3uFile.getParentFile();
        BufferedReader reader = new BufferedReader(new FileReader(m3uFile));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore comments (deals with EXTM3U format files too)
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;

                // The line could be a relative or absolute file, or a URI
                File file = new File(line);
                if (file.isAbsolute() && file.exists()) {
                    model.addElement(file);
                    continue;
                }
                else {
                    // Attempt to resolve file relative to m3u, fall through if fails
                    file = new File(m3uDirectory, line);
                    if (file.exists()) {
                        model.addElement(file);
                        continue;
                    }
                }

                // Fall through from above and treat as URI if it couldn't be parsed as a File.
                // We want to try File first above to avoid filenames like "f%2dck" that contain valid URI escapes.
                URI uri = new URI(line);
                if (uri.isAbsolute())
                    model.addElement(uri);
                // Fail if not absolute, if relative then it should have been accepted as a relative File above.
                else
                    throw new URISyntaxException(line, "URI must be absolute");
            }

            playList.setModel(model);
            setPlaylistFile(m3uFile);
        } finally {
            reader.close();
        }
    }

    private void loadPlaylist() {
        if (playlistLoadChooser == null) {
            playlistLoadChooser = new JFileChooser();
            playlistLoadChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_M3U_CHOOSER_TITLE));
            playlistLoadChooser.addChoosableFileFilter(m3uFilter);
            playlistLoadChooser.setAcceptAllFileFilterUsed(false);
        }

        playlistLoadChooser.setCurrentDirectory(mp3Directory);
        playlistLoadChooser.setSelectedFile(playlistFile);

        if (playlistLoadChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File file = playlistLoadChooser.getSelectedFile();
        try {
            readPlaylist(file);
            mp3Directory = playlistLoadChooser.getCurrentDirectory();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_M3U_LOAD_IO, new Object[] { file.getName(), e.getMessage() } ));
            return;
        } catch (URISyntaxException e) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_M3U_LOAD_ERR, new Object[] { file.getName() } ));
            return;
        }
    }

    private void writePlaylist(File m3uFile) throws IOException {
        String parentPath = m3uFile.getParentFile().getCanonicalPath();
        if (!parentPath.endsWith(File.separator))
            parentPath += File.separator;
        BufferedWriter writer = new BufferedWriter(new FileWriter(m3uFile));

        try {
            ListModel model = playList.getModel();
            int size = model.getSize();
            for (int i = 0; i < size; i++) {
                Object o = model.getElementAt(i);
                // Attempt to write files as relative paths to the m3u
                if (o instanceof File) {
                    String filePath = ((File)o).getCanonicalPath();
                    if (filePath.startsWith(parentPath))
                        writer.write(filePath.substring(parentPath.length()));
                    else
                        writer.write(filePath);
                }
                else if (o instanceof URI) {
                    writer.write(((URI)o).toString());
                }
                writer.newLine();
            }

            setPlaylistFile(m3uFile);
            isDirty = false;
        } finally {
            writer.close();
        }
    }

    private void savePlaylist() {
        if (playlistSaveChooser == null) {
            playlistSaveChooser = new JFileChooser();
            playlistSaveChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_M3U_SAVE_TITLE));
            playlistSaveChooser.setAcceptAllFileFilterUsed(false);;
            playlistSaveChooser.addChoosableFileFilter(m3uFilter);
        }

        playlistSaveChooser.setCurrentDirectory(mp3Directory);
        playlistSaveChooser.setSelectedFile(playlistFile);

        if (playlistSaveChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File file = m3uFilter.applyExtension(playlistSaveChooser.getSelectedFile());
        try {
            writePlaylist(file);
            mp3Directory = playlistSaveChooser.getCurrentDirectory();
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_M3U_SAVE_IO, new Object[] { file.getName(), e.getMessage() } ));
            return;
        }
    }

    private void addMP3() {
        if (mp3Chooser == null) {
            mp3Chooser = new MP3FileChooser();
            mp3Chooser.setCurrentDirectory(mp3Directory);
            mp3Chooser.setMultiSelectionEnabled(true);
        }

        if (mp3Chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        File files[] = mp3Chooser.getSelectedFiles();
        if (files != null && files.length > 0) {
            DefaultListModel model = (DefaultListModel)playList.getModel();
            for (int i = 0; i < files.length; i++)
                model.addElement(files[i]);
        }
        mp3Directory = mp3Chooser.getCurrentDirectory();
    }

    private void removeMP3() {
        int minSelection = playList.getMinSelectionIndex();
        int maxSelection = playList.getMaxSelectionIndex();
        if (minSelection == -1 || maxSelection == -1)
            return;
        DefaultListModel model = (DefaultListModel)playList.getModel();
        model.removeRange(minSelection, maxSelection);
    }

    private void updateUIState() {
        int minSelection = playList.getMinSelectionIndex();
        int maxSelection = playList.getMaxSelectionIndex();

        // Enable if there is a selection not already at the topmost
        moveUpButton.setEnabled(!(minSelection == -1 || minSelection == 0));
        // Enable if there is a selection not already at the bottommost
        moveDownButton.setEnabled(!(maxSelection == -1 || maxSelection == playList.getModel().getSize() - 1));
        // Enable if any selection
        removeButton.setEnabled(!(minSelection == -1 || maxSelection == -1));
    }

    private void setPlaylistFile(File file) {
        playlistFile = file;
        playlistLabel.setText(file.getName());
        playlistLabel.setToolTipText(file.getAbsolutePath());
    }

    protected boolean confirmOK() {
        // Prompt to save playlist if dirty and not empty
        if (isDirty && playList.getModel().getSize() > 0) {
            int choice = ErrorDialog.showConfirmDialog(this,
                    UIMessages.getMessage(UIMessages.ERR_M3U_CONFIRMSAVE),
                    UIMessages.getMessage(UIMessages.UI_M3U_CONFIRMSAVE_TITLE),
                    ErrorDialog.YES_NO_CANCEL_OPTION);
            if (choice == ErrorDialog.YES_OPTION)
                savePlaylist();
            // Do not close if user canceled
            else if (choice == ErrorDialog.CLOSED_OPTION || choice == ErrorDialog.CANCEL_OPTION)
                return false;
        }

        return true;
    }

    public File showEditor(Frame owner) {
        if (showDialog(owner))
            return playlistFile;
        else
            return null;
    }

    /**
     * Add MP3s to playlist
     */
    private class AddMP3Handler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            addMP3();
        }
    }

    /**
     * Remove selected MP3s from playlist
     */
    private class RemoveMP3Handler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            removeMP3();
        }
    }

    /**
     * Move selected contiguous mp3s up in list
     */
    private class MoveMP3UpHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int minSelection = playList.getMinSelectionIndex();
            int maxSelection = playList.getMaxSelectionIndex();
            if (minSelection == 0 || minSelection == -1 || maxSelection == -1)
                return;
            DefaultListModel model = (DefaultListModel)playList.getModel();
            Object mp3 = model.remove(minSelection - 1);
            model.add(maxSelection, mp3);
            playList.setSelectionInterval(minSelection - 1, maxSelection - 1);
            playList.ensureIndexIsVisible(minSelection - 1);
        }
    }

    /**
     * Move selected contiguous mp3s down in list
     */
    private class MoveMP3DownHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            DefaultListModel model = (DefaultListModel)playList.getModel();
            int minSelection = playList.getMinSelectionIndex();
            int maxSelection = playList.getMaxSelectionIndex();
            if (minSelection == -1 || maxSelection == -1 || maxSelection == model.getSize() - 1)
                return;

            Object mp3 = model.remove(maxSelection + 1);
            model.add(minSelection, mp3);
            playList.setSelectionInterval(minSelection + 1, maxSelection + 1);
            playList.ensureIndexIsVisible(maxSelection + 1);
        }
    }

    private class OpenPlaylistHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            loadPlaylist();
        }
    }

    private class SavePlaylistHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            savePlaylist();
        }
    }

    /**
     * Update button states based on list selection and contents.
     */
    private class UIStateHandler implements ListSelectionListener, ListDataListener, PropertyChangeListener {
        public void valueChanged(ListSelectionEvent e) {
            updateUIState();
        }
        public void contentsChanged(ListDataEvent e) {
            isDirty = true;
        }
        public void intervalAdded(ListDataEvent e) {
            isDirty = true;
        }
        public void intervalRemoved(ListDataEvent e) {
            isDirty = true;
        }
        public void propertyChange(PropertyChangeEvent evt) {
            isDirty = false;
            // Listen to the new list model
            playList.getModel().addListDataListener(this);
        }
    }

    private static class CellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof File) {
                File file = (File)value;
                value = file.getName();
                setToolTipText(file.getAbsolutePath());
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    /*
    public static void main(String[] args) {
        ShowModel model = new ShowModel();
        model.setMP3Directory(new File("c:\\temp\\m3u"));
        model.setMP3(new MP3(new File("c:\\temp\\m3u\\test.m3u"), 20));
        System.out.println(new MP3PlaylistEditor(model).showEditor(new JFrame()));
        System.exit(0);
    }
    */
}
