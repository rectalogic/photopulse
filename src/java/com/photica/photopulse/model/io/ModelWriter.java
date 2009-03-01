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
 
package com.photica.photopulse.model.io;

import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.ShowSelectionModel;
import com.photica.photopulse.model.effects.BaseEffect;
import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EffectVisitor;
import com.photica.photopulse.model.effects.EndTransition;
import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.model.effects.PanoramaEffect;
import com.photica.photopulse.model.effects.RisingFallingStuffEffect;
import com.photica.photopulse.model.effects.ToneEffect;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO - the ModelWriter currently need to understand how to write xml
// for each transition and effect. In might be better to delegate this to
// the effect itself so that the writer does not need to change new effects
// are added.

public class ModelWriter implements EffectVisitor {

    /**
     * Create an instance that will write the ShowModel to the specified file.
     */
    public ModelWriter() {
        super();
    }

    public void process(ShowModel model, File f)
            throws
            IOException,
            InvalidModelException {
        fileTarget = f;
        process(model, new FileOutputStream(f), true);
    }

    /**
     * Write the model to the provided outputStream. Internal state is reset
     * at the end of this method. This enables a single instance to be used
     * in an applicaiton without concern that the instance is maintaining a
     * handle to the model or outputstream.
     * @throws InvalidModelException
     */
    public void process(ShowModel model, OutputStream target, boolean closeOnComplete)
            throws
            IOException,
            InvalidModelException {
        show = model;
        this.target = target;

        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element showElement = doc.createElement(Constants.XML_TAG_SHOW);
            String photoDir = getFilePath(fileTarget, show.getPhotoDirectory());
            String mp3Dir = getFilePath(fileTarget, show.getMP3Directory());
            String exportDir = getFilePath(fileTarget, show.getExportDirectory());
            String exportFile = getFilePath(fileTarget, show.getExportFile());

            doc.appendChild(showElement);
            skin(showElement, show);
            // set the file format version
            showElement.setAttribute(Constants.XML_ATTR_FILE_FORMAT_VERSION, Constants.XML_FILE_FORMAT_VERSION);
            showElement.setAttribute(Constants.XML_ATTR_FRAMERATE, String.valueOf(show.getFrameRate()));
            showElement.setAttribute(Constants.XML_ATTR_BACKGROUND_COLOR, getHexColor(show.getBackgroundColor()));
            showElement.setAttribute(Constants.XML_ATTR_DEFAULT_EFFECT_DURATION, String.valueOf(show.getDefaultEffectDuration()));
            showElement.setAttribute(Constants.XML_ATTR_DEFAULT_TRANSITION_DURATION, String.valueOf(show.getDefaultTransitionDuration()));
            // optional value can be null
            if (photoDir != null) {
                showElement.setAttribute(Constants.XML_ATTR_PHOTO_DIRECTORY, photoDir);
            }
            if (mp3Dir != null) {
                showElement.setAttribute(Constants.XML_ATTR_MP3_DIRECTORY, mp3Dir);
            }
            if (exportDir != null) {
                showElement.setAttribute(Constants.XML_ATTR_EXPORT_DIRECTORY, exportDir);
            }

            if (exportFile != null) {
                showElement.setAttribute(Constants.XML_ATTR_EXPORT_FILE, exportFile);
            }


            showElement.setAttribute(Constants.XML_ATTR_MP3_MODE, show.getMP3Mode().toString());
            showElement.setAttribute(Constants.XML_ATTR_SHOW_TYPE, show.getShowType().toString());
            showElement.setAttribute(Constants.XML_ATTR_ENDSHOW_MODE, show.getEndShowMode().toString());

            beginTrans(showElement, show);
            effects(showElement, show);
            endTrans(showElement, show);
            photos(showElement, show);
            mp3s(showElement, show);
            writeXML();
            target.flush();

        } catch (ParserConfigurationException e) {
            throw new InvalidModelException(e);
        } catch (DOMException e) {
            throw new InvalidModelException(e);
        } catch (TransformerException e) {
            throw new InvalidModelException(e);
        } finally {
            // reset all local variables to null we no longer need
            // references to this data.
            // get a copy of target since we are going to blow it away in init
            OutputStream os = target;
            init();
            if (closeOnComplete) os.close();

        }
    }

    private void writeXML()
            throws
            TransformerConfigurationException,
            TransformerException,
            IOException {

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        BufferedOutputStream bout = new BufferedOutputStream(target);
        transformer.transform(new DOMSource(doc), new StreamResult(bout));
        bout.flush();
        if (DEBUG) transformer.transform(new DOMSource(doc), new StreamResult(System.out));
    }


    private static void skin(
            Element parent,
            ShowModel show
            ) {
        Document doc = parent.getOwnerDocument();
        Element node = doc.createElement(Constants.XML_TAG_SKIN);
        Skin skin = show.getSkin();
        URI skinURI = SkinManager.getSkinURI(skin);
        node.setAttribute(Constants.XML_ATTR_URI, skinURI.toString());
        parent.appendChild(node);

    }


    private static <WE extends WizardEffect> void transOrEfect(
            Element parent,
            String tag,
            ShowSelectionModel lsm,
            List<WE> wizardList
            ) {

        Document doc = parent.getOwnerDocument();
        Element beginTransElement = doc.createElement(tag);
        parent.appendChild(beginTransElement);
        Iterator<WE> it = wizardList.listIterator();
        int idx = 0;
        while (it.hasNext()) {
            WizardEffect we = it.next();
            if (lsm.isSelectedIndex(idx++)) {
                Element transElement = doc.createElement(Constants.XML_TAG_SELECTION);
                beginTransElement.appendChild(transElement);
                String key = we.getKey();
                transElement.setAttribute(Constants.XML_ATTR_EFFECT_OR_TRANS_NAME, key);
            }
        }

    }

    private static void beginTrans(
            Element parent,
            ShowModel show
            ) {
        transOrEfect(
                parent,
                Constants.XML_TAG_BEGIN_TRANS_LIST,
                show.getBeginTransitionSelectionModel(),
                Wizard.LIST_BEGINTRANSITIONS
        );

    }

    private static void effects(
            Element parent,
            ShowModel show
            ) {
        transOrEfect(
                parent,
                Constants.XML_TAG_EFFECT_LIST,
                show.getEffectSelectionModel(),
                Wizard.LIST_EFFECTS
        );
    }

    private static void endTrans(
            Element parent,
            ShowModel show
            ) {
        transOrEfect(
                parent,
                Constants.XML_TAG_END_TRANS_LIST,
                show.getEndTransitionSelectionModel(),
                Wizard.LIST_ENDTRANSITIONS

        );
    }

    private void photos(
            Element parent,
            ShowModel show
            ) throws IOException {
        Document doc = parent.getOwnerDocument();
        Element photosElement = doc.createElement(Constants.XML_TAG_PHOTOEFFECTS_LIST);
        parent.appendChild(photosElement);
        List<PhotoEffects> photoList = show.getPhotoEffectList();
        Iterator<PhotoEffects> it = photoList.listIterator();
        while (it.hasNext()) {
            PhotoEffects pe = it.next();
            photoEffectElement = doc.createElement(Constants.XML_TAG_PHOTOEFFECT);
            photoEffectElement.setAttribute(Constants.XML_ATTR_PHOTO_SCALE, String.valueOf(pe.getPhotoScale()));
            photoEffectElement.setAttribute(
                    Constants.XML_ATTR_BEGIN_TRANS_DURATION,
                    String.valueOf(pe.getBeginTransitionDuration()));
            photoEffectElement.setAttribute(
                    Constants.XML_ATTR_EFFECT_DURATION,
                    String.valueOf(pe.getEffectDuration()));
            photoEffectElement.setAttribute(
                    Constants.XML_ATTR_END_TRANS_DURATION,
                    String.valueOf(pe.getEndTransitionDuration()));
            photoEffectElement.setAttribute(
                    Constants.XML_ATTR_END_TRANS_ON_TOP,
                    String.valueOf(pe.isEndTransitionTopLayer())
            );
            photoEffectElement.setAttribute(
                    Constants.XML_ATTR_LOCKED,
                    String.valueOf(pe.isLocked())
            );
            photosElement.appendChild(photoEffectElement);

            Element photoElement = doc.createElement(Constants.XML_TAG_PHOTO);
            photoEffectElement.appendChild(photoElement);

            photoElement.setAttribute(Constants.XML_ATTR_FILE, getFilePath(fileTarget, pe.getPhoto().getFile()));
            photoElement.setAttribute(Constants.XML_ATTR_WIDTH, String.valueOf(pe.getPhoto().getWidth()));
            photoElement.setAttribute(Constants.XML_ATTR_HEIGHT, String.valueOf(pe.getPhoto().getHeight()));

            String eventArg = pe.getEventArg();
            if (eventArg != null) {
                Element eventArgElement = doc.createElement(Constants.XML_TAG_EVENT_ARG);
                eventArgElement.appendChild(doc.createTextNode(eventArg));
                photoEffectElement.appendChild(eventArgElement);
            }

            pe.getBeginTransition().accept(this);
            pe.getEffect().accept(this);
            pe.getEndTransition().accept(this);

        }
    }


    private void mp3s(
            Element parent,
            ShowModel show
            ) throws IOException {
        Document doc = parent.getOwnerDocument();
        Element mp3ListElement = doc.createElement(Constants.XML_TAG_MP3_LIST);
        parent.appendChild(mp3ListElement);
        // model currently supports single mp3. We build a list here to support the
        // exptected future use.
        List<MP3> mp3List = new ArrayList<MP3>();
        MP3 mp3tmp = show.getMP3();
        if (mp3tmp != null) {
            mp3List.add(mp3tmp);
        }
        Iterator<MP3> it = mp3List.listIterator();
        while (it.hasNext()) {
            MP3 mp3 = it.next();
            Element mp3Elem = doc.createElement(Constants.XML_TAG_MP3);
            mp3ListElement.appendChild(mp3Elem);

            mp3Elem.setAttribute(Constants.XML_ATTR_FILE, getFilePath(fileTarget, mp3.getFile()));
            mp3Elem.setAttribute(Constants.XML_ATTR_DURATION, String.valueOf(mp3.getDuration()));


        }
    }


    /**
     * Create and instance and write the show to the specified xml file. Close the outputstream when done.
     * @param show
     * @param out
     * @throws InvalidModelException
     */
    public static void toXML(ShowModel show, OutputStream out)
            throws InvalidModelException,
            IOException {
        ModelWriter exporter = new ModelWriter();
        exporter.process(show, out, true);
    }

    private void setEffectAttributes(Element elem, BaseEffect e) {
        elem.setAttribute(Constants.XML_ATTR_BASE_EFFECT_KEY, e.getTag());
    }

    public void visit(BeginTransition e) {
        Element beginTransElement = doc.createElement(Constants.XML_TAG_BEGIN_TRANSITION);
        photoEffectElement.appendChild(beginTransElement);
        setEffectAttributes(beginTransElement, e);
    }

    public void visit(EndTransition e) {
        Element endTransElement = doc.createElement(Constants.XML_TAG_END_TRANSITION);
        photoEffectElement.appendChild(endTransElement);
        setEffectAttributes(endTransElement, e);


    }

    public void visit(Effect e) {
        Element effectElement = doc.createElement(Constants.XML_TAG_EFFECT);
        photoEffectElement.appendChild(effectElement);
        setEffectAttributes(effectElement, e);

    }

    public void visit(PanZoomEffect e) {
        Element effectElement = doc.createElement(Constants.XML_TAG_EFFECT);
        photoEffectElement.appendChild(effectElement);
        setEffectAttributes(effectElement, e);
        PanZoomEffect.Keyframe[] keyFrames = e.getKeyframes();
        if (keyFrames != null)
            for (int x = 0; x < keyFrames.length; x++) {
                Element keyFrameElement = doc.createElement(Constants.XML_TAG_KEYFRAME);
                effectElement.appendChild(keyFrameElement);
                keyFrameElement.setAttribute(Constants.XML_ATTR_START_TIME, String.valueOf(keyFrames[x].getStartTime()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_ROTATION, String.valueOf(keyFrames[x].getRotation()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_SCALE, String.valueOf(keyFrames[x].getScale()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_TRANSLATE_X, String.valueOf(keyFrames[x].getTranslateX()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_TRANSLATE_Y, String.valueOf(keyFrames[x].getTranslateY()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_EASING, String.valueOf(keyFrames[x].hasEasing()));
                keyFrameElement.setAttribute(Constants.XML_ATTR_LINEAR, String.valueOf(keyFrames[x].isLinear()));

            }


    }

    public void visit(RisingFallingStuffEffect e) {
        Element effectElement = doc.createElement(Constants.XML_TAG_EFFECT);
        photoEffectElement.appendChild(effectElement);
        setEffectAttributes(effectElement, e);
        effectElement.setAttribute(Constants.XML_ATTR_STUFF, e.getStuff().toString());
        effectElement.setAttribute(Constants.XML_ATTR_DIRECTION, e.getDirection().toString());
    }


    public void visit(ToneEffect e) {
        Element effectElement = doc.createElement(Constants.XML_TAG_EFFECT);
        photoEffectElement.appendChild(effectElement);
        setEffectAttributes(effectElement, e);
        effectElement.setAttribute(Constants.XML_ATTR_TONE, e.getTone().toString());
    }

    public void visit(PanoramaEffect e) {
        Element effectElement = doc.createElement(Constants.XML_TAG_EFFECT);
        photoEffectElement.appendChild(effectElement);
        setEffectAttributes(effectElement, e);
        effectElement.setAttribute(Constants.XML_ATTR_DIRECTION, e.getDirection().toString());
    }


    private static String getFilePath(File parent, File possibleChild) throws IOException {
        if (possibleChild == null) return null;
        if (parent == null) return possibleChild.toURI().toString();
        // todo handle null parent and child
        URI parentURI = parent.isDirectory() ? parent.getCanonicalFile().toURI() : parent.getParentFile().getCanonicalFile().toURI();
        URI childURI = parentURI.relativize(possibleChild.getCanonicalFile().toURI());
        URI relativeURI = parentURI.relativize(childURI);
        return relativeURI.toString();
    }


    private void init() {
        showElement = null;
        photoEffectElement = null;
        doc = null;
        show = null;
        target = null;

    }

    private static String getHexColor(Color c) {
        int rgbColor = c.getRGB();
        long rgbColorLong = rgbColor & 0x0ffffffffL;
        // don't use toHexString - it does not work for # < 0...
        return Long.toString(rgbColorLong, 16);

    }

    Element showElement = null;
    Element photoEffectElement = null;
    Document doc = null;
    ShowModel show = null;
    OutputStream target = null;
    private static final boolean DEBUG = false;
    private File fileTarget = null;

}
