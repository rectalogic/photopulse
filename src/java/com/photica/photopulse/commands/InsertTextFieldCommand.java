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

import com.iv.flash.api.AlphaColor;
import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.text.Font;
import com.iv.flash.api.text.TextField;
import com.iv.flash.commands.GenericCommand;
import com.iv.flash.context.Context;
import com.iv.flash.util.GeomHelper;
import com.iv.flash.util.IVException;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.geom.Rectangle2D;

/**
 * Inserts a new TextField which can reference a named font.
 * The font must be available in the containing FlashFile
 * (e.g. via the SkinExternal mechanism)
 */
public class InsertTextFieldCommand extends GenericCommand {
    public void doCommand(FlashFile file, Context context, Script parent, int frame) throws IVException {
        int width = getIntParameter(context, "width", 0) * ShowGenerator.TWIPS_PER_PIXEL;
        int height = getIntParameter(context, "height", 0) * ShowGenerator.TWIPS_PER_PIXEL;
        boolean wordWrap = getBoolParameter(context, "wordWrap", true);
        String fontName = getParameter(context, "fontName", "_sans");
        int fontHeight = getIntParameter(context, "fontHeight", 16) * ShowGenerator.TWIPS_PER_PIXEL;
        AlphaColor color = getColorParameter(context, "fontColor", AlphaColor.black);
        String variable = getParameter(context, "variable");

        String param = getParameter(context, "alignment");
        int alignment = -1;
        if ("left".equals(param))
            alignment = 0;
        else if ("right".equals(param))
            alignment = 1;
        else if ("center".equals(param))
            alignment = 2;
        else if ("justify".equals(param))
            alignment = 3;

        Instance inst = getInstance();

        // The font definition must already be loaded (e.g. as a FlashFile external)
        Font font = file.getFont(fontName);

        TextField textField = new TextField(null, variable, font, fontHeight, color);
        textField.addFlags(TextField.NOSELECT);

        Rectangle2D winBounds = GeomHelper.getTransformedSize(inst.matrix,
                GeomHelper.newRectangle(-1024, -1024, 2048, 2048));
        GeomHelper.deScaleMatrix(inst.matrix);
        inst.matrix.translate(-winBounds.getWidth()/2, -winBounds.getHeight()/2);

        textField.setBounds(new Rectangle2D.Double(0, 0,
                width == 0 ? winBounds.getWidth() : width,
                height == 0 ? winBounds.getHeight() : height));

        if (font != null)
            textField.addFlags(TextField.USEOUTLINES);
        if (wordWrap)
            textField.addFlags(TextField.WORDWRAP);
        textField.setFont(font, fontHeight);

        textField.setColor(color);
        if (alignment != -1) {
            textField.setLayout(alignment, textField.getLeftMargin(), textField.getRightMargin(),
                    textField.getIndent(), textField.getLeading());
        }

        inst.def = textField;
    }
}
