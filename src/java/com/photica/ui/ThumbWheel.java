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
 
package com.photica.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;


/**
 * ThumbWheel UI component. Draggable scrolling thumbwheel.
 */
public class ThumbWheel extends JComponent {
    public enum Orientation { HORIZONTAL, VERTICAL };

    private static final double HIGHLIGHT_FACTOR = 1.3;
    private static final double FOREGROUND_FACTOR = 1.2;
    private static final double BACKGROUND_FACTOR = 1.0;
    private static final double SHADOW_FACTOR = 0.8;

    /** The current value the thumbwheel represents */
    private double wheelValue;

    /** Diameter of wheel (visible length) */
    private int wheelDiameter;
    /** Thickness of wheel */
    private int wheelThickness;

    private Orientation wheelOrientation;

    /** Image containing strips with each unique wheel position image */
    private BufferedImage wheelImageStrips;

    /** Table of calculated radians, one for each pixel in diameter. Ranges from 0->90->0 in degrees (0->PI/2->0) */
    private double[] radTable;

    /** Table of calculated sines, one for each pixel in diameter.
     * For unit circle, sin is the height (y) of each point on the circle. This is used to adjust the color shading. */
    private double[] sinTable;

    /** Set if state variables (squareCount, stepSize) need to be recalculated */
    private boolean dirtyState;
    /** Total number of squares on entire 360degree wheel (only half would be visible) */
    private int squareCount;
    /** Size of a pixel step along diameter in wheel radians */
    private double stepSize;

    // Used to cache event to avoid allocating
    private ChangeEvent changeEvent = null;
    // Used to avoid allocating each time we need insets
    private Insets insets = new Insets(0,0,0,0);

    public ThumbWheel(Orientation orientation, int diameter, int thickness) {
        setOpaque(true);
        setDoubleBuffered(false);

        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setCursor(Cursor.getPredefinedCursor(orientation == Orientation.HORIZONTAL
                ? Cursor.E_RESIZE_CURSOR : Cursor.N_RESIZE_CURSOR));

        dirtyState = true;
        this.wheelOrientation = orientation;
        setWheelSize(diameter, thickness);

        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    /**
     * This should be called to invalidate internal state when size changes
     */
    private void setWheelSize(int diameter, int thickness) {
        if (diameter != this.wheelDiameter) {
            radTable = sinTable = null;
            dirtyState = true;
            wheelImageStrips = null;
        }
        else if (thickness != this.wheelThickness) {
            dirtyState = true;
            wheelImageStrips = null;
        }
        else
            return;

        this.wheelDiameter = diameter;
        this.wheelThickness = thickness;
    }

    /**
     * Total number of wheel image strips needed
     */
    private int getWheelImageStripCount() {
        // Square width is thickness less 2 pixels top and bottom (4), plus a shadow/highligh pixel left and right
        return wheelThickness - 4 + 2;
    }

    /**
     * Recalculate state variables and tables if invalid.
     * Create array of wheel images if needed.
     */
    private void validateState() {
        if (radTable == null || sinTable == null) {
            assert(dirtyState);

            double radius = (wheelDiameter + 1.0) / 2.0;

            radTable = new double[wheelDiameter];
            sinTable = new double[wheelDiameter];

            for (int i = 0; i < wheelDiameter; i++) {
                double cos;
                // Compute acos angle in radians for left half of wheel (0->90 degrees, cos from 1-> 0)
                if (i <= radius) {
                    cos = (radius - i) / radius;
                    radTable[i] = Math.acos(cos);
                }
                // Compute acos angle in radians for right half of wheel (90-180 degrees, cos from 0-> 1)
                // Subtract from PI (180 degrees) so we range from 90->0
                else {
                    cos = (i - radius) / radius;
                    radTable[i] = Math.PI - Math.acos(cos);
                }

                // sin(t)=opposite/hypotenuse. In this case hypotenuse=1 (unit circle).
                // adjacent=cos and adjacent^2 + opposite^2 = hypotenuse^2
                // So opposite=sqrt(hypotenuse^2 - adjacent^2) so sqrt(1 - adjacent^2)
                // So sin(t)=sqrt(1 - adjacent^2) / 1
                // http://www.mathwords.com/s/sine.htm
                sinTable[i] = Math.sqrt(1.0 - cos * cos);
            }
        }

        if (dirtyState) {
            assert(!(radTable == null || sinTable == null));
            // Compute number of radians in a single pixel in the diameter.
            // If even, take difference between middle two pixels. (*) (*)
            // If odd, take half difference between pixels on either side of center (*) * (*)
            if ((wheelDiameter % 2) == 0)
                stepSize = radTable[wheelDiameter / 2] - radTable[(wheelDiameter / 2) - 1];
            else
                stepSize = (radTable[(wheelDiameter / 2) + 1] - radTable[(wheelDiameter / 2) - 1]) / 2.0;

            // Radians in a square (thickness less 2 pixels top and bottom (4), plus a shadow/highligh pixel left and right)
            double radiansPerSquare = (wheelThickness - 4 + 2) * stepSize;
            // Number of squares in the full wheel circle
            squareCount = (int)Math.floor(((2.0 * Math.PI) / radiansPerSquare) + 0.5);

            dirtyState = false;
        }

        // Create a single image to hold each wheel strip and render each strip into it at the proper offset
        if (wheelImageStrips == null) {
            assert(!dirtyState && !(radTable == null || sinTable == null));
            int imageStripCount = getWheelImageStripCount();
            GraphicsConfiguration gc = this.getGraphicsConfiguration();
            if (gc == null)
                gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            switch (wheelOrientation) {
            case HORIZONTAL:
                wheelImageStrips = gc.createCompatibleImage(wheelDiameter, wheelThickness * imageStripCount, Transparency.OPAQUE);
                break;
            case VERTICAL:
                wheelImageStrips = gc.createCompatibleImage(wheelThickness * imageStripCount, wheelDiameter, Transparency.OPAQUE);
                break;
            }
            for (int i = 0; i < imageStripCount; i++)
                renderWheelImageStrip(wheelImageStrips, i, imageStripCount);
        }
    }

    private void renderWheelImageStrip(BufferedImage wheelImage, int imageStripNumber, int imageCount) {
        int imageStripOffset = imageStripNumber * wheelThickness;

        double radiansPerSquare = (2.0 * Math.PI) / squareCount;
        // Current radian within the current square
        double radian = radiansPerSquare - (radiansPerSquare * ((imageStripNumber - 1) / (double)imageCount));

        Color backgroundColor = getBackground();
        int red = backgroundColor.getRed();
        int green = backgroundColor.getGreen();
        int blue = backgroundColor.getBlue();

        boolean newSquareFirstPixel = true;
        boolean newSquareSecondPixel = false;

        for (int diameterPos = 0; diameterPos < wheelDiameter; diameterPos++) {

            double r = red * sinTable[diameterPos];
            double g = green * sinTable[diameterPos];
            double b = blue * sinTable[diameterPos];
            int highlightRGB = (clamp8Bit(r * HIGHLIGHT_FACTOR) << 16) +
                    (clamp8Bit(g * HIGHLIGHT_FACTOR) << 8) +
                    (clamp8Bit(b * HIGHLIGHT_FACTOR));
            int foregroundRGB = (clamp8Bit(r * FOREGROUND_FACTOR) << 16) +
                    (clamp8Bit(g * FOREGROUND_FACTOR) << 8) +
                    (clamp8Bit(b * FOREGROUND_FACTOR));
            int backgroundRGB = (clamp8Bit(r * BACKGROUND_FACTOR) << 16) +
                    (clamp8Bit(g * BACKGROUND_FACTOR) << 8) +
                    (clamp8Bit(b * BACKGROUND_FACTOR));
            int shadowRGB = (clamp8Bit(r * SHADOW_FACTOR) << 16) +
                    (clamp8Bit(g * SHADOW_FACTOR) << 8) +
                    (clamp8Bit(b * SHADOW_FACTOR));

            int colorRGB;
            if (newSquareFirstPixel) {
                colorRGB = foregroundRGB;
                newSquareFirstPixel = false;
                newSquareSecondPixel = true;
            }
            else {
                if (newSquareSecondPixel) {
                    if (diameterPos < (wheelDiameter * 2 / 3))
                        colorRGB = shadowRGB;
                    else
                        colorRGB = backgroundRGB;
                    newSquareSecondPixel = false;
                }
                else
                    colorRGB = backgroundRGB;
            }

            switch (wheelOrientation) {
            case HORIZONTAL:
                wheelImage.setRGB(diameterPos, imageStripOffset + 0, foregroundRGB);
                wheelImage.setRGB(diameterPos, imageStripOffset + 1, foregroundRGB);
                wheelImage.setRGB(diameterPos, imageStripOffset + 2, newSquareSecondPixel ? foregroundRGB : shadowRGB);

                for (int thicknessPos = 2; thicknessPos < (wheelThickness - 2); thicknessPos++)
                    wheelImage.setRGB(diameterPos, imageStripOffset + thicknessPos, colorRGB);

                wheelImage.setRGB(diameterPos, imageStripOffset + wheelThickness - 3, newSquareSecondPixel ? foregroundRGB : backgroundRGB);
                wheelImage.setRGB(diameterPos, imageStripOffset + wheelThickness - 2, foregroundRGB);
                wheelImage.setRGB(diameterPos, imageStripOffset + wheelThickness - 1, foregroundRGB);
                break;
            case VERTICAL:
                wheelImage.setRGB(imageStripOffset + 0, diameterPos, foregroundRGB);
                wheelImage.setRGB(imageStripOffset + 1, diameterPos, foregroundRGB);
                wheelImage.setRGB(imageStripOffset + 2, diameterPos, newSquareSecondPixel ? foregroundRGB : shadowRGB);

                for (int thicknessPos = 2; thicknessPos < (wheelThickness - 2); thicknessPos++)
                    wheelImage.setRGB(imageStripOffset + thicknessPos, diameterPos, colorRGB);

                wheelImage.setRGB(imageStripOffset + wheelThickness - 3, diameterPos, newSquareSecondPixel ? foregroundRGB : backgroundRGB);
                wheelImage.setRGB(imageStripOffset + wheelThickness - 2, diameterPos, foregroundRGB);
                wheelImage.setRGB(imageStripOffset + wheelThickness - 1, diameterPos, foregroundRGB);
                break;
            }

            if (diameterPos < wheelDiameter - 1) {
                radian += radTable[diameterPos + 1] - radTable[diameterPos];
                if (radian > radiansPerSquare) {
                    colorRGB = 0;
                    if (diameterPos > (wheelDiameter * 2 / 3))
                        colorRGB = highlightRGB;
                    else if (diameterPos > (wheelDiameter / 3))
                        colorRGB = foregroundRGB;

                    if (colorRGB != 0) {
                        switch (wheelOrientation) {
                        case HORIZONTAL:
                            for (int thicknessPos = 3; thicknessPos < (wheelThickness - 2); thicknessPos++)
                                wheelImage.setRGB(diameterPos, imageStripOffset + thicknessPos, colorRGB);
                            break;
                        case VERTICAL:
                            for (int thicknessPos = 3; thicknessPos < (wheelThickness - 2); thicknessPos++)
                                wheelImage.setRGB(imageStripOffset + thicknessPos, diameterPos, colorRGB);
                            break;
                        }
                    }

                    radian = radian % radiansPerSquare;
                    newSquareFirstPixel = true;
                }
            }
        }
    }

    private int clamp8Bit(double value) {
        return value > 255 ? 255 : (int)Math.floor(value);
    }

    private int clampPos(int pos) {
        if (pos < 0)
            pos = 0;
        if (pos >= wheelDiameter)
            pos = wheelDiameter - 1;
        return pos;
    }

    /**
     * Return the image strip index needed to display the specified value.
     */
    private int findDisplayWheelImageStrip(double value) {
        double squareRange = (2.0 * Math.PI) / squareCount;
        double normalizedModValue = (value % squareRange) / squareRange;
        if (normalizedModValue < 0.0)
            normalizedModValue += 1.0;
        return (int)(normalizedModValue * getWheelImageStripCount());
    }

    /**
     * Compute wheel value based on users mouse position.
     * @param startValue Original wheel value when interaction began
     * @param startPos Original mouse position when interaction began
     * @param currentPos Current mouse position
     * @return New wheel value
     */
    private double computeWheelValue(double startValue, int startPos, int currentPos) {
        double delta;
        // If we are off the wheel, use uniform scrolling
        if (currentPos < 0)
            delta = (stepSize * (currentPos - 0)) - radTable[0] - radTable[startPos];
        else if (currentPos >= wheelDiameter)
            delta = (stepSize * (currentPos - wheelDiameter)) + radTable[wheelDiameter - 1] - radTable[startPos];
        // Otherwise use realistic scrolling - the users mouse stays over the same wheelpoint
        else
            delta = radTable[currentPos] - radTable[startPos];

        return startValue + delta;
    }

    public void setWheelValue(double value, boolean notifyListeners) {
        if (wheelValue == value)
            return;
        wheelValue = value;
        repaint();
        if (notifyListeners)
            fireValueChanged();
    }

    public double getWheelValue() {
        return wheelValue;
    }

    public Orientation getWheelOrientation() {
        return wheelOrientation;
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    private void fireValueChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public Dimension getPreferredSize() {
        insets = getInsets(insets);
        switch (wheelOrientation) {
        case HORIZONTAL:
            return new Dimension(wheelDiameter + insets.left + insets.right, wheelThickness + insets.top + insets.bottom);
        case VERTICAL:
            return new Dimension(wheelThickness + insets.top + insets.bottom, wheelDiameter + insets.left + insets.right);
        default:
            return super.getPreferredSize();
        }
    }

    protected void paintComponent(Graphics g) {
        insets = getInsets(insets);
        switch (wheelOrientation) {
        case HORIZONTAL:
            setWheelSize(getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
            break;
        case VERTICAL:
            setWheelSize(getHeight() - (insets.top + insets.bottom), getWidth() - (insets.left + insets.right));
            break;
        }

        validateState();

        // Find the image strip to render the current value, and draw that segment of the image
        int imageStrip = findDisplayWheelImageStrip(getWheelValue());
        switch (wheelOrientation) {
        case HORIZONTAL:
            g.drawImage(wheelImageStrips,
                    insets.left, insets.top,
                    insets.left + wheelDiameter, insets.top + wheelThickness,
                    0, imageStrip * wheelThickness,
                    wheelDiameter, imageStrip * wheelThickness + wheelThickness,
                    this);
            break;
        case VERTICAL:
            g.drawImage(wheelImageStrips,
                    insets.left, insets.top,
                    insets.left + wheelThickness, insets.top + wheelDiameter,
                    imageStrip * wheelThickness, 0,
                    imageStrip * wheelThickness + wheelThickness, wheelDiameter,
                    this);
            break;
        }
    }

    private class MouseHandler implements MouseListener, MouseMotionListener {
        private boolean isDragging = false;
        private int startPos;
        private double startValue;

        public void mouseClicked(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e) || !isEnabled())
                return;

            validateState();

            insets = getInsets(insets);
            startPos = clampPos(wheelOrientation == Orientation.HORIZONTAL ? insets.left + e.getX() : insets.top + e.getY());
            startValue = getWheelValue();
            isDragging = true;
        }

        public void mouseReleased(MouseEvent e) {
            if (!isDragging)
                return;
            setWheelValue(computeWheelValue(startValue, startPos,
                    wheelOrientation == Orientation.HORIZONTAL ? insets.left + e.getX() : insets.top + e.getY()),
                    true);
            isDragging = false;
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}

        public void mouseDragged(MouseEvent e) {
            if (!isDragging)
                return;
            setWheelValue(computeWheelValue(startValue, startPos,
                    wheelOrientation == Orientation.HORIZONTAL ? insets.left + e.getX() : insets.top + e.getY()),
                    true);
        }

        public void mouseMoved(MouseEvent e) {}
    }
}
