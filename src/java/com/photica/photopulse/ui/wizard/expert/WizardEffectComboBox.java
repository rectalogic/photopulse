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

import com.photica.photopulse.model.ShowSelectionModel;
import com.photica.photopulse.wizard.WizardEffect;
import com.photica.ui.JComboBoxFix;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.List;


/**
 * Combo that renders two independently sorted effect lists with a visual separator between.
 * The first list is selected effects, the second is unselected.
 */
class WizardEffectComboBox<WE extends WizardEffect> extends JComboBoxFix {
    private List<WE> effectList;
    private WE firstEffect;
    private int separatorIndex = 0;

    private boolean isCustomEffect = false;
    private String customLabel;

    /**
     * @param effectList Sorted (by display name) list of WizardEffects
     * @param firstEffect Effect that should go first regardless of sort
     */
    public WizardEffectComboBox(List<WE> effectList, WE firstEffect, ActionListener listener) {
        this.firstEffect = firstEffect;
        this.effectList = effectList;

        setMaximumRowCount(10);
        setPrototypeDisplayValue("mmmmmmmmmm");
        setRenderer(new WizardEffectComboRenderer(this));
        addActionListener(listener);

        Dimension minSize = getPreferredSize();
        minSize.width = 50;
        setMinimumSize(minSize);
    }

    /**
     * @param isCustom If true, and selection is set to null, then "Custom" will be rendered instead of empty
     */
    public void setCustomEffect(boolean isCustom) {
        this.isCustomEffect = isCustom;
    }

    public boolean isCustomEffect() {
        return isCustomEffect;
    }

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }

    public void setShowSelectionModel(ShowSelectionModel selectionModel) {
        selectionModel.addShowSelectionListener(new ShowSelectionListener());
        partitionLists(selectionModel);
    }

    private void partitionLists(ShowSelectionModel selectionModel) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        int sepIndex = 0;

        // Add the extra effect first, and ignore it if found later in the list iteration
        model.addElement(firstEffect);
        sepIndex++;

        int selectionIndex = 0;
        for (WizardEffect effect : effectList) {
            if (!firstEffect.equals(effect)) {
                // If the effect is selected, add it to the first partition, otherwise add to
                // end (second partition)
                if (selectionModel.isSelectedIndex(selectionIndex)) {
                    model.insertElementAt(effect, sepIndex);
                    sepIndex++;
                }
                else
                    model.addElement(effect);
            }
            selectionIndex++;
        }

        separatorIndex = sepIndex - 1;

        // Copy over the selection and replace the model
        model.setSelectedItem(getModel().getSelectedItem());
        setModel(model);
    }

    /**
     * Dividing index between the two partitions
     */
    public int getSeparatorIndex() {
        return separatorIndex;
    }

    private class ShowSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            partitionLists((ShowSelectionModel)e.getSource());
        }
    }
}


/**
 * Renders WizardEffect display names
 */
class WizardEffectComboRenderer extends BasicComboBoxRenderer {
    private static final Color DISABLED_COLOR = UIManager.getColor("Label.disabledForeground");
    private static Border SEPARATOR_BORDER = BorderFactory.createMatteBorder(1/*top*/, 0, 0, 0, Color.black);

    private WizardEffectComboBox combo;
    private Font customFont;
    private Font normalFont;

    public WizardEffectComboRenderer(WizardEffectComboBox combo) {
        this.combo = combo;
        this.customFont = combo.getFont().deriveFont(Font.ITALIC);
    }

    // 'index' will be -1 when the combo paints the current value (selection)
    public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        boolean displayCustom = value == null && combo.isCustomEffect();

        Component c = super.getListCellRendererComponent(list,
                value instanceof WizardEffect
                    ? ((WizardEffect)value).getDisplayName()
                    : (displayCustom ? combo.getCustomLabel() : value),
                index, isSelected, cellHasFocus);

        setFont(displayCustom ? customFont : combo.getFont());
        setEnabled(combo.isEnabled());

        // Paint everything disabled below separator.
        // Draw separator line using a custom Border above this cell.
        int separatorIndex = combo.getSeparatorIndex();
        setForeground(index > separatorIndex ? DISABLED_COLOR : null);
        setBorder(index == separatorIndex + 1 ? SEPARATOR_BORDER : null);

        return c;
    }
}
