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

package com.photica.photopulse.commands;

import com.iv.flash.api.Frame;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.util.FlashOutput;

public class NoopInstance extends Instance {

    /**
     * Replace inst with a Noop instance.
     * It is not safe to remove an instance from the Frame in a command because
     * Frame.doCommand is looping over instances.
     */
    public void replaceInstance(Script script, int nFrame, Instance inst) {
        Frame frmParent = script.getFrameAt(nFrame);
        int nSize = frmParent.size();
        for (int i = 0; i < nSize; i++) {
            if (frmParent.elementAt(i) == inst) {
                frmParent.setFlashObjectAt(this, i);
                break;
            }
        }
    }

    public void write(FlashOutput fob) {
        // Write nothing
    }
}
