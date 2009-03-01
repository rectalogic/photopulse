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

import com.photica.photopulse.model.effects.PanZoomEffect;
import com.photica.photopulse.ui.wizard.ResourceIcon;
import com.photica.photopulse.ui.wizard.UIMessages;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalIconFactory;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;


/**
 * Graphical timeline of moveable keyframes.
 * Manages startTime of each keyframe. Manages currently selected keyframe.
 */
public class Timeline extends JPanel {
    private static final int TRACK_HEIGHT = 5;

    // This fails with Alloy: UIManager.getIcon("Slider.horizontalThumbIcon")
    // Because that Icon attempts to cast its Component to something Alloy specific.
    public static Icon KEYFRAME_ICON = MetalIconFactory.getHorizontalSliderThumbIcon();
    private static Icon SELECTION_ICON = new ResourceIcon("resources/timeline-star.gif");

    private static double HALF_ICON_WIDTH = KEYFRAME_ICON.getIconWidth()/2.0;

    private static final MessageFormat TIME_FORMAT = new MessageFormat(UIMessages.getMessage(UIMessages.UI_PZ_TIMELINE_TIME));

    /** Total duration in seconds the timeline represents */
    private double totalDuration;
    /** Percentage of timeline begin transition occupies */
    private double beginTransPercent;
    /** Percentage of timeline end transition occupies */
    private double endTransPercent;

    private int controlCount = 0;
    private KeyframeControl headControl = null;
    private KeyframeControl tailControl = null;

    private KeyframeControl selectedControl;
    private ChangeEvent changeEvent = null;

    /**
     * These are the only child components of the Timeline.
     * Each draws an icon normal or flipped vertically and also holds it's Keyframe object.
     * Also maintains linked list of controls.
     */
    private class KeyframeControl extends JComponent {
        private KeyframeControl prevControl;
        private KeyframeControl nextControl;

        /** Time from 0.0 - 1.0 */
        private double startTime;

        private Keyframe keyframe;
        private boolean isFlipped;

        private int dragOffsetX;
        private boolean wasDragged = false;

        public KeyframeControl(Keyframe keyframe, boolean isMoveable) {
            this.keyframe = keyframe;

            setSize(KEYFRAME_ICON.getIconWidth(), KEYFRAME_ICON.getIconHeight() + SELECTION_ICON.getIconHeight());

            if (isMoveable)
                enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
            enableEvents(AWTEvent.MOUSE_EVENT_MASK);

            ToolTipManager.sharedInstance().registerComponent(this);
        }

        public Keyframe getKeyframe() {
            return keyframe;
        }

        public double getStartTime() {
            return startTime;
        }

        public void setStartTime(double startTime) {
            this.startTime = startTime;
        }

        public boolean isFlipped() {
            return isFlipped;
        }

        public void setFlipped(boolean flipped) {
            isFlipped = flipped;
        }

        public KeyframeControl getPrevControl() {
            return prevControl;
        }

        public void setPrevControl(KeyframeControl control) {
            this.prevControl = control;
        }

        public KeyframeControl getNextControl() {
            return nextControl;
        }

        public void setNextControl(KeyframeControl control) {
            this.nextControl = control;
        }

        /**
         * Compute time for control based on position.
         * @param x X coord of control (upper left, not center)
         */
        private double computeTime(int x) {
            // Take into account half icon padding on left/right of timeline.
            // So upper left X coord since it is half icon to left of center.
            return (double)x / getTimelineSize();
        }

        /**
         * Compute proper X coordinate for control
         */
        public int computePosition() {
            // Center control on its time.
            // This computes centerX - which we can use as X since timeline has a half icon space on left.
            return (int)(startTime * getTimelineSize());
        }

        protected void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);

            switch (e.getID()) {
            case MouseEvent.MOUSE_PRESSED:
                wasDragged = false;
                dragOffsetX = e.getX();
                break;
            case MouseEvent.MOUSE_RELEASED:
                dragOffsetX = 0;
                // Only recompute time if we were dragged (to avoid introducing error by just selecting)
                if (wasDragged)
                    setStartTime(computeTime(getX()));
                setSelection(this);
                wasDragged = false;
                break;
            }
        }

        protected void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);

            switch (e.getID()) {
            case MouseEvent.MOUSE_DRAGGED:
                int x = getX() + e.getX() - dragOffsetX;
                double time = computeTime(x);
                // Must not be left of control on left
                if (prevControl != null && time < prevControl.getStartTime())
                    return;
                // Must not be off left edge
                if (prevControl == null && time < 0)
                    return;
                // Must not be right of control on right
                if (nextControl != null && time > nextControl.getStartTime())
                    return;
                // Must not be off right edge
                if (nextControl == null && time > 1)
                    return;

                setLocation(x, getY());
                wasDragged = true;
                break;
            }
        }

        /**
         * Compute tooltip time as needed, instead of keeping track
         */
        public String getToolTipText(MouseEvent event) {
            return TIME_FORMAT.format(new Object[] { new Double(startTime * totalDuration) });
        }

        public Point getToolTipLocation(MouseEvent event) {
            // Display tooltips below
            return new Point(0, getHeight());
        }

        protected void paintComponent(Graphics g) {
            if (isFlipped) {
                Graphics2D g2 = (Graphics2D)g;
                g2.translate(0, getHeight());
                g2.scale(1.0, -1.0);
            }

            KEYFRAME_ICON.paintIcon(this, g, 0, SELECTION_ICON.getIconHeight());

            // Draw as selected.
            // Display selection above control so it is visible when multiple overlap.
            if (this == selectedControl)
                SELECTION_ICON.paintIcon(this, g, (getWidth() - SELECTION_ICON.getIconHeight())/2, 0);
        }
    }

    public Timeline(double beginTransDuration, double effectDuration, double endTransDuration, Keyframe keyframe) {
        this.totalDuration = beginTransDuration + effectDuration + endTransDuration;
        this.beginTransPercent = beginTransDuration / totalDuration;
        this.endTransPercent = endTransDuration / totalDuration;

        // Initial time 0 keyframe
        insertKeyframe(keyframe, 0);
        
        // Absolute position keyframes
        setLayout(null);
    }

    public PanZoomEffect.Keyframe[] buildKeyframes(Stage stage) {
        PanZoomEffect.Keyframe keyframes[] = new PanZoomEffect.Keyframe[controlCount];

        int i = 0;
        KeyframeControl control = headControl;
        while (control != null) {
            keyframes[i] = control.getKeyframe().getKeyframe(control.getStartTime(), stage.getStageSize(), stage.getImageSize());
            control = control.getNextControl();
            i++;
        }

        return keyframes;
    }

    public Keyframe getFirstKeyframe() {
        return headControl.getKeyframe();
    }

    /**
     * Insert a new keyframe after the selected one, and make it the current selection.
     */
    public void insertKeyframe(Keyframe keyframe, double startTime) {
        // First keyframe is not moveable
        KeyframeControl control = new KeyframeControl(keyframe, headControl != null);

        insertControl(selectedControl, control);

        // If we just added the last control and it's time was 0 (unspecified), set it to 1.0 and scale times
        if (tailControl == control && startTime == 0)
            scaleKeyframeTimes(-1);
        // Otherwise attempt to use specified time
        else
            setKeyframeTime(control, startTime);

        // Set selection to new keyframe
        setSelection(control);

        revalidate();
    }

    /**
     * Remove selected keyframe, except the first one.
     * Make previous keyframe the selection.
     */
    public void removeSelectedKeyframe() {
        // Ignore if no keyframes or selection is the first keyframe (time 0)
        if (headControl == null || headControl == selectedControl)
            return;

        removeControl(selectedControl);

        // If we just removed the last control and it was at time 1.0, then set new tail to 1.0 and scale times
        if (tailControl == selectedControl.getPrevControl() && selectedControl.getStartTime() == 1.0)
            scaleKeyframeTimes(1);

        // Set selection to left control (since head is not removeable, there will always be one)
        setSelection(selectedControl.getPrevControl());

        revalidate();
    }

    /**
     * Set keyframe to startTime if it is between prev/next keyframe and non-zero.
     * Otherwise set time to halfway between prev/next.
     * Do not adjust time if first keyframe.
     */
    private void setKeyframeTime(KeyframeControl control, double startTime) {
        if (headControl == control)
            return;

        if (startTime != 0) {
            KeyframeControl nextControl = control.getNextControl();
            if (startTime >= control.getPrevControl().getStartTime()
                    && ((nextControl == null && startTime <= 1.0) || (nextControl != null && startTime <= nextControl.getStartTime()))) {
                control.setStartTime(startTime);
                return;
            }
        }

        // Out of range or 0 specified, use halfway between prev and next
        double prevTime = control.getPrevControl().getStartTime();
        control.setStartTime(prevTime + (control.getNextControl().getStartTime() - prevTime) / 2.0);
    }

    /**
     * Force tail control to time 1.0 and scale previous keyframe times
     * @param scaleDirection negative if scaling down, positive if scaling up
     */
    private void scaleKeyframeTimes(int scaleDirection) {
        if (headControl == tailControl)
            return;

        // Set new tail time to 1.0 and scale previous times by (N-2)/(N-1) to scale down
        // or N/(N-1) to scale up
        tailControl.setStartTime(1);
        if (controlCount <= 2)
            return;
        double timeScale = scaleDirection < 0
                ? (double)(controlCount - 2) / (controlCount - 1)
                : (double)controlCount / (controlCount - 1);
        KeyframeControl c = headControl;
        while (c != tailControl) {
            c.setStartTime(c.getStartTime() * timeScale);
            c = c.getNextControl();
        }
    }

    /**
     * Insert newControl after control.
     */
    private void insertControl(KeyframeControl control, KeyframeControl newControl) {
        // Add to UI layered above control we are inserting after
        add(newControl, null, getComponentZOrder(control));

        if (headControl == null) {
            headControl = newControl;
            tailControl = newControl;
        }
        else {
            KeyframeControl existingNext = control.getNextControl();
            newControl.setNextControl(existingNext);
            newControl.setPrevControl(control);
            control.setNextControl(newControl);
            if (existingNext != null)
                existingNext.setPrevControl(newControl);
            if (tailControl == control)
                tailControl = newControl;
        }

        controlCount++;
    }

    /**
     * Remove contol from linked list. Must not be called to remove head control.
     */
    private void removeControl(KeyframeControl control) {
        if (headControl == control)
            return;

        // Remove from UI
        remove(control);

        if (tailControl == control) {
            tailControl = control.getPrevControl();
            tailControl.setNextControl(null);
        }
        else {
            control.getPrevControl().setNextControl(control.getNextControl());
            control.getNextControl().setPrevControl(control.getPrevControl());
        }

        controlCount--;
    }

    public void setSelection(Keyframe keyframe) {
        // Find corresponding control and select it
        KeyframeControl control = headControl;
        while (control != null) {
            if (control.getKeyframe() == keyframe) {
                setSelection(control);
                return;
            }
            control = control.getNextControl();
        }
    }

    public Keyframe getSelection() {
        return selectedControl.getKeyframe();
    }

    private void setSelection(KeyframeControl control) {
        if (control == selectedControl)
            return;
        KeyframeControl oldControl = selectedControl;
        selectedControl = control;
        control.repaint();
        if (oldControl != null)
            oldControl.repaint();
        fireSelectionChanged();
    }

    public Dimension getMinimumSize() {
        return new Dimension(headControl.getWidth(), headControl.getHeight() * 2 + TRACK_HEIGHT);
    }

    public Dimension getPreferredSize() {
        Dimension size = getMinimumSize();
        size.width = headControl.getWidth() * 10;
        return size;
    }

    /**
     * Useable size of timeline
     */
    private int getTimelineSize() {
        return (int)(getWidth() - (HALF_ICON_WIDTH * 2));
    }

    /**
     * Add listener for keyframe selection change.
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireSelectionChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public void doLayout() {
        // Layout each control based on its Keyframe time.
        // Flip every other control above/below the track.
        boolean isFlipped = false;
        KeyframeControl control = headControl;
        while (control != null) {
            control.setFlipped(isFlipped);
            control.setLocation(control.computePosition(),
                    isFlipped ? control.getHeight() + TRACK_HEIGHT : 0);
            isFlipped = !isFlipped;
            control = control.getNextControl();
        }
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = (int)(getWidth() - HALF_ICON_WIDTH*2);
        int beginTransSize = (int)(beginTransPercent * width);
        int endTransSize = (int)(endTransPercent * width);

        g.setColor(Color.GRAY);
        g.fillRect((int)(HALF_ICON_WIDTH + beginTransSize), headControl.getHeight(), width - (beginTransSize + endTransSize), TRACK_HEIGHT);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect((int)HALF_ICON_WIDTH, headControl.getHeight(), beginTransSize, TRACK_HEIGHT);
        g.fillRect((int)(HALF_ICON_WIDTH + width - endTransSize), headControl.getHeight(), endTransSize, TRACK_HEIGHT);
    }
}
