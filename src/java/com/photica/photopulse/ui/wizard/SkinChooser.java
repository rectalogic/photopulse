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

import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.skin.SkinSet;
import com.photica.ui.HTMLViewer;
import com.photica.ui.JSpinnerFix;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays dialog to allow choosing of Skins
 */
public class SkinChooser extends BaseDialog {

    // Set if custom size skin selected
    private boolean isCustomSize = false;

    // Preferred show aspect ratio
    private float aspectRatio;

    // Controls whether size spinners are visible/hidden
    private JPanel spinnerPanel;

    // Displays skinset description
    private HTMLViewer descriptionViewer;

    // Currently selected skin
    private Skin skin;

    private String customSizeMessage = UIMessages.getMessage(UIMessages.UI_CUSTOMSIZE_LABEL);

    private JList skinsetList;
    private JList skinList;
    private JSpinner customWidthSpinner;
    private JSpinner customHeightSpinner;

    public SkinChooser() {
        super(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_TITLE), true);
        buildUI();
    }

    // Display a dialog containing this instance.
    // We need to dynamically create a dialog each time since the dialog parent changes.
    // Return true if user chose OK - in that case getSkin() is valid
    // Overrides BaseDialog
    public boolean showDialog(Component parent, Skin skin, float aspectRatio) {
        this.aspectRatio = aspectRatio;
        selectSkin(skin);
        boolean isConfirmed = super.showDialog(parent);
        this.aspectRatio = 0;

        // If custom size skin is selected, create new custom size skin
        if (isConfirmed && isCustomSize) {
            int width = ((Integer)customWidthSpinner.getValue()).intValue();
            int height = ((Integer)customHeightSpinner.getValue()).intValue();
            setSkin(new Skin(new Dimension(width, height)));
        }

        return isConfirmed;
    }

    /**
     * Attempt to select the given Skin in the UI
     */
    private void selectSkin(Skin skin) {
        if (skin == null)
            return;

        // Select the skinset.
        // If no skinset, use BUILTINSET and select the last skin (custom size skin)
        SkinSet skinset = skin.getSkinSet();
        if (skinset == null) {
            skinsetList.setSelectedValue(SkinManager.BUILTINSET, true);
            int index = skinList.getModel().getSize() - 1;
            skinList.setSelectedIndex(index);
            skinList.ensureIndexIsVisible(index);
            Dimension2D skinSize = skin.getSkinSizePixels();
            customHeightSpinner.setValue(new Integer((int)skinSize.getHeight()));
            customWidthSpinner.setValue(new Integer((int)skinSize.getWidth()));
        }
        else {
            skinsetList.setSelectedValue(skinset, true);
            skinList.setSelectedValue(skin, true);
        }
    }

    private void buildUI() {
        // Skin set selector
        skinsetList = new JList(new ListListModel(SkinManager.getSkinSets()));
        skinsetList.setSelectionModel(new SingleListSelectionModel());
        skinsetList.setVisibleRowCount(9);
        skinsetList.addListSelectionListener(new SkinSetSelectionHandler());
        JScrollPane skinsetListScroll = new JScrollPane(skinsetList);

        // Skin size selector
        skinList = new JList();
        skinList.setSelectionModel(new SingleListSelectionModel());
        skinList.setVisibleRowCount(9);
        skinList.setCellRenderer(new SkinCellRenderer());
        skinList.addListSelectionListener(new SkinSelectionHandler());
        JScrollPane skinListScroll = new JScrollPane(skinList);

        // Skin set description
        descriptionViewer = new HTMLViewer();
        JScrollPane descriptionTextScroll = new JScrollPane(descriptionViewer);
        // Lock down description size
        final int DESCRIPTION_ROWS = 10;
        final int DESCRIPTION_COLS = 30;
        FontMetrics metrics = descriptionViewer.getFontMetrics(descriptionViewer.getFont());
        Insets insets = descriptionViewer.getInsets();
        Dimension descriptionSize = new Dimension(DESCRIPTION_COLS * metrics.charWidth('m') + insets.left + insets.right,
                DESCRIPTION_ROWS * metrics.getHeight() + insets.top + insets.bottom);
        descriptionTextScroll.setPreferredSize(descriptionSize);

        // Lock down scrollpane preferred sizes.
        // Otherwise it changes depending on whether it is displaying
        // a scrollbar or not.
        // This makes everything jump around as the scrollbar is displayed/hidden.
        // Don't rely on JList.setPrototypeCellValue, compute preferred size here.
        String prototypeCellLabel = "mmmmmmmmmmmmmmmmm";
        Dimension cellSize = skinList.getCellRenderer().getListCellRendererComponent(skinList, prototypeCellLabel, 0, false, false).getPreferredSize();
        Dimension listSize = new Dimension(cellSize.width, skinListScroll.getPreferredSize().height);
        skinListScroll.setPreferredSize(listSize);
        skinsetListScroll.setPreferredSize(listSize);

        // Flash editor limits movie size to 18-2880 pixels
        final int maxSize = 2880;
        final int minSize = 20;
        final int stepSize = 10;

        // Configure width/height spinners
        //XXX get defaults from prefs?
        customWidthSpinner = configureSpinner(320, minSize, maxSize, stepSize);
        customHeightSpinner = configureSpinner(240, minSize, maxSize, stepSize);

        WizardGBC gbc = new WizardGBC();

        // Add spinners to a panel that can be shown/hidden
        JPanel spinnerPanel = new JPanel(new GridBagLayout());

        gbc.reset();
        gbc.anchor = gbc.WEST;
        spinnerPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_WIDTH)), gbc);

        gbc.reset();
        gbc.gridy = 1;
        spinnerPanel.add(customWidthSpinner, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.anchor = gbc.WEST;
        gbc.insets.left = 5;
        spinnerPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_HEIGHT)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.insets.left = 5;
        spinnerPanel.add(customHeightSpinner, gbc);


        // Add spinners and empty component to card layout - so we can hide/show
        this.spinnerPanel = new JPanel(new CardLayout());
        this.spinnerPanel.add("a", new Component() {});
        this.spinnerPanel.add("b", spinnerPanel);

        // Lay it out
        this.setLayout(new GridBagLayout());

        gbc.reset();
        gbc.anchor = gbc.WEST;
        gbc.gridwidth = 2;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = gbc.insets.bottom = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_MSG)), gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = gbc.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_THEME)), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1;
        gbc.gridheight = 2;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        this.add(skinsetListScroll, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = gbc.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        this.add(new JLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_SIZE)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        this.add(skinListScroll, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = gbc.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.insets.top = gbc.insets.right = gbc.insets.left = 5;
        this.add(descriptionTextScroll, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = gbc.NORTHEAST;
        gbc.weightx = 1;
        gbc.insets.top = gbc.insets.right = gbc.insets.left = 5;
        this.add(this.spinnerPanel, gbc);

        // Set initial skinset
        setSkinSet((SkinSet)skinsetList.getSelectedValue());
    }

    private JSpinner configureSpinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinnerFix(new SpinnerNumberModel(value, min, max, step));
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinner.getEditor();
        editor.getTextField().setColumns(5);
        // Get rid of commas in numbers
        editor.getFormat().applyPattern("#");

        return spinner;
    }

    private void setSkinSet(SkinSet skinset) {
        Component glass = null;
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null && window instanceof JDialog)
            glass = ((JDialog)window).getGlassPane();

        try {
            // Show busy cursor
            if (glass != null) {
                glass.setVisible(true);
                glass.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            // Display HTML description
            // Use read() instead of setText() to avoid side effects (e.g. previous stylesheet).
            // Set document base so embedded images can be loaded
            String description = skinset.getDescription();
            if (description != null) {
                File skinsetFile = skinset.getFile();
                if (skinsetFile != null) {
                    try {
                        // Set document base to directory, otherwise assume it is a ZIP
                        if (skinsetFile.isDirectory())
                            descriptionViewer.setDocumentBase(skinsetFile.toURL());
                        else
                            descriptionViewer.setDocumentBase(new URL("jar:" + skinsetFile.toURL().toExternalForm() + "!/"));
                    } catch (MalformedURLException e) {}
                }
                else
                    descriptionViewer.setDocumentBase(null);
                try {
                    descriptionViewer.read(new StringReader(description), null);
                } catch (IOException e) {}
            }
            else
                descriptionViewer.setText(null);

            List<Skin> skins = skinset.getSkins();
            // Add custom skin prompt to end of builtin set - a String not a Skin
            if (skinset == SkinManager.BUILTINSET) {
                // Copy the list before modifying
                ArrayList<Object> builtinSkins = new ArrayList<Object>(skins);
                builtinSkins.add(customSizeMessage);
                skinList.setModel(new ListListModel(builtinSkins));
            }
            else
                skinList.setModel(new ListListModel(skins));

            // JScrollPane sometimes doesn't layout scrollbars right when resetting model
            Container parent = skinList.getParent();
            if (parent != null)
                parent.validate();

            // Resetting selection may not fire selection event
            // if previous selection was the same index
            setSkin((Skin)skinList.getSelectedValue());
        } finally {
            // Hide busy cursor
            if (glass != null) {
                glass.setCursor(null);
                glass.setVisible(false);
            }
        }
    }

    private void setSkin(Skin skin) {
        this.skin = skin;
    }

    public Skin getSkin() {
        return skin;
    }

    private class SkinSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            Object value = ((JList)e.getSource()).getSelectedValue();
            if (value instanceof Skin) {
                isCustomSize = false;
                setSkin((Skin)value);
                // Hide size controls
                ((CardLayout)spinnerPanel.getLayout()).first(spinnerPanel);
            }
            // Selected String custom size entry
            else {
                isCustomSize = true;
                setSkin(null);
                // Show size controls
                ((CardLayout)spinnerPanel.getLayout()).last(spinnerPanel);
            }
        }
    }

    private class SkinSetSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;

            setSkinSet((SkinSet)((JList)e.getSource()).getSelectedValue());
        }
    }

    // Renders Skin list cells
    private class SkinCellRenderer extends DefaultListCellRenderer {
        private Color badAspectColor = UIManager.getColor("Label.disabledForeground");
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean selected, boolean hasFocus) {
            Component comp = super.getListCellRendererComponent(list, value, index, selected, hasFocus);
            // Draw the cell with different foreground if it is the wrong aspect ratio
            Color cellColor = selected ? list.getSelectionForeground() : list.getForeground();
            if (value instanceof Skin) {
                if (((Skin)value).getStageAspectRatio() != aspectRatio)
                    cellColor = badAspectColor;
            }

            comp.setForeground(cellColor);
            return comp;
        }
    }
}

// Enforce one and only one selection (no less, no more)
class SingleListSelectionModel extends DefaultListSelectionModel {
    public SingleListSelectionModel() {
        setSelectionMode(SINGLE_SELECTION);
        // Force initial selection
        setSelectionInterval(0, 0);
    }

    // Overrides DefaultListSelectionModel
    public void clearSelection() {
        // Don't allow selection to be cleared
        setSelectionInterval(0, 0);
    }

    // Overrides DefaultListSelectionModel
    public void removeIndexInterval(int index0, int index1) {
        // Don't allow selection to be removed
    }

    // Overrides DefaultListSelectionModel
    public void removeSelectionInterval(int index0, int index1) {
        // Don't allow selection to be removed
    }
}