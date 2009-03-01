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

package com.photica.photopulse.samples;

import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.model.effects.EndTransition;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;

import java.awt.Dimension;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Generates every combination of begin and end transition. Useful for testing
 * wwizard generated transitions.
 */
public class GenerateTransitions {

    public static void main(String args[]) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: <output-dir> <image1> <image2>");
            System.exit(1);
        }

        // Force initialization
        Class<PhotoPulse> c = PhotoPulse.class;
        File filOutputDir = new File(args[0]);
        File filImage1 = new File(args[1]);
        File filImage2 = new File(args[2]);
        Photo photo1 = ImageCoder.getInstance().validatePhotoFile(filImage1);
        Photo photo2 = ImageCoder.getInstance().validatePhotoFile(filImage2);


        Skin skin = new Skin(new Dimension(640, 480));
        skin.load();
        StringWriter swLogErrors = new StringWriter();
        ArrayList<WizardEffect> myEndtransList = new ArrayList<WizardEffect>();
        myEndtransList.add(Wizard.WENDTRANS_None);
        myEndtransList.addAll(Wizard.LIST_ENDTRANSITIONS);

        // select transitions and effects that don't change
        BeginTransition bt1 = EffectRegistry.BEGINTRANS_None;
        Effect ef1 = EffectRegistry.EFFECT_None;
        Effect ef2 = EffectRegistry.EFFECT_None;
        EndTransition et2 = EffectRegistry.ENDTRANS_None;


        for (WizardEffect endtrans : myEndtransList) {
            // create a new model

            EndTransition et1 = (EndTransition)(endtrans).getEffect();

            for (WizardEffect begintrans: Wizard.LIST_BEGINTRANSITIONS) {
                BeginTransition bt2 = (BeginTransition)(begintrans).getEffect();

                for (int layer = 0; layer < 2; layer++) {
                    ShowModel model = new ShowModel();
                    model.setShowType(ShowModel.ShowType.SWF);
                    PhotoEffects pe1 = new PhotoEffects(photo1, null, 1.0, bt1, 1, ef1, 1, et1, 1, (layer == 0) ? true : false, false);
                    PhotoEffects pe2 = new PhotoEffects(photo2, null, 1.0, bt2, 1, ef2, 1, et2, 1, false, false);
                    model.getPhotoEffectList().add(pe1);
                    model.getPhotoEffectList().add(pe2);


                    File filOutput = new File(filOutputDir,
                            et1.getTag() + "-" + bt2.getTag() + "(" + (pe1.isEndTransitionTopLayer() ? "bottom" : "top") + ").swf");
                    System.err.println(filOutput.getName());


                    ShowExporter exporter = ExporterFactory.getExporter(model.getShowType(), filOutput);

                    if (!ShowGenerator.generate(model, null, exporter, ImageCoder.getInstance(), swLogErrors)) {
                        System.err.println(swLogErrors.toString());
                        System.exit(1);
                    }
                }
            }
        }


    }
}