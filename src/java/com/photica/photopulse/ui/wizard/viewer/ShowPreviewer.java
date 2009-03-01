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
 
package com.photica.photopulse.ui.wizard.viewer;

import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.PreviewExporter;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.imageio.ImageTranscoder;
import com.photica.photopulse.imageio.cache.ThumbnailCache;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.ui.wizard.DisposeHandler;
import com.photica.photopulse.ui.wizard.IndeterminateProgress;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.photopulse.ui.wizard.worker.BuildShowWorker;
import com.photica.photopulse.ui.wizard.worker.WorkerProgress;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.Color;

public class ShowPreviewer {

    private static final Skin THUMBNAIL_SKIN = new Skin(new Dimension(ThumbnailCache.THUMBNAIL_WIDTH, ThumbnailCache.THUMBNAIL_HEIGHT));

    public static void showDialog(Component parent, Skin skin, Color backgroundColor, PhotoEffects effects1, PhotoEffects effects2) {
        Window window = SwingUtilities.getWindowAncestor(parent);

        WorkerProgress progress;
        if (window instanceof JFrame)
            progress = new IndeterminateProgress((JFrame)window);
        else if (window instanceof JDialog)
            progress = new IndeterminateProgress((JDialog)window);
        else
            progress = new IndeterminateProgress((JFrame)null);

        // Build a simple model with the effects passed
        ShowModel model = new ShowModel();
        // Use special thumbnail skin if none provided
        model.setSkin(skin == null ? THUMBNAIL_SKIN : skin);
        model.setEndShowMode(ShowModel.EndShowMode.LOOP);
        model.setShowType(ShowModel.ShowType.SWF);
        model.setBackgroundColor(backgroundColor);
        model.getPhotoEffectList().add(effects1);
        if (effects2 != null)
            model.getPhotoEffectList().add(effects2);

        // Get preview exporter
        PreviewExporter exporter = null;
        try {
            exporter = ExporterFactory.getPreviewExporter();
        } catch (ExportException e) {
            e.showErrorDialog(window);
            return;
        }

        try {
            // Generate the show on another thread.
            // Use thumbnail cache transcoder for thumbnail skin, so we can get most images from the thumbnail cache.
            BuildShowWorker worker = new BuildShowWorker(progress, model, exporter,
                    skin == THUMBNAIL_SKIN ? (ImageTranscoder)ThumbnailCache.getInstance() : (ImageTranscoder)ImageCoder.getInstance(),
                    null);
            worker.start();

            // This blocks until the worker thread closes the progress
            progress.showProgress();

            if (worker.isError()) {
                worker.showErrorDialog(parent);
                return;
            }

            Component previewComponent = exporter.getPreviewComponent();

            JDialog dialog;
            if (window instanceof JFrame)
                dialog = new JDialog((JFrame)window, UIMessages.getMessage(UIMessages.UI_PREVIEWER_TITLE), true);
            else if (window instanceof JDialog)
                dialog = new JDialog((JDialog)window, UIMessages.getMessage(UIMessages.UI_PREVIEWER_TITLE), true);
            else
                dialog = new JDialog((JFrame)null, UIMessages.getMessage(UIMessages.UI_PREVIEWER_TITLE), true);

            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setResizable(false);
            Container contentPane = dialog.getContentPane();
            contentPane.setLayout(new GridBagLayout());

            JButton closeButton = new JButton(UIMessages.getMessage(UIMessages.UI_BUTTON_CLOSE));
            closeButton.addActionListener(new DisposeHandler(dialog));

            dialog.getRootPane().setDefaultButton(closeButton);

            WizardGBC gbc = new WizardGBC();

            gbc.reset();
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.insets.left = gbc.insets.bottom = gbc.insets.top = 2;
            contentPane.add(previewComponent, gbc);

            gbc.reset();
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.NORTH;
            gbc.insets.left = gbc.insets.right = gbc.insets.top = 5;
            contentPane.add(closeButton, gbc);

            dialog.pack();
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
        } finally {
            // Must dispose of preview exporter
            if (exporter != null)
                exporter.dispose();
        }
    }
}
