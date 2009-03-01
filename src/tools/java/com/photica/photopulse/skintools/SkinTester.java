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
 
package com.photica.photopulse.skintools;

import com.iv.flash.util.IVException;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.io.InvalidModelException;
import com.photica.photopulse.model.io.ModelReader;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.skin.SkinSet;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.FileNotFoundException;

/**
 * Load a project file and then generate a show for every skin in the registry.
 */
public class SkinTester {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: SkinTester <model.ppp> <output-directory>");
            System.exit(1);
        }

        File projectFile = new File(args[0]);
        File exportDir = new File(args[1]);

        ShowModel model = null;
        try {
            ModelReader reader = new ModelReader();
            model = reader.process(projectFile);
        } catch (InvalidModelException e) {
            System.err.println("Invalid model " + projectFile.getPath());
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Bad model file " + projectFile.getPath());
            e.printStackTrace();
            System.exit(1);
        }

        // Ignore MP3
        model.setMP3Mode(ShowModel.MP3Mode.NONE);

        ShowModel.ShowType showType = ShowModel.ShowType.EXE;
        String showExt = ".exe";

        model.setShowType(showType);
        model.setExportDirectory(exportDir);

        for (SkinSet skinSet : SkinManager.getSkinSets()) {
            if (skinSet == SkinManager.BUILTINSET)
                continue;
            for (Skin skin : skinSet.getSkins()) {
                try {
                    exportShow(model, skin, showExt);
                } catch (ExportException e) {
                    handleException(skin, e);
                } catch (IOException e) {
                    handleException(skin, e);
                } catch (IVException e) {
                    handleException(skin, e);
                } catch (SkinException e) {
                    handleException(skin, e);
                }
            }
        }
    }

    private static void handleException(Skin skin, Exception e) {
        String file = buildFileName(skin) + ".err";
        try {
            PrintStream ps = new PrintStream(new FileOutputStream(new File(file)));
            e.printStackTrace(ps);
            ps.close();
        } catch (FileNotFoundException e1) {
            System.err.println("Failed to open error file " + file);
        }
    }

    private static String buildFileName(Skin skin) {
        // Name export file using skin name
        File skinsetFile = skin.getSkinSet().getFile();
        if (skinsetFile != null)
            return skinsetFile.getName() + "." + skin.getName();
        else
            return "builtin." + skin.getName();
    }

    private static void exportShow(ShowModel model, Skin skin, String showExt) throws ExportException, IOException, IVException, SkinException {
        model.setSkin(skin);

        // Name export file using skin name
        String exportName = buildFileName(skin) + showExt;
        System.out.println(exportName);

        File exportFile = new File(model.getExportDirectory(), exportName);
        model.setExportFile(exportFile);

        ShowExporter exporter = ExporterFactory.getExporter(model.getShowType(), false, exportFile);

        // Generate Flash
        StringWriter swLogErrors = new StringWriter();
        if (!ShowGenerator.generate(model, null, exporter, ImageCoder.getInstance(), swLogErrors)) {
            String errors = swLogErrors.toString();
            if (errors.length() > 0)
                throw new ExportException("Generator errors reported: " + errors);
        }
    }
}
