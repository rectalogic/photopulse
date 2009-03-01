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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.ui.wizard.expert.panzoom;

import com.photica.photopulse.imageio.DecodedImage;
import com.photica.photopulse.imageio.ImageCoder;
import com.photica.photopulse.model.Photo;
import com.photica.photopulse.model.PhotoEffects;
import com.photica.photopulse.model.effects.EffectRegistry;
import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.skin.Skin;
import com.photica.photopulse.ui.wizard.AnnotatedIcon;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.UIMessages;
import com.photica.photopulse.ui.wizard.WizardGBC;
import com.photica.photopulse.ui.wizard.expert.PhotoListCellRenderer;
import com.photica.photopulse.ui.wizard.viewer.ShowPreviewer;
import com.photica.ui.JSliderFix;
import com.photica.ui.JSpinnerFix;
import com.photica.ui.ThumbWheel;
import com.photica.ui.ToolButton;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;

/**
 * Manages editing a custom PanZoomEffect for a single PhotoEffects.
 */
public class PanZoomEditor extends JPanel {

    private static final int STAGE_SIZE = 450;

    private static final int SCALE_MAX = 1000;

    private static final int SLIDER_WIDTH = 250;
    private static final int THUMBWHEEL_DIAMETER = 200;
    private static final int THUMBWHEEL_THICKNESS = 18;

    private static final MessageFormat ROTATION_FORMAT = new MessageFormat(UIMessages.getMessage(UIMessages.UI_PZ_DEGREES));

    // Invert Y coord display in spinner. In Keyframe, up is negative. But display so up is positive.
    private static final int INVERT_Y = -1;

    private PhotoEffects sourceEffects;
    private Skin skin;
    private Color backgroundColor;

    private Timeline timeline;

    private Keyframe currentKeyframe;
    private KeyframeHandler keyframeHandler = new KeyframeHandler();

    private JButton addKeyframeButton;
    private JButton deleteKeyframeButton;

    private Stage stage;

    private JLabel rotationLabel;
    private ThumbWheel rotationWheel;
    private JSliderFix scaleSlider;
    private JSpinnerFix translateXSpinner;
    private JSpinnerFix translateYSpinner;
    private JCheckBox curveToggle;
    private JCheckBox easingToggle;


    PanZoomEditor(PhotoEffects effects, Skin skin, Color backgroundColor) throws IOException {
        this.sourceEffects = effects;
        this.skin = skin;
        this.backgroundColor = backgroundColor;

        BufferedImage image;
        Photo photo = effects.getPhoto();

        // Render icon for Flash photo
        if (photo.isFlashPhoto()) {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            image = gc.createCompatibleImage(photo.getWidth(), photo.getHeight(), Transparency.OPAQUE);
            Graphics g = image.getGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, photo.getWidth(), photo.getHeight());
            Icon icon = PhotoListCellRenderer.FLASH_ICON;
            icon.paintIcon(this, g, (photo.getWidth() - icon.getIconWidth())/2, (photo.getHeight() - icon.getIconHeight())/2);
            g.dispose();
        }
        else {
            DecodedImage dimage = ImageCoder.getInstance().decodeImage(photo.getFile(), null, 1, 0, 0, 0, null);
            image = dimage.getImage();
        }

        stage = new Stage(image, STAGE_SIZE, skin.getStageAspectRatio());

        JPanel controlPanel = buildControlPanel();
        JPanel timelinePanel = buildTimelinePanel(effects, image);

        setLayout(new GridBagLayout());

        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = WizardGBC.BOTH;
        gbc.insets.top = gbc.insets.left = gbc.insets.right = 5;
        this.add(stage, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.fill = WizardGBC.VERTICAL;
        gbc.weighty = 1;
        gbc.insets.top = gbc.insets.right = 10;
        gbc.insets.left = 5;
        this.add(controlPanel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.gridwidth = 2;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.top = gbc.insets.left = 5;
        gbc.insets.right = 10;
        this.add(timelinePanel, gbc);

        // Sync to selected keyframe
        setCurrentKeyframe(timeline.getSelection());
    }

    private JPanel buildTimelinePanel(PhotoEffects effects, BufferedImage image) {
        PanZoomEffect.Keyframe initialKeyframes[] = null;
        if (effects.getEffect() instanceof PanZoomEffect)
            initialKeyframes = ((PanZoomEffect)effects.getEffect()).getKeyframes();

        // Populate timeline with keyframes
        Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
        Dimension stageSize = stage.getStageSize();
        if (initialKeyframes == null) {
            timeline = new Timeline(effects.getBeginTransitionDuration(), effects.getEffectDuration(), effects.getEndTransitionDuration(),
                    new Keyframe(stageSize, imageSize));
        }
        else {
            timeline = new Timeline(effects.getBeginTransitionDuration(), effects.getEffectDuration(), effects.getEndTransitionDuration(),
                    new Keyframe(initialKeyframes[0], stageSize, imageSize));
            for (int i = 1; i < initialKeyframes.length; i++) {
                timeline.insertKeyframe(new Keyframe(initialKeyframes[i], stageSize, imageSize),
                        initialKeyframes[i].getStartTime());
            }
        }
        timeline.addChangeListener(new TimelineSelectionHandler());

        addKeyframeButton = new ToolButton(UIMessages.getMessage(UIMessages.UI_PZ_ADDKEYFRAME), new AnnotatedIcon(Timeline.KEYFRAME_ICON, new ResourceIcon("resources/keyframe-add.gif")));
        addKeyframeButton.setHorizontalAlignment(SwingConstants.LEFT);
        addKeyframeButton.addActionListener(new AddKeyframeHandler());
        deleteKeyframeButton = new ToolButton(UIMessages.getMessage(UIMessages.UI_PZ_DELKEYFRAME), new AnnotatedIcon(Timeline.KEYFRAME_ICON, new ResourceIcon("resources/keyframe-delete.gif")));
        deleteKeyframeButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteKeyframeButton.addActionListener(new DeleteKeyframeHandler());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        buttonPanel.add(addKeyframeButton);
        buttonPanel.add(deleteKeyframeButton);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_PZ_TIMELINE)));
        panel.add(timeline, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        scaleSlider = buildSlider(0, SCALE_MAX, new ScaleHandler(), 25, 0, 100, (double)SCALE_MAX/100, "%");

        ChangeListener listener = new TranslationHandler();
        translateXSpinner = buildSpinner(listener);
        translateYSpinner = buildSpinner(listener);

        curveToggle = new JCheckBox(UIMessages.getMessage(UIMessages.UI_PZ_CURVE));
        curveToggle.setVerticalTextPosition(SwingConstants.TOP);
        curveToggle.addActionListener(new CurvePathHandler());
        easingToggle = new JCheckBox(UIMessages.getMessage(UIMessages.UI_PZ_EASING));
        easingToggle.setVerticalTextPosition(SwingConstants.TOP);
        easingToggle.addActionListener(new EasingHandler());

        JButton previewButton = new JButton(UIMessages.getMessage(UIMessages.UI_PZ_PREVIEW), new ResourceIcon("resources/binoculars-star.gif"));
        previewButton.addActionListener(new PreviewHandler());

        WizardGBC gbc = new WizardGBC();

        // Translation
        JPanel translationPanel = new JPanel(new GridBagLayout());
        translationPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_PZ_TRANSLATION)));
        gbc.reset();
        gbc.gridx = 0;
        gbc.insets.right = 2;
        translationPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_PZ_TRANSX)), gbc);
        gbc.reset();
        gbc.gridx = 1;
        gbc.insets.right = 10;
        translationPanel.add(translateXSpinner, gbc);
        gbc.reset();
        gbc.gridx = 2;
        gbc.insets.right = 2;
        translationPanel.add(new JLabel(UIMessages.getMessage(UIMessages.UI_PZ_TRANSY)), gbc);
        gbc.reset();
        gbc.gridx = 3;
        translationPanel.add(translateYSpinner, gbc);

        // Scale
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_PZ_SCALE)));
        scalePanel.add(scaleSlider, BorderLayout.CENTER);

        // Rotation
        JPanel rotationPanel = buildRotationPanel();

        gbc.reset();
        gbc.gridy = 0;
        gbc.anchor = WizardGBC.NORTHWEST;
        panel.add(translationPanel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.anchor = WizardGBC.NORTHWEST;
        gbc.insets.top = 10;
        panel.add(scalePanel, gbc);

        gbc.reset();
        gbc.gridy = 2;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.anchor = WizardGBC.NORTHWEST;
        gbc.insets.top = 10;
        panel.add(rotationPanel, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.anchor = WizardGBC.NORTHWEST;
        gbc.insets.top = 10;
        panel.add(curveToggle, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.anchor = WizardGBC.NORTHWEST;
        gbc.insets.top = 10;
        panel.add(easingToggle, gbc);

        gbc.reset();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1;
        gbc.anchor = WizardGBC.SOUTHWEST;
        gbc.insets.top = 10;
        panel.add(previewButton, gbc);

        return panel;
    }

    private JPanel buildRotationPanel() {
        rotationWheel = new ThumbWheel(ThumbWheel.Orientation.HORIZONTAL, THUMBWHEEL_DIAMETER, THUMBWHEEL_THICKNESS);
        rotationWheel.addChangeListener(new RotationHandler());

        rotationLabel = new JLabel("0");
        rotationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        ToolButton spinLeftButton = new ToolButton(new ResourceIcon("resources/spin-left.gif"), true);
        spinLeftButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_PZ_SPINLEFT));
        spinLeftButton.addActionListener(new RotationPresetHandler(-Math.PI/2));
        ToolButton spinUpButton = new ToolButton(new ResourceIcon("resources/spin-up.gif"), true);
        spinUpButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_PZ_SPINUP));
        spinUpButton.addActionListener(new RotationPresetHandler(0));
        ToolButton spinRightButton = new ToolButton(new ResourceIcon("resources/spin-right.gif"), true);
        spinRightButton.setToolTipText(UIMessages.getMessage(UIMessages.UI_PZ_SPINRIGHT));
        spinRightButton.addActionListener(new RotationPresetHandler(Math.PI/2));

        JPanel rotationPanel = new JPanel(new GridBagLayout());
        rotationPanel.setBorder(BorderFactory.createTitledBorder(UIMessages.getMessage(UIMessages.UI_PZ_ROTATE)));


        WizardGBC gbc = new WizardGBC();

        gbc.reset();
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.bottom = 2;
        rotationPanel.add(rotationLabel, gbc);

        gbc.reset();
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.WEST;
        rotationPanel.add(spinLeftButton, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1;
        gbc.fill = WizardGBC.HORIZONTAL;
        gbc.insets.left = gbc.insets.right = 2;
        rotationPanel.add(rotationWheel, gbc);

        gbc.reset();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = WizardGBC.EAST;
        rotationPanel.add(spinRightButton, gbc);

        gbc.reset();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.insets.top = 2;
        rotationPanel.add(spinUpButton, gbc);

        return rotationPanel;
    }

    private JSliderFix buildSlider(int min, int max, ChangeListener listener, int tickInc, int tickMin, int tickMax, double tickMultiple, String tickSuffix) {
        JSliderFix slider = new JSliderFix(min, max);
        slider.addChangeListener(listener);

        Hashtable<Integer,JLabel> labelTable = new Hashtable<Integer,JLabel>(5);
        for (int i = tickMin; i <= tickMax; i+=tickInc)
            labelTable.put(Integer.valueOf((int)(i * tickMultiple)), new JLabel(String.valueOf(i) + tickSuffix));
        slider.setMajorTickSpacing((int)(tickInc * tickMultiple));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);

        Dimension size = slider.getPreferredSize();
        if (size.width < SLIDER_WIDTH) {
            size.width = SLIDER_WIDTH;
            slider.setPreferredSize(size);
        }

        return slider;
    }

    private JSpinnerFix buildSpinner(ChangeListener listener) {
        JSpinnerFix spinner = new JSpinnerFix(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));

        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)spinner.getEditor();
        editor.getTextField().setColumns(3);
        // Get rid of commas in numbers
        editor.getFormat().applyPattern("#");

        spinner.addChangeListener(listener);
        return spinner;
    }

    public PhotoEffects buildPhotoEffects() {
        // Build PanZoomEffect using timeline keyframes
        PanZoomEffect.Keyframe[] keyframes = timeline.buildKeyframes(stage);
        PanZoomEffect pze = EffectRegistry.EFFECT_PanZoom.clonePanZoomEffect(keyframes);

        // Compute max scale
        double photoScale = 0;
        for (PanZoomEffect.Keyframe keyframe : keyframes)
            photoScale = Math.max(keyframe.getScale(), photoScale);

        return new PhotoEffects(sourceEffects.getPhoto(), sourceEffects.getEventArg(), photoScale,
                sourceEffects.getBeginTransition(), sourceEffects.getBeginTransitionDuration(),
                pze, sourceEffects.getEffectDuration(),
                sourceEffects.getEndTransition(), sourceEffects.getEndTransitionDuration(),
                sourceEffects.isEndTransitionTopLayer(), sourceEffects.isLocked());
    }

    private void setCurrentKeyframe(Keyframe keyframe) {
        Keyframe oldKeyframe = this.currentKeyframe;
        if (oldKeyframe != null)
            oldKeyframe.removePropertyChangeListener(keyframeHandler);
        this.currentKeyframe = keyframe;
        keyframe.addPropertyChangeListener(keyframeHandler);

        stage.setKeyframe(keyframe);

        // Do not invoke listeners
        scaleSlider.setValueQuiet((int)(keyframe.getScaleFactor() * SCALE_MAX));
        rotationWheel.setWheelValue(keyframe.getRotationFactor(), false);
        updateRotationLabel(keyframe.getRotationFactor());

        // Do not invoke listeners
        translateXSpinner.setValueQuiet(keyframe.getTranslateX());
        // Invert Y
        translateYSpinner.setValueQuiet(INVERT_Y * keyframe.getTranslateY());

        // setSelected does not invoke listeners
        curveToggle.setSelected(!keyframe.isLinear());
        easingToggle.setSelected(keyframe.hasEasing());
    }

    private void updateRotationLabel(double rotationFactor) {
        String label = ROTATION_FORMAT.format(new Object[] { new Double(Math.toDegrees(rotationFactor)) });
        rotationLabel.setText(label);
    }

    /**
     * Display effects preview
     */
    private void previewEffect() {
        PhotoEffects effects = buildPhotoEffects();
        ShowPreviewer.showDialog(this, skin, backgroundColor, effects, null);
    }

    /**
     * Handle previewing the effect
     */
    private class PreviewHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            previewEffect();
        }
    }

    /**
     * Insert new keyframe after selection
     */
    private class AddKeyframeHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // Clone current selection and insert after it
            Keyframe keyframe = timeline.getSelection().clone();
            timeline.insertKeyframe(keyframe, 0);
        }
    }

    /**
     * Remove selected keyframe
     */
    private class DeleteKeyframeHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            timeline.removeSelectedKeyframe();
        }
    }

    /**
     * Handle timeline keyframe selection events
     */
    private class TimelineSelectionHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            // Disable delete button if first keyframe selected
            deleteKeyframeButton.setEnabled(timeline.getFirstKeyframe() != timeline.getSelection());
            setCurrentKeyframe(timeline.getSelection());
        }
    }

    /**
     * Handle curve path checkbox selection
     */
    private class CurvePathHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            currentKeyframe.setLinear(!curveToggle.isSelected());
        }
    }

    /**
     * Handle linear checkbox selection
     */
    private class EasingHandler implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            currentKeyframe.setEasing(easingToggle.isSelected());
        }
    }

    /**
     * Handle translation spinner change events
     */
    private class TranslationHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            JSpinnerFix spinner = (JSpinnerFix)e.getSource();
            if (spinner.isSetValueQuiet())
                return;

            if (spinner == translateXSpinner)
                currentKeyframe.setTranslateX(((Integer)spinner.getValue()).intValue());
            // Invert Y
            else if (spinner == translateYSpinner)
                currentKeyframe.setTranslateY(INVERT_Y * ((Integer)spinner.getValue()).intValue());
        }
    }

    /**
     * Handle rotation button events
     */
    private class RotationPresetHandler implements ActionListener {
        // Rotation in radians
        private double rotation;
        public RotationPresetHandler(double rotation) {
            this.rotation = rotation;
        }

        public void actionPerformed(ActionEvent e) {
            rotationWheel.setWheelValue(rotation, true);
        }
    }

    /**
     * Handle rotation slider change events
     */
    private class RotationHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            currentKeyframe.setRotationFactor(rotationWheel.getWheelValue());
        }
    }

    /**
     * Handle scale slider change events
     */
    private class ScaleHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            if (scaleSlider.isSetValueQuiet())
                return;
            double scale = scaleSlider.getValue() / (double)SCALE_MAX;
            if (scaleSlider.getValueIsAdjusting())
                stage.setScalePreview(scale);
            else {
                stage.setScalePreview(-1);
                currentKeyframe.setScaleFactor(scale);
            }
        }
    }

    /**
     * Handle changes to underlying Keyframe model
     */
    private class KeyframeHandler implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            if (Keyframe.TRANSLATE_X_PROPERTY == e.getPropertyName())
                translateXSpinner.setValueQuiet(currentKeyframe.getTranslateX());
            // Invert Y
            else if (Keyframe.TRANSLATE_Y_PROPERTY == e.getPropertyName())
                translateYSpinner.setValueQuiet(INVERT_Y * currentKeyframe.getTranslateY());
            else if (Keyframe.ROTATION_FACTOR_PROPERTY == e.getPropertyName()) {
                updateRotationLabel(currentKeyframe.getRotationFactor());
            }
        }
    }

/*
    public static void main(final String args[]) {
        if (args.length != 1) {
            System.err.println("Usage: PanZoomEditor <imagefile>");
            System.exit(-1);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    PhotoEffects effects = new PhotoEffects(ImageCoder.getInstance().validatePhotoFile(new File(args[0])),
                            null, 1.0, EffectRegistry.BEGINTRANS_None, 1,
                            Wizard.WEFFECT_MaxZoomOutTopCenter.getEffect(), 5,
                            EffectRegistry.ENDTRANS_None, 1, true, false);
                    PanZoomEditor editor = new PanZoomEditor(effects, SkinManager.BUILTIN_LARGE_43);

                    JFrame frame = new JFrame("Keyframe Editor");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(editor, BorderLayout.CENTER);
                    frame.pack();
                    frame.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        });
    }
*/
}
