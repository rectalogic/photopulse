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

package com.photica.photopulse.skin;

import com.iv.flash.api.Frame;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.shape.Shape;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Iterator;

/**
 * An individual skin at a specific size. Part of a SkinSet.
 * A Skin must be loaded via Skin.load() before use if it has fg/bg assets.
 * A Skin must be Skin.init()ed at least once before using name/sizes.
 */
public class Skin {

    private SkinSet skinSet;

    // Flash pathname to frame to be called on show events
    private String eventHandler;

    // JGenerator params for this skin
    private Map<String,String> skinParams;

    private SkinElement[] foregroundElements;
    private SkinElement[] backgroundElements;
    private SkinExternal[] externalElements;

    private Dimension skinSize;
    private Rectangle stageBounds;
    private float stageAspectRatio;

    private String name;
    private String displayName;

    // Dimensions in pixels (stored in twips internally)
    Skin(SkinSet skinSet, String skinName, String displayName, Map<String,String> skinParams,
            SkinElement[] foregroundElements, SkinElement[] backgroundElements, SkinExternal[] externalElements,
            Dimension2D skinSize, Rectangle2D stageBounds, String eventHandler) {

        if (skinSize == null
                && (backgroundElements == null || backgroundElements.length != 1
                    || backgroundElements[0] == null || backgroundElements[0].getURL() == null)
                && (foregroundElements == null || foregroundElements.length != 1
                    || foregroundElements[0] == null || foregroundElements[0].getURL() == null))
            throw new NullPointerException("null skin size and no background or foreground");

        // Can be null
        this.name = skinName;

        // Can be null
        this.displayName = displayName;

        // Can be null
        this.skinParams = skinParams;

        // Can be null
        this.eventHandler = eventHandler;

        this.foregroundElements = foregroundElements;
        this.backgroundElements = backgroundElements;
        this.externalElements = externalElements;

        // Convert pixels to twips
        if (skinSize != null)
            this.skinSize = new Dimension((int)skinSize.getWidth()*ShowGenerator.TWIPS_PER_PIXEL, (int)skinSize.getHeight()*ShowGenerator.TWIPS_PER_PIXEL);
        if (stageBounds != null)
            this.stageBounds = new Rectangle((int)stageBounds.getX()*ShowGenerator.TWIPS_PER_PIXEL, (int)stageBounds.getY()*ShowGenerator.TWIPS_PER_PIXEL,
                (int)stageBounds.getWidth()*ShowGenerator.TWIPS_PER_PIXEL, (int)stageBounds.getHeight()*ShowGenerator.TWIPS_PER_PIXEL);

        // Skins with no fg/bg are effectively preloaded
        if (this.foregroundElements == null && this.backgroundElements == null) {
            try {
                init();
            } catch (SkinException e) {
                // This shouldn't happen since we have no fg/bg
            }
        }

        this.skinSet = skinSet;
        if (skinSet != null)
            skinSet.addSkin(this);
    }

    // For builtin skinset
    // skinSize can not be null
    Skin(SkinSet skinset, Dimension2D skinSize) {
        this(skinset, null, null, null, null, null, null, skinSize, null, null);
    }

    // For size-only skins, not part of a skinset
    // skinSize can not be null
    public Skin(Dimension2D skinSize) {
        this(null, null, null, null, null, null, null, skinSize, null, null);
    }

    public SkinSet getSkinSet() {
        return skinSet;
    }

    public String getName() {
        return name;
    }

    public String getEventHandler() {
        return eventHandler;
    }

    /**
     * Iterator over Map.Entry elements or null, key is param name, value is value
     */
    public Iterator<Map.Entry<String,String>> iterateSkinParams() {
        if (skinParams == null)
            return null;
        return skinParams.entrySet().iterator();
    }

    /**
     * This should be called before using a skin.
     * The skin should be unloaded when finished.
     */
    public void load() throws SkinException {
        if (foregroundElements != null) {
            for (int i = 0; i < foregroundElements.length; i++)
                foregroundElements[i].load();
        }
        if (backgroundElements != null) {
            for (int i = 0; i < backgroundElements.length; i++)
                backgroundElements[i].load();
        }
        if (externalElements != null) {
            for (int i = 0; i < externalElements.length; i++)
                externalElements[i].load();
        }
    }

    /**
     * This should be called after using a skin.
     * The skin files may still remain cached.
     */
    public void unload() {
        if (foregroundElements != null) {
            for (int i = 0; i < foregroundElements.length; i++)
                foregroundElements[i].unload();
        }
        if (backgroundElements != null) {
            for (int i = 0; i < backgroundElements.length; i++)
                backgroundElements[i].unload();
        }
        if (externalElements != null) {
            for (int i = 0; i < externalElements.length; i++)
                externalElements[i].unload();
        }
    }

    /**
     * Should be called once to init a skin, before accessing sizes/name.
     * May safely be called more than once
     */
    public void init() throws SkinException {
        // If no size, compute from background or foreground skin element
        if (skinSize == null) {
            SkinElement element = backgroundElements != null ? backgroundElements[0] : foregroundElements[0];
            element.load();
            try {
                Rectangle2D rect = element.getNativeBounds();
                skinSize = new Dimension((int)rect.getWidth(), (int)rect.getHeight());
            } finally {
                element.unload();
            }
        }

        // If null, compute from skin size
        if (stageBounds == null) {
            stageBounds = new Rectangle(skinSize);
        }

        if (displayName == null)
            displayName = buildDisplayName();

        if (stageAspectRatio == 0)
            stageAspectRatio = (float)(stageBounds.getWidth() / stageBounds.getHeight());
    }

    // Build skin name from stage size
    private String buildDisplayName() {
        int width = (int)stageBounds.getWidth()/ShowGenerator.TWIPS_PER_PIXEL;
        int height = (int)stageBounds.getHeight()/ShowGenerator.TWIPS_PER_PIXEL;
        String message = null;
        if (width > height) {
            if (width >= 600)
                message = SystemMessages.UI_SKIN_NAME_LARGE_LANDSCAPE;
            else
                message = SystemMessages.UI_SKIN_NAME_SMALL_LANDSCAPE;
        }
        else if (height > width) {
            if (height >= 600)
                message = SystemMessages.UI_SKIN_NAME_LARGE_PORTRAIT;
            else
                message = SystemMessages.UI_SKIN_NAME_SMALL_PORTRAIT;
        }
        else {
            if (width >= 600)
                message = SystemMessages.UI_SKIN_NAME_LARGE;
            else
                message = SystemMessages.UI_SKIN_NAME_SMALL;
        }

        return SystemMessages.getMessage(message,
            new Object[] { new Integer(width), new Integer(height) });
    }

    /**
     * @return Overall skin size in twips
     */
    public Dimension getSkinSize() {
        return skinSize;
    }

    /**
     * @return Overall skin size converted to pixels
     */
    public Dimension getSkinSizePixels() {
        return new Dimension(skinSize.width/ShowGenerator.TWIPS_PER_PIXEL,
                skinSize.height/ShowGenerator.TWIPS_PER_PIXEL);
    }

    /**
     * @return Stage bounds within skin in twips
     */
    public Rectangle getStageBounds() {
        return stageBounds;
    }

    /**
     * @return Stage aspect ratio
     */
    public float getStageAspectRatio() {
        return stageAspectRatio;
    }

    public void addExternalElements(FlashFile flashFile) {
        if (externalElements == null)
            return;

        for (int i = 0; i < externalElements.length; i++)
            externalElements[i].addExternal(flashFile);
    }

    public Instance getForegroundSkin() {
        return getSkinInstance("foreground", foregroundElements);
    }

    public Instance getBackgroundSkin() {
        return getSkinInstance("background", backgroundElements);
    }

    private Instance getSkinInstance(String name, SkinElement[] elements) {
        if (elements == null)
            return null;

        // Create empty script to hold elements
        Script script = new Script(1);
        Frame frame = script.newFrame();

        // Add all elements to frame.
        // Layer in array order bottom to top
        // Mask each one if needed
        int depth = 1;
        for (int i = 0; i < elements.length; i++) {
            Instance inst = elements[i].getInstance();
            frame.addInstance(inst, depth+1);
            Shape mask = elements[i].getMask();
            if (mask != null)
                frame.addInstance(mask, depth, null, null).clip = depth+1;
            depth += 2;
        }

        Instance instance = new Instance();
        instance.name = name;
        instance.def = script;
        return instance;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String toString() {
        return getDisplayName();
    }
}