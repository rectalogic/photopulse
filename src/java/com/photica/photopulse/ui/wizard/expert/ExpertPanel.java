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
import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.ShowList;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.effects.BeginTransition;
import com.photica.photopulse.model.effects.Effect;
import com.photica.photopulse.model.effects.EndTransition;
import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.ui.wizard.DurationSpinner;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.photopulse.ui.wizard.WizardUI;
import com.photica.photopulse.ui.wizard.ErrorDialog;
import com.photica.photopulse.ui.wizard.expert.panzoom.PanZoomEditorDialog;
import com.photica.photopulse.ui.wizard.viewer.PhotoViewer;
import com.photica.photopulse.ui.wizard.viewer.ShowPreviewer;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;
import com.photica.photopulse.wizard.WizardEffectBeginTransition;
import com.photica.photopulse.wizard.WizardEffectEffect;
import com.photica.photopulse.wizard.WizardEffectEndTransition;
import com.photica.ui.JComboBoxFix;
import com.photica.ui.JSpinnerFix;
import com.photica.ui.PopupEditor;
import com.photica.ui.TitledBorderFix;
import com.photica.ui.ToolButton;
import com.photica.ui.UIUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.io.IOException;


public class ExpertPanel extends JPanel {
    private WizardUI ui;
    private ShowList showList;
    private Skin skin;
    private Color backgroundColor;

    // Contains PhotoEffects to be cut/copy/pasted to the dragList
    private List<PhotoEffects> clipboard;

    private Action cutAction;
    private Action copyAction;
    private Action pasteAction;
    private Action deleteAction;

    private DragListTweak dragList;

    private JSplitPane splitPane;
    private JPanel effectEditorPanel;

    private JLabel photoNameLabel;

    private JCheckBox lockToggle;

    private PopupEditor eventArgEditor;

    private WizardEffectComboBox<WizardEffectBeginTransition> begintransCombo;
    private WizardEffectComboBox<WizardEffectEffect> effectCombo;
    private JButton customEffectButton;
    private WizardEffectComboBox<WizardEffectEndTransition> endtransCombo;

    private JComboBoxFix endtransLayerCombo;

    private DurationSpinner begintransDurationSpinner;
    private DurationSpinner effectDurationSpinner;
    private DurationSpinner endtransDurationSpinner;

    private JLabel mp3DurationLabel;
    private JLabel showDurationLabel;

    private ModelChangeHandler modelChangeHandler = new ModelChangeHandler();

    // If true, ignore ShowList model change events
    private boolean ignorePhotoListModelChanges = false;

    private static final Double ZERO = new Double(0);

    public ExpertPanel(WizardUI ui) {
        super(new BorderLayout());
        this.ui = ui;
        buildUI();
        setEditorPanelVisible(ui.isExpertMode());

        initActions();
        ui.addPropertyChangeListener(new WizardPropertyListener());
    }

    public Action getCutAction() {
        return cutAction;
    }

    public Action getCopyAction() {
        return copyAction;
    }

    public Action getPasteAction() {
        return pasteAction;
    }

    public Action getDeleteAction() {
        return deleteAction;
    }

    private void initActions() {
        cutAction = new CutPhotoAction();
        copyAction = new CopyPhotoAction();
        pasteAction = new PastePhotoAction();
        deleteAction = new DeletePhotoAction();

        // Initially no selection and empty clipboard
        updateEditActions(false);
    }

    private void buildUI() {
        ViewPhotoHandler viewHandler = new ViewPhotoHandler();
        // Main thumbnail list
        dragList = new DragListTweak(new ResourceIcon("resources/logo.png"), new ShowList());
        dragList.addListSelectionListener(new PhotoListSelectionHandler());
        dragList.addMouseListener(viewHandler);
        PhotoListCellRenderer cellRenderer = new PhotoListCellRenderer();
        dragList.setCellRenderer(cellRenderer);
        // Use special value for prototype
        dragList.setPrototypeCellValue(PhotoListCellRenderer.EMPTY_ICON);
        dragList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        // visibleRowCount<0 means wrap columns at viewport width
        dragList.setVisibleRowCount(-1);
        //XXX splitpane returns it's preferred size as sum of expert panel and draglist - even when pane is hidden
        dragList.setVisibleGrid(3, 3);

        JScrollPane scrollList = new JScrollPane(dragList);
        scrollList.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Effect/trans editor pane. Everything in this is recursively enabled/disabled as needed.
        effectEditorPanel = buildEditorPanel(viewHandler);

        // Panel to display durations and hold effect editor
        JPanel durationPanel = buildDurationPanel(effectEditorPanel);

        // Splitter with list and duration/effect editor
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                scrollList, new JScrollPane(durationPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(1.0);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel buildEditorPanel(ViewPhotoHandler viewHandler) {
        WizardGBC gbc = new WizardGBC();
        JPanel effectEditorPanel = new JPanel(new GridBagLayout());

        // Display photo name of current photo selection
        photoNameLabel = new JLabel(" ", SwingConstants.TRAILING);
        // Force small preferred width so label does not resize the layout when long
        photoNameLabel.setPreferredSize(photoNameLabel.getPreferredSize());

        // Display photo viewer when clicked
        JButton viewerButton = new ToolButton(new ResourceIcon("resources/zoom.gif"));
        viewerButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_TOOLTIP_VIEWPHOTO));
        viewerButton.addActionListener(viewHandler);

        // Display previewer when clicked
        JButton previewerButton = new ToolButton(new ResourceIcon("resources/binoculars.gif"));
        previewerButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_TOOLTIP_PREVIEW));
        previewerButton.addActionListener(new PreviewHandler(false));

        // Display skin previewer when clicked
        JButton skinPreviewerButton = new ToolButton(new ResourceIcon("resources/binoculars-star.gif"));
        skinPreviewerButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_TOOLTIP_FULL_PREVIEW));
        skinPreviewerButton.addActionListener(new PreviewHandler(true));

        // Toggle locked state of photos effect/trans
        lockToggle = new JCheckBox(UIMessages.getMessage(UIMessages.UI_LOCK_TOGGLE));
        lockToggle.setToolTipText(UIMessages.getMessage(UIMessages.UI_LOCK_TOOLTIP));
        lockToggle.addActionListener(new EditorChangeHandler(EditorChangeHandler.CHANGE_LOCK));

        // Popup text editor for eventArg for Skin (e.g. photo title)
        eventArgEditor = new PopupEditor(UIMessages.getMessage(UIMessages.UI_POPUP_PROMPT), 5, 15);
        eventArgEditor.addActionListener(new EditorChangeHandler(EditorChangeHandler.CHANGE_EVENTARG));
        JPanel eventArgPanel = new JPanel(new BorderLayout(0, 2));
        eventArgPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_PHOTOCAPTION)), BorderLayout.NORTH);
        eventArgPanel.add(eventArgEditor, BorderLayout.CENTER);

        // Begin transitions
        begintransCombo = new WizardEffectComboBox<WizardEffectBeginTransition>(Wizard.LIST_BEGINTRANSITIONS, Wizard.WBEGINTRANS_None,
                new EditorChangeHandler(EditorChangeHandler.CHANGE_BEGINTRANS));
        begintransDurationSpinner = buildDurationSpinner(new EditorChangeHandler(EditorChangeHandler.CHANGE_BEGINTRANS_DURATION));

        JPanel begintransPanel = new JPanel(new GridBagLayout());
        begintransPanel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_TAB_BEGINTRANS)));
        gbc.reset();
        gbc.weightx = 1;
        gbc.insets.bottom = 5;
        gbc.fill = WizardGBC.HORIZONTAL;
        begintransPanel.add(begintransCombo, gbc);
        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        begintransPanel.add(begintransDurationSpinner, gbc);

        // Effects
        effectCombo = new WizardEffectComboBox<WizardEffectEffect>(Wizard.LIST_EFFECTS, Wizard.WEFFECT_None,
                new EditorChangeHandler(EditorChangeHandler.CHANGE_EFFECT));
        effectCombo.setCustomLabel(UIMessages.getMessage(UIMessages.UI_CUSTOMEFFECT_LABEL));
        effectDurationSpinner = buildDurationSpinner(new EditorChangeHandler(EditorChangeHandler.CHANGE_EFFECT_DURATION));
        customEffectButton = new ToolButton(UIMessages.getMessage(UIMessages.UI_CUSTOMEFFECT_BUTTON));
        customEffectButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_CUSTOMEFFECT_TOOLTIP));
        customEffectButton.addActionListener(new CustomEffectHandler());

        JPanel effectPanel = new JPanel(new GridBagLayout());
        effectPanel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_TAB_EFFECTS)));
        gbc.reset();
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 5;
        gbc.fill = WizardGBC.HORIZONTAL;
        effectPanel.add(effectCombo, gbc);
        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.right = 2;
        effectPanel.add(effectDurationSpinner, gbc);
        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        effectPanel.add(customEffectButton, gbc);

        // End transitions and end layer
        endtransCombo = new WizardEffectComboBox<WizardEffectEndTransition>(Wizard.LIST_ENDTRANSITIONS, Wizard.WENDTRANS_None,
                new EditorChangeHandler(EditorChangeHandler.CHANGE_ENDTRANS));
        endtransLayerCombo = buildEndtransLayerCombo();
        endtransDurationSpinner = buildDurationSpinner(new EditorChangeHandler(EditorChangeHandler.CHANGE_ENDTRANS_DURATION));

        JPanel endtransPanel = new JPanel(new GridBagLayout());
        endtransPanel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_TAB_ENDTRANS)));
        gbc.reset();
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.insets.bottom = 5;
        gbc.fill = WizardGBC.HORIZONTAL;
        endtransPanel.add(endtransCombo, gbc);
        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.right = 2;
        endtransPanel.add(endtransDurationSpinner, gbc);
        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        endtransPanel.add(endtransLayerCombo, gbc);


        // Main layout

        gbc.reset();
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.gridwidth = 4;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.top = 5;
        gbc.insets.left = gbc.insets.right = 5;
        effectEditorPanel.add(photoNameLabel, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.top = 5;
        gbc.insets.left = 5;
        gbc.insets.right = 2;
        effectEditorPanel.add(lockToggle, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.anchor = WizardGBC.EAST;
        gbc.insets.top = 5;
        gbc.insets.right = 2;
        effectEditorPanel.add(viewerButton, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        gbc.insets.top = 5;
        gbc.insets.right = 2;
        effectEditorPanel.add(previewerButton, gbc);

        gbc.reset();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        gbc.insets.top = 5;
        gbc.insets.right = 5;
        effectEditorPanel.add(skinPreviewerButton, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.gridwidth = 4;
        gbc.insets.top = 5;
        gbc.insets.left = gbc.insets.right = 5;
        effectEditorPanel.add(eventArgPanel, gbc);

        gbc.reset();
        gbc.insets.top = 5;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.gridwidth = 4;
        gbc.insets.left = gbc.insets.right = 5;
        effectEditorPanel.add(begintransPanel, gbc);

        gbc.reset();
        gbc.insets.top = 5;
        gbc.gridy = 4;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.gridwidth = 4;
        gbc.insets.left = gbc.insets.right = 5;
        effectEditorPanel.add(effectPanel, gbc);

        gbc.reset();
        gbc.insets.top = 5;
        gbc.gridy = 5;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.anchor = WizardGBC.NORTH;
        gbc.insets.left = gbc.insets.right = 5;
        gbc.insets.bottom = 5;
        gbc.gridwidth = 4;
        effectEditorPanel.add(endtransPanel, gbc);

        return effectEditorPanel;
    }

    private JPanel buildDurationPanel(JPanel effectEditorPanel) {
        JPanel durationPanel = new JPanel(new GridBagLayout());

        mp3DurationLabel = new JLabel(" ");
        mp3DurationLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        showDurationLabel = new JLabel(" ");
        showDurationLabel.setHorizontalAlignment(SwingConstants.TRAILING);

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.fill = WizardGBC.BOTH;
        durationPanel.add(effectEditorPanel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.insets.top = 5;
        durationPanel.add(new JSeparator(), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.top = gbc.insets.left = 5;
        durationPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_SHOW_DURATION)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.top = gbc.insets.right = 5;
        durationPanel.add(showDurationLabel, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.anchor = WizardGBC.WEST;
        gbc.insets.top = 2;
        gbc.insets.left = gbc.insets.bottom = 5;
        durationPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_MP3_DURATION)), gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.top = 2;
        gbc.insets.right = gbc.insets.bottom = 5;
        durationPanel.add(mp3DurationLabel, gbc);

        return durationPanel;
    }

    private DurationSpinner buildDurationSpinner(ChangeListener listener) {
        DurationSpinner spinner = new DurationSpinner();
        spinner.addChangeListener(listener);
        return spinner;
    }

    private JComboBoxFix buildEndtransLayerCombo() {
        JComboBoxFix combo = new JComboBoxFix(new Object[] {
            new EndTransitionLayer(new ResourceIcon("resources/up.gif"), UIMessages.getMessage(UIMessages.UI_ENDTRANS_LAYER_TOP), true),
            new EndTransitionLayer(new ResourceIcon("resources/down.gif"), UIMessages.getMessage(UIMessages.UI_ENDTRANS_LAYER_BOTTOM), false)
        });
        combo.setRenderer(new EndTransitionLayerComboRenderer(combo));
        combo.addActionListener(new EditorChangeHandler(EditorChangeHandler.CHANGE_ENDTRANS_LAYER));

        return combo;
    }

    private void setEditorPanelVisible(boolean visible) {
        // resetToPreferredSizes does not work if the expert panel has a vertical scrollbar
        if (visible) {
            splitPane.setDividerLocation(splitPane.getSize().width - splitPane.getRightComponent().getPreferredSize().width - splitPane.getDividerSize());
        }
        // setDividerLocation(1.0) only works once the component is layed out, so just force to a large value
        else {
            splitPane.setDividerLocation(Integer.MAX_VALUE);
        }

        splitPane.setOneTouchExpandable(visible);
        splitPane.setEnabled(visible);
    }

    /**
     * Update PhotoEffects for the selected photos using the changed selections in editor panel
     * @param changeID CHANGE_* constant identifying the selections that changed
     */
    private void updateSelectedPhotoEffects(int changeID) {
        int minSelection = dragList.getMinSelectionIndex();
        // Return if no selection
        if (minSelection == -1)
            return;
        int maxSelection = dragList.getMaxSelectionIndex();

        Boolean lockState = (changeID & EditorChangeHandler.CHANGE_LOCK) != 0 ? Boolean.valueOf(lockToggle.isSelected()) : null;

        String eventArg = (changeID & EditorChangeHandler.CHANGE_EVENTARG) != 0 ? eventArgEditor.getText() : null;
        if (eventArg != null && eventArg.length() == 0)
            eventArg = null;

        WizardEffectBeginTransition begintransWE = getComboSelection(changeID, EditorChangeHandler.CHANGE_BEGINTRANS, begintransCombo);
        WizardEffectEffect effectWE = getComboSelection(changeID, EditorChangeHandler.CHANGE_EFFECT, effectCombo);
        WizardEffectEndTransition endtransWE = getComboSelection(changeID, EditorChangeHandler.CHANGE_ENDTRANS, endtransCombo);

        Boolean endtransLayer = (changeID & EditorChangeHandler.CHANGE_ENDTRANS_LAYER) != 0
                ? Boolean.valueOf(((EndTransitionLayer)endtransLayerCombo.getSelectedItem()).isEndTransitionTopLayer())
                : null;

        double begintransDuration = (changeID & EditorChangeHandler.CHANGE_BEGINTRANS_DURATION) != 0 ? begintransDurationSpinner.getDuration() : -1;
        double effectDuration = (changeID & EditorChangeHandler.CHANGE_EFFECT_DURATION) != 0 ? effectDurationSpinner.getDuration() : -1;
        double endtransDuration = (changeID & EditorChangeHandler.CHANGE_ENDTRANS_DURATION) != 0 ? endtransDurationSpinner.getDuration() : -1;

        try {
            // Ignore model changes as we repopulate the model
            ignorePhotoListModelChanges = true;

            // Apply changed setting to all photos in selection
            for (int i = minSelection; i <= maxSelection; i++) {
                if (!dragList.isSelectedIndex(i))
                    continue;
                PhotoEffects effects = showList.get(i);

                boolean isLocked = lockState != null ? lockState.booleanValue() : effects.isLocked();

                // Do not change transitions, effect, layer or photoScale if locked
                effects = new PhotoEffects(effects.getPhoto(),
                        (changeID & EditorChangeHandler.CHANGE_EVENTARG) != 0 ? eventArg : effects.getEventArg(),
                        (isLocked || effectWE == null) ? effects.getPhotoScale() : effectWE.getPhotoScale(),
                        (isLocked || begintransWE == null) ? effects.getBeginTransition() : begintransWE.getEffect(),
                        begintransDuration == -1 ? effects.getBeginTransitionDuration() : begintransDuration,
                        (isLocked || effectWE == null) ? effects.getEffect() : effectWE.getEffect(),
                        effectDuration == -1 ? effects.getEffectDuration() : effectDuration,
                        (isLocked || endtransWE == null) ? effects.getEndTransition() : endtransWE.getEffect(),
                        endtransDuration == -1 ? effects.getEndTransitionDuration() : endtransDuration,
                        (isLocked || endtransLayer == null) ? effects.isEndTransitionTopLayer() : endtransLayer.booleanValue(),
                        isLocked);

                showList.set(i, effects);
            }
        } finally {
            ignorePhotoListModelChanges = false;

            // Now resync to the changed model
            handleShowListDataChange();
        }
    }

    private <WE extends WizardEffect> WE getComboSelection(int changeID, int mask, WizardEffectComboBox<WE> combo) {
        if ((changeID & mask) != 0)
            return (WE)combo.getSelectedItem();
        return null;
    }

    /**
     * Set effect combo selection. Force into custom mode if this is a custom panzoom.
     */
    private void setEffectComboSelection(WizardEffectComboBox<WizardEffectEffect> combo, Effect effect) {
        WizardEffect we = effect != null ? Wizard.findWizardEffect(effect) : null;
        combo.setCustomEffect(we == null && (effect instanceof PanZoomEffect));
        combo.setSelectedItemQuiet(we);
    }

    /**
     * Sync the editor panel contents to the DragList selection.
     * Enable/disable panel components as needed.
     */
    private void updateEditorPanel() {
        // Display values from single selection. Enable panel.
        if (dragList.isSelectionSingle()) {
            PhotoEffects effects = (PhotoEffects)dragList.getSelectedValue();

            photoNameLabel.setText(effects.getPhoto().getFile().getName());

            lockToggle.setSelected(effects.isLocked());

            eventArgEditor.setText(effects.getEventArg());

            begintransCombo.setSelectedItemQuiet(Wizard.findWizardEffect(effects.getBeginTransition()));
            setEffectComboSelection(effectCombo, effects.getEffect());
            endtransCombo.setSelectedItemQuiet(Wizard.findWizardEffect(effects.getEndTransition()));

            updateEndtransLayerCombo(Boolean.valueOf(effects.isEndTransitionTopLayer()));

            begintransDurationSpinner.setDuration(effects.getBeginTransitionDuration());
            begintransDurationSpinner.setDisplayEmpty(false);
            effectDurationSpinner.setDuration(effects.getEffectDuration());
            effectDurationSpinner.setDisplayEmpty(false);
            endtransDurationSpinner.setDuration(effects.getEndTransitionDuration());
            endtransDurationSpinner.setDisplayEmpty(false);

            UIUtilities.enableComponentTree(effectEditorPanel, true);
            enableLocked(effects.isLocked());
        }
        // Display "indeterminate" state unless all selected photos have the same value. Enable panel.
        else if (dragList.isSelectionMultiple()) {
            photoNameLabel.setText(UIMessages.getMessage(UIMessages.UI_LABEL_MULTIPLE));

            int minSelection = dragList.getMinSelectionIndex();
            int maxSelection = dragList.getMaxSelectionIndex();

            PhotoEffects initialEffects = showList.get(minSelection);

            // Get settings for first photo in selection
            String eventArgInitial = initialEffects.getEventArg();
            boolean eventArgIndeterminate = false;
            BeginTransition begintransInitial = initialEffects.getBeginTransition();
            boolean begintransIndeterminate = false;
            Effect effectInitial = initialEffects.getEffect();
            boolean effectIndeterminate = false;
            EndTransition endtransInitial = initialEffects.getEndTransition();
            boolean endtransIndeterminate = false;
            boolean endtransLayerInitial = initialEffects.isEndTransitionTopLayer();
            boolean endtransLayerIndeterminate = false;
            double begintransDurationInitial = initialEffects.getBeginTransitionDuration();
            boolean begintransDurationIndeterminate = false;
            double effectDurationInitial = initialEffects.getEffectDuration();
            boolean effectDurationIndeterminate = false;
            double endtransDurationInitial = initialEffects.getEndTransitionDuration();
            boolean endtransDurationIndeterminate = false;

            boolean isLockedInitial = initialEffects.isLocked();
            boolean isLockedIndeterminate = false;

            // Compare initial settings to other photos in selection.
            // If any are different, then that setting is indeterminate.
            // XXX This can result in IndexOutOfBoundsException due to JDK 1.4.2 bug - list selection is mismanaged when deleting from model
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4623505
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4905083
            for (int i = minSelection + 1; i <= maxSelection; i++) {
                if (!dragList.isSelectedIndex(i))
                    continue;
                PhotoEffects effects = showList.get(i);

                String eventArg = effects.getEventArg();
                if (!eventArgIndeterminate && (eventArgInitial != eventArg || (eventArgInitial != null && !eventArgInitial.equals(eventArg))))
                    eventArgIndeterminate = true;

                if (!begintransIndeterminate && !begintransInitial.equals(effects.getBeginTransition()))
                    begintransIndeterminate = true;
                if (!effectIndeterminate && !effectInitial.equals(effects.getEffect()))
                    effectIndeterminate = true;
                if (!endtransIndeterminate && !endtransInitial.equals(effects.getEndTransition()))
                    endtransIndeterminate = true;
                if (!endtransLayerIndeterminate && endtransLayerInitial != effects.isEndTransitionTopLayer())
                    endtransLayerIndeterminate = true;
                if (!begintransDurationIndeterminate && begintransDurationInitial != effects.getBeginTransitionDuration())
                    begintransDurationIndeterminate = true;
                if (!effectDurationIndeterminate && effectDurationInitial != effects.getEffectDuration())
                    effectDurationIndeterminate = true;
                if (!endtransDurationIndeterminate && endtransDurationInitial != effects.getEndTransitionDuration())
                    endtransDurationIndeterminate = true;

                if (!isLockedIndeterminate && isLockedInitial != effects.isLocked())
                    isLockedIndeterminate = true;

                // Stop looking if all settings are already different
                if (isLockedIndeterminate && eventArgIndeterminate && begintransIndeterminate && effectIndeterminate
                        && endtransIndeterminate && endtransLayerIndeterminate
                        && begintransDurationIndeterminate && effectDurationIndeterminate && endtransDurationIndeterminate)
                    break;
            }

            // Set controls to indeterminate or common value

            eventArgEditor.setText(eventArgIndeterminate ? null : eventArgInitial);

            begintransCombo.setSelectedItemQuiet(begintransIndeterminate ? null : Wizard.findWizardEffect(begintransInitial));
            setEffectComboSelection(effectCombo, effectIndeterminate ? null : effectInitial);
            endtransCombo.setSelectedItemQuiet(endtransIndeterminate ? null : Wizard.findWizardEffect(endtransInitial));

            updateEndtransLayerCombo(endtransLayerIndeterminate ? null : Boolean.valueOf(endtransLayerInitial));

            Double defaultTransitionDuration = (begintransDurationIndeterminate || endtransDurationIndeterminate) ? new Double(ui.getShowModel().getDefaultTransitionDuration()) : null;
            begintransDurationSpinner.setValueQuiet(begintransDurationIndeterminate ? defaultTransitionDuration : new Double(begintransDurationInitial));
            begintransDurationSpinner.setDisplayEmpty(begintransDurationIndeterminate);
            effectDurationSpinner.setValueQuiet(effectDurationIndeterminate ? new Double(ui.getShowModel().getDefaultEffectDuration()) : new Double(effectDurationInitial));
            effectDurationSpinner.setDisplayEmpty(effectDurationIndeterminate);
            endtransDurationSpinner.setValueQuiet(endtransDurationIndeterminate ? defaultTransitionDuration : new Double(endtransDurationInitial));
            endtransDurationSpinner.setDisplayEmpty(endtransDurationIndeterminate);

            UIUtilities.enableComponentTree(effectEditorPanel, true);
            // If any are locked, disable effect/trans combos
            enableLocked(isLockedInitial || isLockedIndeterminate);

            // If all locked or all unlocked, set toggle.
            // If mixture, then deselect and disable toggle
            lockToggle.setSelected(isLockedIndeterminate ? false : isLockedInitial);
            lockToggle.setEnabled(!isLockedIndeterminate);

            // Disable custom effect button, it can only operate on a single selection
            customEffectButton.setEnabled(false);
        }
        // Deselect everything and disable
        else if (dragList.isSelectionEmpty()) {
            photoNameLabel.setText(UIMessages.getMessage(UIMessages.UI_LABEL_NONE));
            lockToggle.setSelected(false);
            eventArgEditor.setText(null);
            begintransCombo.setSelectedItem(null);
            setEffectComboSelection(effectCombo, null);
            endtransCombo.setSelectedItem(null);
            endtransLayerCombo.setSelectedItem(null);

            begintransDurationSpinner.setValueQuiet(ZERO);
            begintransDurationSpinner.setDisplayEmpty(true);
            effectDurationSpinner.setValueQuiet(ZERO);
            effectDurationSpinner.setDisplayEmpty(true);
            endtransDurationSpinner.setValueQuiet(ZERO);
            endtransDurationSpinner.setDisplayEmpty(true);

            UIUtilities.enableComponentTree(effectEditorPanel, false);
        }
    }

    /**
     * Enable/disable effect/transition and duration spinner controls for locked photo.
     */
    private void enableLocked(boolean isLocked) {
        begintransCombo.setEnabled(!isLocked);
        begintransDurationSpinner.setEnabled(!isLocked);
        effectCombo.setEnabled(!isLocked);
        effectDurationSpinner.setEnabled(!isLocked);
        customEffectButton.setEnabled(!isLocked);
        endtransCombo.setEnabled(!isLocked);
        endtransDurationSpinner.setEnabled(!isLocked);
        endtransLayerCombo.setEnabled(!isLocked);
    }

    private void updateEndtransLayerCombo(Boolean isTopLayer) {
        if (isTopLayer == null) {
            endtransLayerCombo.setSelectedIndexQuiet(-1);
            return;
        }

        if (isTopLayer.booleanValue() == ((EndTransitionLayer)endtransLayerCombo.getItemAt(0)).isEndTransitionTopLayer())
            endtransLayerCombo.setSelectedIndexQuiet(0);
        else
            endtransLayerCombo.setSelectedIndexQuiet(1);
    }

    /**
     * Get model show list.
     * Update effect combos with selections.
     */
    private void updateShowModel() {
        ShowModel model = ui.getShowModel();
        model.addPropertyChangeListener(modelChangeHandler);
        showList = model.getPhotoEffectList();
        showList.addListDataListener(modelChangeHandler);
        dragList.setModel(showList);

        skin = model.getSkin();
        backgroundColor = model.getBackgroundColor();

        // Only enable photo tooltips in expert mode
        dragList.setToolTipEnabled(ui.isExpertMode());

        begintransCombo.setShowSelectionModel(model.getBeginTransitionSelectionModel());
        effectCombo.setShowSelectionModel(model.getEffectSelectionModel());
        endtransCombo.setShowSelectionModel(model.getEndTransitionSelectionModel());

        handleShowListDataChange();
    }

    /**
     * Handle change in ShowList data
     */
    private void handleShowListDataChange() {
        updateShowDurationLabel();
        updateEditorPanel();
    }

    private void updateShowDurationLabel() {
        showDurationLabel.setText(Util.formatDuration(ui.getShowModel().computeShowDuration()));
    }

    private void updateMP3DurationLabel() {
        MP3 mp3 = ui.getShowModel().getMP3();
        if (mp3 == null)
            mp3DurationLabel.setText(" ");
        else
            mp3DurationLabel.setText(Util.formatDuration(mp3.getDuration()));
    }

    /**
     * Delete selected photos from dragList
     */
    private void deleteSelectedPhotos() {
        // Delete in reverse order
        int minSelection = dragList.getMinSelectionIndex();
        int maxSelection = dragList.getMaxSelectionIndex();
        for (int i = maxSelection; i >= minSelection; i--) {
            if (!dragList.isSelectedIndex(i))
                continue;
            showList.remove(i);
        }
    }

    private void displayPanZoomEditor() {
        if (dragList.isSelectionSingle()) {
            PhotoEffects effects = null;
            try {
                int selectedIndex = dragList.getSelectedIndex();
                effects = PanZoomEditorDialog.showDialog(customEffectButton, showList.get(selectedIndex), skin, backgroundColor);
                if (effects != null)
                    showList.set(selectedIndex, effects);
            } catch (IOException e) {
                ErrorDialog.showErrorDialog(customEffectButton,
                        UIMessages.getMessage(UIMessages.ERR_LOAD_PHOTO,
                                effects != null ? effects.getPhoto().getFile().getName() : "", e.getMessage()));
            }
        }
    }

    /**
     * Set enabled state of edit actions
     * @param enable If false, disable. If true, enable if possible.
     */
    public void updateEditActions(boolean enable) {
        if (!enable) {
            cutAction.setEnabled(false);
            copyAction.setEnabled(false);
            pasteAction.setEnabled(false);
            deleteAction.setEnabled(false);
        }
        else {
            pasteAction.setEnabled(clipboard != null && clipboard.size() > 0);
            boolean isSelection = !dragList.isSelectionEmpty();
            cutAction.setEnabled(isSelection);
            copyAction.setEnabled(isSelection);
            deleteAction.setEnabled(isSelection);
        }
    }

    /**
     * Select or deselect all photos
     */
    public void selectAllPhotos(boolean select) {
        if (select)
            dragList.setSelectionInterval(0, dragList.getModel().getSize() - 1);
        else
            dragList.clearSelection();
    }

    /**
     * Handle WizardUI property changes
     */
    private class WizardPropertyListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();

            if (WizardUI.SHOWMODEL_PROPERTY.equals(prop)) {
                updateShowModel();
                if (ui.isExpertMode())
                    updateMP3DurationLabel();
            }
            // Update when expert mode changes
            else if (WizardUI.EXPERT_MODE_PROPERTY.equals(prop)) {
                boolean isExpertMode = ((Boolean)e.getNewValue()).booleanValue();

                // Show or hide editor panel
                setEditorPanelVisible(isExpertMode);
                if (isExpertMode) {
                    // Display current selection settings
                    updateEditorPanel();
                    // Display MP3 duration
                    updateMP3DurationLabel();
                }
            }
        }
    }

    /**
     * Handle custom effect button
     */
    private class CustomEffectHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            displayPanZoomEditor();
        }
    }

    /**
     * Rebuild the selected PhotoEffects using the current UI settings.
     */
    private class EditorChangeHandler implements ActionListener, ChangeListener {

        public static final int CHANGE_BEGINTRANS = 1;
        public static final int CHANGE_EFFECT = 1<<1;
        public static final int CHANGE_ENDTRANS = 1<<2;
        public static final int CHANGE_ENDTRANS_LAYER = 1<<3;
        public static final int CHANGE_BEGINTRANS_DURATION = 1<<4;
        public static final int CHANGE_EFFECT_DURATION = 1<<5;
        public static final int CHANGE_ENDTRANS_DURATION = 1<<6;
        public static final int CHANGE_EVENTARG = 1<<7;
        public static final int CHANGE_LOCK = 1<<8;
        public static final int CHANGE_ALL = CHANGE_BEGINTRANS | CHANGE_EFFECT | CHANGE_ENDTRANS
                | CHANGE_ENDTRANS_LAYER | CHANGE_BEGINTRANS_DURATION | CHANGE_EFFECT_DURATION
                | CHANGE_ENDTRANS_DURATION | CHANGE_EVENTARG | CHANGE_LOCK;

        private int changeID;

        /**
         * Specify CHANGE_* constant to identify what changed
         */
        public EditorChangeHandler(int changeID) {
            this.changeID = changeID;
        }

        public void stateChanged(ChangeEvent e) {
            // If we are programmatically setting the value, ignore listener notifications
            if (((JSpinnerFix)e.getSource()).isSetValueQuiet())
                return;
            updateSelectedPhotoEffects(changeID);
        }

        public void actionPerformed(ActionEvent e) {
            updateSelectedPhotoEffects(changeID);
        }
    }

    /**
     * Handles changes in selection of photos in DragList.
     */
    private class PhotoListSelectionHandler implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;

            updateEditActions(true);

            if (ui.isExpertMode())
                updateEditorPanel();
        }
    }

    /**
     * Handle changes in the DragList model.
     * We need to update the editor panel, in case a currently selected cell contents changes.
     * At the time this listener is invoked, the ListUIs listener may not have been invoked yet,
     * and so it may not have synced the list selection with the new list data.
     * So we have to invoke later before doing anything.
     * http://developer.java.sun.com/developer/bugParade/bugs/4758217.html
     */
    private class ModelChangeHandler implements ListDataListener, PropertyChangeListener, Runnable {
        public void intervalAdded(ListDataEvent e) {
            if (!ignorePhotoListModelChanges && ui.isExpertMode())
                SwingUtilities.invokeLater(this);
        }
        public void intervalRemoved(ListDataEvent e) {
            if (!ignorePhotoListModelChanges && ui.isExpertMode())
                SwingUtilities.invokeLater(this);
        }
        public void contentsChanged(ListDataEvent e) {
            if (!ignorePhotoListModelChanges && ui.isExpertMode())
                SwingUtilities.invokeLater(this);
        }
        public void run() {
            handleShowListDataChange();
        }
        /**
         * Update MP3 duration when model MP3 changes.
         * Save new skin and color for previewer.
         */
        public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if (ShowModel.MP3_FILE_PROPERTY.equals(property)) {
                if (ui.isExpertMode())
                    updateMP3DurationLabel();
            }
            else if (ShowModel.SKIN_PROPERTY.equals(property))
                skin = (Skin)e.getNewValue();
            else if (ShowModel.BACKGROUND_COLOR_PROPERTY.equals(property))
                backgroundColor = (Color)e.getNewValue();
        }
    }

    /**
     * Display photo viewer when a photo is double clicked in DragList or when label button is pressed
     */
    private class ViewPhotoHandler extends MouseAdapter implements ActionListener {
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() != 2)
                return;
            int index = dragList.locationToIndex(e.getPoint());
            if (index == -1)
                return;
            Rectangle cellBounds = dragList.getCellBounds(index, index);
            if (!cellBounds.contains(e.getPoint()))
                return;
            PhotoViewer.showDialog(dragList, index);
        }

        public void actionPerformed(ActionEvent e) {
            // Use first selected photo
            int index = dragList.getSelectedIndex();
            if (index == -1)
                return;
            PhotoViewer.showDialog(dragList, index);
        }
    }

    /**
     * Display show preview when clicked.
     * Handles preview with skin, or "draft" preview using thumbnails without skin.
     */
    private class PreviewHandler implements ActionListener {
        private boolean useSkinPreview;
        public PreviewHandler(boolean useSkinPreview) {
            this.useSkinPreview = useSkinPreview;
        }
        public void actionPerformed(ActionEvent e) {
            // Use first selected photo
            int index = dragList.getSelectedIndex();
            if (index == -1)
                return;
            ListModel model = dragList.getModel();
            PhotoEffects effects1 = (PhotoEffects)model.getElementAt(index);
            PhotoEffects effects2 = null;
            if (index + 1 < model.getSize())
                effects2 = (PhotoEffects)model.getElementAt(index + 1);
            ShowPreviewer.showDialog(dragList, useSkinPreview ? skin : null, backgroundColor, effects1, effects2);
        }
    }

    private abstract class DragListAction extends AbstractAction {
        public DragListAction(String label, Icon icon, String mnemonicKey, String accelKey) {
            super(label, icon);
            KeyStroke accel = (KeyStroke)UIMessages.getResource(accelKey);
            putValue(Action.MNEMONIC_KEY, UIMessages.getResource(mnemonicKey));
            putValue(Action.ACCELERATOR_KEY, accel);

            // Override keybinding from DragList InputMap - otherwise it eats Ctrl-C/Ctrl-V etc.
            dragList.getInputMap().put(accel, this);
        }
    }

    private class CutPhotoAction extends DragListAction {
        public CutPhotoAction() {
            super(UIMessages.getMessage(UIMessages.UI_LABEL_CUT), new ResourceIcon("resources/cut.gif"),
                    UIMessages.I_UI_MN_CUT, UIMessages.I_UI_ACCEL_CUT);
        }

        public void actionPerformed(ActionEvent e) {
            if (dragList.isSelectionEmpty())
                return;

            // Save selected PhotoEffects
            clipboard = dragList.getSelectedElements();

            // Now delete them
            deleteSelectedPhotos();

            // Now we have something to paste
            updateEditActions(true);
        }
    }

    private class CopyPhotoAction extends DragListAction {
        public CopyPhotoAction() {
            super(UIMessages.getMessage(UIMessages.UI_LABEL_COPY), new ResourceIcon("resources/copy.gif"),
                    UIMessages.I_UI_MN_COPY, UIMessages.I_UI_ACCEL_COPY);
        }

        public void actionPerformed(ActionEvent e) {
            if (dragList.isSelectionEmpty())
                return;

            // Save selected PhotoEffects
            clipboard = dragList.getSelectedElements();

            // Now we have something to paste
            updateEditActions(true);
        }
    }

    private class PastePhotoAction extends DragListAction {
        public PastePhotoAction() {
            super(UIMessages.getMessage(UIMessages.UI_LABEL_PASTE), new ResourceIcon("resources/paste.gif"),
                    UIMessages.I_UI_MN_PASTE, UIMessages.I_UI_ACCEL_PASTE);
        }

        public void actionPerformed(ActionEvent e) {
            if (clipboard == null || clipboard.size() == 0)
                return;

            int index = -1;

            // Paste clipboard before selection if single selection
            if (dragList.isSelectionSingle()) {
                index = dragList.getSelectedIndex();
                showList.addAll(index, clipboard);
            }
            // Append to end of list if no selection or multiple selection
            else {
                index = showList.size();
                showList.addAll(clipboard);
            }

            // Select pasted cells
            dragList.setSelectionInterval(index, index + clipboard.size() - 1);
        }
    }

    private class DeletePhotoAction extends DragListAction {
        public DeletePhotoAction() {
            super(UIMessages.getMessage(UIMessages.UI_LABEL_DELETE), new ResourceIcon("resources/clear.gif"),
                    UIMessages.I_UI_MN_DELETE, UIMessages.I_UI_ACCEL_DELETE);
        }

        public void actionPerformed(ActionEvent e) {
            deleteSelectedPhotos();
        }
    }

    /**
     * Catch mouse pressed and pop down eventArg editor before the list has a chance to change the selection.
     */
    private class DragListTweak extends LogoDragList {
        public DragListTweak(Icon logoIcon, ShowList showList) {
            super(logoIcon, showList);
        }

        protected void processMouseEvent(MouseEvent event) {
            // Commit eventArg editor and spinners. Do this now on the click, before the list selection changes.
            // Waiting for the focusLost event to commit is too late, the list selection has changed.
            if (event.getID() == MouseEvent.MOUSE_PRESSED) {
                eventArgEditor.commitEdit();
                begintransDurationSpinner.commitEditIfChanged();
                effectDurationSpinner.commitEditIfChanged();
                endtransDurationSpinner.commitEditIfChanged();
            }

            super.processMouseEvent(event);
        }
    }
}
