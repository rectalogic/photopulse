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
 
package com.photica.photopulse.model.effects;

import java.util.HashMap;

/**
 * Registry of supported effects/transitions.
 * These are immutable, mutable versions can be obtained by cloning.
 */
public class EffectRegistry {
    private static final HashMap<String,BaseEffect> TAGMAP = new HashMap<String, BaseEffect>();
    private static final String KEY_BEGINTRANSITION = "BT";
    private static final String KEY_EFFECT = "E";
    private static final String KEY_ENDTRANSITION = "ET";

    private EffectRegistry() {
    }

    public static final Effect EFFECT_Lens = addEffectMap(new Effect("Lens"));
    public static final Effect EFFECT_None = addEffectMap(new Effect("None"));
    public static final PanZoomEffect EFFECT_PanZoom = addEffectMap(new PanZoomEffect("PanZoom"));
    public static final PanoramaEffect EFFECT_Panorama = addEffectMap(new PanoramaEffect("Panorama"));
    public static final Effect EFFECT_Ripple = addEffectMap(new Effect("Ripple"));
    public static final RisingFallingStuffEffect EFFECT_RisingFallingStuff = addEffectMap(new RisingFallingStuffEffect("RisingFallingStuff"));
    public static final Effect EFFECT_Shake = addEffectMap(new Effect("Shake"));
    public static final Effect EFFECT_Spotlight = addEffectMap(new Effect("Spotlight"));
    public static final ToneEffect EFFECT_Tone = addEffectMap(new ToneEffect("Tone"));
    public static final Effect EFFECT_Vortex = addEffectMap(new Effect("Vortex"));

    public static final BeginTransition BEGINTRANS_Fade = addEffectMap(new BeginTransition("Fade"));
    public static final BeginTransition BEGINTRANS_FadeBlack = addEffectMap(new BeginTransition("FadeBlack"));
    public static final BeginTransition BEGINTRANS_FadeWhite = addEffectMap(new BeginTransition("FadeWhite"));
    public static final BeginTransition BEGINTRANS_FlipHorizontal = addEffectMap(new BeginTransition("FlipHorizontal"));
    public static final BeginTransition BEGINTRANS_FlipVertical = addEffectMap(new BeginTransition("FlipVertical"));
    public static final BeginTransition BEGINTRANS_Heart = addEffectMap(new BeginTransition("Heart"));
    public static final BeginTransition BEGINTRANS_Iris = addEffectMap(new BeginTransition("Iris"));
    public static final BeginTransition BEGINTRANS_LawBadge = addEffectMap(new BeginTransition("LawBadge"));
    public static final BeginTransition BEGINTRANS_Star = addEffectMap(new BeginTransition("Star"));
    public static final BeginTransition BEGINTRANS_StarBurst = addEffectMap(new BeginTransition("StarBurst"));
    public static final BeginTransition BEGINTRANS_Melt = addEffectMap(new BeginTransition("Melt"));
    public static final BeginTransition BEGINTRANS_None = addEffectMap(new BeginTransition("None"));
    public static final BeginTransition BEGINTRANS_Plus = addEffectMap(new BeginTransition("Plus"));
    public static final BeginTransition BEGINTRANS_SkewLeft = addEffectMap(new BeginTransition("SkewLeft"));
    public static final BeginTransition BEGINTRANS_SkewRight = addEffectMap(new BeginTransition("SkewRight"));
    public static final BeginTransition BEGINTRANS_SlideDown = addEffectMap(new BeginTransition("SlideDown"));
    public static final BeginTransition BEGINTRANS_SlideLeft = addEffectMap(new BeginTransition("SlideLeft"));
    public static final BeginTransition BEGINTRANS_SlideRight = addEffectMap(new BeginTransition("SlideRight"));
    public static final BeginTransition BEGINTRANS_SlideUp = addEffectMap(new BeginTransition("SlideUp"));
    public static final BeginTransition BEGINTRANS_Spin = addEffectMap(new BeginTransition("Spin"));
    public static final BeginTransition BEGINTRANS_StretchHorizontal = addEffectMap(new BeginTransition("StretchHorizontal"));
    public static final BeginTransition BEGINTRANS_StretchVertical = addEffectMap(new BeginTransition("StretchVertical"));
    public static final BeginTransition BEGINTRANS_VenetianHorizontal = addEffectMap(new BeginTransition("VenetianHorizontal"));
    public static final BeginTransition BEGINTRANS_VenetianVertical = addEffectMap(new BeginTransition("VenetianVertical"));
    public static final BeginTransition BEGINTRANS_WipeCenter = addEffectMap(new BeginTransition("WipeCenter"));
    public static final BeginTransition BEGINTRANS_WipeDiagonalBottomLeft = addEffectMap(new BeginTransition("WipeDiagonalBottomLeft"));
    public static final BeginTransition BEGINTRANS_WipeDiagonalBottomRight = addEffectMap(new BeginTransition("WipeDiagonalBottomRight"));
    public static final BeginTransition BEGINTRANS_WipeDiagonalTopLeft = addEffectMap(new BeginTransition("WipeDiagonalTopLeft"));
    public static final BeginTransition BEGINTRANS_WipeDiagonalTopRight = addEffectMap(new BeginTransition("WipeDiagonalTopRight"));
    public static final BeginTransition BEGINTRANS_WipeDown = addEffectMap(new BeginTransition("WipeDown"));
    public static final BeginTransition BEGINTRANS_WipeHorizontal = addEffectMap(new BeginTransition("WipeHorizontal"));
    public static final BeginTransition BEGINTRANS_WipeLeft = addEffectMap(new BeginTransition("WipeLeft"));
    public static final BeginTransition BEGINTRANS_WipeRight = addEffectMap(new BeginTransition("WipeRight"));
    public static final BeginTransition BEGINTRANS_WipeUp = addEffectMap(new BeginTransition("WipeUp"));
    public static final BeginTransition BEGINTRANS_WipeVertical = addEffectMap(new BeginTransition("WipeVertical"));
    public static final BeginTransition BEGINTRANS_ZoomBoth = addEffectMap(new BeginTransition("ZoomBoth"));
    public static final BeginTransition BEGINTRANS_ZoomHorizontal = addEffectMap(new BeginTransition("ZoomHorizontal"));
    public static final BeginTransition BEGINTRANS_ZoomVertical = addEffectMap(new BeginTransition("ZoomVertical"));

    public static final EndTransition ENDTRANS_Fade = addEffectMap(new EndTransition("Fade"));
    public static final EndTransition ENDTRANS_FadeBlack = addEffectMap(new EndTransition("FadeBlack"));
    public static final EndTransition ENDTRANS_FadeWhite = addEffectMap(new EndTransition("FadeWhite"));
    public static final EndTransition ENDTRANS_FlipHorizontal = addEffectMap(new EndTransition("FlipHorizontal"));
    public static final EndTransition ENDTRANS_FlipVertical = addEffectMap(new EndTransition("FlipVertical"));
    public static final EndTransition ENDTRANS_Heart = addEffectMap(new EndTransition("Heart"));
    public static final EndTransition ENDTRANS_Iris = addEffectMap(new EndTransition("Iris"));
    public static final EndTransition ENDTRANS_LawBadge = addEffectMap(new EndTransition("LawBadge"));
    public static final EndTransition ENDTRANS_Melt = addEffectMap(new EndTransition("Melt"));
    public static final EndTransition ENDTRANS_None = addEffectMap(new EndTransition("None"));
    public static final EndTransition ENDTRANS_Plus = addEffectMap(new EndTransition("Plus"));
    public static final EndTransition ENDTRANS_SkewLeft = addEffectMap(new EndTransition("SkewLeft"));
    public static final EndTransition ENDTRANS_SkewRight = addEffectMap(new EndTransition("SkewRight"));
    public static final EndTransition ENDTRANS_SlideDown = addEffectMap(new EndTransition("SlideDown"));
    public static final EndTransition ENDTRANS_SlideLeft = addEffectMap(new EndTransition("SlideLeft"));
    public static final EndTransition ENDTRANS_SlideRight = addEffectMap(new EndTransition("SlideRight"));
    public static final EndTransition ENDTRANS_SlideUp = addEffectMap(new EndTransition("SlideUp"));
    public static final EndTransition ENDTRANS_Spin = addEffectMap(new EndTransition("Spin"));
    public static final EndTransition ENDTRANS_Star = addEffectMap(new EndTransition("Star"));
    public static final EndTransition ENDTRANS_StarBurst = addEffectMap(new EndTransition("StarBurst"));
    public static final EndTransition ENDTRANS_StretchHorizontal = addEffectMap(new EndTransition("StretchHorizontal"));
    public static final EndTransition ENDTRANS_StretchVertical = addEffectMap(new EndTransition("StretchVertical"));
    public static final EndTransition ENDTRANS_VenetianHorizontal = addEffectMap(new EndTransition("VenetianHorizontal"));
    public static final EndTransition ENDTRANS_VenetianVertical = addEffectMap(new EndTransition("VenetianVertical"));
    public static final EndTransition ENDTRANS_WipeCenter = addEffectMap(new EndTransition("WipeCenter"));
    public static final EndTransition ENDTRANS_WipeDiagonalBottomLeft = addEffectMap(new EndTransition("WipeDiagonalBottomLeft"));
    public static final EndTransition ENDTRANS_WipeDiagonalBottomRight = addEffectMap(new EndTransition("WipeDiagonalBottomRight"));
    public static final EndTransition ENDTRANS_WipeDiagonalTopLeft = addEffectMap(new EndTransition("WipeDiagonalTopLeft"));
    public static final EndTransition ENDTRANS_WipeDiagonalTopRight = addEffectMap(new EndTransition("WipeDiagonalTopRight"));
    public static final EndTransition ENDTRANS_WipeDown = addEffectMap(new EndTransition("WipeDown"));
    public static final EndTransition ENDTRANS_WipeHorizontal = addEffectMap(new EndTransition("WipeHorizontal"));
    public static final EndTransition ENDTRANS_WipeLeft = addEffectMap(new EndTransition("WipeLeft"));
    public static final EndTransition ENDTRANS_WipeRight = addEffectMap(new EndTransition("WipeRight"));
    public static final EndTransition ENDTRANS_WipeUp = addEffectMap(new EndTransition("WipeUp"));
    public static final EndTransition ENDTRANS_WipeVertical = addEffectMap(new EndTransition("WipeVertical"));
    public static final EndTransition ENDTRANS_ZoomBoth = addEffectMap(new EndTransition("ZoomBoth"));
    public static final EndTransition ENDTRANS_ZoomHorizontal = addEffectMap(new EndTransition("ZoomHorizontal"));
    public static final EndTransition ENDTRANS_ZoomVertical = addEffectMap(new EndTransition("ZoomVertical"));

    static {
        // Register Tone using the name Tint also for backwards compatibility - for when we used to have a TintEffect
        TAGMAP.put(KEY_EFFECT + "Tint", EFFECT_Tone);
    }

    public static Effect findEffect(String tag) {
        return (Effect) TAGMAP.get(KEY_EFFECT + tag);
    }

    public static BeginTransition findBeginTransition(String tag) {
        return (BeginTransition) TAGMAP.get(KEY_BEGINTRANSITION + tag);
    }

    public static EndTransition findEndTransition(String tag) {
        return (EndTransition) TAGMAP.get(KEY_ENDTRANSITION + tag);
    }

    private static <E extends Effect> E addEffectMap(E effect) {
        TAGMAP.put(KEY_EFFECT + effect.getTag(), effect);
        return effect;
    }

    private static BeginTransition addEffectMap(BeginTransition effect) {
        TAGMAP.put(KEY_BEGINTRANSITION + effect.getTag(), effect);
        return effect;
    }

    private static EndTransition addEffectMap(EndTransition effect) {
        TAGMAP.put(KEY_ENDTRANSITION + effect.getTag(), effect);
        return effect;
    }
}
