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

import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.PhotoPulse;
import com.photica.photopulse.Util;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages installed skins
 */
public class SkinManager  {

    public static final SkinSet BUILTINSET;

    public static final Skin BUILTIN_SMALL_43;
    public static final Skin BUILTIN_LARGE_43;
    public static final Skin BUILTIN_SMALL_34;
    public static final Skin BUILTIN_LARGE_34;
    public static final Skin BUILTIN_SMALL_32;
    public static final Skin BUILTIN_LARGE_32;
    public static final Skin BUILTIN_SMALL_23;
    public static final Skin BUILTIN_LARGE_23;

    public static final File SKINSETDIR = new File(PhotoPulse.INSTALLDIR, "skins");

    private static final List<SkinSet> SKINSETS = new ArrayList<SkinSet>();

    private static final String SKIN_SCHEME = "skin";
    private static final String BUILTIN_SCHEME = "builtin";
    private static final String CUSTOM_SCHEME = "custom";

    private static final String SIZE_SEP = "x";

    static {

        try {
            BUILTINSET = new SkinSet(new URI(BUILTIN_SCHEME, "/", null),
                    SystemMessages.getMessage(SystemMessages.UI_BUILTIN_SKINSET),
                    SystemMessages.getMessage(SystemMessages.UI_BUILTIN_SKINSET_DESC));
        } catch (URISyntaxException e) {
            throw new ExceptionInInitializerError(e);
        }

        // 4:3 aspect
        BUILTIN_SMALL_43 = new Skin(BUILTINSET, new Dimension(320, 240));
        // 4:3 aspect
        BUILTIN_LARGE_43 = new Skin(BUILTINSET, new Dimension(640, 480));
        // 4:3 aspect
        new Skin(BUILTINSET, new Dimension(800, 600));
        // 4:3 aspect
        new Skin(BUILTINSET, new Dimension(1024, 768));

        // 3:4 aspect
        BUILTIN_SMALL_34 = new Skin(BUILTINSET, new Dimension(240, 320));
        // 3:4 aspect
        BUILTIN_LARGE_34 = new Skin(BUILTINSET, new Dimension(480, 640));
        // 3:4 aspect
        new Skin(BUILTINSET, new Dimension(600, 800));

        // 3:2 aspect
        BUILTIN_SMALL_32 = new Skin(BUILTINSET, new Dimension(300, 200));
        // 3:2 aspect
        BUILTIN_LARGE_32 = new Skin(BUILTINSET, new Dimension(600, 400));
        // 3:2 aspect
        new Skin(BUILTINSET, new Dimension(720, 480));
        // 3:2 aspect
        new Skin(BUILTINSET, new Dimension(900, 600));

        // 2:3 aspect
        BUILTIN_SMALL_23 = new Skin(BUILTINSET, new Dimension(200, 300));
        // 2:3 aspect
        BUILTIN_LARGE_23 = new Skin(BUILTINSET, new Dimension(400, 600));
    }

    public static List<SkinSet> getSkinSets() {
        if (SKINSETS.isEmpty())
            buildSkinSets();
        return SKINSETS;
    }

    private static void buildSkinSets() {
        try {
            File[] skinsetFiles = SKINSETDIR.listFiles();
            if (skinsetFiles == null || skinsetFiles.length == 0)
                return;

            for (int i = 0; i < skinsetFiles.length; i++) {
                try {
                    // Skip directories that aren't skins - e.g. shared asset directories
                    if (skinsetFiles[i].isDirectory() && !new File(skinsetFiles[i], SkinSet.SKINMANIFEST).exists())
                        continue;
                    URI uri = new URI(SKIN_SCHEME, "/" + skinsetFiles[i].getName(), null);
                    SkinSet skinset = new SkinSet(uri, skinsetFiles[i]);
                    SKINSETS.add(skinset);
                } catch (URISyntaxException e) {
                    // Skip this skinset
                    PhotoPulse.logException(e);
                } catch (SkinException e) {
                    // Skip this skinset
                    PhotoPulse.logException(e);
                }
            }

            Collections.sort(SKINSETS);
        } finally {
            // Bultin set should always be first in the list (unsorted)
            SKINSETS.add(0, BUILTINSET);
        }
    }

    /**
     * Return the Skin identified by the URI
     * Supports URIs of the form:
     * <pre>
     * skin:/photo03.jpg#image
     * custom:/643x488
     * builtin:/640x480
     * skin:/fullscreen.zip#xxlarge43
     * </pre>
     */
    public static Skin getSkin(URI uri) {
        String scheme = uri.getScheme();

        if (BUILTIN_SCHEME.equals(scheme)) {
            Dimension size = Util.parseSize(uri.getPath().substring(1), SIZE_SEP);
            if (size == null)
                return null;

            // Convert from pixels to twips
            size.width *= ShowGenerator.TWIPS_PER_PIXEL;
            size.height *= ShowGenerator.TWIPS_PER_PIXEL;

            // Search builtin skinset for this size
            for (Skin skin : BUILTINSET.getSkins()) {
                if (size.equals(skin.getSkinSize()))
                    return skin;
            }
            return null;
        }
        else if (CUSTOM_SCHEME.equals(scheme)) {
            // Create a new skin of the specified size
            Dimension size = Util.parseSize(uri.getPath().substring(1), SIZE_SEP);
            if (size == null)
                return null;
            return new Skin(size);
        }
        // Skin in skins directory
        else if (SKIN_SCHEME.equals(scheme)) {
            // Get base URI without fragment, to compare to skinset URIs
            URI baseURI = null;
            try {
                baseURI = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), null);
            } catch (URISyntaxException e) {
                // Shouldn't happen
                return null;
            }
            String skinName = uri.getFragment();

            // Find a skinset with the same base URI
            for (SkinSet skinSet : getSkinSets()) {
                if (baseURI.equals(skinSet.getURI())) {
                    // Find a skin in this skinset whose name is the fragment name
                    for (Skin skin : skinSet.getSkins()) {
                        if (skinName.equals(skin.getName()))
                            return skin;
                    }
                }
            }
            return null;
        }
        return null;
    }

    /**
     * Called from Skin to build it's URI
     */
    public static URI getSkinURI(Skin skin) {
        String skinName = skin.getName();

        // Skin name in SkinSet
        // Use size as name (WxH) if not set (e.g. for builtin/custom skins)
        if (skinName == null) {
            Dimension2D size = skin.getSkinSize();
            skinName = (int)size.getWidth()/ShowGenerator.TWIPS_PER_PIXEL
                    + SIZE_SEP + (int)size.getHeight()/ShowGenerator.TWIPS_PER_PIXEL;
        }

        SkinSet skinSet = skin.getSkinSet();

        // Custom/builtin URI that encodes size
        if (skinSet == BUILTINSET || skinSet == null) {
            try {
                return new URI(skinSet == null ? CUSTOM_SCHEME : BUILTIN_SCHEME, "/" + skinName, null);
            } catch (URISyntaxException e) {
                IllegalArgumentException e2 = new IllegalArgumentException("Illegal skin URI for " + skinName);
                e2.initCause(e);
                throw e2;
            }
        }
        // Build URI relative to skinset URI
        else {
            URI baseURI = skinSet.getURI();
            return baseURI.resolve("#" + skinName);
        }
    }

    /**
     * Choose a pair of skins from the builtins based on the aspect ratio
     */
    public static SkinPair chooseBuiltinSkinPair(float ratio) {
        if (ratio > 1) {
            if (ratio == BUILTIN_SMALL_32.getStageAspectRatio())
                return new SkinPair(BUILTIN_SMALL_32, BUILTIN_LARGE_32);
            else
                return new SkinPair(BUILTIN_SMALL_43, BUILTIN_LARGE_43);
        }
        // Ratio 0 probably means no photos, so use the ShowModel default skin
        else if (ratio == 0) {
            return new SkinPair(BUILTIN_SMALL_43, BUILTIN_LARGE_43);
        }
        else {
            if (ratio == BUILTIN_SMALL_23.getStageAspectRatio())
                return new SkinPair(BUILTIN_SMALL_23, BUILTIN_LARGE_23);
            else
                return new SkinPair(BUILTIN_SMALL_34, BUILTIN_LARGE_34);
        }
    }
}
