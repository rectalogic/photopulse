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

package com.photica.photopulse.progress;

import java.util.Stack;

/**
 * Global singleton progress reporter.
 */
public class ProgressReporter {

    private static ProgressIndicator progressIndicator;

    private static float absoluteProgress;
    private static float progressPortionBase;
    private static float progressPortion;
    private static Stack<ProgressPortion> progressStack = new Stack<ProgressPortion>();

    /**
     * Register a ProgressIndicator for the reporter to use.
     * Set to null when finished.
     */
    public static void setProgressIndicator(ProgressIndicator pi) {
        progressIndicator = pi;
        progressPortionBase = 0;
        progressPortion = 1.0f;
        absoluteProgress = 0;
        progressStack.clear();
    }

    /**
     * Set the current portion
     * @param portion Percentage 0.0->1.0
     */
    public static void pushProgressPortion(float portion) {
        progressStack.push(new ProgressPortion(progressPortionBase, portion));
        progressPortionBase = absoluteProgress;

        // Multiply in portion
        if (portion != 0)
            progressPortion *= portion;
    }

    public static void popProgressPortion() {
        ProgressPortion popPortion = progressStack.pop();
        float portion = popPortion.getPortion();

        // Update to 100% of current portion
        if (portion != 0)
            updateProgress(1.0f);

        // Reset base
        progressPortionBase = popPortion.getBase();
        // Divide out portion
        if (portion != 0)
            progressPortion /= portion;
    }

    /**
     * Report progress as a percentage of the current portion - 0.0->1.0
     */
    public static void updateProgress(float progress) {
        absoluteProgress = progressPortionBase + (progress * progressPortion);
        ProgressIndicator pi = progressIndicator;
        if (pi != null)
            pi.updateProgress(absoluteProgress);
    }

    public static boolean isCanceled() {
        ProgressIndicator pi = progressIndicator;
        if (pi != null)
            return pi.isCanceled();
        else
            return false;
    }
}

class ProgressPortion {
    private float base;
    private float portion;

    public ProgressPortion(float base, float portion) {
        this.base = base;
        this.portion = portion;
    }

    public float getBase() {
        return base;
    }

    public float getPortion() {
        return portion;
    }
}