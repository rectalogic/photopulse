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

import com.photica.photopulse.Branding;
import com.photica.photopulse.License;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.io.InvalidModelException;
import com.photica.photopulse.model.io.ModelWriter;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.ui.wizard.expert.ExpertPanel;
import com.photica.photopulse.ui.wizard.settings.ShowSettingsPanel;
import com.photica.photopulse.ui.wizard.tools.MP3PlaylistEditor;
import com.photica.photopulse.ui.wizard.tools.Tool;
import com.photica.photopulse.ui.wizard.tools.ToolAction;
import com.photica.photopulse.ui.wizard.tools.ToolLauncher;
import com.photica.photopulse.ui.wizard.tools.ToolRegistry;
import com.photica.photopulse.ui.wizard.tools.UploadTool;
import com.photica.photopulse.ui.wizard.worker.BuildMP3DataWorker;
import com.photica.photopulse.ui.wizard.worker.BuildPhotoListWorker;
import com.photica.photopulse.ui.wizard.worker.BuildShowWorker;
import com.photica.photopulse.ui.wizard.worker.LoadModelWorker;
import com.photica.photopulse.ui.wizard.worker.WorkerProgress;
import com.photica.photopulse.wizard.Wizard;
import com.photica.ui.JMenuItemFix;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class WizardUI extends JFrame {

    public static final String EXPERT_MODE_PROPERTY = "expertMode";
    public static final String SHOWMODEL_PROPERTY = "showModel";

    // Singleton instance
    private static WizardUI INSTANCE;

    private ShowModel model;

    private WizardPrefs prefs;

    private ExpertPanel expertPanel;

    // Cache the MP3Data corresponding to the MP3 in the model
    private MP3Data mp3Data = null;

    private File projectFile;

    private WizardList beginTransitionWizardList;
    private WizardList effectsWizardList;
    private WizardList endTransitionWizardList;

    private HyperLabel exportFileLink;
    private JButton openMP3Button;
    private Action exportAction;

    private List<ToolAction> toolActionList;
    private UploadTool uploadTool;

    private PhotoFileChooser photoFileChooser = null;
    private MP3FileChooser mp3FileChooser = null;
    private ExportFileChooser exportFileChooser = null;

    private boolean isExpertMode = false;

    private JTabbedPane tabbedPane;

    // Opaque identifiers for the different tab panels
    private Component tabPhotos;
    private Component tabBeginTransitions;
    private Component tabEffects;
    private Component tabEndTransitions;

    private WizardUI() {
        super(UIMessages.getMessage(UIMessages.UI_FRAME_TITLE, UIMessages.getMessage(UIMessages.UI_FRAME_UNTITLED)));

        this.prefs = new WizardPrefs();

        this.uploadTool = findUploadTool();

        // Initialize from prefs
        ShowModel initialModel = createModelFromPrefs();
        setExpertMode(prefs.isExpertMode());

        // Build main Wizard UI
        buildWizardUI();

        setShowModel(initialModel);

        displayUI();

        showStartupDialogs();
    }

    private WizardUI(File[] photoFiles) {
        this();
        loadInitialPhotoFiles(photoFiles);
    }

    private WizardUI(File projectFile) {
        this();
        if (projectFile != null)
            loadModel(projectFile);
    }

    // Init with list of photos
    public static void init(File[] photoFiles) {
        if (INSTANCE == null)
            INSTANCE = new WizardUI(photoFiles);
    }

    // Init with project file
    public static void init(File projectFile) {
        if (INSTANCE == null)
            INSTANCE = new WizardUI(projectFile);
    }

    public static WizardUI getInstance() {
        return INSTANCE;
    }

    public void setShowModel(ShowModel model) {
        if (this.model == model)
            return;

        ShowModel oldModel = this.model;
        this.model = model;

        // New model is not yet modified, track new modifications
        if (Branding.ENABLE_PROJECTS) {
            model.setModified(false);
            model.setTrackModifications(true);
        }

        // We will need to revalidate the mp3 (if any)
        mp3Data = null;

        // Update UI with new model
        syncUIToModel();

        firePropertyChange(SHOWMODEL_PROPERTY, oldModel, model);
    }

    public ShowModel getShowModel() {
        return model;
    }

    private void syncUIToModel() {
        // Hook up lists to effect selections
        beginTransitionWizardList.setShowSelectionModel(model.getBeginTransitionSelectionModel());
        effectsWizardList.setShowSelectionModel(model.getEffectSelectionModel());
        endTransitionWizardList.setShowSelectionModel(model.getEndTransitionSelectionModel());

        // If mp3 changed, blow away mp3Data cache
        if (mp3Data != null && model.getMP3() != mp3Data.getMP3())
            mp3Data = null;

        // Update UI state from model
        updateOpenMP3Button();
        handleNewPhotos();
        updateExportFileActions();

        // Listen for property changes and photo list changes
        ShowModelChangeHandler handler = new ShowModelChangeHandler();
        model.addPropertyChangeListener(handler);
        model.getPhotoEffectList().addListDataListener(handler);
    }

    /**
     * Set the UI to wizard or expert mode. Used to determine if the
     * wizard should be run at generate time.
     */
    public void setExpertMode(boolean isExpertMode) {
        if (!Branding.ENABLE_EXPERTMODE)
            isExpertMode = false;

        if (this.isExpertMode == isExpertMode)
            return;

        boolean oldExpertMode = this.isExpertMode;
        this.isExpertMode = isExpertMode;
        firePropertyChange(EXPERT_MODE_PROPERTY, oldExpertMode, this.isExpertMode);
    }

    public boolean isExpertMode() {
        return isExpertMode;
    }

    /**
     * Set the project file and take care of any related
     * synchronization - like updating the title bar.
     * @param newFile
     */
    private void setProjectFile(File newFile) {
        projectFile = newFile;

        // Append the current project file name to the title bar
        String fileName;
        if (projectFile != null) {
            fileName = projectFile.getName();

            // Set the parent directory - prefs.setProjectDirectory accepts a file or
            // dir and converts as necessary so no need to do that work here.
            prefs.setProjectDirectory(projectFile);
        }
        else
            fileName = UIMessages.getMessage(UIMessages.UI_FRAME_UNTITLED);

        setTitle(UIMessages.getMessage(UIMessages.UI_FRAME_TITLE, fileName));
    }

    /**
     * Transfer prefs to new model
     */
    private ShowModel createModelFromPrefs() {
        ShowModel newModel = new ShowModel(prefs.getBeginTransitionSelection(),
                prefs.getEffectSelection(), prefs.getEndTransitionSelection());

        newModel.setMP3Directory(prefs.getMP3Directory());
        newModel.setExportDirectory(prefs.getExportDirectory());
        newModel.setPhotoDirectory(prefs.getPhotoDirectory());

        // Initialize skin from prefs
        URI skinURI = prefs.getSkinURI();
        if (skinURI != null) {
            Skin skin = SkinManager.getSkin(skinURI);
            if (skin != null)
                newModel.setSkin(skin);
        }
        return newModel;
    }

    public void raiseWindow() {
        // Deiconify first
        int state = getExtendedState();
        if ((Frame.ICONIFIED & state) != 0)
            setExtendedState(state & ~Frame.ICONIFIED);

        toFront();
    }

    /**
     * Called when PhotoEffectList in model changes
     */
    private void handleNewPhotos() {
        // Count source photos, not what is in the models list (it includes repeats/truncates)
        int photoCount = model.getPhotoEffectList().size();

        // Enable/disable export action
        if (photoCount > 0)
            exportAction.setEnabled(true);
        else
            exportAction.setEnabled(false);
    }

    /**
     * If the app was invoked with a file of photo filenames, inform the user and load those photos.
     */
    private void loadInitialPhotoFiles(File[] photoFiles) {
        toFront();

        // Inform user of file list loading
        ErrorDialog.showInfoDialog(this, UIMessages.getMessage(UIMessages.ERR_NEW_IMAGELIST));

        // Set the photo directory to the directory of the first photo
        model.setPhotoDirectory(photoFiles[0].getParentFile());

        // Don't sort the images, use in the order given.
        // Load photos and populate model
        loadPhotos(photoFiles);
    }

    private void updateOpenMP3Button() {
        MP3 mp3 = model.getMP3();
        if (mp3 == null)
            openMP3Button.setToolTipText(null);
        // Set button tooltip to MP3 filename
        else {
            String tip = UIMessages.getMessage(UIMessages.UI_TOOLTIP_SOUNDFILE,
                    new Object[] {mp3.getFile().getAbsolutePath(),
                                Util.formatDuration(mp3.getDuration())});
            openMP3Button.setToolTipText(tip);
        }
    }

    /**
     * Update hyperlink to exported file and tools
     */
    private void updateExportFileActions() {
        File exportFile = model.getExportFile();

        // Set label to display and link to saved file
        exportFileLink.setLink(exportFile == null ? null : exportFile.getAbsolutePath());

        // See if tools can handle this show
        for (ToolAction action : toolActionList)
            action.updateAction(model);
    }

    private void showAboutDialog() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setVisible(true);
    }

    // Launch the specified document
    private void launchDocument(String document) {
        if (!HyperLabel.launchDocument(document)) {
            ErrorDialog.showErrorDialog(this,
                    UIMessages.getMessage(UIMessages.ERR_LAUNCH_FAILED, document));
        }
    }

    public String getRegistrationURL() {
        String serial = "trial";
        License license = PhotoPulse.LICENSE;
        if (license != null)
            serial = license.getSerialString();
        return UIMessages.getMessage(UIMessages.URL_REGISTER, serial);
    }

    // Launch registration URL, passing serial
    private void showRegistrationURL() {
        launchDocument(getRegistrationURL());
    }

    private void showQuickstartDialog() {
        QuickstartDialog quickstartDialog = new QuickstartDialog(this);
        quickstartDialog.setLocationRelativeTo(this);
        quickstartDialog.setVisible(true);
    }

    public boolean showLicenseDialog(Component parent) {
        if (parent == null)
            parent = this;
        LicenseDialog dlgLicense = new LicenseDialog();
        boolean result = dlgLicense.showDialog(parent);
        if (result)
            ErrorDialog.showInfoDialog(parent, UIMessages.getMessage(UIMessages.ERR_LICENSE_OK));
        return result;
    }

    /**
     * Show chooser to open a project file
     */
    private void showOpenProjectChooser() {
        // Save current project first
        if (!confirmSaveProject())
            return;

        JFileChooser fileChooser = new JFileChooser(projectFile == null ? prefs.getProjectDirectory() : projectFile.getParentFile());
        fileChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_OPEN_PROJECTCHOOSER_TITLE));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new WizardFileFilter(WizardFileFilter.FILTER_PPP));

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        loadModel(fileChooser.getSelectedFile());
    }

    /**
     * Prompt user to save current project if modified.
     * @return false if user canceled
     */
    private boolean confirmSaveProject() {
        if (Branding.ENABLE_PROJECTS && model.isModified()) {
            int choice = ErrorDialog.showConfirmDialog(this,
                    UIMessages.getMessage(UIMessages.ERR_CONFIRMSAVE),
                    UIMessages.getMessage(UIMessages.UI_CONFIRMSAVE_TITLE),
                    ErrorDialog.YES_NO_CANCEL_OPTION);
            if (choice == ErrorDialog.YES_OPTION)
                saveProject();
            else if (choice == ErrorDialog.CLOSED_OPTION || choice == ErrorDialog.CANCEL_OPTION)
                return false;
        }
        return true;
    }

    /**
     * Show chooser to save a project file
     */
    private void showSaveProjectAsChooser() {
        JFileChooser fileChooser = new JFileChooser(projectFile == null ? prefs.getProjectDirectory() : projectFile.getParentFile());
        fileChooser.setDialogTitle(UIMessages.getMessage(UIMessages.UI_SAVE_PROJECTCHOOSER_TITLE));
        fileChooser.setAcceptAllFileFilterUsed(false);
        WizardFileFilter wff = new WizardFileFilter(WizardFileFilter.FILTER_PPP);
        fileChooser.addChoosableFileFilter(wff);

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        saveModel(wff.applyExtension(fileChooser.getSelectedFile()));
    }

    /**
     * Save project file
     */
    private void saveProject() {
        if (projectFile != null && projectFile.canWrite())
            saveModel(projectFile);
        else
            showSaveProjectAsChooser();
    }

    /**
     * Replace current model with a fresh one
     */
    private void newModel() {
        // Save current project first
        if (!confirmSaveProject())
            return;

        // Copy forward some settings from previous model
        ShowModel newModel = new ShowModel(this.model);
        setShowModel(newModel);
        setProjectFile(null);
    }

    /**
     * Load model from file on a worker thread, resync UI
     */
    private boolean loadModel(File modelFile) {
        if (Branding.ENABLE_PROJECTS) {
            WorkerProgress progress = new IndeterminateProgress(this);

            LoadModelWorker worker = new LoadModelWorker(progress, modelFile);
            worker.start();

            // This blocks until the worker thread closes the progress
            progress.showProgress();

            String errors = worker.getErrorMessages();
            if (errors != null) {
                ErrorDialog.showErrorDialog(this, errors);
                return false;
            }

            String warnings = worker.getWarningMessages();
            if (warnings != null)
                ErrorDialog.showWarningDialog(this, warnings);

            // If we succeeded, set the new model
            setShowModel(worker.getModel());
            setProjectFile(modelFile);

            return true;
        }
        return true;
    }

    /**
     * Save model to file
     */
    private void saveModel(File modelFile) {
        try {
            ModelWriter writer = new ModelWriter();
            writer.process(model, modelFile);
            // If we succeeded, make this the new project file
            setProjectFile(modelFile);
            // Saved model is not yet modified
            model.setModified(false);
        } catch (IOException e) {
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_SAVE_MODEL_IO, new Object[] { modelFile.getName(), e.getMessage() } ));
        } catch (InvalidModelException e) {
            PhotoPulse.logException(e);
            ErrorDialog.showErrorDialog(this, UIMessages.getMessage(UIMessages.ERR_SAVE_MODEL_FAILED, modelFile.getName()));
        }
    }

    /**
     * Load a set of photos on a background thread. Update the model if successful.
     */
    private boolean loadPhotos(File[] photoFiles) {
        WorkerProgress progress = new IndeterminateProgress(this);

        BuildPhotoListWorker worker = new BuildPhotoListWorker(progress, photoFiles);
        worker.start();

        // This blocks until the worker thread closes the progress
        progress.showProgress();

        String errors = worker.getErrorMessages();
        if (errors != null) {
            ErrorDialog.showErrorDialog(this, errors);
            return false;
        }

        // We succeeded, now update the model

        // Replace each Photo with a PhotoEffects for the model
        double transDuration = model.getDefaultTransitionDuration();
        double effectDuration = model.getDefaultEffectDuration();
        List<Photo> photoList = worker.getPhotoList();
        List<PhotoEffects> effectList = new ArrayList<PhotoEffects>(photoList.size());
        for (Photo photo : photoList)
            effectList.add(new PhotoEffects(photo, transDuration, effectDuration, transDuration));

        // Append new photos
        model.getPhotoEffectList().addAll(effectList);

        return true;
    }

    /**
     * Load an MP3 on a background thread. Update the model if successful.
     * Also cache the MP3Data for the MP3.
     */
    private boolean loadMP3(File mp3File) {
        WorkerProgress progress = new IndeterminateProgress(this);

        BuildMP3DataWorker worker = new BuildMP3DataWorker(progress, mp3File, model.getFrameRate());
        worker.start();

        // This blocks until the worker thread closes the progress
        progress.showProgress();

        String errors = worker.getErrorMessages();
        if (errors != null) {
            ErrorDialog.showErrorDialog(this, errors);
            return false;
        }

        MP3Data prevMP3Data = mp3Data;

        // Cache MP3Data and populate model with MP3
        mp3Data = worker.getMP3Data();
        model.setMP3(mp3Data.getMP3());

        // If this is the first time to load an mp3 and the mode is NONE, change it to INTERNAL.
        // Do this after setting MP3 above.
        if (prevMP3Data == null && model.getMP3Mode() == ShowModel.MP3Mode.NONE)
            model.setMP3Mode(ShowModel.MP3Mode.INTERNAL);

        return true;
    }

    private void buildM3U() {
        MP3PlaylistEditor m3uEditor = new MP3PlaylistEditor(model);
        File m3uFile = m3uEditor.showEditor(this);
        if (m3uFile != null)
            loadMP3(m3uFile);
    }

    /**
     * Export a show on a background thread. Update the model if successful.
     */
    private boolean exportShow(ShowExporter exporter) {
        // Double check that our cached MP3Data is for the models MP3 and for the current framerate.
        // If not, then first load/reload the MP3 into the model before continuing.
        if (model.getMP3Mode() != ShowModel.MP3Mode.NONE && model.getMP3() != null) {
            if (mp3Data == null
                    || !mp3Data.getMP3().equals(model.getMP3())
                    || mp3Data.getFrameRate() != model.getFrameRate()) {
                if (!loadMP3(model.getMP3().getFile()))
                    return false;
            }
        }

        WizardProgressIndicator progress = new WizardProgressIndicator(this);

        // Clear icons, so they can be GC'd when building show
        clearModelIcons(model.getPhotoEffectList());

        ShowModel showModel = model;

        // Build a wizard set of PhotoEffects in a model clone.
        // This can add/delete photos in the clones list.
        if (!isExpertMode) {
            showModel = model.clone();
            List<PhotoEffects> photoEffectsList = Wizard.buildShowList(showModel, showModel.getPhotoEffectList(), true);
            showModel.getPhotoEffectList().clear();
            showModel.getPhotoEffectList().addAll(photoEffectsList);
        }

        // Report error if show is too long.
        double showDuration = Math.max(showModel.computeShowDuration(), Wizard.computeMP3Duration(showModel));
        double maxDuration = ShowGenerator.computeMaxDuration(showModel.getFrameRate());
        if (showDuration > maxDuration) {
            ErrorDialog.showErrorDialog(WizardUI.this,
                    UIMessages.getMessage(UIMessages.ERR_SHOW_LENGTH,
                            new Object[] {Util.formatDuration(showDuration), Util.formatDuration(maxDuration)}));
            return false;
        }

        // Prompt user to delete existing export files
        File exportFile = exporter.getExportFile();
        File[] extraFiles = exporter.getExtraExportFiles(showModel);
        if (!deleteExportFilesAndContinue(exportFile, extraFiles))
            return false;

        BuildShowWorker worker = new BuildShowWorker(progress, showModel, exporter, ImageCoder.getInstance(), mp3Data);
        worker.start();

        // This blocks until the worker thread closes the progress
        progress.showProgress();

        if (worker.isError()) {
            worker.showErrorDialog(this);
            return false;
        }

        // If no errors and we weren't canceled, set the export file in the model
        if (!worker.isCanceled()) {
            ShowModel.ShowType showType = exporter.getShowType();
            model.setExportFile(exportFile);
            model.setShowType(showType);

            // Let user launch a tool on the export file
            ToolLauncher.showDialog(this, UIMessages.getMessage(UIMessages.UI_SHOW_CREATED_MESSAGE, exportFile.getName()),
                    model);

            return true;
        }
        return false;
    }

    /**
     * Prompt user to delete any existing export files.
     * If user agrees, delete them.
     * @return false if user canceled or deletion failed
     */
    private boolean deleteExportFilesAndContinue(File exportFile, File[] extraFiles) {
        StringBuilder sbFiles = new StringBuilder();
        int existingFileCount = 0;
        if (exportFile.exists()) {
            sbFiles.append(exportFile.getName());
            sbFiles.append("\n");
            existingFileCount++;
        }
        if (extraFiles != null) {
            for (int i = 0; i < extraFiles.length; i++) {
                if (extraFiles[i].exists()) {
                    sbFiles.append(extraFiles[i].getName());
                    sbFiles.append("\n");
                    existingFileCount++;
                }
            }
        }

        // Confirm user wants to overwrite existing files
        if (existingFileCount > 0) {
            int confirmChoice = ErrorDialog.showConfirmDialog(WizardUI.this,
                    UIMessages.getMessage(UIMessages.ERR_SAVE_EXISTS,
                            new Object[] {new Integer(existingFileCount), sbFiles.toString()}),
                    UIMessages.getMessage(UIMessages.UI_SAVE_EXISTS_TITLE),
                    ErrorDialog.YES_NO_OPTION);
            if (confirmChoice == ErrorDialog.YES_OPTION) {
                // Delete files, if deletion fails show error and abort
                File failedFile = deleteFiles(exportFile, extraFiles);
                if (failedFile != null) {
                    ErrorDialog.showErrorDialog(WizardUI.this,
                            UIMessages.getMessage(UIMessages.ERR_EXPORT_DELETE, failedFile.getAbsolutePath()));
                    return false;
                }
                return true;
            }
            // User canceled, abort
            return false;
        }

        // Nothing to delete, continue
        return true;
    }

    /**
     * Deletes files and directories passed, if they exist.
     * If delete fails, returns the File that failed deletion
     * @return First File that failed deletion, or null if all deleted
     */
    private File deleteFiles(File exportFile, File[] extraFiles) {
        if (deleteFile(exportFile))
            return exportFile;
        if (extraFiles != null) {
            for (int i = 0; i < extraFiles.length; i++) {
                if (extraFiles[i].isDirectory()) {
                    // Delete files in the directory, fail if we can't (don't delete subdirectories)
                    File[] dirFiles = extraFiles[i].listFiles();
                    if (dirFiles != null) {
                        for (int d = 0; d < dirFiles.length; d++) {
                            if (!dirFiles[d].isDirectory() && !dirFiles[d].delete())
                                return dirFiles[d];
                        }
                    }
                    // It's OK if we can't delete the directory itself, it may contain subdirectories
                    extraFiles[i].delete();
                }
                else if (deleteFile(extraFiles[i]))
                    return extraFiles[i];
            }
        }
        return null;
    }

    // Return true if the file exists and we failed to delete
    private boolean deleteFile(File file) {
        // Win32 DeleteFile() API (File.delete()) succeeds without deleting if the file is in use
        // (delete is delayed until the file is closed). So double check the file is gone when deleting.
        // When the file is "pending" deletion, length is still reported.
        // http://msdn.microsoft.com/library/default.asp?url=/library/en-us/fileio/fs/deletefile.asp
        return file.exists() && (!file.delete() || file.length() > 0);
    }

    /**
     * Clear all Photo icons from showList.
     * This allows soft cached thumbnails to be GC'd when generating a big show.
     */
    private void clearModelIcons(ShowList showList) {
        for (PhotoEffects effects : showList) {
            Photo photo = effects.getPhoto();
            photo.clearIcon();
        }
    }

    /**
     * Randomize effects/trans in model list - do not tweak timing
     */
    private void randomizeEffects() {
        ShowList showList = model.getPhotoEffectList();

        // Randomize effects, do not touch timing
        List<PhotoEffects> randomList = Wizard.buildShowList(model, showList, false);

        // The two lists should be the same size, but this is defensive in case that ever changes
        int showSize = showList.size();
        int randomSize = randomList.size();
        int size = Math.min(showSize, randomSize);
        for (int i = 0; i < size; i++)
            showList.set(i, randomList.get(i));
        if (randomSize > showSize)
            showList.addAll(randomList.subList(showSize, randomSize));
    }

    private void exitPhotoPulse() {
        // Prompt user to save project if modified
        if (!confirmSaveProject())
            return;

        // Save app preferences
        prefs.setLocation(this.getLocation());
        prefs.setExpertMode(isExpertMode);
        prefs.storePrefs(model);

        System.exit(0);
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING)
            exitPhotoPulse();
    }

    private UploadTool findUploadTool() {
        for (Tool tool : ToolRegistry.getTools()) {
            if (tool instanceof UploadTool)
                return (UploadTool) tool;
        }
        throw new IllegalStateException("Cannot find UploadTool");
    }

    private void buildWizardUI() {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setIconImage(new ResourceIcon("resources/icon.gif").getImage());

        // Create effect/trans lists with model selections
        beginTransitionWizardList = new WizardList(new ListListModel(Wizard.LIST_BEGINTRANSITIONS), 10, 2);
        effectsWizardList = new WizardList(new ListListModel(Wizard.LIST_EFFECTS), 10, 2);
        endTransitionWizardList = new WizardList(new ListListModel(Wizard.LIST_ENDTRANSITIONS), 10, 2);

        // Label to link to generated document
        exportFileLink = new HyperLabel(UIMessages.getMessage(UIMessages.UI_LABEL_LAUNCH));
        exportFileLink.setEnabled(false);

        // Action to choose sound file
        Action openMP3Action = new ShowMP3FileChooserAction(UIMessages.getMessage(UIMessages.UI_BUTTON_SOUND));
        openMP3Action.putValue(Action.SMALL_ICON, new ResourceIcon("resources/music-small.gif"));
        openMP3Action.putValue(Action.MNEMONIC_KEY, UIMessages.getResource(UIMessages.I_UI_MN_FILE_MUSIC));

        // Action to choose file to save to, initially disabled
        exportAction = new ShowExportFileAction(UIMessages.getMessage(UIMessages.UI_BUTTON_SAVE));
        exportAction.putValue(Action.SMALL_ICON, new ResourceIcon("resources/save-small.gif"));
        exportAction.putValue(Action.MNEMONIC_KEY, UIMessages.getResource(UIMessages.I_UI_MN_FILE_CREATE));
        exportAction.setEnabled(false);

        // Action to choose photo images
        Action openPhotosAction = new ShowPhotoFileChooserAction(UIMessages.getMessage(UIMessages.UI_BUTTON_OPEN));
        openPhotosAction.putValue(Action.SMALL_ICON, new ResourceIcon("resources/image-directory-small.gif"));
        openPhotosAction.putValue(Action.MNEMONIC_KEY, UIMessages.getResource(UIMessages.I_UI_MN_FILE_PHOTOS));

        // Button to invoke open photo images action
        JButton openPhotosButton = new JButton(openPhotosAction);
        openPhotosButton.setIcon(new ResourceIcon("resources/image-directory.gif"));

        // Button label
        JLabel openPhotosLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_OPEN_LABEL));
        openPhotosLabel.setLabelFor(openPhotosButton);

        // Button to invoke open sound action
        openMP3Button = new JButton(openMP3Action);
        openMP3Button.setIcon(new ResourceIcon("resources/music.gif"));

        // Button label
        JLabel openMP3Label = new JLabel(UIMessages.getMessage(UIMessages.UI_SOUND_LABEL));
        openMP3Label.setLabelFor(openMP3Button);

        // Button to invoke export action
        JButton exportButton = new JButton(exportAction);
        exportButton.setIcon(new ResourceIcon("resources/save.gif"));

        // Button label
        JLabel exportLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_SAVE_LABEL));
        exportLabel.setLabelFor(exportButton);

        // Make the 3 buttons the same size
        // XXX should use SpringLayout or something for this
        Dimension openPhotosSize = openPhotosButton.getPreferredSize();
        Dimension openMP3Size = openMP3Button.getPreferredSize();
        Dimension exportSize = exportButton.getPreferredSize();
        exportSize.width = Math.max(exportSize.width, Math.max(openPhotosSize.width, openMP3Size.width));
        exportSize.height = Math.max(exportSize.height, Math.max(openPhotosSize.height, openMP3Size.height));
        openPhotosButton.setPreferredSize(exportSize);
        openMP3Button.setPreferredSize(exportSize);
        exportButton.setPreferredSize(exportSize);

        // Tabbed lists
        tabbedPane = new JTabbedPane();

        // Expert panel tab
        expertPanel = new ExpertPanel(this);
        tabbedPane.addTab(UIMessages.getMessage(UIMessages.UI_TAB_PHOTOS),
                new ResourceIcon("resources/photos.gif"),
                tabPhotos = expertPanel);

        tabbedPane.addTab(UIMessages.getMessage(UIMessages.UI_TAB_BEGINTRANS),
                new ResourceIcon("resources/trans-in.gif"),
                tabBeginTransitions = new JScrollPane(beginTransitionWizardList));
        tabbedPane.addTab(UIMessages.getMessage(UIMessages.UI_TAB_EFFECTS),
                new ResourceIcon("resources/effect.gif"),
                tabEffects = new JScrollPane(effectsWizardList));
        tabbedPane.addTab(UIMessages.getMessage(UIMessages.UI_TAB_ENDTRANS),
                new ResourceIcon("resources/trans-out.gif"),
                tabEndTransitions = new JScrollPane(endTransitionWizardList));

        // Settings panel tab
        tabbedPane.addTab(UIMessages.getMessage(UIMessages.UI_TAB_SETTINGS),
                new ResourceIcon("resources/settings.gif"),
                new JScrollPane(new ShowSettingsPanel(this)));

        tabbedPane.addChangeListener(new TabChangeHandler());

        tabbedPane.setSelectedComponent(tabPhotos);

        // Layout main frame
        JPanel mainPanel = new JPanel(new GridBagLayout());
        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.NORTHEAST;
        gbc.insets.right = 5;
        // Overlap link with tabs. Assumes tabs are taller than label.
        gbc.insets.bottom = -exportFileLink.getPreferredSize().height;
        mainPanel.add(exportFileLink, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 3;
        gbc.fill = WizardGBC.BOTH;
        gbc.insets.left = gbc.insets.right = 5;
        mainPanel.add(tabbedPane, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.top = gbc.insets.bottom = 5;
        gbc.insets.left = 5;
        mainPanel.add(openPhotosLabel, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.left = gbc.insets.bottom = 5;
        mainPanel.add(openPhotosButton, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.CENTER;
        gbc.insets.top = gbc.insets.bottom = 5;
        mainPanel.add(openMP3Label, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.CENTER;
        gbc.insets.bottom = 5;
        mainPanel.add(openMP3Button, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.EAST;
        gbc.insets.top = gbc.insets.bottom = 5;
        gbc.insets.right = 5;
        mainPanel.add(exportLabel, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.EAST;
        gbc.insets.right = gbc.insets.bottom = 5;
        mainPanel.add(exportButton, gbc);

        this.getContentPane().add(mainPanel);

        // Build menubar
        JMenuBar menubar = buildMenuBar(openPhotosAction, openMP3Action);
        this.setJMenuBar(menubar);
    }

    private JMenuBar buildMenuBar(Action openPhotosAction, Action openMP3Action) {
        JMenuBar menubar = new JMenuBar();

        // File menu
        JMenu menuFile = new JMenu(UIMessages.getMessage(UIMessages.UI_MENU_FILE));
        menuFile.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE));

        // File New Project
        JMenuItem itemNewProject = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_FILE_PROJECT_NEW));
        itemNewProject.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE_PROJECT_NEW));
        itemNewProject.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newModel();
            }
        });
        menuFile.add(itemNewProject);

        // Only expose open/save/saveAs if projects are supported
        if (Branding.ENABLE_PROJECTS) {
            // File Open Project
            JMenuItem itemOpenProject = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_FILE_PROJECT_OPEN));
            itemOpenProject.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE_PROJECT_OPEN));
            itemOpenProject.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showOpenProjectChooser();
                }
            });
            menuFile.add(itemOpenProject);

            // File Save Project
            JMenuItem itemSaveProject = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_FILE_PROJECT_SAVE));
            itemSaveProject.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE_PROJECT_SAVE));
            itemSaveProject.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    saveProject();
                }
            });
            menuFile.add(itemSaveProject);

            // File Save Project As
            JMenuItem itemSaveProjectAs = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_FILE_PROJECT_SAVE_AS));
            itemSaveProjectAs.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE_PROJECT_SAVE_AS));
            itemSaveProjectAs.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showSaveProjectAsChooser();
                }
            });
            menuFile.add(itemSaveProjectAs);
        }

        menuFile.add(new JMenuItemFix(openPhotosAction));
        menuFile.add(new JMenuItemFix(openMP3Action));
        menuFile.add(new JMenuItemFix(exportAction));

        // File Exit
        JMenuItem itemExit = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_FILE_EXIT));
        itemExit.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_FILE_EXIT));
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitPhotoPulse();
            }
        });
        menuFile.add(itemExit);

        menubar.add(menuFile);

        // Edit menu
        JMenu menuEdit = new JMenu(UIMessages.getMessage(UIMessages.UI_MENU_EDIT));
        menuEdit.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT));

        // Cut/Copy/Paste/Delete actions for expert panel photos
        menuEdit.add(new JMenuItemFix(expertPanel.getCutAction()));
        menuEdit.add(new JMenuItemFix(expertPanel.getCopyAction()));
        menuEdit.add(new JMenuItemFix(expertPanel.getPasteAction()));
        menuEdit.add(new JMenuItemFix(expertPanel.getDeleteAction()));

        menuEdit.addSeparator();

        // Edit Select All
        JMenuItem itemSelectAll = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_EDIT_SELECTALL));
        itemSelectAll.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT_SELECTALL));
        itemSelectAll.addActionListener(new SelectListHandler(true));
        menuEdit.add(itemSelectAll);

        // Edit Deselect All
        JMenuItem itemDeselectAll = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_EDIT_DESELECTALL));
        itemDeselectAll.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT_DESELECTALL));
        itemDeselectAll.addActionListener(new SelectListHandler(false));
        menuEdit.add(itemDeselectAll);

        menuEdit.addSeparator();

        // Edit FTP Settings
        JMenuItem itemFTPSettings = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_EDIT_FTPSETTINGS));
        itemFTPSettings.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT_FTPSETTINGS));
        itemFTPSettings.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                uploadTool.showUploadSettingsDialog(WizardUI.this);
            }
        });
        menuEdit.add(itemFTPSettings);

        // Check if expert mode is allowed for this build
        if (Branding.ENABLE_EXPERTMODE) {
            // Edit Randomize Effects
            // Declare this before itemExpertMode, but add it to the menu after
            final JMenuItem itemRandomize = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_EDIT_RANDOMIZE));
            itemRandomize.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT_RANDOMIZE));
            itemRandomize.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    randomizeEffects();
                }
            });
            itemRandomize.setEnabled(isExpertMode);

            final JMenuItem itemRedistributeTime = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_EDIT_REDIST_TIME));
            itemRedistributeTime.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_EDIT_REDIST_TIME));
            itemRedistributeTime.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Wizard.redistributeTime(getShowModel());
                }
            });
            itemRedistributeTime.setEnabled(isExpertMode);
            addPropertyChangeListener(EXPERT_MODE_PROPERTY, new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    // Enable/disable Randomize/Redist time menus based on expert mode
                    itemRandomize.setEnabled(isExpertMode());
                    itemRedistributeTime.setEnabled(isExpertMode());
                }
            });

            menuEdit.add(itemRandomize);
            menuEdit.add(itemRedistributeTime);
        }

        menubar.add(menuEdit);

        if (Branding.ENABLE_EXPERTMODE) {
            // View menu
            JMenu menuView = new JMenu(UIMessages.getMessage(UIMessages.UI_MENU_VIEW));
            menuView.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_VIEW));

            // View Expert mode toggle
            // Alloy checkbox icon is 10x10, so pad it with an extra 6 so it aligns with other menu icons.
            final JCheckBoxMenuItem itemExpertMode = new JCheckBoxMenuItem(UIMessages.getMessage(UIMessages.UI_MENU_VIEW_EXPERTMODE),
                    new EmptyIcon(6, 16), isExpertMode);
            itemExpertMode.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_VIEW_EXPERTMODE));
            itemExpertMode.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setExpertMode(itemExpertMode.isSelected());
                }
            });

            menuView.add(itemExpertMode);

            menubar.add(menuView);
        }

        // Tools menu
        JMenu menuTools = new JMenu(UIMessages.getMessage(UIMessages.UI_MENU_TOOLS));
        menuTools.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_TOOLS));

        // Tools Build M3U
        JMenuItem itemBuildM3U = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_TOOLS_BUILDM3U));
        itemBuildM3U.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_TOOLS_BUILDM3U));
        itemBuildM3U.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                buildM3U();
            }
        });
        menuTools.add(itemBuildM3U);

        // Tools FTP Upload
        // Treat this tool specially - it is always enabled and can prompt for file.
        JMenuItem itemUpload = new JMenuItemFix(uploadTool.getToolMenuLabel());
        itemUpload.setMnemonic(uploadTool.getToolMenuMnemonic().intValue());
        itemUpload.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File directory = uploadTool.getUploadDirectory();
                if (directory == null) {
                    directory = model.getExportDirectory();
                    uploadTool.setUploadDirectory(directory);
                }
                uploadTool.showUploadDialog(WizardUI.this, model.getExportFile());
            }
        });
        menuTools.add(itemUpload);

        // Tools Manage Show
        JMenuItem itemManageShow = new JMenuItemFix(uploadTool.getManageShowAction());
        menuTools.add(itemManageShow);

        // Tools Available tools
        toolActionList = ToolLauncher.createToolActionList();
        for (ToolAction action : toolActionList)
            menuTools.add(new JMenuItemFix(action));

        menubar.add(menuTools);

        // Help menu
        JMenu menuHelp = new JMenu(UIMessages.getMessage(UIMessages.UI_MENU_HELP));
        menuHelp.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP));

        // Help Help
        JMenuItem itemHelp = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_HELP));
        itemHelp.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_HELP));
        itemHelp.addActionListener(new LaunchDocumentHandler(new File(PhotoPulse.INSTALLDIR, "PhotoPulse.chm").getAbsolutePath()));
        menuHelp.add(itemHelp);

        // Help Quickstart
        JMenuItem itemQuickstart = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_QUICKSTART));
        itemQuickstart.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_QUICKSTART));
        itemQuickstart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showQuickstartDialog();
            }
        });
        menuHelp.add(itemQuickstart);

        // Help Purchase
        if (PhotoPulse.LICENSE == null) {
            JMenuItem itemPurchase = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_PURCHASE));
            itemPurchase.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_PURCHASE));
            itemPurchase.addActionListener(new LaunchDocumentHandler(UIMessages.getMessage(UIMessages.URL_PURCHASE)));
            menuHelp.add(itemPurchase);
        }

        // Help License
        JMenuItem itemLicense = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_LICENSE));
        itemLicense.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_LICENSE));
        itemLicense.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLicenseDialog(null);
            }
        });
        menuHelp.add(itemLicense);

        // Help Register
        JMenuItem itemRegister = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_REGISTER));
        itemRegister.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_REGISTER));
        itemRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRegistrationURL();
            }
        });
        menuHelp.add(itemRegister);

        // Help About
        JMenuItem itemAbout = new JMenuItemFix(UIMessages.getMessage(UIMessages.UI_MENU_HELP_ABOUT));
        itemAbout.setMnemonic(UIMessages.getInteger(UIMessages.I_UI_MN_HELP_ABOUT));
        itemAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
        menuHelp.add(itemAbout);

        menubar.add(menuHelp);

        return menubar;
    }

    /**
     * Position and display the UI
     */
    private void displayUI() {
        // Pack before setting location
        this.pack();

        // Restore location from prefs
        Point location = prefs.getLocation();
        if (location == null)
            this.setLocationRelativeTo(null);
        else
            this.setLocation(location);

        this.setVisible(true);
    }

    private void showStartupDialogs() {
        toFront();

        // Unlicensed
        if (PhotoPulse.LICENSE == null) {
            // Require a valid license before continuing
            if (Branding.REQUIRE_LICENSE) {
                if (!showLicenseDialog(this)) {
                    exitPhotoPulse();
                    return;
                }

            }
            // If we don't require a license, show the trial dialog
            else {
                // Exit if TrialDialog tells us to
                if (TrialDialog.showDialog(this)) {
                    exitPhotoPulse();
                    return;
                }
            }
        }

        if (prefs.isFirstRun()) {
            showQuickstartDialog();
            prefs.setFirstRun(false);
        }
    }

    /**
     * Handle changes in ShowModel properties and photo list
     */
    private class ShowModelChangeHandler implements PropertyChangeListener, ListDataListener {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();

            if (prop.equals(ShowModel.MP3_FILE_PROPERTY))
                updateOpenMP3Button();
            else if (prop.equals(ShowModel.EXPORT_FILE_PROPERTY) || prop.equals(ShowModel.SHOW_TYPE_PROPERTY))
                updateExportFileActions();
        }

        public void intervalAdded(ListDataEvent e) {
            handleNewPhotos();
        }

        public void intervalRemoved(ListDataEvent e) {
            handleNewPhotos();
        }

        public void contentsChanged(ListDataEvent e) {
            handleNewPhotos();
        }
    }

    /**
     * Launch the specified document
     */
    private class LaunchDocumentHandler implements ActionListener {
        private String document;

        public LaunchDocumentHandler(String document) {
            this.document = document;
        }

        public void actionPerformed(ActionEvent e) {
            launchDocument(document);
        }
    }

    /**
     * Manage tab sensitive actions when tabbed pane changes
     */
    private class TabChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            Component tab = tabbedPane.getSelectedComponent();
            // Enable edit actions if this is the photo tab
            expertPanel.updateEditActions(tab == tabPhotos);
        }
    }

    /**
     * Handle select-all/deselect-all for the effect lists and photo list.
     * Context sensitive based on which tab is active
     */
    private class SelectListHandler implements ActionListener {
        private boolean select;

        public SelectListHandler(boolean select) {
            this.select = select;
        }

        public void actionPerformed(ActionEvent e) {
            Component tab = tabbedPane.getSelectedComponent();

            if (tab == tabPhotos)
                expertPanel.selectAllPhotos(select);
            else {
                if (tab == tabBeginTransitions)
                    beginTransitionWizardList.selectAll(select);
                else if (tab == tabEffects)
                    effectsWizardList.selectAll(select);
                else if (tab == tabEndTransitions)
                    endTransitionWizardList.selectAll(select);
            }
        }
    }

    /**
     * Handler for displaying photo file chooser and building list of PhotoEffects
     */
    private class ShowPhotoFileChooserAction extends AbstractAction {
        public ShowPhotoFileChooserAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            if (photoFileChooser == null)
                photoFileChooser = new PhotoFileChooser();

            PhotoFileChooser.Transfer transfer = new PhotoFileChooser.Transfer();
            transfer.setDirectory(model.getPhotoDirectory());

            if (!photoFileChooser.showModalDialog(WizardUI.this, transfer))
                return;

            // Save directory in model even if we fail to load the images
            model.setPhotoDirectory(transfer.getDirectory());

            // Load photos and populate model
            loadPhotos(transfer.getSelectedFiles());
        }
    }

    /**
     * Handler for displaying MP3 file chooser and processing an MP3
     */
    private class ShowMP3FileChooserAction extends AbstractAction {
        public ShowMP3FileChooserAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent e) {
            if (mp3FileChooser == null)
                mp3FileChooser = new MP3FileChooser();

            mp3FileChooser.setCurrentDirectory(model.getMP3Directory());
            MP3 mp3 = model.getMP3();
            mp3FileChooser.setSelectedFile(mp3 != null ? mp3.getFile() : null);

            if (mp3FileChooser.showOpenDialog(WizardUI.this) != JFileChooser.APPROVE_OPTION)
                return;

            // Save directory in model even if we fail to load the mp3
            model.setMP3Directory(mp3FileChooser.getCurrentDirectory());

            // Load MP3 and populate model
            loadMP3(mp3FileChooser.getSelectedFile());
        }
    }

    /**
     * Handler for displaying export file chooser and exporting a show
     */
    private class ShowExportFileAction extends AbstractAction {
        public ShowExportFileAction(String name) {
            super(name);
        }

        public void actionPerformed(ActionEvent event) {
            if (exportFileChooser == null)
                exportFileChooser = new ExportFileChooser();

            ExportFileChooser.Transfer transfer = new ExportFileChooser.Transfer();
            transfer.setDirectory(model.getExportDirectory());
            transfer.setShowType(model.getShowType());
            transfer.setHighQuality(ExporterFactory.isExporterHighQuality(model.getShowType()));
            transfer.setSelectedFile(model.getExportFile());

            if (!exportFileChooser.showModalDialog(WizardUI.this, transfer))
                return;

            // Save directory in model even if we fail to export
            model.setExportDirectory(transfer.getDirectory());

            File exportFile = transfer.getSelectedFile();
            // Determine file type being exported and construct an exporter
            ShowExporter exporter = null;
            ShowModel.ShowType showType = transfer.getShowType();
            try {
                exporter = ExporterFactory.getExporter(showType, transfer.isHighQuality(), exportFile);
            } catch (ExportException e) {
                PhotoPulse.logException(e);
                e.showErrorDialog(WizardUI.this);
                return;
            }

            // Export the show and update the exportFile property if successful.
            // Our listener for the exportFile property will update the hyperlink
            exportShow(exporter);
        }
    }
}
