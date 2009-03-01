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

import com.iv.flash.util.IVException;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.exporter.ExportException;
import com.photica.photopulse.exporter.ExporterFactory;
import com.photica.photopulse.exporter.ShowExporter;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinException;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;
import com.photica.photopulse.wizard.WizardEffectBeginTransition;
import com.photica.photopulse.wizard.WizardEffectEffect;
import com.photica.photopulse.wizard.WizardEffectEndTransition;

import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.List;


/**
 * Generate samples of each effect and transition.
 * Generates index.html which relies an the launching JS in the website pages.
 */
public class GenerateSamples {

    private static final Skin SKIN = SkinManager.BUILTIN_SMALL_43;
    private StringWriter m_swLogErrors = new StringWriter();
    private File m_filOutputDir;
    private Photo m_photo;
    private FileWriter m_fw;
    private String m_strHTMTemplate;

    public static void main(String args[]) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: <output-dir> <imagefile>");
            System.exit(1);
        }

        // Force initialization
        Class<PhotoPulse> c = PhotoPulse.class;

        GenerateSamples gs = new GenerateSamples();
        Photo photo = ImageCoder.getInstance().validatePhotoFile(new File(args[1]));
        if (photo == null) {
            System.out.println("NO PHOTO!!! " + args[1]);
            System.exit(-1);
        }
        gs.generate(new File(args[0]), photo);
    }

    private void generate(File filOutputDir, Photo photo) throws SkinException, IVException, IOException, ExportException {
        m_filOutputDir = filOutputDir;
        m_photo = photo;

        InputStream is = getClass().getResourceAsStream("host.htm");
        m_strHTMTemplate = Util.readStream(is);

        m_fw = new FileWriter(new File(filOutputDir, "samples.html"));

        try {
            processList(Wizard.LIST_BEGINTRANSITIONS, "Transitions In");
            processList(Wizard.LIST_EFFECTS, "Effects");
            processList(Wizard.LIST_ENDTRANSITIONS, "Transitions Out");
        } finally {
            m_fw.close();
        }
    }

    private <WE extends WizardEffect> void processList(List<WE> list, String strTitle) throws
            IOException, IVException, SkinException, ExportException {
        File filSamples = new File(m_filOutputDir, "samples");
        filSamples.mkdirs();

        m_fw.write("<p><table border=\"1\">\n");
        m_fw.write("<tr><th colspan=\"2\">" + strTitle + "</th></tr>\n");

        Dimension2D skinSize = SKIN.getSkinSizePixels();
        String strTarget = "pp" + (int)skinSize.getWidth() + "x" + (int)skinSize.getHeight();

        ShowModel model = new ShowModel();
        model.setSkin(SKIN);
        model.setShowType(ShowModel.ShowType.SWF);
        model.setEndShowMode(ShowModel.EndShowMode.LOOP);

        int nCount = 0;
        for (WizardEffect weff : list) {
            WizardEffectEffect effect = Wizard.WEFFECT_None;
            double effectDuration = 0.1;
            WizardEffectBeginTransition begintrans = Wizard.WBEGINTRANS_None;
            double begintransDuration = 0.1;
            WizardEffectEndTransition endtrans = Wizard.WENDTRANS_None;
            double endtransDuration = 0.1;

            if (weff instanceof WizardEffectEffect) {
                effect = (WizardEffectEffect)weff;
                effectDuration = 2.0;
            }
            else if (weff instanceof WizardEffectBeginTransition) {
                begintrans = (WizardEffectBeginTransition)weff;
                begintransDuration = 2.0;
            }
            else if (weff instanceof WizardEffectEndTransition) {
                endtrans = (WizardEffectEndTransition)weff;
                endtransDuration = 2.0;
            }

            PhotoEffects photoEffects = new PhotoEffects(m_photo, null, effect.getPhotoScale(),
                    begintrans.getEffect(), begintransDuration, effect.getEffect(), effectDuration, endtrans.getEffect(), endtransDuration, true, false);
            model.getPhotoEffectList().clear();
            model.getPhotoEffectList().add(photoEffects);

            String strName = weff.getKey();
            File filSWF = new File(filSamples, strName + ".swf");
            ShowExporter exporter = ExporterFactory.getExporter(model.getShowType(), filSWF);

            boolean success = ShowGenerator.generate(model, null, exporter, ImageCoder.getInstance(), m_swLogErrors);
            if (!success) {
                System.out.println(m_swLogErrors);
            }

            File filHTM = new File(filSamples, strName + ".htm");
            writeHTM(filHTM, weff.getDisplayName(), filSWF.getName());

            if (nCount % 2 == 0)
                m_fw.write("<tr>\n");

            m_fw.write("<td><a href=\"samples/" + filHTM.getName()
                    + "\" target=\"" + strTarget + "\" onClick=\"launch('', '" + strTarget + "', "
                    + (int)skinSize.getWidth() + ", " + (int)skinSize.getHeight() + ")\">"
                    + weff.getDisplayName() + "</a></td>\n");

            if (nCount % 2 != 0)
                m_fw.write("</tr>\n");
            nCount++;
        }
        m_fw.write("</table></p>\n");
    }


    private void writeHTM(File filOutput, String strTitle, String strFile) throws IOException {
        String strHTM = MessageFormat.format(m_strHTMTemplate,
                new Object[]{
                    strTitle,
                    strFile
                });

        FileWriter fwHTM = new FileWriter(filOutput);
        fwHTM.write(strHTM);
        fwHTM.close();
    }


}