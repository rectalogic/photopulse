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

import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.ShowSelectionModel;
import com.photica.photopulse.model.effects.BaseEffect;
import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EffectRegistry;
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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


// TODO the entire approach to reading the model from XML may be flawed. A better approach
// might be to use an event driven api and callbacks to populate the model as nodes are
// read in. Another problem with the current approach is that the XPATH apis can change
// whenever the VM changes - breaking the parsing.
//
// TODO - the ModelReader currently need to understand how to read xml
// for each transition and effect. In might be better to delegate this to
// the effect itself so that the reader does not need to change new effects
// are added.

public class ModelReader {

    private ShowModel process(InputSource source)
            throws InvalidModelException, IOException {
        ShowModel model = new ShowModel();
        process(model, source);
        return model;
    }


    public ShowModel process(File f)
            throws IOException, InvalidModelException {
        if (!f.isDirectory())
            targetDirectory = f.getParentFile();
        else
            targetDirectory = f;

        BufferedInputStream fin = null;
        ShowModel model = null;
        try {
            fin = new BufferedInputStream(new FileInputStream(f));
            model = process(new InputSource(fin));
        } finally {
            if (fin != null) fin.close();
        }
        return model;
    }


    public void process(ShowModel model, File f)
            throws IOException, InvalidModelException {
        BufferedInputStream fin = null;
        if (!f.isDirectory())
            targetDirectory = f.getParentFile();
        else
            targetDirectory = f;
        try {
            fin = new BufferedInputStream(new FileInputStream(f));
            process(model, new InputSource(fin));
        } finally {
            if (fin != null) fin.close();
        }
    }

    /**
     * Read a model file and mutate the supplied model to reflect the settings of the file.
     *
     * @param model  - the model to be modified
     */
    public void process(ShowModel model, InputSource source)
            throws InvalidModelException, IOException {
        try {
            // reset errors at the beginning of processing
            invalidPhotoSet = null;
            invalidSkinName = null;
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);

            // get the root node
            Node showNode = getShowNode(doc);
            // get the file format version and throw exception if we can't handle it or if it does not exist.
            formatVersion = getAttribute(showNode, Constants.XML_ATTR_FILE_FORMAT_VERSION, true);
            if (!Constants.XML_FILE_FORMAT_VERSION.equals(formatVersion) &&
                    !Constants.XML_FILE_FORMAT_VERSION_1_0.equals(formatVersion) &&
                    !Constants.XML_FILE_FORMAT_VERSION_1_1.equals(formatVersion)) {
                throw new InvalidModelException("Invalid file format version:" + Constants.XML_FILE_FORMAT_VERSION + " != " + formatVersion);
            }


            // get attributes on the root node
            String hexBGColor = getAttribute(showNode, Constants.XML_ATTR_BACKGROUND_COLOR, true);
            model.setBackgroundColor(colorFromHexString(hexBGColor));
            double duration = getDoubleAttribute(showNode, Constants.XML_ATTR_DEFAULT_EFFECT_DURATION);
            model.setDefaultEffectDuration(duration);
            duration = getDoubleAttribute(showNode, Constants.XML_ATTR_DEFAULT_TRANSITION_DURATION);
            model.setDefaultTransitionDuration(duration);
            float frameRate = getFloatAttribute(showNode, Constants.XML_ATTR_FRAMERATE, ShowModel.DEFAULT_FRAMERATE);
            model.setFrameRate(frameRate);
            String mp3PrefStr = getAttribute(showNode, Constants.XML_ATTR_MP3_MODE, true);
            try {
                // Force value to uppercase for JDK 1.5 enums
                ShowModel.MP3Mode mp3Mode = Enum.valueOf(ShowModel.MP3Mode.class, mp3PrefStr.toUpperCase());
                model.setMP3Mode(mp3Mode);
            } catch (IllegalArgumentException e) {
                throw new InvalidModelException("Invalid attribute value " + mp3PrefStr + " for "
                        + Constants.XML_ATTR_MP3_MODE + " on node " + showNode.getNodeName(), e);
            }

            // don't require this in the xml. If it's null we we use the default set
            // in the model
            String endShowPrefStr = getAttribute(showNode, Constants.XML_ATTR_ENDSHOW_MODE, false);
            if (endShowPrefStr != null) {
                try {
                    // Force value to uppercase for JDK 1.5 enums
                    ShowModel.EndShowMode endMode = Enum.valueOf(ShowModel.EndShowMode.class, endShowPrefStr.toUpperCase());
                    model.setEndShowMode(endMode);
                } catch (IllegalArgumentException e) {
                    throw new InvalidModelException("Invalid attribute value " + endShowPrefStr + " for "
                            + Constants.XML_ATTR_ENDSHOW_MODE + " on node " + showNode.getNodeName(), e);
                }
            }


            String showTypeStr = getAttribute(showNode, Constants.XML_ATTR_SHOW_TYPE, true);
            try {
                // Force value to uppercase for JDK 1.5 enums
                ShowModel.ShowType showType = Enum.valueOf(ShowModel.ShowType.class, showTypeStr.toUpperCase());
                model.setShowType(showType);
            } catch (IllegalArgumentException e) {
                // Support old projects that used avi/mov - force them to use wmv
                if ("avi".equalsIgnoreCase(showTypeStr) || "mov".equalsIgnoreCase(showTypeStr))
                    model.setShowType(ShowModel.ShowType.WMV);
                else {
                    throw new InvalidModelException("Invalid attribute value " + showTypeStr + " for "
                            + Constants.XML_ATTR_SHOW_TYPE + " on node " + showNode.getNodeName(), e);
                }
            }

            File photoDir = getFileAttribute(showNode, Constants.XML_ATTR_PHOTO_DIRECTORY, false);
            if (photoDir != null) {
                model.setPhotoDirectory(photoDir);
            }
            File mp3Dir = getFileAttribute(showNode, Constants.XML_ATTR_MP3_DIRECTORY, false);
            if (mp3Dir != null) {
                model.setMP3Directory(mp3Dir);
            }
            File exportDir = getFileAttribute(showNode, Constants.XML_ATTR_EXPORT_DIRECTORY, false);
            if (exportDir != null) {
                model.setExportDirectory(exportDir);
            }

            File exportFile = getFileAttribute(showNode, Constants.XML_ATTR_EXPORT_FILE, false);
            if (exportFile != null) {
                model.setExportFile(exportFile);
            }


            // skin
            Skin s = loadSkin(doc);
            model.setSkin(s);

            // transitions and effects
            model.getBeginTransitionSelectionModel().clearSelection();
            loadBeginTrans(doc, model);
            model.getEffectSelectionModel().clearSelection();
            loadEffect(doc, model);
            model.getEndTransitionSelectionModel().clearSelection();
            loadEndTrans(doc, model);

            // photos
            model.getPhotoEffectList().clear();
            NodeList nphotoEffectsl = doc.getElementsByTagName(Constants.XML_TAG_PHOTOEFFECT);
            loadImages(nphotoEffectsl, model);

            // mp3s
            NodeList mp3List = doc.getElementsByTagName(Constants.XML_TAG_MP3);
            loadMP3s(mp3List, model);
        } catch (ParserConfigurationException e) {
            throw new InvalidModelException(e);
        } catch (DOMException e) {
            throw new InvalidModelException(e);
        } catch (SAXException e) {
            throw new InvalidModelException(e);
        } catch (URISyntaxException e) {
            throw new InvalidModelException(e);
        }

    }

    private String getAttribute(Node n, String attribute, boolean required) throws InvalidModelException {
        Node node = n.getAttributes().getNamedItem(attribute);
        if (node != null) {
            return node.getNodeValue();
        }
        else if (required) {
            throw new InvalidModelException("Required Attribute " + attribute + " for node " + n.getNodeName() + " missing");
        }
        else {
            return null;
        }
    }

    private int getIntAttribute(Node n, String attribute, boolean required) throws InvalidModelException {
        try {
            return Integer.parseInt(getAttribute(n, attribute, required));
        } catch (NumberFormatException e) {
            throw new InvalidModelException(e);
        }
    }

    /**
     * Get a required doubleAddribute.
     * @param n
     * @param attribute
     * @return
     * @throws InvalidModelException
     */
    private double getDoubleAttribute(Node n, String attribute) throws InvalidModelException {
        try {
            return Double.parseDouble(getAttribute(n, attribute, true));
        } catch (NumberFormatException e) {
            throw new InvalidModelException(e);
        }
    }

    /**
     * Get the double attribute and return the provided default value if the attrubute
     * does not exist. NOTE that an InvalidModelException can still be thrown if the value
     * cannot be parsed as a double
     * @param n
     * @param attribute
     * @return
     */
    private double getDoubleAttribute(Node n, String attribute, double defaultValue) throws InvalidModelException {
        double ret = defaultValue;
        try {
            String val = getAttribute(n, attribute, false);
            if (val != null) ret = Double.parseDouble(val);
        } catch (NumberFormatException e) {
            throw new InvalidModelException(e);
        }
        return ret;
    }

    /**
     * Get the float attribute and return the provided default value if the attrubute
     * does not exist. NOTE that an InvalidModelException can still be thrown if the value
     * cannot be parsed as a float
     * @param n
     * @param attribute
     * @return
     */
    private float getFloatAttribute(Node n, String attribute, float defaultValue) throws InvalidModelException {
        float ret = defaultValue;
        try {
            String val = getAttribute(n, attribute, false);
            if (val != null) ret = Float.parseFloat(val);
        } catch (NumberFormatException e) {
            throw new InvalidModelException(e);
        }
        return ret;
    }

    private boolean getBooleanAttribute(Node n, String attribute, boolean required) throws InvalidModelException {
        return new Boolean(getAttribute(n, attribute, required)).booleanValue();
    }

    private File getFileAttribute(Node n, String attribute, boolean required) throws InvalidModelException {
        String s = getAttribute(n, attribute, required);
        if (s == null) return null;

        // version 1.0 files are absolute file paths - handle them here
        if (formatVersion.equals(Constants.XML_FILE_FORMAT_VERSION_1_0)) return new File(s);


        try {
            URI targetURI = targetDirectory.toURI();
            URI resolvedURI = targetURI.resolve(s);
            return new File(resolvedURI);
        } catch (IllegalArgumentException e) {
            throw new InvalidModelException(e);
        }
        /* if( s.startsWith(Constants.PROJECT_DIR))  return new File(targetDirectory, s); */
        // return new File (s);
    }


    private Node getShowNode(Document doc) throws InvalidModelException {
        NodeList nl = doc.getElementsByTagName(Constants.XML_TAG_SHOW);
        if (nl.getLength() != 1) {
            throw new InvalidModelException("Expected 1 " + Constants.XML_TAG_SHOW + " element, found " + nl.getLength());
        }
        return nl.item(0);
    }


    /**
     * Load begin transition selections into the model.an
     * @param doc
     * @param model
     */
    private void loadBeginTrans(Document doc, ShowModel model) throws InvalidModelException {
        loadTransOrEffect(
                doc,
                Constants.XML_TAG_BEGIN_TRANS_LIST,
                Wizard.LIST_BEGINTRANSITIONS,
                model.getBeginTransitionSelectionModel()
        );
    }

    private void loadEffect(Document doc, ShowModel model) throws InvalidModelException {
        loadTransOrEffect(
                doc,
                Constants.XML_TAG_EFFECT_LIST,
                Wizard.LIST_EFFECTS,
                model.getEffectSelectionModel()
        );
    }

    private void loadEndTrans(Document doc, ShowModel model) throws InvalidModelException {
        loadTransOrEffect(
                doc,
                Constants.XML_TAG_END_TRANS_LIST,
                Wizard.LIST_ENDTRANSITIONS,
                model.getEndTransitionSelectionModel()
        );
    }

    /**
     *
     * @param doc
     * @param tag xml tag - only ONE of this tag allowed in doc
     * @param transList
     * @param lsm
     */
    private <WE extends WizardEffect> void loadTransOrEffect(
            Document doc,
            String tag,
            List<WE> transList,
            ShowSelectionModel lsm
            ) throws InvalidModelException {

        //XXX NodeList parent  = doc.getElementsByTagName(tag);
        //XXX Element elem = (Element)parent.item(0);
        Element elem = getRequiredElement(doc, tag);
        NodeList btList = elem.getElementsByTagName(Constants.XML_TAG_SELECTION);

        int sz = btList.getLength();
        lsm.clearSelection();
        List<WE> l = transList;
        int listSize = l.size();
        HashMap<String,Integer> nameIndexMap = new HashMap<String, Integer>();
        for (int idx = 0; idx < listSize; idx++) {
            WizardEffect e = l.get(idx);
            String name = e.getKey();
            nameIndexMap.put(name, new Integer(idx));
        }
        for (int x = 0; x < sz; x++) {
            Node n = btList.item(x);
            String name = getAttribute(n, Constants.XML_ATTR_EFFECT_OR_TRANS_NAME, true);
            Object o = nameIndexMap.get(name);
            if (o != null) {
                Integer i = (Integer)o;
                int ii = i.intValue();
                lsm.addSelectionInterval(ii, ii);
            }
        }
    }


    /**
     *
     * @param imageList - NodeList of Elements
     * @param model
     * @throws InvalidModelException
     */
    private void loadImages(NodeList imageList, ShowModel model) throws InvalidModelException {
        EffectVisitorCreator effectCreator = new EffectVisitorCreator();

        int sz = imageList.getLength();
        HashSet<File> errors = null;
        HashMap<File,Photo> validated = null;
        if (sz > 0) {
            errors = new HashSet<File>();
            validated = new HashMap<File,Photo>();
        }
        nextPhoto: for (int x = 0; x < sz; x++) {
            Element n = (Element)imageList.item(x);
            File photoFile = getPhotoFile(n);

            // Get the file object and validate the photo
            Photo photo = validated.get(photoFile);
            boolean isInvalidPhoto = errors.contains(photoFile);
            // if photo is not valid then stop processing
            if (isInvalidPhoto)
                continue nextPhoto;
            // if photo has not be tested then test and if invalid stop
            // procssing, or if valid, continue processing
            else if (photo == null) {
                photo = ImageCoder.getInstance().validatePhotoFile(photoFile);
                if (photo == null) {
                    errors.add(photoFile);
                    continue nextPhoto;
                }
                else {
                    validated.put(photoFile, photo);
                }
            }

            String eventArg = getEventArg(n);

            double photoScale = getDoubleAttribute(n, Constants.XML_ATTR_PHOTO_SCALE, ShowModel.DEFAULT_PHOTO_SCALE);

            BeginTransition bt = getBeginTransition(n);
            double btTime = getDoubleAttribute(n, Constants.XML_ATTR_BEGIN_TRANS_DURATION);

            EndTransition et = getEndTransition(n);
            double etTime = getDoubleAttribute(n, Constants.XML_ATTR_END_TRANS_DURATION);

            Effect ef = getEffect(n, effectCreator);
            double efTime = getDoubleAttribute(n, Constants.XML_ATTR_EFFECT_DURATION);

            boolean etOnTop = getBooleanAttribute(n, Constants.XML_ATTR_END_TRANS_ON_TOP, true);
            boolean isLocked = getBooleanAttribute(n, Constants.XML_ATTR_LOCKED, false);

            PhotoEffects pe = new PhotoEffects(
                    photo, eventArg, photoScale,
                    bt, btTime,
                    ef, efTime,
                    et, etTime,
                    etOnTop, isLocked
            );

            model.getPhotoEffectList().add(pe);

        }
        invalidPhotoSet = errors;
    }

    /**
     * If we are validating photos then a list of invalid photo files is kept
     * so the user can be informed. This method can be called after reading
     * a persisted model file to get a list of photo errors.
     * @return iterator if invalid File objects
     */
    public Iterator invalidPhotoIterator() {
        if (invalidPhotoSet != null)
            return invalidPhotoSet.iterator();
        else
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }

                // there is no next
                public Object next() {
                    throw new NoSuchElementException();
                }

                // since there are no objects remove is illegal
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
    }

    /**
     * If the model contained an invalid skin, return the name
     * @return null if skin was valid
     */
    public String getInvalidSkinName() {
        return invalidSkinName;
    }

    private void loadMP3s(NodeList mp3List, ShowModel model)
            throws InvalidModelException {

        int sz = mp3List.getLength();
        for (int x = 0; x < sz; x++) {
            Node n = mp3List.item(x);
            MP3 mp3 = getMP3(n);
            model.setMP3(mp3);
        }
    }


    private Skin loadSkin(Document doc) throws URISyntaxException, InvalidModelException {
        NodeList nl = doc.getElementsByTagName(Constants.XML_TAG_SKIN);
        if (nl.getLength() != 1) {
            throw new InvalidModelException("Single Skin Node Required");
        }
        Node skinNode = nl.item(0);
        String uriStr = getAttribute(skinNode, Constants.XML_ATTR_URI, true);
        URI uri = new URI(uriStr);
        Skin skin = SkinManager.getSkin(uri);
        if (skin == null)
            invalidSkinName = uri.getPath();
        return skin;
    }

    private Element getOptionalElement(Element n, String name) {
        NodeList nl = n.getElementsByTagName(name);
        if (nl.getLength() == 1)
            return (Element)nl.item(0);
        return null;
    }

    /**
     * Get a required element - there must be one and only one element
     * @param n the parent element
     * @param name tagname of child element
     * @return
     */
    private Element getRequiredElement(Element n, String name) throws InvalidModelException {
        Element el = getOptionalElement(n, name);
        if (el == null)
            throw new InvalidModelException(name + " is a required child of " + n.getNodeName());
        return el;
    }

    private Element getRequiredElement(Document doc, String name) throws InvalidModelException {
        NodeList nl = doc.getElementsByTagName(name);
        if (nl.getLength() == 1) {
            return (Element)nl.item(0);
        }
        else {
            throw new InvalidModelException(name + " is a required child of the document");
        }
    }

    private File getPhotoFile(Element n) throws InvalidModelException {
        n = getRequiredElement(n, Constants.XML_TAG_PHOTO);
        if (n != null) {
            return getFileAttribute(n, Constants.XML_ATTR_FILE, true);
        }
        else {
            return null;
        }
    }

    private String getEventArg(Element n) {
        n = getOptionalElement(n, Constants.XML_TAG_EVENT_ARG);
        if (n == null)
            return null;
        Node textNode = n.getFirstChild();
        if (textNode == null || textNode.getNodeType() != Node.TEXT_NODE)
            return null;
        return textNode.getNodeValue();
    }

    private MP3 getMP3(Node n) throws InvalidModelException {
        // TODO this will break as soon as more than 1 mp3 file is
        // aaa
        // n = getRequiredElement(n,Constants.XML_TAG_MP3);
        if (n != null) {
            File file = getFileAttribute(n, Constants.XML_ATTR_FILE, true);
            double dur = getDoubleAttribute(n, Constants.XML_ATTR_DURATION);
            return new MP3(file, dur);
        }
        else {
            return null;
        }
    }


    private BeginTransition getBeginTransition(Element n)
            throws InvalidModelException {
        n = getRequiredElement(n, Constants.XML_TAG_BEGIN_TRANSITION);
        if (n != null) {
            BeginTransition bt = (BeginTransition)getBaseEffect(n, BEGIN_TRANS);
            return bt;
        }
        else {
            return null;
        }
    }

    // get a base effect
    private BaseEffect getBaseEffect(Node n, int type) throws InvalidModelException {
        String key = getAttribute(n, Constants.XML_ATTR_BASE_EFFECT_KEY, true);
        BaseEffect bt = getEffect(key, type);
        return bt;
    }

    private EndTransition getEndTransition(Element n)
            throws InvalidModelException {
        n = getRequiredElement(n, Constants.XML_TAG_END_TRANSITION);
        if (n != null) {
            EndTransition et = (EndTransition)getBaseEffect(n, END_TRANS);
            return et;
        }
        else {
            return null;
        }
    }

    private Effect getEffect(Element n, EffectVisitorCreator effectCreator) throws InvalidModelException {
        n = getRequiredElement(n, Constants.XML_TAG_EFFECT);
        if (n != null) {
            BaseEffect proto = getBaseEffect(n, EFFECT);
            if (proto != null) {
                effectCreator.setCurrentNode(n);
                proto.accept(effectCreator);
                if (effectCreator.getException() != null)
                    throw effectCreator.getException();
                else
                    return (Effect)effectCreator.getNewEffect();
            }
        }
        return null;
    }

    /**
     * Given an effect key find the static instance in the EffectRegistry
     * @param key
     * @param type
     * @return the transition/effect associated with the key or the
     *  apropriate None effect
     */
    private BaseEffect getEffect(String key, int type) {
        BaseEffect ret = null;
        switch (type) {

        case BEGIN_TRANS:
            ret = EffectRegistry.findBeginTransition(key);
            if (ret == null) {
                ret = EffectRegistry.BEGINTRANS_None;
            }
            break;
        case EFFECT:
            ret = EffectRegistry.findEffect(key);
            if (ret == null) {
                ret = EffectRegistry.EFFECT_None;
            }
            break;
        case END_TRANS:
            ret = EffectRegistry.findEndTransition(key);
            if (ret == null) {
                ret = EffectRegistry.ENDTRANS_None;
            }
            break;
        default:
            assert false; // :"Transition ";
        }
        return ret;
    }

    private Color colorFromHexString(String hexColor) {
        Color retColor = ShowModel.DEFAULT_BACKGROUND_COLOR;

        try {
            long l = Long.parseLong(hexColor, 16);
            int i = (int)l;
            retColor = new Color(i);
        } catch (NumberFormatException ex) {
            ; // ignore number format exception
        }
        return retColor;

    }

    private class EffectVisitorCreator implements EffectVisitor {
        private Element currentNode;
        private BaseEffect newEffect;
        private InvalidModelException exception;

        public void setCurrentNode(Element node) {
            this.currentNode = node;
            newEffect = null;
            exception = null;
        }

        public BaseEffect getNewEffect() {
            return newEffect;
        }

        public InvalidModelException getException() {
            return exception;
        }

        public void visit(BeginTransition proto) {
        }

        public void visit(EndTransition e) {
        }

        public void visit(Effect proto) {
            newEffect = proto;
        }

        public void visit(PanZoomEffect proto) {
            try {
                NodeList keyFrameList = currentNode.getChildNodes();
                int sz = keyFrameList.getLength();

                PanZoomEffect.Keyframe[] keyFrames = new PanZoomEffect.Keyframe[0];
                ArrayList<PanZoomEffect.Keyframe> keyFrameArrayList = new ArrayList<PanZoomEffect.Keyframe>();

                for (int x = 0; x < sz; x++) {
                    Node kfNode = keyFrameList.item(x);
                    if (kfNode.getNodeType() == Node.ELEMENT_NODE) {
                        double startTime = 0.0;
                        // get the optional start time - defaulting to 0.0 if the attribute does
                        // no exist
                        startTime = getDoubleAttribute(kfNode, Constants.XML_ATTR_START_TIME, startTime);

                        double rot = getDoubleAttribute(kfNode, Constants.XML_ATTR_ROTATION);
                        double sc = getDoubleAttribute(kfNode, Constants.XML_ATTR_SCALE);
                        double tx = getDoubleAttribute(kfNode, Constants.XML_ATTR_TRANSLATE_X);
                        double ty = getDoubleAttribute(kfNode, Constants.XML_ATTR_TRANSLATE_Y);
                        boolean ea = getBooleanAttribute(kfNode, Constants.XML_ATTR_EASING, true);
                        boolean li = getBooleanAttribute(kfNode, Constants.XML_ATTR_LINEAR, true);
                        PanZoomEffect.Keyframe kf = new PanZoomEffect.Keyframe(startTime, li, ea, tx, ty, sc, rot);
                        keyFrameArrayList.add(kf);
                    }
                    keyFrames = keyFrameArrayList.toArray(keyFrames);
                }

                newEffect = proto.clonePanZoomEffect(keyFrames);
            } catch (IllegalArgumentException e) {
                newEffect = null;
                exception = new InvalidModelException("PanZoomEffect: invalid keyframes", e);
            } catch (InvalidModelException e) {
                newEffect = null;
                exception = e;
            }
        }

        public void visit(RisingFallingStuffEffect proto) {
            try {
                // get attr - thows exception if not exists, lookup val,
                String stuffStr = getAttribute(currentNode, Constants.XML_ATTR_STUFF, true);
                RisingFallingStuffEffect.Stuff myStuff = null;
                try {
                    // Force value to uppercase for JDK 1.5 enums
                    myStuff = Enum.valueOf(RisingFallingStuffEffect.Stuff.class, stuffStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new InvalidModelException("RisingFallingStuffEffect invalid attribute value: (" + Constants.XML_ATTR_STUFF + ")" + stuffStr, e);
                }

                String directionStr = getAttribute(currentNode, Constants.XML_ATTR_DIRECTION, true);
                RisingFallingStuffEffect.Direction direction = null;
                try {
                    // Force value to uppercase for JDK 1.5 enums
                    direction = Enum.valueOf(RisingFallingStuffEffect.Direction.class, directionStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new InvalidModelException("RisingFallingStuffEffect invalid attribute value: (" + Constants.XML_ATTR_DIRECTION + ")" + directionStr, e);
                }

                if (myStuff != proto.getStuff() || direction != proto.getDirection())
                    newEffect = proto.cloneRisingFallingStuffEffect(myStuff, direction);
                else
                    newEffect = proto;
            } catch (InvalidModelException e) {
                newEffect = null;
                exception = e;
            }
        }

        public void visit(ToneEffect proto) {
            try {
                ToneEffect.Tone tone = ToneEffect.Tone.SEPIA;
                // Don't require tone attr, default to sepia to support old projects that used the Tint effect
                String toneStr = getAttribute(currentNode, Constants.XML_ATTR_TONE, false);
                if (toneStr != null) {
                    try {
                        // Force value to uppercase for JDK 1.5 enums
                        tone = Enum.valueOf(ToneEffect.Tone.class, toneStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new InvalidModelException("ToneEffect invalid attribute value: (" + Constants.XML_ATTR_TONE + ")" + toneStr, e);
                    }
                }

                if (proto.getTone() != tone)
                    newEffect = proto.cloneToneEffect(tone);
                else
                    newEffect = proto;
            } catch (InvalidModelException e) {
                newEffect = null;
                exception = e;
            }
        }

        public void visit(PanoramaEffect proto) {
            try {
                // required - throws exception if does not exist
                String dirStr = getAttribute(currentNode, Constants.XML_ATTR_DIRECTION, true);
                // still possible we don't suceed in lookup
                PanoramaEffect.Direction dir = null;
                try {
                    // Force value to uppercase for JDK 1.5 enums
                    dir = Enum.valueOf(PanoramaEffect.Direction.class, dirStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new InvalidModelException("PanoramaEffect invalid attribute value: (" + Constants.XML_ATTR_DIRECTION + ")" + dirStr, e);
                }
                if (proto.getDirection() != dir)
                    newEffect = proto.clonePanoramaEffect(dir);
                else
                    newEffect = proto;
            } catch (InvalidModelException e) {
                newEffect = null;
                exception = e;
            }
        }
    }


    private static final int BEGIN_TRANS = 1;
    private static final int EFFECT = 2;
    private static final int END_TRANS = 3;

    private HashSet invalidPhotoSet = null;
    private String invalidSkinName = null;
    private File targetDirectory = null;
    private String formatVersion = null;
}

