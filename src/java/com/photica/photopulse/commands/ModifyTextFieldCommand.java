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

import com.iv.flash.api.FlashFile;
import com.iv.flash.api.FlashObject;
import com.iv.flash.api.Instance;
import com.iv.flash.api.Script;
import com.iv.flash.api.AlphaColor;
import com.iv.flash.api.text.TextField;
import com.iv.flash.commands.GenericCommand;
import com.iv.flash.context.Context;
import com.iv.flash.util.IVException;
import com.iv.flash.util.ScriptCopier;
import com.photica.photopulse.flash.ShowGenerator;

import java.awt.geom.Rectangle2D;

/**
 * Locates an existing TextField in the instance the command is applied to,
 * and modifies its width/height, font height and color.
 * The command must be applied to a MovieClip or Graphic that contains only a TextField on frame 1.
 * XXX This command is now only used for compatiblity with old skins. It should be removed once they are updated.
 * @deprecated
 */
@Deprecated
public class ModifyTextFieldCommand extends GenericCommand {
    public void doCommand(FlashFile file, Context context, Script parent, int frame) throws IVException {
        int width = getIntParameter(context, "width", 0) * ShowGenerator.TWIPS_PER_PIXEL;
        int height = getIntParameter(context, "height", 0) * ShowGenerator.TWIPS_PER_PIXEL;
        int fontHeight = getIntParameter(context, "fontHeight", 0) * ShowGenerator.TWIPS_PER_PIXEL;
        AlphaColor color = getColorParameter(context, "fontColor", null);

        String param = getParameter(context, "alignment", null);
        int alignment = -1;
        if ("left".equals(param))
            alignment = 0;
        else if ("right".equals(param))
            alignment = 1;
        else if ("center".equals(param))
            alignment = 2;
        else if ("justify".equals(param))
            alignment = 3;

        Instance inst = getCommandInstance(file, context, parent, frame);
        Script script = inst.getScript();
        if (script.getFrameCount() != 1)
            return;
        FlashObject targetObj = script.getFrameAt(0).getFlashObjectAt(0);
        if (!(targetObj instanceof Instance))
            return;
        Instance targetInst = (Instance)targetObj;

        TextField textField = (TextField)(targetInst).def.getCopy(new ScriptCopier());
        Rectangle2D bounds = textField.getBounds();
        bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(),
                width != 0 ? width : bounds.getWidth(),
                height != 0 ? height : bounds.getHeight());
        textField.setBounds(bounds);
        if (fontHeight != 0)
            textField.setFont(textField.getFont(), fontHeight);
        if (color != null)
            textField.setColor(color);
        if (alignment != -1) {
            textField.setLayout(alignment, textField.getLeftMargin(), textField.getRightMargin(),
                    textField.getIndent(), textField.getLeading());
        }
        inst.def = textField;

        inst.matrix.concatenate(targetInst.matrix);
    }
}
