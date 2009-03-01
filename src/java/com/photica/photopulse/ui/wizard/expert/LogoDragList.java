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
 
package com.photica.photopulse.ui.wizard.expert;

import com.photica.photopulse.Util;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;
import com.photica.ui.DragList;

import javax.swing.Icon;
import javax.swing.JToolTip;
import javax.swing.plaf.basic.BasicHTML;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.net.URL;

/**
 * A DragList that renders a logo icon when empty.
 */
class LogoDragList extends DragList<ShowList,PhotoEffects> {
    private static final URL HTML_BASE = ResourceIcon.class.getResource("resources/");

    private boolean isToolTipEnabled = false;
    private Icon logoIcon;

    public LogoDragList(Icon logoIcon, ShowList showList) {
        super(showList, new DragList.ListMutator<PhotoEffects,ShowList>());
        this.logoIcon = logoIcon;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logoIcon != null && getModel().getSize() == 0) {
            Rectangle visibleRect = getVisibleRect();
            logoIcon.paintIcon(this, g,
                    visibleRect.x + visibleRect.width/2 - logoIcon.getIconWidth()/2,
                    visibleRect.y + visibleRect.height/2 - logoIcon.getIconHeight()/2);
        }
    }

    public void setToolTipEnabled(boolean enabled) {
        this.isToolTipEnabled = enabled;
    }

    public boolean isToolTipEnabled() {
        return isToolTipEnabled;
    }

    /**
     * Compute tooltip text for cell here instead of setting the tooltip on the cell renderer component.
     * Setting it on the component requires we compute it every time a cell is rendered,
     * computing it here only does it when the tooltip is actually needed.
     */
    public String getToolTipText(MouseEvent event) {
        if (!isToolTipEnabled)
            return null;

        Point point = event.getPoint();
        int index = locationToIndex(point);
        Rectangle cellBounds;
        if (index != -1 && (cellBounds = getCellBounds(index, index)) != null &&
                cellBounds.contains(point.x, point.y)) {

            PhotoEffects effects = (PhotoEffects)getModel().getElementAt(index);

            // Get display names for each effect
            WizardEffect effect;
            effect = Wizard.findWizardEffect(effects.getBeginTransition());
            String begintransDisplayName = effect != null ? effect.getDisplayName() : null;
            effect = Wizard.findWizardEffect(effects.getEffect());
            String effectDisplayName = effect != null ? effect.getDisplayName() : null;
            effect = Wizard.findWizardEffect(effects.getEndTransition());
            String endtransDisplayName = effect != null ? effect.getDisplayName() : null;

            // If any names are null (custom effect not wrapped by a WizardEffect),
            // then use custom message.
            if (begintransDisplayName == null || effectDisplayName == null || endtransDisplayName == null) {
                String customMessage = UIMessages.getMessage(UIMessages.UI_CUSTOM_EFFECT);
                if (begintransDisplayName == null)
                    begintransDisplayName = customMessage;
                if (effectDisplayName == null)
                    effectDisplayName = customMessage;
                if (endtransDisplayName == null)
                    endtransDisplayName = customMessage;
            }

            // Start time of photo
            String startTime = Util.formatDuration(ShowModel.computeStartTime((ShowList)getModel(), index));

            return UIMessages.getMessage(UIMessages.UI_PHOTO_TOOLTIP,
                    new Object[] {
                        effects.getPhoto().getFile().getName(), startTime,
                        begintransDisplayName, effectDisplayName, endtransDisplayName,
                        effects.isEndTransitionTopLayer() ? UIMessages.getMessage(UIMessages.UI_ENDTRANS_LAYER_TOP) : UIMessages.getMessage(UIMessages.UI_ENDTRANS_LAYER_BOTTOM),
                    });
        }
        else
            return super.getToolTipText(event);
    }

    /**
     * Align tooltips to upper right of list - so they don't get in the way of clicking photos.
     */
    public Point getToolTipLocation(MouseEvent event) {
        // Need to take into account visible rect in case we are scrolled
        Rectangle visibleRect = getVisibleRect();
        return new Point(visibleRect.x + visibleRect.width, visibleRect.y);
    }

    /**
     * Set the base URL for tooltips so the tooltip HTML can locate image resources
     */
    public JToolTip createToolTip() {
        JToolTip tooltip = super.createToolTip();
        tooltip.putClientProperty(BasicHTML.documentBaseKey, HTML_BASE);
        return tooltip;
    }
}
