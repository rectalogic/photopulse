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

package com.photica.photopulse.wizard;

import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.effects.BaseEffect;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.model.effects.PanoramaEffect;
import com.photica.photopulse.model.effects.RisingFallingStuffEffect;
import com.photica.photopulse.model.effects.ToneEffect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wizard {

    ///////////////////////////////
    // Effects
    ///////////////////////////////

    // Standard effects
    public static final WizardEffectEffect WEFFECT_Lens = new WizardEffectEffect(EffectRegistry.EFFECT_Lens, "E_Lens");
    public static final WizardEffectEffect WEFFECT_None = new WizardEffectEffect(EffectRegistry.EFFECT_None, "E_None");
    public static final WizardEffectEffect WEFFECT_Ripple = new WizardEffectEffect(EffectRegistry.EFFECT_Ripple, "E_Ripple");
    public static final WizardEffectEffect WEFFECT_Shake = new WizardEffectEffect(EffectRegistry.EFFECT_Shake, "E_Shake", 1.05);
    public static final WizardEffectEffect WEFFECT_Spotlight = new WizardEffectEffect(EffectRegistry.EFFECT_Spotlight, "E_Spotlight");
    public static final WizardEffectEffect WEFFECT_Vortex = new WizardEffectEffect(EffectRegistry.EFFECT_Vortex, "E_Vortex");

    // Customized effects provided by Wizard
    public static final WizardEffectEffect WEFFECT_ZoomIn = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0, 1.0, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0, 1.1, 0) }), "E_ZoomIn", 1.1);
    public static final WizardEffectEffect WEFFECT_ZoomOut = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0, 1.1, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0, 1.0, 0) }), "E_ZoomOut", 1.1);
    public static final WizardEffectEffect WEFFECT_MaxZoomIn = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0, 1.0, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0, 1.5, 0) }), "E_MaxZoomIn", 1.5);
    public static final WizardEffectEffect WEFFECT_MaxZoomInTopCenter = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0, 1.0, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0.5, 1.5, 0) }), "E_MaxZoomInTC", 1.5);
    public static final WizardEffectEffect WEFFECT_MaxZoomOut = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0, 1.5, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0, 1.0, 0) }), "E_MaxZoomOut", 1.5);
    public static final WizardEffectEffect WEFFECT_MaxZoomOutTopCenter = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, true, 0, 0.5, 1.5, 0), new PanZoomEffect.Keyframe(1.0, false, true, 0, 0, 1.0, 0) }), "E_MaxZoomOutTC", 1.5);
    public static final WizardEffectEffect WEFFECT_PanLeftRight = new WizardEffectEffect(EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(new PanZoomEffect.Keyframe[] { new PanZoomEffect.Keyframe(0, false, false, 0.5, 0.5, 1.5, 0), new PanZoomEffect.Keyframe(1.0, false, false, -0.5, 0.5, 1.5, 0) }), "E_PanLR", 1.5);
    public static final WizardEffectEffect WEFFECT_PanoramaLeft = new WizardEffectEffect(EffectRegistry.EFFECT_Panorama.clonePanoramaEffect(PanoramaEffect.Direction.LEFT), "E_PanoramaLeft");
    public static final WizardEffectEffect WEFFECT_PanoramaRight = new WizardEffectEffect(EffectRegistry.EFFECT_Panorama.clonePanoramaEffect(PanoramaEffect.Direction.RIGHT), "E_PanoramaRight");
    public static final WizardEffectEffect WEFFECT_RisingBubbles = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.BUBBLE, RisingFallingStuffEffect.Direction.UP), "E_RisingBubbles");
    public static final WizardEffectEffect WEFFECT_RisingHearts = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.HEART, RisingFallingStuffEffect.Direction.UP), "E_RisingHearts");
    public static final WizardEffectEffect WEFFECT_FallingBubbles = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.BUBBLE, RisingFallingStuffEffect.Direction.DOWN), "E_FallingBubbles");
    public static final WizardEffectEffect WEFFECT_FallingHearts = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.HEART, RisingFallingStuffEffect.Direction.DOWN), "E_FallingHearts");
    public static final WizardEffectEffect WEFFECT_FallingSnowflakes = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.SNOWFLAKE, RisingFallingStuffEffect.Direction.DOWN), "E_FallingSnowflakes");
    public static final WizardEffectEffect WEFFECT_FallingLeaves = new WizardEffectEffect(EffectRegistry.EFFECT_RisingFallingStuff.cloneRisingFallingStuffEffect(RisingFallingStuffEffect.Stuff.LEAF, RisingFallingStuffEffect.Direction.DOWN), "E_FallingLeaves");
    public static final WizardEffectEffect WEFFECT_ToneGray = new WizardEffectEffect(EffectRegistry.EFFECT_Tone.cloneToneEffect(ToneEffect.Tone.GRAY), "E_ToneGray");
    public static final WizardEffectEffect WEFFECT_ToneSepia = new WizardEffectEffect(EffectRegistry.EFFECT_Tone.cloneToneEffect(ToneEffect.Tone.SEPIA), "E_ToneSepia");


    // Includes Wizard configured effects, does not include EffectRegistry base effects (e.g. EFFECT_PanZoom)
    private static final WizardEffectEffect[] EFFECTS = new WizardEffectEffect[] {
        WEFFECT_Lens,
        WEFFECT_None,
        WEFFECT_PanLeftRight,
        WEFFECT_PanoramaLeft,
        WEFFECT_PanoramaRight,
        WEFFECT_Ripple,
        WEFFECT_Shake,
        WEFFECT_Spotlight,
        WEFFECT_Vortex,
        WEFFECT_ZoomIn,
        WEFFECT_ZoomOut,
        WEFFECT_MaxZoomIn,
        WEFFECT_MaxZoomInTopCenter,
        WEFFECT_MaxZoomOut,
        WEFFECT_MaxZoomOutTopCenter,
        WEFFECT_RisingBubbles,
        WEFFECT_RisingHearts,
        WEFFECT_FallingBubbles,
        WEFFECT_FallingHearts,
        WEFFECT_FallingLeaves,
        WEFFECT_FallingSnowflakes,
        WEFFECT_ToneGray,
        WEFFECT_ToneSepia,
    };
    public static final List<WizardEffectEffect> LIST_EFFECTS = Collections.unmodifiableList(Arrays.asList(EFFECTS));


    ///////////////////////////////
    // BeginTransitions
    ///////////////////////////////

    public static final WizardEffectBeginTransition WBEGINTRANS_None = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_None, "BT_None", Layer.BOTTOM);
    public static final WizardEffectBeginTransition WBEGINTRANS_Fade = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Fade, "BT_Fade", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_FadeBlack = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_FadeBlack, "BT_FadeBlack", Layer.BOTTOM);
    public static final WizardEffectBeginTransition WBEGINTRANS_FadeWhite = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_FadeWhite, "BT_FadeWhite", Layer.BOTTOM);
    public static final WizardEffectBeginTransition WBEGINTRANS_FlipHorizontal = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_FlipHorizontal, "BT_FlipHorizontal", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_FlipVertical = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_FlipVertical, "BT_FlipVertical", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Heart = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Heart, "BT_Heart", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Iris = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Iris, "BT_Iris", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_LawBadge = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_LawBadge, "BT_LawBadge", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Melt = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Melt, "BT_Melt", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Plus = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Plus, "BT_Plus", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SkewLeft = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SkewLeft, "BT_SkewLeft", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SkewRight = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SkewRight, "BT_SkewRight", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SlideDown = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SlideDown, "BT_SlideDown", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SlideLeft = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SlideLeft, "BT_SlideLeft", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SlideRight = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SlideRight, "BT_SlideRight", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_SlideUp = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_SlideUp, "BT_SlideUp", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Spin = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Spin, "BT_Spin", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_Star = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_Star, "BT_Star", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_StarBurst = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_StarBurst, "BT_StarBurst", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_StretchHorizontal = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_StretchHorizontal, "BT_StretchHorizontal", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_StretchVertical = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_StretchVertical, "BT_StretchVertical", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_VenetianHorizontal = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_VenetianHorizontal, "BT_VenetianHorizontal", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_VenetianVertical = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_VenetianVertical, "BT_VenetianVertical", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeCenter = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeCenter, "BT_WipeCenter", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeDiagonalBottomLeft = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeDiagonalBottomLeft, "BT_WipeDiagonalBottomLeft", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeDiagonalBottomRight = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeDiagonalBottomRight, "BT_WipeDiagonalBottomRight", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeDiagonalTopLeft = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeDiagonalTopLeft, "BT_WipeDiagonalTopLeft", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeDiagonalTopRight = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeDiagonalTopRight, "BT_WipeDiagonalTopRight", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeDown = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeDown, "BT_WipeDown", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeHorizontal = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeHorizontal, "BT_WipeHorizontal", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeLeft = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeLeft, "BT_WipeLeft", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeRight = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeRight, "BT_WipeRight", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeUp = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeUp, "BT_WipeUp", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_WipeVertical = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_WipeVertical, "BT_WipeVertical", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_ZoomBoth = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_ZoomBoth, "BT_ZoomBoth", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_ZoomHorizontal = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_ZoomHorizontal, "BT_ZoomHorizontal", Layer.EITHER);
    public static final WizardEffectBeginTransition WBEGINTRANS_ZoomVertical = new WizardEffectBeginTransition(EffectRegistry.BEGINTRANS_ZoomVertical, "BT_ZoomVertical", Layer.EITHER);


    // Does not include BEGINTRANS_None
    private static final WizardEffectBeginTransition[] BEGINTRANSITIONS = new WizardEffectBeginTransition[] {
        WBEGINTRANS_Fade,
        WBEGINTRANS_FadeBlack,
        WBEGINTRANS_FadeWhite,
        WBEGINTRANS_FlipHorizontal,
        WBEGINTRANS_FlipVertical,
        WBEGINTRANS_Heart,
        WBEGINTRANS_Iris,
        WBEGINTRANS_LawBadge,
        WBEGINTRANS_Melt,
        WBEGINTRANS_Plus,
        WBEGINTRANS_SkewLeft,
        WBEGINTRANS_SkewRight,
        WBEGINTRANS_SlideDown,
        WBEGINTRANS_SlideLeft,
        WBEGINTRANS_SlideRight,
        WBEGINTRANS_SlideUp,
        WBEGINTRANS_Spin,
        WBEGINTRANS_Star,
        WBEGINTRANS_StarBurst,
        WBEGINTRANS_StretchHorizontal,
        WBEGINTRANS_StretchVertical,
        WBEGINTRANS_VenetianHorizontal,
        WBEGINTRANS_VenetianVertical,
        WBEGINTRANS_WipeCenter,
        WBEGINTRANS_WipeDiagonalBottomLeft,
        WBEGINTRANS_WipeDiagonalBottomRight,
        WBEGINTRANS_WipeDiagonalTopLeft,
        WBEGINTRANS_WipeDiagonalTopRight,
        WBEGINTRANS_WipeDown,
        WBEGINTRANS_WipeHorizontal,
        WBEGINTRANS_WipeLeft,
        WBEGINTRANS_WipeRight,
        WBEGINTRANS_WipeUp,
        WBEGINTRANS_WipeVertical,
        WBEGINTRANS_ZoomBoth,
        WBEGINTRANS_ZoomHorizontal,
        WBEGINTRANS_ZoomVertical,
    };
    public static final List<WizardEffectBeginTransition> LIST_BEGINTRANSITIONS = Collections.unmodifiableList(Arrays.asList(BEGINTRANSITIONS));


    ///////////////////////////////
    // EndTransitions
    ///////////////////////////////

    public static final WizardEffectEndTransition WENDTRANS_None = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_None, "ET_None", Layer.BOTTOM);
    public static final WizardEffectEndTransition WENDTRANS_Fade = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Fade, "ET_Fade", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_FadeBlack = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_FadeBlack, "ET_FadeBlack", Layer.BOTTOM);
    public static final WizardEffectEndTransition WENDTRANS_FadeWhite = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_FadeWhite, "ET_FadeWhite", Layer.BOTTOM);
    public static final WizardEffectEndTransition WENDTRANS_FlipHorizontal = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_FlipHorizontal, "ET_FlipHorizontal", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_FlipVertical = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_FlipVertical, "ET_FlipVertical", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Heart = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Heart, "ET_Heart", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Iris = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Iris, "ET_Iris", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_LawBadge = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_LawBadge, "ET_LawBadge", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Melt = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Melt, "ET_Melt", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Plus = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Plus, "ET_Plus", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SkewLeft = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SkewLeft, "ET_SkewLeft", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SkewRight = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SkewRight, "ET_SkewRight", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SlideDown = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SlideDown, "ET_SlideDown", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SlideLeft = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SlideLeft, "ET_SlideLeft", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SlideRight = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SlideRight, "ET_SlideRight", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_SlideUp = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_SlideUp, "ET_SlideUp", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Spin = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Spin, "ET_Spin", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_Star = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_Star, "ET_Star", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_StarBurst = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_StarBurst, "ET_StarBurst", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_StretchHorizontal = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_StretchHorizontal, "ET_StretchHorizontal", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_StretchVertical = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_StretchVertical, "ET_StretchVertical", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_VenetianHorizontal = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_VenetianHorizontal, "ET_VenetianHorizontal", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_VenetianVertical = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_VenetianVertical, "ET_VenetianVertical", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeCenter = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeCenter, "ET_WipeCenter", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeDiagonalBottomLeft = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeDiagonalBottomLeft, "ET_WipeDiagonalBottomLeft", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeDiagonalBottomRight = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeDiagonalBottomRight, "ET_WipeDiagonalBottomRight", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeDiagonalTopLeft = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeDiagonalTopLeft, "ET_WipeDiagonalTopLeft", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeDiagonalTopRight = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeDiagonalTopRight, "ET_WipeDiagonalTopRight", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeDown = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeDown, "ET_WipeDown", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeHorizontal = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeHorizontal, "ET_WipeHorizontal", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeLeft = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeLeft, "ET_WipeLeft", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeRight = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeRight, "ET_WipeRight", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeUp = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeUp, "ET_WipeUp", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_WipeVertical = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_WipeVertical, "ET_WipeVertical", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_ZoomBoth = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_ZoomBoth, "ET_ZoomBoth", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_ZoomHorizontal = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_ZoomHorizontal, "ET_ZoomHorizontal", Layer.EITHER);
    public static final WizardEffectEndTransition WENDTRANS_ZoomVertical = new WizardEffectEndTransition(EffectRegistry.ENDTRANS_ZoomVertical, "ET_ZoomVertical", Layer.EITHER);


    // Does not include ENDTRANS_None
    private static final WizardEffectEndTransition[] ENDTRANSITIONS = new WizardEffectEndTransition[] {
        WENDTRANS_Fade,
        WENDTRANS_FadeBlack,
        WENDTRANS_FadeWhite,
        WENDTRANS_FlipHorizontal,
        WENDTRANS_FlipVertical,
        WENDTRANS_Heart,
        WENDTRANS_Iris,
        WENDTRANS_LawBadge,
        WENDTRANS_Melt,
        WENDTRANS_Plus,
        WENDTRANS_SkewLeft,
        WENDTRANS_SkewRight,
        WENDTRANS_SlideDown,
        WENDTRANS_SlideLeft,
        WENDTRANS_SlideRight,
        WENDTRANS_SlideUp,
        WENDTRANS_Spin,
        WENDTRANS_Star,
        WENDTRANS_StarBurst,
        WENDTRANS_StretchHorizontal,
        WENDTRANS_StretchVertical,
        WENDTRANS_VenetianHorizontal,
        WENDTRANS_VenetianVertical,
        WENDTRANS_WipeCenter,
        WENDTRANS_WipeDiagonalBottomLeft,
        WENDTRANS_WipeDiagonalBottomRight,
        WENDTRANS_WipeDiagonalTopLeft,
        WENDTRANS_WipeDiagonalTopRight,
        WENDTRANS_WipeDown,
        WENDTRANS_WipeHorizontal,
        WENDTRANS_WipeLeft,
        WENDTRANS_WipeRight,
        WENDTRANS_WipeUp,
        WENDTRANS_WipeVertical,
        WENDTRANS_ZoomBoth,
        WENDTRANS_ZoomHorizontal,
        WENDTRANS_ZoomVertical,
    };
    public static final List<WizardEffectEndTransition> LIST_ENDTRANSITIONS = Collections.unmodifiableList(Arrays.asList(ENDTRANSITIONS));

    // Map WizardEffect key to its index in its list
    private static HashMap<String,Integer> begintransKey2IndexMap = new HashMap<String, Integer>(BEGINTRANSITIONS.length);
    private static HashMap<String,Integer> effectKey2IndexMap = new HashMap<String, Integer>(EFFECTS.length);
    private static HashMap<String,Integer> endtransKey2IndexMap = new HashMap<String, Integer>(ENDTRANSITIONS.length);

    // Mape BaseEffect to its WizardEffect
    private static HashMap<BaseEffect,WizardEffect> base2WizardMap = new HashMap<BaseEffect, WizardEffect>(BEGINTRANSITIONS.length + 1 + EFFECTS.length + ENDTRANSITIONS.length + 1);

    static {
        initPartnerTransitions();

        // Sort arrays by display name.
        // Sorting the wrapping Lists underlying arrays is OK.
        DisplayNameComparator dnc = new DisplayNameComparator();
        Arrays.sort(BEGINTRANSITIONS, dnc);
        Arrays.sort(ENDTRANSITIONS, dnc);
        Arrays.sort(EFFECTS, dnc);

        // Map each WizardEffect to its index
        populateKey2IndexMap(begintransKey2IndexMap, BEGINTRANSITIONS);
        populateKey2IndexMap(effectKey2IndexMap, EFFECTS);
        populateKey2IndexMap(endtransKey2IndexMap, ENDTRANSITIONS);

        // Map each BaseEffect to its WizardEffect
        populateBase2WizardMap(base2WizardMap, BEGINTRANSITIONS);
        base2WizardMap.put(WBEGINTRANS_None.getEffect(), WBEGINTRANS_None);
        populateBase2WizardMap(base2WizardMap, EFFECTS);
        populateBase2WizardMap(base2WizardMap, ENDTRANSITIONS);
        base2WizardMap.put(WENDTRANS_None.getEffect(), WENDTRANS_None);
    }

    // Enable for tracing
    private static final boolean DEBUG = false;

    // Configure each transitions preferred partner transitions
    private static void initPartnerTransitions() {

        // Begin transition partners
        WBEGINTRANS_Fade.setPartnerTransitions(WENDTRANS_Fade);
        WBEGINTRANS_FadeBlack.setPartnerTransitions(WENDTRANS_Heart, WENDTRANS_Iris, WENDTRANS_LawBadge, WENDTRANS_Plus, WENDTRANS_Star, WENDTRANS_StarBurst);
        WBEGINTRANS_FadeWhite.setPartnerTransitions(WENDTRANS_Heart, WENDTRANS_Iris, WENDTRANS_LawBadge, WENDTRANS_Plus, WENDTRANS_Star, WENDTRANS_StarBurst);
        WBEGINTRANS_FlipHorizontal.setPartnerTransitions(WENDTRANS_FlipHorizontal, WENDTRANS_FlipVertical);
        WBEGINTRANS_FlipVertical.setPartnerTransitions(WENDTRANS_FlipVertical, WENDTRANS_FlipHorizontal);
        WBEGINTRANS_Heart.setPartnerTransitions(WENDTRANS_Heart, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_Iris.setPartnerTransitions(WENDTRANS_Iris, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_LawBadge.setPartnerTransitions(WENDTRANS_LawBadge, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_Melt.setPartnerTransitions(WENDTRANS_Melt);
        WBEGINTRANS_Plus.setPartnerTransitions(WENDTRANS_Plus, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_SkewLeft.setPartnerTransitions(WENDTRANS_SkewLeft);
        WBEGINTRANS_SkewRight.setPartnerTransitions(WENDTRANS_SkewRight);
        WBEGINTRANS_SlideDown.setPartnerTransitions(WENDTRANS_SlideDown);
        WBEGINTRANS_SlideLeft.setPartnerTransitions(WENDTRANS_SlideLeft);
        WBEGINTRANS_SlideRight.setPartnerTransitions(WENDTRANS_SlideRight);
        WBEGINTRANS_SlideUp.setPartnerTransitions(WENDTRANS_SlideUp);
        WBEGINTRANS_Spin.setPartnerTransitions(WENDTRANS_Spin);
        WBEGINTRANS_Star.setPartnerTransitions(WENDTRANS_Star, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_StarBurst.setPartnerTransitions(WENDTRANS_StarBurst, WENDTRANS_Fade, WENDTRANS_FadeBlack, WENDTRANS_FadeWhite);
        WBEGINTRANS_StretchHorizontal.setPartnerTransitions(WENDTRANS_StretchHorizontal);
        WBEGINTRANS_StretchVertical.setPartnerTransitions(WENDTRANS_StretchVertical);
        WBEGINTRANS_VenetianHorizontal.setPartnerTransitions(WENDTRANS_VenetianHorizontal, WENDTRANS_VenetianVertical);
        WBEGINTRANS_VenetianVertical.setPartnerTransitions(WENDTRANS_VenetianVertical, WENDTRANS_VenetianHorizontal);
        WBEGINTRANS_WipeCenter.setPartnerTransitions(WENDTRANS_WipeCenter);
        WBEGINTRANS_WipeDiagonalBottomLeft.setPartnerTransitions(WENDTRANS_WipeDiagonalBottomLeft);
        WBEGINTRANS_WipeDiagonalBottomRight.setPartnerTransitions(WENDTRANS_WipeDiagonalBottomRight);
        WBEGINTRANS_WipeDiagonalTopLeft.setPartnerTransitions(WENDTRANS_WipeDiagonalTopLeft);
        WBEGINTRANS_WipeDiagonalTopRight.setPartnerTransitions(WENDTRANS_WipeDiagonalTopRight);
        WBEGINTRANS_WipeDown.setPartnerTransitions(WENDTRANS_WipeDown);
        WBEGINTRANS_WipeHorizontal.setPartnerTransitions(WENDTRANS_WipeHorizontal, WENDTRANS_WipeVertical);
        WBEGINTRANS_WipeLeft.setPartnerTransitions(WENDTRANS_WipeLeft);
        WBEGINTRANS_WipeRight.setPartnerTransitions(WENDTRANS_WipeRight);
        WBEGINTRANS_WipeUp.setPartnerTransitions(WENDTRANS_WipeUp);
        WBEGINTRANS_WipeVertical.setPartnerTransitions(WENDTRANS_WipeVertical, WENDTRANS_WipeHorizontal);
        WBEGINTRANS_ZoomBoth.setPartnerTransitions(WENDTRANS_ZoomBoth);
        WBEGINTRANS_ZoomHorizontal.setPartnerTransitions(WENDTRANS_ZoomHorizontal, WENDTRANS_ZoomVertical);
        WBEGINTRANS_ZoomVertical.setPartnerTransitions(WENDTRANS_ZoomVertical, WENDTRANS_ZoomHorizontal);

        // End transition partners
        WENDTRANS_Fade.setPartnerTransitions(WBEGINTRANS_Fade);
        WENDTRANS_FadeBlack.setPartnerTransitions(WBEGINTRANS_Heart, WBEGINTRANS_Iris, WBEGINTRANS_LawBadge, WBEGINTRANS_Plus, WBEGINTRANS_Star, WBEGINTRANS_StarBurst);
        WENDTRANS_FadeWhite.setPartnerTransitions(WBEGINTRANS_Heart, WBEGINTRANS_Iris, WBEGINTRANS_LawBadge, WBEGINTRANS_Plus, WBEGINTRANS_Star, WBEGINTRANS_StarBurst);
        WENDTRANS_FlipHorizontal.setPartnerTransitions(WBEGINTRANS_FlipHorizontal, WBEGINTRANS_FlipVertical);
        WENDTRANS_FlipVertical.setPartnerTransitions(WBEGINTRANS_FlipVertical, WBEGINTRANS_FlipHorizontal);
        WENDTRANS_Heart.setPartnerTransitions(WBEGINTRANS_Heart, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_Iris.setPartnerTransitions(WBEGINTRANS_Iris, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_LawBadge.setPartnerTransitions(WBEGINTRANS_LawBadge, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_Melt.setPartnerTransitions(WBEGINTRANS_Melt);
        WENDTRANS_Plus.setPartnerTransitions(WBEGINTRANS_Plus, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_SkewLeft.setPartnerTransitions(WBEGINTRANS_SkewLeft);
        WENDTRANS_SkewRight.setPartnerTransitions(WBEGINTRANS_SkewRight);
        WENDTRANS_SlideDown.setPartnerTransitions(WBEGINTRANS_SlideDown);
        WENDTRANS_SlideLeft.setPartnerTransitions(WBEGINTRANS_SlideLeft);
        WENDTRANS_SlideRight.setPartnerTransitions(WBEGINTRANS_SlideRight);
        WENDTRANS_SlideUp.setPartnerTransitions(WBEGINTRANS_SlideUp);
        WENDTRANS_Spin.setPartnerTransitions(WBEGINTRANS_Spin);
        WENDTRANS_Star.setPartnerTransitions(WBEGINTRANS_Star, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_StarBurst.setPartnerTransitions(WBEGINTRANS_StarBurst, WBEGINTRANS_Fade, WBEGINTRANS_FadeBlack, WBEGINTRANS_FadeWhite);
        WENDTRANS_StretchHorizontal.setPartnerTransitions(WBEGINTRANS_StretchHorizontal);
        WENDTRANS_StretchVertical.setPartnerTransitions(WBEGINTRANS_StretchVertical);
        WENDTRANS_VenetianHorizontal.setPartnerTransitions(WBEGINTRANS_VenetianHorizontal, WBEGINTRANS_VenetianVertical);
        WENDTRANS_VenetianVertical.setPartnerTransitions(WBEGINTRANS_VenetianVertical, WBEGINTRANS_VenetianHorizontal);
        WENDTRANS_WipeCenter.setPartnerTransitions(WBEGINTRANS_WipeCenter);
        WENDTRANS_WipeDiagonalBottomLeft.setPartnerTransitions(WBEGINTRANS_WipeDiagonalBottomLeft);
        WENDTRANS_WipeDiagonalBottomRight.setPartnerTransitions(WBEGINTRANS_WipeDiagonalBottomRight);
        WENDTRANS_WipeDiagonalTopLeft.setPartnerTransitions(WBEGINTRANS_WipeDiagonalTopLeft);
        WENDTRANS_WipeDiagonalTopRight.setPartnerTransitions(WBEGINTRANS_WipeDiagonalTopRight);
        WENDTRANS_WipeDown.setPartnerTransitions(WBEGINTRANS_WipeDown);
        WENDTRANS_WipeHorizontal.setPartnerTransitions(WBEGINTRANS_WipeHorizontal, WBEGINTRANS_WipeVertical);
        WENDTRANS_WipeLeft.setPartnerTransitions(WBEGINTRANS_WipeLeft);
        WENDTRANS_WipeRight.setPartnerTransitions(WBEGINTRANS_WipeRight);
        WENDTRANS_WipeUp.setPartnerTransitions(WBEGINTRANS_WipeUp);
        WENDTRANS_WipeVertical.setPartnerTransitions(WBEGINTRANS_WipeVertical, WBEGINTRANS_WipeHorizontal);
        WENDTRANS_ZoomBoth.setPartnerTransitions(WBEGINTRANS_ZoomBoth);
        WENDTRANS_ZoomHorizontal.setPartnerTransitions(WBEGINTRANS_ZoomHorizontal, WBEGINTRANS_ZoomVertical);
        WENDTRANS_ZoomVertical.setPartnerTransitions(WBEGINTRANS_ZoomVertical, WBEGINTRANS_ZoomHorizontal);
    }

    private static void populateKey2IndexMap(Map<String,Integer> map, WizardEffect[] effects) {
        for (int i = 0; i < effects.length; i++)
            map.put(effects[i].getKey(), new Integer(i));
    }

    private static void populateBase2WizardMap(Map<BaseEffect,WizardEffect> map, WizardEffect[] effects) {
        for (int i = 0; i < effects.length; i++)
            map.put(effects[i].getEffect(), effects[i]);
    }

    /**
     * Compute the common aspect ratio of the photos in the model
     */
    public static float computeAspectRatio(ShowModel model) {
        if (model.getPhotoEffectList().size() == 0)
            return 0;

        class Counter implements Comparable<Counter> {
            private float ratio;
            private int count = 1;
            public Counter(float ratio) {
                this.ratio = ratio;
            }
            public void increment() {
                count++;
            }
            public float getRatio() {
                return ratio;
            }
            public int compareTo(Counter counter) {
                return count - counter.count;
            }
        };

        // Map ratio to a counter, increment counter for each Photo that has that ratio
        HashMap<Float,Counter> ratioCount = new HashMap<Float,Counter>();
        for (PhotoEffects effects : model.getPhotoEffectList()) {
            Photo photo = effects.getPhoto();
            Float ratio = new Float((float)photo.getWidth() / photo.getHeight());
            Counter count = ratioCount.get(ratio);
            if (count != null)
                count.increment();
            else
                ratioCount.put(ratio, new Counter(ratio.floatValue()));
        }

        // Use ratio with highest count
        return Collections.max(ratioCount.values()).getRatio();
    }

    /**
     * @param directory Directory to look for image files in
     * @return List of Photo objects for valid images
     */
    public static List<Photo> buildPhotoList(File directory) throws IOException {
        File[] photoFiles = directory.listFiles(new ExtensionFileFilter(ExtensionFileFilter.FILTER_IMAGES));

        if (photoFiles == null)
            throw new IOException(SystemMessages.getMessage(SystemMessages.ERR_INVALIDDIRECTORY, directory.toString()));

        // File implements Comparable, so hopefully this sorts based on filesystem order
        Arrays.sort(photoFiles);

        return ImageCoder.getInstance().validatePhotoFiles(photoFiles);
    }

    /**
     * Lookup a WizardEffect index by key.
     * @return null if key not found
     */
    public static Integer findEffectIndex(List effectList, String key) {
        if (effectList == LIST_BEGINTRANSITIONS)
            return begintransKey2IndexMap.get(key);
        else if (effectList == LIST_EFFECTS)
            return effectKey2IndexMap.get(key);
        else if (effectList == LIST_ENDTRANSITIONS)
            return endtransKey2IndexMap.get(key);
        return null;
    }

    /**
     * Find the WizardEffect wrapping the given BaseEffect
     * @return null if no wrapper
     */
    public static WizardEffect findWizardEffect(BaseEffect effect) {
        return base2WizardMap.get(effect);
    }

    /**
     * Compute number of photos needed to fill the duration given the effect and trans durations
     */
    public static int computePhotoCount(double dblTotalDuration,
            double dblTransDuration, double dblEffectDuration) {
        // TN + EN + T = D
        // dblTransDuration*nImageCount + dblEffectDuration*nImageCount + dblTransDuration = dblTotalDuration
        // Solve for N (nImageCount). This may duplicate or drop images.
        int nImageCount = (int)Math.round((dblTotalDuration - dblTransDuration)
            / (dblEffectDuration + dblTransDuration));
        if (nImageCount <= 0)
            nImageCount = 1;
        return nImageCount;
    }

    /**
     * Compute the total duration given the number of photos and effect and trans durations
     */
    public static double computeShowDuration(int nImageCount,
            double dblTransDuration, double dblEffectDuration) {
        // TN + EN + T = D
        return dblTransDuration*nImageCount + dblEffectDuration*nImageCount + dblTransDuration;
    }

    /**
     * Return MP3 duration if an mp3 is set and used, otherwise return 0
     */
    public static double computeMP3Duration(ShowModel model) {
        MP3 mp3 = model.getMP3();
        if (mp3 != null && model.getMP3Mode() != ShowModel.MP3Mode.NONE)
            return mp3.getDuration();
        return 0;
    }

    /**
     * Evenly redistribute time among the unlocked photos effect durations to match the shows MP3 duration.
     */
    public static void redistributeTime(ShowModel model) {
        double showDuration = model.computeShowDuration();

        // Use mp3 duration, or total show duration if none.
        // Ignore mp3 mode, use duration even if mode is NONE
        MP3 mp3 = model.getMP3();
        double mp3Duration = showDuration;
        if (mp3 != null)
            mp3Duration = mp3.getDuration();

        ShowList showList = model.getPhotoEffectList();
        int showListCount = showList.size();

        // Subtract unlocked photos effect duration from show total
        int unlockedCount = 0;
        for (PhotoEffects effects : showList) {
            if (!effects.isLocked()) {
                showDuration -= effects.getEffectDuration();
                unlockedCount++;
            }
        }

        // All are locked
        if (unlockedCount == 0)
            return;

        // Compute time that needs to be distributed among unlocked photos
        double distributeTime = mp3Duration - showDuration;
        double distributeEffectDuration = (distributeTime >= 0.0 ? distributeTime : 0.0) / unlockedCount;

        // Reset each unlocked photos duration in model
        for (int i = 0; i < showListCount; i++) {
            PhotoEffects effects = showList.get(i);
            if (!effects.isLocked()) {
                effects = new PhotoEffects(effects, effects.getBeginTransitionDuration(),
                        distributeEffectDuration, effects.getEndTransitionDuration());
                showList.set(i, effects);
            }
        }
    }

    /**
     * Builds a list of PhotoEffects using the Photos from the PhotoEffects in sourcePhotoEffectList.
     * The ShowModels list is not used or modified.
     * @param model The model is not modified.
     * @param sourcePhotoEffectList Source list of PhotoEffects
     * @param adjustTiming If true, adjust timing of each PhotoEffects and repeat/truncate photos to fit MP3.
     *   Otherwise use existing timing for each PhotoEffects.
     * @return List of PhotoEffects built by Wizard
     */
    public static List<PhotoEffects> buildShowList(ShowModel model, List<PhotoEffects> sourcePhotoEffectList, boolean adjustTiming) {
        // Get allowable selected effects/trans.
        List<WizardEffectBeginTransition> begintransList = ShowModel.applyListSelection(model.getBeginTransitionSelectionModel(), LIST_BEGINTRANSITIONS);
        List<WizardEffectEffect> effectList = ShowModel.applyListSelection(model.getEffectSelectionModel(), LIST_EFFECTS);
        List<WizardEffectEndTransition> endtransList = ShowModel.applyListSelection(model.getEndTransitionSelectionModel(), LIST_ENDTRANSITIONS);

        int photoCount = sourcePhotoEffectList.size();
        int photoListCount = photoCount;
        double effectDuration = 0;
        double transitionDuration = 0;
        // We adjust timing even of locked photos
        if (adjustTiming) {
            // Get duration based on MP3
            double totalDuration = computeMP3Duration(model);

            effectDuration = model.getDefaultEffectDuration();
            transitionDuration = model.getDefaultTransitionDuration();
            if (totalDuration > 0) {
                // Compute images that will fit (repeating or dropping as needed)
                photoCount = computePhotoCount(totalDuration, transitionDuration, effectDuration);

                // Now use new N (photoCount) to solve for a new E (dblEffectDuration)
                double newEffectDuration = (totalDuration - transitionDuration - (transitionDuration * photoCount)) / photoCount;
                if (newEffectDuration > 0)
                    effectDuration = newEffectDuration;
            }

            if (DEBUG) System.err.println(photoListCount + " images specified, " + photoCount + " used");
        }
        ArrayList<PhotoEffects> showList = new ArrayList<PhotoEffects>(photoCount);

        WizardEffectBeginTransition currentBeginTrans = null;

        for (int i = 0; i < photoCount; i++) {

            PhotoEffects sourcePhotoEffects = sourcePhotoEffectList.get(i % photoListCount);
            PhotoEffects nextSourcePhotoEffects = i < photoCount ? sourcePhotoEffectList.get((i + 1) % photoListCount) : null;
            PhotoEffects newPhotoEffects = null;

            // If current photo is locked, we must use effect/transitions from the source
            if (sourcePhotoEffects.isLocked()) {
                if (DEBUG) System.err.println("Photo " + i + " locked");
                if (adjustTiming) {
                    newPhotoEffects = new PhotoEffects(sourcePhotoEffects,
                            transitionDuration, effectDuration, transitionDuration);
                }
                else
                    newPhotoEffects = sourcePhotoEffects;

                if (DEBUG) System.err.println("  locked " + newPhotoEffects.getBeginTransition().getTag() + " " + newPhotoEffects.getEffect().getTag() + " " + newPhotoEffects.getEndTransition().getTag() + " " + (newPhotoEffects.isEndTransitionTopLayer() ? "(top)" : "(bottom)"));

                // If next photo is not locked, we need to pick its BeginTrans partner for our EndTrans
                if (!nextSourcePhotoEffects.isLocked()) {
                    currentBeginTrans = pickTransitionPartner((WizardEffectEndTransition)findWizardEffect(newPhotoEffects.getEndTransition()),
                            begintransList, newPhotoEffects.isEndTransitionTopLayer() ? Layer.TOP : Layer.BOTTOM, WBEGINTRANS_None);
                    if (DEBUG) System.err.println("    next partner BeginTrans " + currentBeginTrans);
                }
                else
                    currentBeginTrans = null;
            }
            // If current photo unlocked, pick its transitions
            else {
                if (DEBUG) System.err.println("Photo " + i + " unlocked");

                // Pick random BeginTrans if one not chosen for us
                if (currentBeginTrans == null)
                    currentBeginTrans = pickRandom(begintransList, WBEGINTRANS_None);

                if (DEBUG) System.err.println("  current BeginTrans " + currentBeginTrans);

                // Pick random Effect
                WizardEffectEffect currentEffect = pickRandom(effectList, WEFFECT_None);
                if (DEBUG) System.err.println("  current Effect " + currentEffect);

                WizardEffectBeginTransition nextBeginTrans = null;
                WizardEffectEndTransition currentEndTrans;

                // Next source photo is locked
                if (nextSourcePhotoEffects != null && nextSourcePhotoEffects.isLocked()) {
                    if (DEBUG) System.err.println(" next locked");

                    // Pick current EndTrans partner for next BeginTrans
                    nextBeginTrans = (WizardEffectBeginTransition)findWizardEffect(nextSourcePhotoEffects.getBeginTransition());
                    currentEndTrans = pickTransitionPartner(nextBeginTrans, endtransList,
                            nextBeginTrans.getPreferredLayer(), WENDTRANS_None);

                    if (DEBUG) System.err.println("  current partner EndTrans " + currentEndTrans);
                }
                // Next source photo is not locked
                else {
                    if (DEBUG) System.err.println("  next unlocked");
                    // Pick random current EndTrans, then pick partner next BeginTrans
                    if (pickRandomBoolean()) {
                        // Pick random current EndTrans
                        currentEndTrans = pickRandom(endtransList, WENDTRANS_None);
                        if (DEBUG) System.err.println("  current random EndTrans " + currentEndTrans);

                        // Pick next BeginTrans partner for current EndTrans
                        nextBeginTrans = pickTransitionPartner(currentEndTrans, begintransList,
                                currentEndTrans.getPreferredLayer(), WBEGINTRANS_None);
                        if (DEBUG) System.err.println("    next partner BeginTrans " + nextBeginTrans);
                    }
                    // Otherwise pick random next BeginTrans, then pick partner current EndTrans
                    else {
                        // Pick random next BeginTrans
                        nextBeginTrans = pickRandom(begintransList, WBEGINTRANS_None);
                        if (DEBUG) System.err.println("    next random BeginTrans " + nextBeginTrans);

                        // Pick next BeginTrans partner for current EndTrans
                        currentEndTrans = pickTransitionPartner(nextBeginTrans, endtransList,
                                nextBeginTrans.getPreferredLayer(), WENDTRANS_None);
                        if (DEBUG) System.err.println("  current partner EndTrans " + currentEndTrans);
                    }
                }

                // Reconcile transition layers
                boolean isEndTransitionTopLayer = false;
                if (currentEndTrans.getPreferredLayer() == Layer.EITHER) {
                    switch (nextBeginTrans.getPreferredLayer()) {
                    case TOP:
                        isEndTransitionTopLayer = false;
                        break;
                    case BOTTOM:
                        isEndTransitionTopLayer = true;
                        break;
                    case EITHER:
                        isEndTransitionTopLayer = pickRandomBoolean();
                        break;
                    }
                }
                else
                    isEndTransitionTopLayer = currentEndTrans.getPreferredLayer() == Layer.TOP;

                if (DEBUG) System.err.println("  current EndTrans layer " + (isEndTransitionTopLayer ? "top" : "bottom"));

                // Create a PhotoEffects for this sourcePhotoEffects
                if (adjustTiming) {
                    newPhotoEffects = new PhotoEffects(sourcePhotoEffects.getPhoto(),
                            sourcePhotoEffects.getEventArg(), currentEffect.getPhotoScale(),
                            currentBeginTrans.getEffect(), transitionDuration,
                            currentEffect.getEffect(), effectDuration,
                            currentEndTrans.getEffect(), transitionDuration, isEndTransitionTopLayer, false);
                }
                else {
                    newPhotoEffects = new PhotoEffects(sourcePhotoEffects.getPhoto(),
                            sourcePhotoEffects.getEventArg(), currentEffect.getPhotoScale(),
                            currentBeginTrans.getEffect(), sourcePhotoEffects.getBeginTransitionDuration(),
                            currentEffect.getEffect(), sourcePhotoEffects.getEffectDuration(),
                            currentEndTrans.getEffect(), sourcePhotoEffects.getEndTransitionDuration(),
                            isEndTransitionTopLayer, false);
                }

                currentBeginTrans = nextBeginTrans;
            }

            showList.add(newPhotoEffects);
        }

        return showList;
    }

    private static boolean pickRandomBoolean() {
        return random(2) == 0;
    }

    private static <T extends WizardEffectTransition<?,P>, P extends WizardEffectTransition<?,T>>
            P pickTransitionPartner(T transition, List<P> basePartnerList, Layer transitionLayer, P defaultPartner) {

        // Pick partner from this transitions partner list
        int partnerTransIndex;
        P partnerTrans = null;
        List<P> partnerList = transition.getPartnerTransitions();
        if (partnerList != null) {
            partnerTransIndex = pickRandomIndex(partnerList);
            if (partnerTransIndex != -1) {
                partnerTrans = partnerList.get(partnerTransIndex);
                // If we found a partner, but it wants to use the same layer or is not allowed, then keep looking
                if (!basePartnerList.contains(partnerTrans) || !isCompatibleLayer(partnerTrans.getPreferredLayer(), transitionLayer)) {
                    // If there are no additional partners, don't bother
                    if (partnerList.size() > 1) {
                        List<P> filteredPartnerList = new FilteredList<P>(partnerList);
                        while (!basePartnerList.contains(partnerTrans) || !isCompatibleLayer(partnerTrans.getPreferredLayer(), transitionLayer)) {
                            filteredPartnerList.remove(partnerTransIndex);
                            partnerTransIndex = pickRandomIndex(filteredPartnerList);
                            if (partnerTransIndex == -1) {
                                partnerTrans = null;
                                break;
                            }
                            partnerTrans = filteredPartnerList.get(partnerTransIndex);
                        }
                    }
                    else
                        partnerTrans = null;
                }
            }
        }

        if (partnerTrans != null)
            return partnerTrans;

        // No partner, so pick a random partner from the base list
        partnerTransIndex = pickRandomIndex(basePartnerList);
        if (partnerTransIndex == -1)
            return defaultPartner;

        partnerTrans = basePartnerList.get(partnerTransIndex);

        // If layer is compatible, use it
        if (isCompatibleLayer(partnerTrans.getPreferredLayer(), transitionLayer))
            return partnerTrans;

        // If layer is incompatible, continue searching.
        // Wrap the list in a filtered list so we can appear to "remove" items from it
        // without modifying the original.
        List<P> filteredBasePartnerList = new FilteredList<P>(basePartnerList);
        while (!isCompatibleLayer(partnerTrans.getPreferredLayer(), transitionLayer)) {
            filteredBasePartnerList.remove(partnerTransIndex);
            partnerTransIndex = pickRandomIndex(filteredBasePartnerList);
            if (partnerTransIndex == -1)
                return defaultPartner;
            partnerTrans = filteredBasePartnerList.get(partnerTransIndex);
        }

        return partnerTrans;
    }

    private static boolean isCompatibleLayer(Layer layer1, Layer layer2) {
        return layer1 != layer2 || (layer1 == layer2 && layer1 == Layer.EITHER);
    }

    private static <T> T pickRandom(List<T> list, T defaultEffect) {
        int size = list.size();
        if (size == 0)
            return defaultEffect;
        else if (size == 1)
            return list.get(0);
        else
            return list.get(random(size));
    }

    private static int pickRandomIndex(List list) {
        int size = list.size();
        if (size == 0)
            return -1;
        else if (size == 1)
            return 0;
        else
            return random(size);
    }

    // Returns random number between 0 and max-1
    private static int random(int max) {
        return (int)Math.floor(Math.random() * max);
    }
}

// Compare effects by display name
//XXX implement Comparable on WizardEffect? that might confuse List.contains()
class DisplayNameComparator implements Comparator<WizardEffect> {
    public int compare(WizardEffect w1, WizardEffect w2) {
        if (w1 == w2)
            return 0;
        String name1 = (w1).getDisplayName();
        String name2 = (w2).getDisplayName();
        return name1.compareTo(name2);
    }
}
