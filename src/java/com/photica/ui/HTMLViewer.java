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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.ui;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ObjectView;
import java.awt.Color;
import java.awt.Component;
import java.net.URL;

/**
 * Readonly HTML viewer that does not allow embedded OBJECT elements
 * since these could compromise the hosting application.
 */
public class HTMLViewer extends JEditorPane {
    private URL documentBase;

    public HTMLViewer() {
        setEditable(false);
        // No selection highlighting
        setHighlighter(null);
    }

    protected EditorKit createDefaultEditorKit() {
        return new RestrictedHTMLEditorKit();
    }

    /**
     * Set base URL to resolve document images relative to.
     */
    public void setDocumentBase(URL url) {
        documentBase = url;
        Document document = getDocument();
        if (document instanceof HTMLDocument)
            ((HTMLDocument)document).setBase(url);
    }

    public URL getDocumentBase() {
        return documentBase;
    }

    private class RestrictedHTMLEditorKit extends HTMLEditorKit {
        public ViewFactory getViewFactory() {
            return new RestrictedViewFactory();
        }

        public Document createDefaultDocument() {
            Document document = super.createDefaultDocument();
            // Set viewers base URL on the document
            if (document instanceof HTMLDocument)
                ((HTMLDocument)document).setBase(getDocumentBase());
            return document;
        }
    }
}


class RestrictedViewFactory extends HTMLEditorKit.HTMLFactory {
    public View create(Element element) {
        View view = super.create(element);
        if (view instanceof ObjectView)
            return new RestrictedObjectView(element);
        else
            return view;
    }
}

class RestrictedObjectView extends ObjectView {
    public RestrictedObjectView(Element elem) {
        super(elem);
    }

    protected Component createComponent() {
        Component comp = new JLabel("??");
        comp.setForeground(Color.red);
        return comp;
    }
}
