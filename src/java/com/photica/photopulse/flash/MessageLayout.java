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

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import com.photica.photopulse.PhotoPulse;

public class MessageLayout extends Layout {

    // Overrides OptionHandler
    public void activateOptions() {
    }

    public String format(LoggingEvent event) {
        String strMsg = event.getRenderedMessage() + "\n";

        ThrowableInformation ti = event.getThrowableInformation();
        if (ti != null) {
            Throwable throwable = ti.getThrowable();
            strMsg += throwable.getMessage() + "\n";
            PhotoPulse.logException(throwable);
        }
        return strMsg;
    }

    public boolean ignoresThrowable() {
        // We handle the throwable
        return false;
    }
};
