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

package com.photica.photopulse.ui.wizard;

import com.photica.photopulse.PhotoPulse;

/**
 * This class is the value of the magic "sun.awt.exception.handler" property.
 * See the source for java.awt.EventDispatchThread.handleException()
 * XXX this is a Sun specific hack until a public Java API exists
 *
 * This classes misses any exceptions thrown while calling java.beans.EventHandler.invoke
 * EventHandler catches exceptions and just dumps them to stderr.
 */
public class AWTExceptionHandler {

    public void handle(Throwable e) {
        PhotoPulse.logException(e);
        ErrorDialog.showErrorDialog(WizardUI.getInstance(),
            UIMessages.getMessage(UIMessages.ERR_AWT_EXCEPTION, e.toString()));
    }
}
