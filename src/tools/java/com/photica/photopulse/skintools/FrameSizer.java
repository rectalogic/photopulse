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
 
package com.photica.photopulse.skintools;

import com.photica.photopulse.Util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * Utility for resizing Paint Shop Pro "Picture Frames".
 * Provide the source frame size and the size/position (rectangle) of the stage within the source frame image,
 * and the target stage size. Additional source rectangles can be provided.
 * The tool generates the new frame size and the stage rectangle within that resized frame.
 * In PSP, open a *.PspFrame image and use the Crop tool to drag out where the photo should fit.
 * The size and coordinate of the crop tool provide the source frame stage size.
 * Sample usage:
 * com.photica.photopulse.skin.FrameSizer 1800,1200 stage=174,196,1430,827 320,240
 * com.photica.photopulse.skin.FrameSizer 1800,1200 stage=174,196,1430,827 caption=596,13,1195,162 320,240
 */
public class FrameSizer {
    public static void main(String args[]) {
        if (args.length < 3)
            usage();
        Dimension size = Util.parseSize(args[0], ",");
        NamedRectangle stage = parseRect(args[1]);
        Dimension targetSize = Util.parseSize(args[args.length - 1], ",");
        if (size == null || stage == null || !"stage".equals(stage.getName()) || targetSize == null)
            usage();

        String name = "skin" + targetSize.width + "x" + targetSize.height;

        double scaleX = targetSize.getWidth() / stage.getRectangle().getWidth();
        double scaleY = targetSize.getHeight() / stage.getRectangle().getHeight();

        // Skin size
        System.out.println(name + ".size=" + (int)(size.width * scaleX) + "," + (int)(size.height * scaleY));

        // Stage rect
        System.out.print(name + "." + stage.getName() + "=");
        dumpScaledRect(scaleX, scaleY, stage);

        // Parse regions
        System.out.print(name + ".foregrounds=");
        ArrayList<NamedRectangle> regions = new ArrayList<NamedRectangle>();
        for (int argc = 2; argc < args.length - 1; argc++) {
            NamedRectangle rect = parseRect(args[argc]);
            System.out.print(rect.getName());
            if (argc < args.length - 2)
                System.out.print(",");
            regions.add(rect);
        }
        System.out.println();

        // Dump region
        for (int i = 0; i < regions.size(); i++) {
            NamedRectangle rect = regions.get(i);
            System.out.print(name + ".foreground." + rect.getName() + ".region=");
            dumpScaledRect(scaleX, scaleY, rect);
        }
    }

    private static NamedRectangle parseRect(String spec) {
        int index = spec.indexOf("=");
        if (index == -1)
            return null;
        String name = spec.substring(0, index);
        Rectangle rect = Util.parseRect(spec.substring(index+1), true);
        if (rect == null)
            return null;
        return new NamedRectangle(name, rect);
    }

    private static void dumpScaledRect(double scaleX, double scaleY, NamedRectangle namedRect) {
        Rectangle rect = namedRect.getRectangle();
        System.out.println((int)(rect.getX() * scaleX)
                + "," + (int)(rect.getY() * scaleY)
                + "," + (int)(rect.getWidth() * scaleX)
                + "," + (int)(rect.getHeight() * scaleY));
    }

    private static void usage() {
        System.err.println("Usage: FrameSizer <name> <source frame w,h> stage=<source stage x,y,w,h> [[<name>=<source rectangle x,y,w,h>]...] <target stage w,h>");
        System.exit(1);
    }
}

class NamedRectangle {
    private Rectangle rectangle;
    private String name;

    public NamedRectangle(String name, Rectangle rect) {
        this.name = name;
        this.rectangle = rect;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public String getName() {
        return name;
    }
}