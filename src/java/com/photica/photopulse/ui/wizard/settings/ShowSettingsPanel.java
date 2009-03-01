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
 
package com.photica.photopulse.ui.wizard.settings;

import com.photica.photopulse.model.MP3;
import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.skin.SkinManager;
import com.photica.photopulse.skin.SkinPair;
import com.photica.photopulse.skin.SkinSet;
import com.photica.photopulse.ui.wizard.DurationSpinner;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.SkinChooser;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.photopulse.ui.wizard.WizardUI;
import com.photica.photopulse.wizard.Wizard;
import com.photica.ui.TitledBorderFix;
import com.photica.ui.UIUtilities;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

/**
 * ShowModel settings UI
 */
public class ShowSettingsPanel extends JPanel {
    private static final int SPEED_SLIDER_MIN = -100;
    private static final int SPEED_SLIDER_MAX = 100;

    // Bounds on effect/transition durations
    private static final double DURATION_EFFECT_SLOWEST = 25;
    private static final double DURATION_EFFECT_DEFAULT = 10;
    private static final double DURATION_EFFECT_FASTEST = 0.1;
    private static final double DURATION_TRANSITION_SLOWEST = 5;
    private static final double DURATION_TRANSITION_DEFAULT = 2;
    private static final double DURATION_TRANSITION_FASTEST = 0.5;


    // Lazily initialized
    private JColorChooser colorChooser = null;

    private JButton colorButton;
    private ColorIcon colorChip;

    private JRadioButton smallSkinRadio;
    private JRadioButton largeSkinRadio;
    private JRadioButton customSkinRadio;
    private JButton customSkinButton;

    private JLabel photoCountLabel;
    private JLabel computedPhotoCountLabel;

    private JSlider transitionSlider;
    private JSlider effectSlider;

    private DurationSpinner transitionSpinner;
    private DurationSpinner effectSpinner;

    private Container durationPanel;

    private JPanel mp3ModePanel;
    private JComboBox mp3ModeCombo;

    // Array order must match MP3MODES
    private static String[] MP3MODE_NAMES = {
        UIMessages.getMessage(UIMessages.UI_DISABLE_SOUND),
        UIMessages.getMessage(UIMessages.UI_INTERNAL_SOUND),
        UIMessages.getMessage(UIMessages.UI_EXTERNAL_SOUND)
    };
    private static ShowModel.MP3Mode[] MP3MODES = {
        ShowModel.MP3Mode.NONE,
        ShowModel.MP3Mode.INTERNAL,
        ShowModel.MP3Mode.EXTERNAL
    };

    private JComboBox endShowModeCombo;

    // Array order must match ENDSHOWMODES
    private static String[] ENDSHOWMODE_NAMES = {
        UIMessages.getMessage(UIMessages.UI_ENDSHOW_LOOP),
        UIMessages.getMessage(UIMessages.UI_ENDSHOW_PAUSE),
        UIMessages.getMessage(UIMessages.UI_ENDSHOW_STOP),
        UIMessages.getMessage(UIMessages.UI_ENDSHOW_CLOSE)
    };
    private static ShowModel.EndShowMode[] ENDSHOWMODES = {
        ShowModel.EndShowMode.LOOP,
        ShowModel.EndShowMode.PAUSE,
        ShowModel.EndShowMode.STOP,
        ShowModel.EndShowMode.CLOSE
    };

    private static final String PANEL_EXPERT = "expert";
    private static final String PANEL_WIZARD = "wizard";

    // Lazily initialized
    private SkinChooser skinChooser = null;

    private DurationSpinner frameRateSpinner;

    private WizardUI ui;
    private ShowModel model;
    private Skin customSkin;
    private SkinPair skinPair;
    private float photoAspectRatio = -1;

    private boolean ignoreModelChanges;
    private boolean ignoreUIChanges;


    public ShowSettingsPanel(WizardUI ui) {
        super(new GridBagLayout());
        this.ui = ui;
        buildUI();
        syncUIToExpertMode();
        ui.addPropertyChangeListener(new WizardPropertyListener());
    }

    private void syncUIToModel(ShowModel model) {
        ignoreUIChanges = true;
        try {
            this.model = model;

            // Monitor changes to model
            ModelChangeHandler handler = new ModelChangeHandler();
            model.addPropertyChangeListener(handler);
            model.getPhotoEffectList().addListDataListener(handler);

            syncUIToModelBackgroundColor();
            syncUIToModelMP3Mode();
            syncUIToModelEndShowMode();
            syncUIToModelShowList();
            syncUIToModelSkin();
            syncUIToModelDefaultEffectDuration();
            syncUIToModelDefaultTransitionDuration();
            syncUIToModelFrameRate();

            // Display computed number of photos that will fit based on durations
            syncUIComputedPhotoCountToModel();
        } finally {
            ignoreUIChanges = false;
        }
    }

    private void syncUIToModelShowList() {
        float newRatio = Wizard.computeAspectRatio(model);
        if (newRatio == photoAspectRatio)
            return;
        photoAspectRatio = newRatio;

        // Find builtin small/large skins that match photo aspect
        skinPair = SkinManager.chooseBuiltinSkinPair(photoAspectRatio);

        setSkinToolTip(smallSkinRadio, skinPair.getSmall());
        setSkinToolTip(largeSkinRadio, skinPair.getLarge());

        // Number of photos (unique photos)
        photoCountLabel.setText(UIMessages.getMessage(UIMessages.UI_LABEL_PHOTOCOUNT,
                new Integer(model.getPhotoEffectList().size())));
    }

    private void syncUIToModelSkin() {
        Skin skin = model.getSkin();

        // See if model skin is a preset small or large, otherwise use it as custom
        customSkin = skin;
        if (skin == skinPair.getSmall())
            smallSkinRadio.setSelected(true);
        else if (skin == skinPair.getLarge())
            largeSkinRadio.setSelected(true);
        else
            customSkinRadio.setSelected(true);
        setSkinToolTip(customSkinButton, skin);
    }

    private void syncUIToModelBackgroundColor() {
        // Don't set colorChooser color until it is created
        colorChip.setColor(model.getBackgroundColor());
    }

    /**
     * Select an mp3 mode combo based on current MP3Mode
     */
    private void syncUIToModelMP3Mode() {
        // Use NONE if no MP3
        ShowModel.MP3Mode mode = model.getMP3() == null ? ShowModel.MP3Mode.NONE : model.getMP3Mode();
        for (int i = 0; i < MP3MODES.length; i++) {
            if (mode == MP3MODES[i])
                mp3ModeCombo.setSelectedIndex(i);
        }

        // If no MP3, disable combo
        UIUtilities.enableComponentTree(mp3ModePanel, model.getMP3() != null);
    }

    /**
     * Select an EndShow mode combo based on current EndShowMode
     */
    private void syncUIToModelEndShowMode() {
        ShowModel.EndShowMode mode = model.getEndShowMode();
        for (int i = 0; i < ENDSHOWMODES.length; i++) {
            if (mode == ENDSHOWMODES[i])
                endShowModeCombo.setSelectedIndex(i);
        }
    }

    private void syncUIToModelDefaultEffectDuration() {
        syncUISpinnerToModelDefaultEffectDuration();
        syncUISliderToModelDefaultEffectDuration();
    }

    private void syncUISpinnerToModelDefaultEffectDuration() {
        // Update spinner for expert mode
        effectSpinner.setDuration(model.getDefaultEffectDuration());
    }

    private void syncUISliderToModelDefaultEffectDuration() {
        // Update slider for wizard mode
        int effectValue = computeSliderValueFromDuration(model.getDefaultEffectDuration(),
                DURATION_EFFECT_DEFAULT, DURATION_EFFECT_SLOWEST, DURATION_EFFECT_FASTEST);
        effectSlider.setValue(effectValue);
    }

    private void syncUIToModelDefaultTransitionDuration() {
        syncUISpinnerToModelDefaultTransitionDuration();
        syncUISliderToModelDefaultTransitionDuration();
    }

    private void syncUISpinnerToModelDefaultTransitionDuration() {
        // Update spinner for expert mode
        transitionSpinner.setDuration(model.getDefaultTransitionDuration());
    }

    private void syncUISliderToModelDefaultTransitionDuration() {
        // Update slider for wizard mode
        int transitionValue = computeSliderValueFromDuration(model.getDefaultTransitionDuration(),
                DURATION_TRANSITION_DEFAULT, DURATION_TRANSITION_SLOWEST, DURATION_TRANSITION_FASTEST);
        transitionSlider.setValue(transitionValue);
    }

    private void syncUIToModelFrameRate() {
        frameRateSpinner.setDuration(model.getFrameRate());
    }

    /**
     * Display number of photos wizard will use
     */
    private void syncUIComputedPhotoCountToModel() {
        // Compute mp3 duration based on mp3 and mode
        MP3 mp3 = model.getMP3();
        double mp3Duration = model.getMP3Mode() != ShowModel.MP3Mode.NONE ? (mp3 != null ? mp3.getDuration() : 0) : 0;
        if (mp3Duration > 0) {
            int computedCount = Wizard.computePhotoCount(mp3Duration, model.getDefaultTransitionDuration(), model.getDefaultEffectDuration());
            computedPhotoCountLabel.setText(UIMessages.getMessage(UIMessages.UI_LABEL_COMPUTED_PHOTOCOUNT,
                    new Integer(computedCount)));
        }
        else
            computedPhotoCountLabel.setText(" ");
    }

    private void syncUIToExpertMode() {
        ((CardLayout)durationPanel.getLayout()).show(durationPanel, ui.isExpertMode() ? PANEL_EXPERT : PANEL_WIZARD);
    }

    private void buildUI() {
        // Build radio buttons to select show size
        JPanel skinRadioPanel = buildSkinSelectionPanel();

        // Background color chooser button
        JPanel colorPanel = buildBackgroundColorPanel();

        // Display either expert or wizard duration panel
        JPanel expertDurationPanel = wrapDurationPanel(buildExpertDurationPanel());
        JPanel wizardDurationPanel = wrapDurationPanel(buildWizardDurationPanel());
        durationPanel = new JPanel(new CardLayout());
        durationPanel.add(expertDurationPanel, PANEL_EXPERT);
        durationPanel.add(wizardDurationPanel, PANEL_WIZARD);

        // Build MP3 mode combo panel
        mp3ModePanel = buildMP3ModePanel();

        // Build EndShow mode combo panel
        JComponent endShowMode = buildEndShowModePanel();

        // Build framerate spinner panel
        JComponent frameRatePanel = buildFrameRatePanel();

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = gbc.insets.left = 10;
        this.add(skinRadioPanel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = 5;
        gbc.insets.left = 10;
        this.add(colorPanel, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets.top = gbc.insets.left = 10;
        this.add(endShowMode, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        gbc.insets.top = gbc.insets.left = gbc.insets.bottom = 10;
        this.add(mp3ModePanel, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weighty = 1;
        gbc.insets.left = gbc.insets.right = 20;
        this.add(new JSeparator(JSeparator.VERTICAL), gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.insets.top = 10;
        this.add(durationPanel, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets.top = 10;
        this.add(frameRatePanel, gbc);
    }

    /**
     * Wrap panel in another panel so it is in upper left instead of centered.
     */
    private JPanel wrapDurationPanel(JPanel panel) {
        JPanel parentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        parentPanel.add(panel);
        return parentPanel;
    }

    /**
     * In wizard mode, durations are set via sliders with labels that show repeated/deleted photo count
     */
    private JPanel buildWizardDurationPanel() {
        // Photo count labels
        // Use placeholder text so preferred size is OK.
        // Lock in small preferred size, let layout manager resize larger.
        photoCountLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_PHOTOCOUNT, "000"), JLabel.RIGHT);
        photoCountLabel.setPreferredSize(photoCountLabel.getPreferredSize());
        computedPhotoCountLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_COMPUTED_PHOTOCOUNT, "000"), JLabel.RIGHT);
        computedPhotoCountLabel.setPreferredSize(computedPhotoCountLabel.getPreferredSize());

        final int sliderSpeed = 0;

        // Slider labels
        Hashtable<Integer,JLabel> sliderLabelHash = new Hashtable<Integer, JLabel>(2);
        sliderLabelHash.put(new Integer(SPEED_SLIDER_MIN),
            new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_SPEED_SLOW)));
        sliderLabelHash.put(new Integer(SPEED_SLIDER_MAX),
            new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_SPEED_FAST)));

        DurationChangeHandler sliderHandler = new DurationChangeHandler();
        Dimension sliderSize;
        final int MINWIDTH = 150;

        // Transition speed slider
        transitionSlider = new JSlider(SPEED_SLIDER_MIN, SPEED_SLIDER_MAX, sliderSpeed);
        transitionSlider.setLabelTable(sliderLabelHash);
        transitionSlider.setPaintLabels(true);
        transitionSlider.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_TRANSITION_SPEED_TITLE)));
        sliderSize = transitionSlider.getPreferredSize();
        sliderSize.width = Math.max(MINWIDTH, sliderSize.width);
        transitionSlider.setPreferredSize(sliderSize);
        transitionSlider.addChangeListener(sliderHandler);

        // Effect speed slider
        effectSlider = new JSlider(SPEED_SLIDER_MIN, SPEED_SLIDER_MAX, sliderSpeed);
        effectSlider.setLabelTable(sliderLabelHash);
        effectSlider.setPaintLabels(true);
        effectSlider.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_EFFECT_SPEED_TITLE)));
        sliderSize = effectSlider.getPreferredSize();
        sliderSize.width = Math.max(MINWIDTH, sliderSize.width);
        effectSlider.setPreferredSize(sliderSize);
        effectSlider.addChangeListener(sliderHandler);

        JPanel panel = new JPanel(new GridBagLayout());

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets.top = 5;
        panel.add(transitionSlider, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(photoCountLabel, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(computedPhotoCountLabel, gbc);

        gbc.reset();
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(effectSlider, gbc);

        return panel;
    }

    /**
     * In expert mode, durations are displayed via spinners
     */
    private JPanel buildExpertDurationPanel() {
        DurationChangeHandler handler = new DurationChangeHandler();
        transitionSpinner = new DurationSpinner();
        transitionSpinner.addChangeListener(handler);
        effectSpinner = new DurationSpinner();
        effectSpinner.addChangeListener(handler);

        String secondsText = UIMessages.getMessage(UIMessages.UI_SECONDS_LABEL);

        JPanel panel = new JPanel(new GridBagLayout());

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.bottom = 2;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_TRANSITION_DURATION_LABEL)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(transitionSpinner, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.left = 5;
        panel.add(new JLabel(secondsText), gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.top = 5;
        gbc.insets.bottom = 2;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_EFFECT_DURATION_LABEL)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(effectSpinner, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.left = 5;
        panel.add(new JLabel(secondsText), gbc);

        return panel;
    }

    private JPanel buildBackgroundColorPanel() {
        JPanel colorPanel = new JPanel(new BorderLayout(5, 0));
        colorChip = new ColorIcon();
        colorButton = new JButton(UIMessages.getMessage(UIMessages.UI_LABEL_COLOR_ELLIPSIS), colorChip);
        colorButton.setMargin(new Insets(5, 5, 5, 5));
        colorButton.setHorizontalTextPosition(SwingConstants.CENTER);
        colorButton.setVerticalTextPosition(SwingConstants.CENTER);
        colorButton.addActionListener(new ShowColorChooserHandler());

        JLabel colorLabel = new JLabel(UIMessages.getMessage(UIMessages.UI_LABEL_COLOR));

        colorPanel.add(colorButton, BorderLayout.WEST);
        colorPanel.add(colorLabel, BorderLayout.EAST);
        return colorPanel;
    }

    private JPanel buildMP3ModePanel() {
        mp3ModeCombo = new JComboBox(MP3MODE_NAMES);
        mp3ModeCombo.addActionListener(new MP3ModeSelectionHandler());

        // Titled panel for MP3 combo
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_BOX_MP3_TITLE)));

        panel.add(mp3ModeCombo, BorderLayout.CENTER);

        return panel;
    }

    private JComponent buildEndShowModePanel() {
        endShowModeCombo = new JComboBox(ENDSHOWMODE_NAMES);
        endShowModeCombo.addActionListener(new EndShowModeSelectionHandler());

        // Titled panel for EndShow combo
        JPanel modePanel = new JPanel(new BorderLayout());
        modePanel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_BOX_ENDSHOW_TITLE)));

        modePanel.add(endShowModeCombo, BorderLayout.CENTER);

        return modePanel;
    }

    private JComponent buildFrameRatePanel() {
        // Allow 1-30 fps for now
        frameRateSpinner = new DurationSpinner(ShowModel.DEFAULT_FRAMERATE, 1.0, 30.0, 1.0, 3);
        frameRateSpinner.addChangeListener(new FrameRateChangeHandler());

        JPanel panel = new JPanel(new GridBagLayout());

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.bottom = 2;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FRAMERATE)), gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(frameRateSpinner, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets.left = 5;
        panel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_FPS)), gbc);

        return panel;
    }

    private JPanel buildSkinSelectionPanel() {
        WizardGBC gbc = new WizardGBC();

        // Show size radio buttons
        ButtonGroup skinGroup = new ButtonGroup();

        SkinRadioHandler listener = new SkinRadioHandler();

        largeSkinRadio = new JRadioButton(UIMessages.getMessage(UIMessages.UI_RADIO_SIZE_LARGE));
        largeSkinRadio.setHorizontalAlignment(JRadioButton.CENTER);
        largeSkinRadio.addActionListener(listener);
        skinGroup.add(largeSkinRadio);

        smallSkinRadio = new JRadioButton(UIMessages.getMessage(UIMessages.UI_RADIO_SIZE_SMALL));
        smallSkinRadio.setHorizontalAlignment(JRadioButton.CENTER);
        smallSkinRadio.addActionListener(listener);
        skinGroup.add(smallSkinRadio);

        customSkinRadio = new JRadioButton();
        customSkinRadio.setHorizontalAlignment(JRadioButton.CENTER);
        customSkinRadio.addActionListener(listener);
        skinGroup.add(customSkinRadio);

        // Custom radio button has a "custom" button for a label
        customSkinButton = new JButton(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_BUTTON));
        customSkinButton.addActionListener(new CustomSkinHandler());

        // Layout radio buttons
        JPanel sizeRadioPanel = new JPanel(new GridBagLayout());
        sizeRadioPanel.setBorder(new TitledBorderFix(UIMessages.getMessage(UIMessages.UI_BOX_THEME_TITLE)));

        gbc.reset();
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        sizeRadioPanel.add(largeSkinRadio, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        sizeRadioPanel.add(smallSkinRadio, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        sizeRadioPanel.add(customSkinRadio, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        sizeRadioPanel.add(customSkinButton, gbc);

        return sizeRadioPanel;
    }


    private JColorChooser buildColorChooser() {
        // Initialize to colorchip color
        JColorChooser chooser = new JColorChooser();

        // XXX CenterLayout modifies the preferred size returned, so we can't use setPreferredSize
        // http://developer.java.sun.com/developer/bugParade/bugs/4674040.html
        JPanel pnlPreview = new JPanel() {
            public Dimension getPreferredSize() {
                return new Dimension(120, 20);
            }
        };
        pnlPreview.setBorder(BorderFactory.createLoweredBevelBorder());
        pnlPreview.setOpaque(true);
        chooser.setPreviewPanel(pnlPreview);
        // Set preview panel background to selected color
        chooser.getSelectionModel()
            .addChangeListener(EventHandler.create(ChangeListener.class,
                pnlPreview, "background", "source.selectedColor"));

        return chooser;
    }

    // Normalize slider value to a percentage (-1.0->1.0) and convert to a duration in seconds
    // Sliders range from SPEED_SLIDER_MIN->0->SPEED_SLIDER_MAX,
    // durations map onto this as SLOWEST->DEFAULT->FASTEST
    private double computeDurationFromSliderValue(int sliderValue,
            double defaultDuration, double slowestDuration, double fastestDuration) {

        double duration = defaultDuration;
        if (sliderValue < 0) {
            double percent = (double)sliderValue / SPEED_SLIDER_MIN;
            duration = defaultDuration + percent * (slowestDuration - defaultDuration);
        }
        else if (sliderValue > 0) {
            double percent = (double)sliderValue / SPEED_SLIDER_MAX;
            duration = defaultDuration + percent * (fastestDuration - defaultDuration);
        }

        return duration;
    }

    // Normalize duration to a percentage (-1.0->1.0) and convert to a slider value
    private int computeSliderValueFromDuration(double duration,
            double defaultDuration, double slowestDuration, double fastestDuration) {

        int value = 0;

        // Slow duration, -100->0 slider value
        if (duration > defaultDuration) {
            double percent = (duration - defaultDuration) / (slowestDuration - defaultDuration);
            value = (int)(percent * SPEED_SLIDER_MIN);
            if (value < SPEED_SLIDER_MIN)
                value = SPEED_SLIDER_MIN;
        }
        // Fast duration, 0->100 slider value
        else if (duration < defaultDuration) {
            double percent = (defaultDuration - duration) / (defaultDuration - fastestDuration);
            value = (int)(percent * SPEED_SLIDER_MAX);
            if (value > SPEED_SLIDER_MAX)
                value = SPEED_SLIDER_MAX;
        }

        return value;
    }

    private void updateModelSkinFromUI() {
        // Set skin based on radio selection
        if (smallSkinRadio.isSelected())
            model.setSkin(skinPair.getSmall());
        else if (largeSkinRadio.isSelected())
            model.setSkin(skinPair.getLarge());
        else
            model.setSkin(customSkin);
    }

    private void updateModelMP3ModeFromUI() {
        model.setMP3Mode(MP3MODES[mp3ModeCombo.getSelectedIndex()]);
    }

    private void updateModelEndShowModeFromUI() {
        model.setEndShowMode(ENDSHOWMODES[endShowModeCombo.getSelectedIndex()]);
    }

    private void updateModelDefaultEffectDurationFromSpinnerUI() {
        double duration = ((Double)effectSpinner.getValue()).doubleValue();
        model.setDefaultEffectDuration(duration);
    }

    private void updateModelDefaultEffectDurationFromSliderUI() {
        double duration = computeDurationFromSliderValue(effectSlider.getValue(),
                DURATION_EFFECT_DEFAULT, DURATION_EFFECT_SLOWEST, DURATION_EFFECT_FASTEST);
        model.setDefaultEffectDuration(duration);
    }

    private void updateModelDefaultTransitionDurationFromSpinnerUI() {
        double duration = ((Double)transitionSpinner.getValue()).doubleValue();
        model.setDefaultTransitionDuration(duration);
    }

    private void updateModelDefaultTransitionDurationFromSliderUI() {
        double duration = computeDurationFromSliderValue(transitionSlider.getValue(),
                DURATION_TRANSITION_DEFAULT, DURATION_TRANSITION_SLOWEST, DURATION_TRANSITION_FASTEST);
        model.setDefaultTransitionDuration(duration);
    }

    private void updateModelBackgroundColorFromUI() {
        model.setBackgroundColor(colorChip.getColor());
    }

    private void updateModelFrameRateFromUI() {
        model.setFrameRate((float)frameRateSpinner.getDuration());
    }

    private void setSkinToolTip(JComponent component, Skin skin) {
        SkinSet skinset = skin.getSkinSet();
        // If no skinset, then it is a custom size skin
        String skinsetName = skinset == null ? UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_TOOLTIP_SIZE) : skinset.getDisplayName();
        component.setToolTipText(UIMessages.getMessage(UIMessages.UI_CUSTOMSKIN_TOOLTIP,
                new Object[] { skinsetName, skin.getDisplayName() }));
    }

    /**
     * Handle MP3Mode combo selection
     */
    private class MP3ModeSelectionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (ignoreUIChanges)
                return;

            ignoreModelChanges = true;
            try {
                updateModelMP3ModeFromUI();
            } finally {
                ignoreModelChanges = false;
            }

            // Display new computed photo count
            syncUIComputedPhotoCountToModel();
        }
    }

    /**
     * Handle EndShowMode combo selection
     */
    private class EndShowModeSelectionHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (ignoreUIChanges)
                return;

            ignoreModelChanges = true;
            try {
                updateModelEndShowModeFromUI();
            } finally {
                ignoreModelChanges = false;
            }
        }
    }

    /**
     * Handle transition/effect slider/spinner changes
     */
    private class DurationChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (ignoreUIChanges)
                return;

            Object source = e.getSource();

            ignoreModelChanges = true;
            ignoreUIChanges = true;
            try {
                if (source == effectSlider) {
                    updateModelDefaultEffectDurationFromSliderUI();
                    // Keep spinner in sync
                    syncUISpinnerToModelDefaultEffectDuration();
                }
                else if (source == effectSpinner) {
                    updateModelDefaultEffectDurationFromSpinnerUI();
                    // Keep slider in sync
                    syncUISliderToModelDefaultEffectDuration();
                }
                else if (source == transitionSlider) {
                    updateModelDefaultTransitionDurationFromSliderUI();
                    // Keep spinner in sync
                    syncUISpinnerToModelDefaultTransitionDuration();
                }
                else if (source == transitionSpinner) {
                    updateModelDefaultTransitionDurationFromSpinnerUI();
                    // Keep slider in sync
                    syncUISliderToModelDefaultTransitionDuration();
                }
            } finally {
                ignoreModelChanges = false;
                ignoreUIChanges = false;
            }

            // Display new computed photo count
            syncUIComputedPhotoCountToModel();
        }
    }

    /**
     * Handle framerate spinner changes
     */
    private class FrameRateChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (ignoreUIChanges)
                return;

            ignoreModelChanges = true;
            try {
                updateModelFrameRateFromUI();
            } finally {
                ignoreModelChanges = false;
            }
        }
    }

    /**
     * When the colorchooser is confirmed, set colorchip to selected color
     */
    private class ConfirmColorChooserHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Color color = colorChooser.getColor();
            colorChip.setColor(color);

            ignoreModelChanges = true;
            try {
                updateModelBackgroundColorFromUI();
            } finally {
                ignoreModelChanges = false;
            }
        }
    }

    /**
     * Handles clicking on the background color button
     */
    private class ShowColorChooserHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (colorChooser == null)
                colorChooser = buildColorChooser();

            colorChooser.setColor(colorChip.getColor());

            // Set color chip color to color chooser color
            ActionListener okListener = new ConfirmColorChooserHandler();

            // JColorChooser.showDialog creates a new chooser - so we can't use it since we customize the preview
            JDialog dialog = JColorChooser.createDialog(colorButton,
                    UIMessages.getMessage(UIMessages.UI_COLORCHOOSER_TITLE),
                    true, colorChooser, okListener, null);
            dialog.setVisible(true);
        }
    }

    /**
     * Handlers clicking one of the skin radio buttons
     */
    private class SkinRadioHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (ignoreUIChanges)
                return;

            ignoreModelChanges = true;
            try {
                updateModelSkinFromUI();
            } finally {
                ignoreModelChanges = false;
            }
        }
    }

    /**
     * Handles clicking the "Custom Show" button
     */
    private class CustomSkinHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Select custom radio button.
            // Allow this to trigger callback and change the model, in case the dialog is canceled below.
            customSkinRadio.doClick();

            if (skinChooser == null)
                skinChooser = new SkinChooser();

            if (skinChooser.showDialog(customSkinRadio, customSkin, photoAspectRatio)) {
                customSkin = skinChooser.getSkin();

                ignoreModelChanges = true;
                try {
                    updateModelSkinFromUI();
                } finally {
                    ignoreModelChanges = false;
                }

                // Set button tooltip to new skin name
                setSkinToolTip(customSkinButton, customSkin);
            }
        }
    }

    /**
     * Handle WizardUI property changes
     */
    private class WizardPropertyListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();

            if (WizardUI.SHOWMODEL_PROPERTY.equals(prop))
                syncUIToModel((ShowModel)e.getNewValue());
            else if (WizardUI.EXPERT_MODE_PROPERTY.equals(prop))
                syncUIToExpertMode();
        }
    }

    /**
     * Handle changes in the show model and photo list.
     */
    private class ModelChangeHandler implements ListDataListener, PropertyChangeListener {
        public void intervalAdded(ListDataEvent e) {
            handleShowListChange();
        }
        public void intervalRemoved(ListDataEvent e) {
            handleShowListChange();
        }
        public void contentsChanged(ListDataEvent e) {
            handleShowListChange();
        }

        private void handleShowListChange() {
            if (ignoreModelChanges)
                return;

            ignoreUIChanges = true;
            try {
                syncUIToModelShowList();
            } finally {
                ignoreUIChanges = false;
            }

            ignoreModelChanges = true;
            try {
                // Skin may have changed as a result of new photo aspect
                updateModelSkinFromUI();
            } finally {
                ignoreModelChanges = false;
            }
        }

        /**
         * Update MP3 duration when model MP3 changes
         */
        public void propertyChange(PropertyChangeEvent e) {
            if (ignoreModelChanges)
                return;

            String prop = e.getPropertyName();

            ignoreUIChanges = true;
            try {
                if (ShowModel.BACKGROUND_COLOR_PROPERTY.equals(prop))
                    syncUIToModelBackgroundColor();
                else if (ShowModel.MP3_FILE_PROPERTY.equals(prop)) {
                    syncUIToModelMP3Mode();
                    syncUIComputedPhotoCountToModel();
                }
                else if (ShowModel.MP3_MODE_PROPERY.equals(prop)) {
                    syncUIToModelMP3Mode();
                    syncUIComputedPhotoCountToModel();
                }
                else if (ShowModel.ENDSHOW_MODE_PROPERTY.equals(prop))
                    syncUIToModelEndShowMode();
                else if (ShowModel.SKIN_PROPERTY.equals(prop))
                    syncUIToModelSkin();
                else if (ShowModel.DEFAULT_EFFECT_DURATION_PROPERTY.equals(prop)) {
                    syncUIToModelDefaultEffectDuration();
                    syncUIComputedPhotoCountToModel();
                }
                else if (ShowModel.DEFAULT_TRANSITION_DURATION_PROPERTY.equals(prop)) {
                    syncUIToModelDefaultTransitionDuration();
                    syncUIComputedPhotoCountToModel();
                }
                else if (ShowModel.FRAMERATE_PROPERTY.equals(prop)) {
                    syncUIToModelFrameRate();
                }
            } finally {
                ignoreUIChanges = false;
            }
        }
    }
}
