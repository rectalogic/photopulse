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

import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.event.MouseEvent;

/**
 * JLabel that acts like a hyperlink and launches a file/URL
 */
public class HyperLabel extends JLabel {

    private String document;

    private static final Color LINK_COLOR = Color.BLUE;

    private static final Border noBorder = BorderFactory.createEmptyBorder(0, 0, 1, 0);
    private static final  Border underlineBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, LINK_COLOR);

    public HyperLabel() {
        this(null, null);
    }

    public HyperLabel(String text) {
        this(text, null);
    }

    public HyperLabel(String label, String document) {
        super(label);
        setLink(document);

        enableEvents(MouseEvent.MOUSE_EVENT_MASK);
    }

    public void setLink(String document) {
        this.document = document;
        if (this.document != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(this.document);
            setEnabled(true);
            setBorder(underlineBorder);
            setForeground(LINK_COLOR);
        }
        else {
            setCursor(null);
            setToolTipText(null);
            setEnabled(false);
            setBorder(noBorder);
            setForeground(null);
        }
    }

    protected void processMouseEvent(MouseEvent ev) {
        if (!isEnabled() || ev.getID() != MouseEvent.MOUSE_CLICKED)
            return;

        if (!launchDocument(document)) {
            ErrorDialog.showErrorDialog(this.getRootPane(),
                    UIMessages.getMessage(UIMessages.ERR_LAUNCH_FAILED, document));
        }
    }

    public static boolean launchDocument(String document) {
        if (document == null)
            return false;
        try {
            return launchDocumentNative(document);
        } catch (UnsatisfiedLinkError e) {
            return false;
        }
    }

    private static native boolean launchDocumentNative(String strDocument);
}
