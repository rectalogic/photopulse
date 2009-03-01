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

package com.photica.photopulse.ui.console;

import com.iv.flash.util.IVException;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.io.InvalidModelException;
import com.photica.photopulse.model.io.ModelReader;
import com.photica.photopulse.model.io.ModelWriter;
import com.photica.photopulse.progress.ProgressReporter;
import com.photica.photopulse.skin.SkinException;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.wizard.Wizard;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

public class Main {

    // Error dialog that wraps message
    private static class WrappingOptionPane extends JOptionPane {
        public WrappingOptionPane(Object oMessage, int nMessageType) {
            super(oMessage, nMessageType);
        }
        public int getMaxCharactersPerLineCount() {
            return 65;
        }
        public static void showErrorDialog(Component cmpParent, String strTitle, Object oMessage) {
            WrappingOptionPane wop = new WrappingOptionPane(oMessage, ERROR_MESSAGE);
            JDialog dialog = wop.createDialog(cmpParent, strTitle);
            dialog.setVisible(true);
        }
    }

    private static final String ARG_SWF = "-swf";
    private static final String ARG_EXE = "-exe";
    private static final String ARG_HTM = "-htm";
    private static final String ARG_MHT = "-mht";
    private static final String ARG_WMV = "-wmv";
    private static final String ARG_MP3 = "-mp3";
    private static final String ARG_IMAGEDIR = "-imagedir";
    private static final String ARG_BGCOLOR = "-bgcolor";
    private static final String ARG_FRAMERATE = "-framerate";
    private static final String ARG_SPEED_EFFECT = "-effectSpeed";
    private static final String ARG_SPEED_TRANS = "-transSpeed";
    private static final String ARG_SKIN = "-skin";
    private static final String ARG_MODEL = "-model";
    private static final String ARG_DUMP = "-dump";

    public static void main(String args[]) {

        Map<String,String> argMap = parseCommandLine(args);

        ShowModel model = null;
        MP3Data mp3Data = null;

        String arg;

        // Framerate
        float frameRate = ShowModel.DEFAULT_FRAMERATE;
        boolean overrideFrameRate = false;
        if ((arg = argMap.get(ARG_FRAMERATE)) != null) {
            try {
                frameRate = Float.parseFloat(arg);
                overrideFrameRate = true;
            } catch (NumberFormatException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_ARG, arg));
            }
        }

        // Load model file
        if ((arg = argMap.get(ARG_MODEL)) != null) {
            try {
                ModelReader reader = new ModelReader();
                model = reader.process(new File(arg));
            } catch (FileNotFoundException e ) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IOEXCEPTION, new Object[] { arg, e.getMessage() }));
            } catch (InvalidModelException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MODEL, new Object[] { arg, e.getMessage() }));
            } catch (IOException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IOEXCEPTION, new Object[] { arg, e.getMessage() }));
            }
            if (overrideFrameRate)
                model.setFrameRate(frameRate);
            else
                frameRate = model.getFrameRate();

            // Load model MP3 if specified
            try {
                MP3 mp3 = model.getMP3();
                if (mp3 != null && model.getMP3Mode() != ShowModel.MP3Mode.NONE)
                    mp3Data = new MP3Data(frameRate, mp3.getFile());
            } catch (IOException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
            } catch (URISyntaxException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
            } catch (IVException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
            }
        }
        // Build model based on command line arguments
        else {
            model = new ShowModel();

            // Background color
            if ((arg = argMap.get(ARG_BGCOLOR)) != null)
                model.setBackgroundColor(Color.decode(arg));

            // Load MP3
            if ((arg = argMap.get(ARG_MP3)) != null) {
                try {
                    File mp3File = new File(arg);
                    mp3Data = new MP3Data(frameRate, mp3File);
                    model.setMP3(mp3Data.getMP3());
                    model.setMP3Mode(ShowModel.MP3Mode.INTERNAL);
                    model.setMP3Directory(mp3File.getParentFile());
                } catch (IOException e) {
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
                } catch (URISyntaxException e) {
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
                } catch (IVException e) {
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MP3, e.getMessage()));
                }
            }

            // Effect speed
            if ((arg = argMap.get(ARG_SPEED_EFFECT)) != null) {
                try {
                    model.setDefaultEffectDuration(Double.parseDouble(arg));
                } catch (NumberFormatException e) {
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_ARG, arg));
                }
            }

            // Transition speed
            if ((arg = argMap.get(ARG_SPEED_TRANS)) != null) {
                try {
                    model.setDefaultTransitionDuration(Double.parseDouble(arg));
                } catch (NumberFormatException e) {
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_ARG, arg));
                }
            }

            // Select all transitions/effects
            model.getBeginTransitionSelectionModel().addSelectionInterval(0, Wizard.LIST_BEGINTRANSITIONS.size() - 1);
            model.getEffectSelectionModel().addSelectionInterval(0, Wizard.LIST_EFFECTS.size() - 1);
            model.getEndTransitionSelectionModel().addSelectionInterval(0, Wizard.LIST_ENDTRANSITIONS.size() - 1);

            File imageDir = null;
            if ((arg = argMap.get(ARG_IMAGEDIR)) != null) {
                imageDir = new File(arg);
                model.setPhotoDirectory(imageDir);
            }

            // Build show with Wizard
            if (imageDir == null)
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.NO_IMAGEDIR));

            List<Photo> photoList = null;
            try {
                photoList = Wizard.buildPhotoList(imageDir);
            } catch (IOException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.NO_IMAGES));
            }
            if (photoList.isEmpty())
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.NO_IMAGES));

            // Replace each Photo with a PhotoEffects
            List<PhotoEffects> photoEffectsList = new ArrayList<PhotoEffects>(photoList.size());
            for (Photo photo : photoList)
                photoEffectsList.add(new PhotoEffects(photo));

            // Run wizard on photo list
            photoEffectsList = Wizard.buildShowList(model, photoEffectsList, true);
            model.getPhotoEffectList().clear();
            model.getPhotoEffectList().addAll(photoEffectsList);
        }


        // Get skin specifier.
        // Allow this to override skin in model.
        if ((arg = argMap.get(ARG_SKIN)) != null) {
            try {
                model.setSkin(SkinManager.getSkin(new URI(arg)));
            } catch (URISyntaxException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_ARG, arg));
            }
        }

        // Show type and export file
        // Allow these to override what was in a loaded model
        if ((arg = argMap.get(ARG_SWF)) != null) {
            model.setShowType(ShowModel.ShowType.SWF);
            File exportFile = new File(arg);
            model.setExportFile(exportFile);
            model.setExportDirectory(exportFile.getParentFile());
        }
        if ((arg = argMap.get(ARG_EXE)) != null) {
            model.setShowType(ShowModel.ShowType.EXE);
            File exportFile = new File(arg);
            model.setExportFile(exportFile);
            model.setExportDirectory(exportFile.getParentFile());
        }
        if ((arg = argMap.get(ARG_HTM)) != null) {
            model.setShowType(ShowModel.ShowType.HTM);
            File exportFile = new File(arg);
            model.setExportFile(exportFile);
            model.setExportDirectory(exportFile.getParentFile());
        }
        if ((arg = argMap.get(ARG_WMV)) != null) {
            model.setShowType(ShowModel.ShowType.WMV);
            File exportFile = new File(arg);
            model.setExportFile(exportFile);
            model.setExportDirectory(exportFile.getParentFile());
        }
        if ((arg = argMap.get(ARG_MHT)) != null) {
            model.setShowType(ShowModel.ShowType.MHT);
            File exportFile = new File(arg);
            model.setExportFile(exportFile);
            model.setExportDirectory(exportFile.getParentFile());
        }

        // Create exporter based on show type
        ShowExporter exporter = null;
        ShowModel.ShowType showType = model.getShowType();
        File exportFile = model.getExportFile();
        if (exportFile == null)
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.NO_OUTPUT));

        try {
            exporter = ExporterFactory.getExporter(showType, exportFile);
        } catch (ExportException e) {
            reportFatalError(null, e.getMessage());
        }

        // Dump model xml
        if ((arg = argMap.get(ARG_DUMP)) != null) {
            try {
                ModelWriter writer = new ModelWriter();
                writer.process(model, new File(arg));
            } catch (FileNotFoundException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IOEXCEPTION, new Object[] { arg, e.getMessage() }));
            } catch (InvalidModelException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MODEL, new Object[] { arg, e.getMessage() }));
            } catch (IOException e) {
                reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.INVALID_MODEL, new Object[] { arg, e.getMessage() }));
            }
        }

        // Generate Flash
        try {
            ProgressReporter.setProgressIndicator(new ConsoleProgressIndicator());
            StringWriter swLogErrors = new StringWriter();
            if (!ShowGenerator.generate(model, mp3Data, exporter, ImageCoder.getInstance(), swLogErrors)) {
                String errors = swLogErrors.toString();
                if (!ProgressReporter.isCanceled() && errors.length() > 0)
                    reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.LOGMESSAGES, errors));
            }
            ProgressReporter.setProgressIndicator(null);
            System.exit(0);
        } catch (SkinException e) {
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IVEXCEPTION, e.getMessage()));
        } catch (IVException e) {
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IVEXCEPTION, e.getMessage()));
        } catch (ExportException e) {
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IVEXCEPTION, e.getMessage()));
        } catch (IOException e) {
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.IOEXCEPTION, e.getMessage()));
        }
    }

    private static void reportFatalError(final JFrame frmParent, final String strMsg) {
        if (frmParent != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    WrappingOptionPane.showErrorDialog(frmParent,
                        ConsoleMessages.getMessage(ConsoleMessages.UIERRORTITLE),
                        strMsg);
                    System.exit(1);
                }
            });
        }
        else {
            System.err.println(strMsg);
            System.exit(1);
        }
    }

    private static Map<String,String> parseCommandLine(String args[]) {
        if (args.length == 0 || args.length % 2 != 0)
            reportFatalError(null, ConsoleMessages.getMessage(ConsoleMessages.USAGE));

        HashMap<String,String> map = new HashMap<String, String>();
        int len = args.length - 1;
        for (int i = 0; i <= len; i++) {
            map.put(args[i], args[++i]);
        }
        return map;
    }
}
