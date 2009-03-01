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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.ui.wizard.expert;

import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.GrayFilter;
import java.awt.Component;


/**
 * Renderer for end transition layer combo cells (renders EndTransitionLayer objects)
 */
class EndTransitionLayerComboRenderer extends BasicComboBoxRenderer {
    private JComboBox combo;
    public EndTransitionLayerComboRenderer(JComboBox combo) {
        this.combo = combo;
        setHorizontalAlignment(SwingConstants.LEADING);
        setHorizontalTextPosition(SwingConstants.TRAILING);
        setVerticalAlignment(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.CENTER);
    }
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        EndTransitionLayer layer = null;
        if (value instanceof EndTransitionLayer) {
            layer = (EndTransitionLayer)value;
            value = layer.getLabel();
        }

        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (layer != null) {
            setDisabledIcon(layer.getDisabledIcon());
            setIcon(layer.getIcon());
        }
        else {
            setDisabledIcon(null);
            setIcon(null);
        }

        setEnabled(combo.isEnabled());
        return c;
    }
}

/**
 * Item in end transition layer combo
 */
class EndTransitionLayer {
    private Icon icon;
    private Icon disabledIcon;
    private String label;
    private boolean isEndTransitionTopLayer;

    public EndTransitionLayer(Icon icon, String label, boolean endTransitionTopLayer) {
        this.icon = icon;
        if (icon instanceof ImageIcon)
            this.disabledIcon = new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon)icon).getImage()));
        this.label = label;
        isEndTransitionTopLayer = endTransitionTopLayer;
    }

    public Icon getIcon() {
        return icon;
    }

    public Icon getDisabledIcon() {
        return disabledIcon;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEndTransitionTopLayer() {
        return isEndTransitionTopLayer;
    }
}