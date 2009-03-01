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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.flash;

import com.iv.flash.xml.XMLHelper;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.model.effects.EffectVisitor;
import com.photica.photopulse.model.effects.EndTransition;
import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.model.effects.PanoramaEffect;
import com.photica.photopulse.model.effects.RisingFallingStuffEffect;
import com.photica.photopulse.model.effects.ToneEffect;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;

/**
 * Build an XML document from a ShowModel.
 * This is the document used in JGenerator templates.
 */
public class ShowDocumentBuilder implements EffectVisitor {

    // Max number of photos allowed if unlicensed
    private static final int MAX_UNLICENSED_PHOTOS = 5;

    // Current Element for visitors
    private Element photoElement;
    private Rectangle2D stageBounds;
    private PhotoEffects currentEffects;
    private double imageScale = 1.0;

    /**
     * Build Flash XML Document from the model. Assumes the model skin has been loaded.
     */
    public Document buildDocument(ShowModel model) {
        DocumentBuilder docBuilder = XMLHelper.getDocumentBuilder();
        Document document = docBuilder.newDocument();
        document.appendChild(document.createElement("photopulse"));

        // Stage bounds in twips
        stageBounds = model.getSkin().getStageBounds();

        float frameRate = model.getFrameRate();

        double accumulatedDuration = 0;
        double accumulatedQuantizedDuration = 0;
        double prevEndTransDuration = 0;

        ShowList effectList = model.getPhotoEffectList();
        int size = effectList.size();
        for (int i = 0; i < size; i++) {
            PhotoEffects effects = (PhotoEffects)effectList.get(i);

            // If unlicensed, reuse the first 5 photos
            Photo photo = effects.getPhoto();
            if (PhotoPulse.LICENSE == null)
                photo = ((PhotoEffects)effectList.get(i % MAX_UNLICENSED_PHOTOS)).getPhoto();

            // Accumulate total duration and frame-quantized duration
            double effectDuration = effects.getEffectDuration();
            double transDuration = Math.max(prevEndTransDuration, effects.getBeginTransitionDuration());
            accumulatedDuration += transDuration + effectDuration;
            accumulatedQuantizedDuration += quantizeDuration(transDuration, frameRate) + quantizeDuration(effectDuration, frameRate);

            // If this is the last photo
            if (i == size - 1) {
                // Include endtrans duration for last photo
                accumulatedDuration += effects.getEndTransitionDuration();
                accumulatedQuantizedDuration += quantizeDuration(effects.getEndTransitionDuration(), frameRate);

                // If the show pauses at the end, force the last end trans to be None, even if locked
                if (model.getEndShowMode() == ShowModel.EndShowMode.PAUSE) {
                    effects = new PhotoEffects(effects.getPhoto(), effects.getEventArg(), effects.getPhotoScale(),
                            effects.getBeginTransition(), effects.getBeginTransitionDuration(),
                            effects.getEffect(), effects.getEffectDuration(),
                            EffectRegistry.ENDTRANS_None, effects.getEndTransitionDuration(), effects.isEndTransitionTopLayer(), effects.isLocked());
                }
            }

            // If quantization error is larger than a frame, add to effect duration
            double quantizationError = accumulatedDuration - accumulatedQuantizedDuration;
            if (quantizationError >= 1.0 / frameRate) {
                double extraFramesTime = quantizeDuration(quantizationError, frameRate);
                effectDuration += extraFramesTime;
                accumulatedQuantizedDuration += extraFramesTime;
            }
            // If there is any error left at the last photo, add a frame
            if (i == size - 1 && quantizationError > 0)
                effectDuration += 1.0 / frameRate;

            addPhotoEffects(document, effects, photo, effectDuration);

            prevEndTransDuration = effects.getEndTransitionDuration();
        }

        return document;
    }

    private double quantizeDuration(double duration, float frameRate) {
        return Math.floor(duration * frameRate) / frameRate;
    }

    private void addPhotoEffects(Document document, PhotoEffects effects, Photo photo, double effectDurationOverride) {
        // Create a photo element for the current photo, save it for visitors
        photoElement = document.createElement("photo");
        document.getDocumentElement().appendChild(photoElement);

        photoElement.setAttribute("imageFile", photo.getFile().getAbsolutePath());
        if (photo.isFlashPhoto())
            photoElement.setAttribute("flashPhoto", "true");

        // Scale the photo bits to a percentage of the stage.
        // Internally we never scale up (i.e. ignore imageScale>1.0)
        // imageScale applies to the photo itself, but photoScale is relative to the stage.
        imageScale = ImageCoder.getInstance().
            computeScaleFactor(photo.getWidth() * ShowGenerator.TWIPS_PER_PIXEL,
                photo.getHeight() * ShowGenerator.TWIPS_PER_PIXEL,
                stageBounds.getWidth() * effects.getPhotoScale(),
                stageBounds.getHeight() * effects.getPhotoScale());
        if (imageScale > 1.0)
            imageScale = 1.0;
        photoElement.setAttribute("imageScale", String.valueOf(imageScale));

        // Use effect duration override, this is tweaked to account for frame quantization errors
        photoElement.setAttribute("effectDuration", String.valueOf(effectDurationOverride));
        photoElement.setAttribute("beginTransDuration", String.valueOf(effects.getBeginTransitionDuration()));
        photoElement.setAttribute("endTransDuration", String.valueOf(effects.getEndTransitionDuration()));

        photoElement.setAttribute("endTransLayer", effects.isEndTransitionTopLayer() ? "top" : "bottom");

        // Add eventArg text child
        String eventArg = effects.getEventArg();
        if (eventArg != null) {
            Element argElement = document.createElement("eventArg");
            argElement.appendChild(document.createTextNode(eventArg));
            photoElement.appendChild(argElement);
        }

        // Visit the effect so we can populate the photo elements XML for this effect
        currentEffects = effects;
        effects.getBeginTransition().accept(this);
        effects.getEndTransition().accept(this);
        effects.getEffect().accept(this);
    }

    public void visit(BeginTransition e) {
        photoElement.setAttribute("beginTransClip", e.getTag());
    }

    public void visit(EndTransition e) {
        photoElement.setAttribute("endTransClip", e.getTag());
    }

    public void visit(Effect e) {
        photoElement.setAttribute("effectClip", e.getTag());
    }

    public void visit(PanZoomEffect e) {
        photoElement.setAttribute("effectClip", e.getTag());

        Document document = photoElement.getOwnerDocument();

        PanZoomEffect.Keyframe[] keyframes = e.getKeyframes();
        if (keyframes == null)
            return;

        double stageWidth2 = (stageBounds.getWidth() / ShowGenerator.TWIPS_PER_PIXEL) / 2;
        double stageHeight2 = (stageBounds.getHeight() / ShowGenerator.TWIPS_PER_PIXEL) / 2;

        for (int i = 0; i < keyframes.length; i++) {
            Element keyframeElement = document.createElement("keyframe");

            // Keyframe duration is a percentage of entire effect/trans time
            double duration;
            if (i + 1 < keyframes.length) {
                duration = keyframes[i+1].getStartTime() - keyframes[i].getStartTime();
                // If duration is 0, then this keyframe has the same start time as the next, so skip it.
                if (duration == 0)
                    continue;
            }
            // Compare last keyframe start time to 1.0
            else
                duration = 1.0 - keyframes[i].getStartTime();

            keyframeElement.setAttribute("duration", String.valueOf(duration));


            // Keyframe scale is relative to stage size, and scales the already bit-scaled photo.
            double scale;
            double keyframeScale = keyframes[i].getScale();
            if (keyframeScale == PanZoomEffect.Keyframe.UNSCALED)
                scale = 1.0;
            else {
                double scaledPhotoWidth = currentEffects.getPhoto().getWidth() * imageScale;
                double scaledPhotoHeight = currentEffects.getPhoto().getHeight() * imageScale;
                scale = ImageCoder.getInstance().
                    computeScaleFactor(scaledPhotoWidth * ShowGenerator.TWIPS_PER_PIXEL,
                        scaledPhotoHeight * ShowGenerator.TWIPS_PER_PIXEL,
                        stageBounds.getWidth() * keyframeScale,
                        stageBounds.getHeight() * keyframeScale);
            }
            keyframeElement.setAttribute("scale", String.valueOf(scale));

            keyframeElement.setAttribute("rotate", String.valueOf(keyframes[i].getRotation()));
            keyframeElement.setAttribute("easing", keyframes[i].hasEasing() ? "true" : "false");
            keyframeElement.setAttribute("linear", keyframes[i].isLinear() ? "true" : "false");

            // Translation is stored using unit coords - 0,0 is centerstage, -1,0 is stage left, 1,0 stage right
            keyframeElement.setAttribute("translateX", String.valueOf(keyframes[i].getTranslateX() * stageWidth2));
            keyframeElement.setAttribute("translateY", String.valueOf(keyframes[i].getTranslateY() * stageHeight2));

            photoElement.appendChild(keyframeElement);
        }
    }

    public void visit(RisingFallingStuffEffect e) {
        photoElement.setAttribute("effectClip", e.getTag());

        Document document = photoElement.getOwnerDocument();

        Element paramElement = document.createElement("param");
        paramElement.setAttribute("name", "stuff");
        // Must use Stuff.toString() and not name() to get the right value
        paramElement.setAttribute("value", e.getStuff().toString());
        photoElement.appendChild(paramElement);

        paramElement = document.createElement("param");
        paramElement.setAttribute("name", "direction");
        // Must use Direction.toString() and not name() to get the right value
        paramElement.setAttribute("value", e.getDirection().toString());
        photoElement.appendChild(paramElement);
    }

    public void visit(ToneEffect e) {
        photoElement.setAttribute("effectClip", e.getTag());

        Document document = photoElement.getOwnerDocument();

        Element elParam = document.createElement("param");
        elParam.setAttribute("name", "tone");
        // Must use Tone.toString() and not name() to get the right value
        elParam.setAttribute("value", e.getTone().toString());
        photoElement.appendChild(elParam);
    }

    public void visit(PanoramaEffect e) {
        photoElement.setAttribute("effectClip", e.getTag());

        Document document = photoElement.getOwnerDocument();

        Element paramElement = document.createElement("param");
        paramElement.setAttribute("name", "direction");
        // Must use Direction.toString() and not name() to get the right value
        paramElement.setAttribute("value", e.getDirection().toString());
        photoElement.appendChild(paramElement);
    }

    /**
     * Debugging utility - dump a DOM Document as XML text
     */
    public void dumpDocument(Document document, OutputStream os) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(os));
        } catch (TransformerException e) {
        }
    }
}
