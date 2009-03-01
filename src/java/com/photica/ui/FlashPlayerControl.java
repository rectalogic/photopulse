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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.io.File;

/**
 * Host native FlashPlayerControl HWND.
 */
public class FlashPlayerControl extends Canvas {

    static {
        // Build path to jawt.dll library
        String jawtLibPath = null;
        String javaHome = System.getProperty("java.home");
        if (javaHome != null)
            jawtLibPath = javaHome + File.separator + "bin" + File.separator + "jawt.dll";

        try {
            System.loadLibrary("FlashPlayerControlJNI");
            initializeControl(jawtLibPath);
            jniLinked = true;
        } catch (UnsatisfiedLinkError e) {
            jniLinked = false;
        }
    }

    private static boolean jniLinked;

    // Native code stores Flash HWND here.
    // Renaming this field requires native code changes.
    private int hwndControl = 0;

    private byte[] movieBuf;
    private int movieBufOffset;
    private int movieBufLength;

    private String errorMessage;

    /**
     * The movie will be loaded and start playing when the control is shown (native HWND created).
     * @param movieSize Preferred size of movie
     * @param movieBuf SWF bytes
     * @param offset Offset into array
     * @param length Number of bytes to use
     * @param errorMessage Message to display if we fail
     */
    public FlashPlayerControl(Dimension movieSize, byte[] movieBuf, int offset, int length, String errorMessage) {
        setPreferredSize(movieSize);
        this.movieBuf = movieBuf;
        this.movieBufOffset = offset;
        this.movieBufLength = length;
        this.errorMessage = errorMessage;
    }

    public void addNotify() {
        super.addNotify();

        if (jniLinked && attachControl())
            loadMovie(movieBuf, movieBufOffset, movieBufLength);

        // Native control copies the bytes
        movieBuf = null;
        movieBufOffset = 0;
        movieBufLength = 0;
    }

    public void paint(Graphics g) {
        if (hwndControl != 0)
            return;
        super.paint(g);
        Rectangle2D rect = g.getFontMetrics().getStringBounds(errorMessage, g);
        g.drawString(errorMessage, (int)(rect.getX() + (getWidth() - rect.getWidth())/2),
                (int)(rect.getY() + (getHeight() - rect.getHeight())/2));
    }

    /**
     * Resize native control to match our size
     */
    @Deprecated
    public void reshape(int x, int y, int width, int height) {
        super.reshape(x, y, width, height);
        if (hwndControl != 0)
            resizeControl(width, height);
    }

    /**
     * Load the Flash movie bytes.
     * This will not work until after the native control has been created in addNotify().
     */
    private void loadMovie(byte[] swf, int offset, int length) {
        if (offset + length > swf.length)
            throw new ArrayIndexOutOfBoundsException("offset + length > array length");
        if (hwndControl != 0)
            loadMovieControl(swf, offset, length);
    }

    private static native void initializeControl(String jawtLibPath);
    private native boolean attachControl();
    private native void resizeControl(int width, int height);
    private native void loadMovieControl(byte[] swf, int offset, int length);
}
