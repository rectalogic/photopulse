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

import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provides display names for effects and transitions.
 */
public class DisplayNames extends ListResourceBundle {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(DisplayNames.class.getName());


    private static final Object[][] contents = {

        { Wizard.WEFFECT_FallingBubbles.getKey(),
            "Falling bubbles" },
        { Wizard.WEFFECT_FallingHearts.getKey(),
            "Falling hearts" },
        { Wizard.WEFFECT_FallingLeaves.getKey(),
            "Falling leaves" },
        { Wizard.WEFFECT_FallingSnowflakes.getKey(),
            "Falling snowflakes" },
        { Wizard.WEFFECT_Lens.getKey(),
            "Draggable roaming magnifying glass" },
        { Wizard.WEFFECT_MaxZoomIn.getKey(),
            "Zoom in 50% (center)" },
        { Wizard.WEFFECT_MaxZoomInTopCenter.getKey(),
            "Zoom in 50% (top center)" },
        { Wizard.WEFFECT_MaxZoomOut.getKey(),
            "Zoom out 50% (center)" },
        { Wizard.WEFFECT_MaxZoomOutTopCenter.getKey(),
            "Zoom out 50% (top center)" },
        { Wizard.WEFFECT_None.getKey(),
            "No effect" },
        { Wizard.WEFFECT_PanLeftRight.getKey(),
            "Pan left to right" },
        { Wizard.WEFFECT_PanoramaLeft.getKey(),
            "Scrolling left panorama" },
        { Wizard.WEFFECT_PanoramaRight.getKey(),
            "Scrolling right panorama" },
        { Wizard.WEFFECT_Ripple.getKey(),
            "Rippling water" },
        { Wizard.WEFFECT_RisingBubbles.getKey(),
            "Rising bubbles" },
        { Wizard.WEFFECT_RisingHearts.getKey(),
            "Rising hearts" },
        { Wizard.WEFFECT_Shake.getKey(),
            "Shaking" },
        { Wizard.WEFFECT_Spotlight.getKey(),
            "Draggable roaming spotlight" },
        { Wizard.WEFFECT_Vortex.getKey(),
            "Swirling balls" },
        { Wizard.WEFFECT_ToneSepia.getKey(),
            "Sepia tone" },
        { Wizard.WEFFECT_ToneGray.getKey(),
            "Gray tone" },
        { Wizard.WEFFECT_ZoomIn.getKey(),
            "Zoom in 10% (center)" },
        { Wizard.WEFFECT_ZoomOut.getKey(),
            "Zoom out 10% (center)" },

        { Wizard.WBEGINTRANS_Fade.getKey(),
            "Fade" },
        { Wizard.WENDTRANS_Fade.getKey(),
            "Fade" },
        { Wizard.WBEGINTRANS_FadeBlack.getKey(),
            "Fade from black" },
        { Wizard.WENDTRANS_FadeBlack.getKey(),
            "Fade to black" },
        { Wizard.WBEGINTRANS_FadeWhite.getKey(),
            "Fade from white" },
        { Wizard.WENDTRANS_FadeWhite.getKey(),
            "Fade to white" },
        { Wizard.WBEGINTRANS_FlipHorizontal.getKey(),
            "Flip horizontal" },
        { Wizard.WENDTRANS_FlipHorizontal.getKey(),
            "Flip horizontal" },
        { Wizard.WBEGINTRANS_FlipVertical.getKey(),
            "Flip vertical" },
        { Wizard.WENDTRANS_FlipVertical.getKey(),
            "Flip vertical" },
        { Wizard.WBEGINTRANS_Heart.getKey(),
            "Heart wipe" },
        { Wizard.WENDTRANS_Heart.getKey(),
            "Heart wipe" },
        { Wizard.WBEGINTRANS_Iris.getKey(),
            "Iris wipe" },
        { Wizard.WENDTRANS_Iris.getKey(),
            "Iris wipe" },
        { Wizard.WBEGINTRANS_LawBadge.getKey(),
            "Sheriff's badge wipe" },
        { Wizard.WENDTRANS_LawBadge.getKey(),
            "Sheriff's badge wipe" },
        { Wizard.WBEGINTRANS_Melt.getKey(),
            "Melt wipe" },
        { Wizard.WENDTRANS_Melt.getKey(),
            "Melt wipe" },
        { Wizard.WENDTRANS_None.getKey(),
            "No Transition" },
        { Wizard.WBEGINTRANS_None.getKey(),
            "No Transition" },
        { Wizard.WBEGINTRANS_Plus.getKey(),
            "Plus morph wipe" },
        { Wizard.WENDTRANS_Plus.getKey(),
            "Plus morph wipe" },
        { Wizard.WBEGINTRANS_SkewLeft.getKey(),
            "Skew left" },
        { Wizard.WENDTRANS_SkewLeft.getKey(),
            "Skew left" },
        { Wizard.WBEGINTRANS_SkewRight.getKey(),
            "Skew right" },
        { Wizard.WENDTRANS_SkewRight.getKey(),
            "Skew right" },
        { Wizard.WBEGINTRANS_SlideDown.getKey(),
            "Slide down" },
        { Wizard.WENDTRANS_SlideDown.getKey(),
            "Slide down" },
        { Wizard.WBEGINTRANS_SlideLeft.getKey(),
            "Slide left" },
        { Wizard.WENDTRANS_SlideLeft.getKey(),
            "Slide left" },
        { Wizard.WBEGINTRANS_SlideRight.getKey(),
            "Slide right" },
        { Wizard.WENDTRANS_SlideRight.getKey(),
            "Slide right" },
        { Wizard.WBEGINTRANS_SlideUp.getKey(),
            "Slide up" },
        { Wizard.WENDTRANS_SlideUp.getKey(),
            "Slide up" },
        { Wizard.WBEGINTRANS_Spin.getKey(),
            "Spin and zoom" },
        { Wizard.WENDTRANS_Spin.getKey(),
            "Spin and zoom" },
        { Wizard.WBEGINTRANS_Star.getKey(),
            "Star wipe" },
        { Wizard.WENDTRANS_Star.getKey(),
            "Star wipe" },
        { Wizard.WBEGINTRANS_StarBurst.getKey(),
            "Starburst wipe" },
        { Wizard.WENDTRANS_StarBurst.getKey(),
            "Starburst wipe" },
        { Wizard.WBEGINTRANS_StretchHorizontal.getKey(),
            "Stretch horizontal" },
        { Wizard.WENDTRANS_StretchHorizontal.getKey(),
            "Stretch horizontal" },
        { Wizard.WBEGINTRANS_StretchVertical.getKey(),
            "Stretch vertical" },
        { Wizard.WENDTRANS_StretchVertical.getKey(),
            "Stretch vertical" },
        { Wizard.WBEGINTRANS_VenetianHorizontal.getKey(),
            "Venetian blinds horizontal" },
        { Wizard.WENDTRANS_VenetianHorizontal.getKey(),
            "Venetian blinds horizontal" },
        { Wizard.WBEGINTRANS_VenetianVertical.getKey(),
            "Venetian blinds vertical" },
        { Wizard.WENDTRANS_VenetianVertical.getKey(),
            "Venetian blinds vertical" },
        { Wizard.WBEGINTRANS_WipeCenter.getKey(),
            "Wipe center" },
        { Wizard.WENDTRANS_WipeCenter.getKey(),
            "Wipe center" },
        { Wizard.WBEGINTRANS_WipeDiagonalBottomLeft.getKey(),
            "Diagonal wipe bottom left" },
        { Wizard.WENDTRANS_WipeDiagonalBottomLeft.getKey(),
            "Diagonal wipe bottom left" },
        { Wizard.WBEGINTRANS_WipeDiagonalBottomRight.getKey(),
            "Diagonal wipe bottom right" },
        { Wizard.WENDTRANS_WipeDiagonalBottomRight.getKey(),
            "Diagonal wipe bottom right" },
        { Wizard.WBEGINTRANS_WipeDiagonalTopLeft.getKey(),
            "Diagonal wipe top left" },
        { Wizard.WENDTRANS_WipeDiagonalTopLeft.getKey(),
            "Diagonal wipe top left" },
        { Wizard.WBEGINTRANS_WipeDiagonalTopRight.getKey(),
            "Diagonal wipe top right" },
        { Wizard.WENDTRANS_WipeDiagonalTopRight.getKey(),
            "Diagonal wipe top right" },
        { Wizard.WBEGINTRANS_WipeDown.getKey(),
            "Wipe down" },
        { Wizard.WENDTRANS_WipeDown.getKey(),
            "Wipe down" },
        { Wizard.WBEGINTRANS_WipeHorizontal.getKey(),
            "Wipe horizontal center" },
        { Wizard.WENDTRANS_WipeHorizontal.getKey(),
            "Wipe horizontal center" },
        { Wizard.WBEGINTRANS_WipeLeft.getKey(),
            "Wipe left" },
        { Wizard.WENDTRANS_WipeLeft.getKey(),
            "Wipe left" },
        { Wizard.WBEGINTRANS_WipeRight.getKey(),
            "Wipe right" },
        { Wizard.WENDTRANS_WipeRight.getKey(),
            "Wipe right" },
        { Wizard.WBEGINTRANS_WipeUp.getKey(),
            "Wipe up" },
        { Wizard.WENDTRANS_WipeUp.getKey(),
            "Wipe up" },
        { Wizard.WBEGINTRANS_WipeVertical.getKey(),
            "Wipe vertical center" },
        { Wizard.WENDTRANS_WipeVertical.getKey(),
            "Wipe vertical center" },
        { Wizard.WBEGINTRANS_ZoomBoth.getKey(),
            "Zoom center" },
        { Wizard.WENDTRANS_ZoomBoth.getKey(),
            "Zoom center" },
        { Wizard.WBEGINTRANS_ZoomHorizontal.getKey(),
            "Zoom horizontal" },
        { Wizard.WENDTRANS_ZoomHorizontal.getKey(),
            "Zoom horizontal" },
        { Wizard.WBEGINTRANS_ZoomVertical.getKey(),
            "Zoom vertical" },
        { Wizard.WENDTRANS_ZoomVertical.getKey(),
            "Zoom vertical" },
    };

    // Overrides ListResourceBundle
    protected Object[][] getContents() {
        return contents;
    }

    public static String getDisplayName(String strKey) throws MissingResourceException {
        return RESOURCE_BUNDLE.getString(strKey);
    }
}
