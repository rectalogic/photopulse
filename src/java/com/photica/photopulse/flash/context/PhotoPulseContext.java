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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.photica.photopulse.flash.context;

import com.iv.flash.context.Context;
import com.iv.flash.context.XMLContext;
import com.photica.photopulse.flash.context.MP3Data;
import com.photica.photopulse.flash.output.LazyGenerator;
import com.photica.photopulse.imageio.ImageTranscoder;

import java.util.List;
import org.w3c.dom.Node;

/**
 * Root context, exposing DOM Document and MP3 data
 */
public class PhotoPulseContext extends XMLContext {
    private XMLContext contextDelegate;
    private String eventHandler;
    private MP3Data mp3Data;
    private ImageTranscoder transcoder;
    private String mp3FileName;
    private int mp3FrameCount;
    private LazyGenerator lazyGenerator;
    private boolean isBranded;
    private boolean isHighQuality;

    /**
     *
     * @param ctxParent
     * @param node
     * @param eventHandler Absolute Flash4 pathname of MC that has a frame labeled "eventHandler" to handle events.
     * @param mp3Data Pass null if no MP3 or disabled or external
     * @param mp3FileName Name of external MP3 - pass null if no MP3 or internal
     * @param mp3FrameCount Count of external MP3 frames - 0 if no MP3 or internal
     * @param transcoder Used to scale/crop photos
     * @param lazyGenerator Lazy bitmap generator or null
     * @param isBranded true if branding should be applied to the show (photopulse splash branding)
     * @param isHighQuality true if show should be exported in high quality
     */
    public PhotoPulseContext(Context ctxParent, Node node, String eventHandler,
            MP3Data mp3Data, String mp3FileName, int mp3FrameCount, ImageTranscoder transcoder,
            LazyGenerator lazyGenerator, boolean isBranded, boolean isHighQuality) {
        super(ctxParent, node);
        contextDelegate = XMLContext.newXMLContext(ctxParent, node);
        this.eventHandler = eventHandler;
        this.mp3Data = mp3Data;
        this.mp3FileName = mp3FileName;
        this.mp3FrameCount = mp3FrameCount;
        this.transcoder = transcoder;
        this.lazyGenerator = lazyGenerator;
        this.isBranded = isBranded;
        this.isHighQuality = isHighQuality;
    }

    public MP3Data getMP3Data() {
        return mp3Data;
    }

    public String getMP3FileName() {
        return mp3FileName;
    }

    public int getMP3FrameCount() {
        return mp3FrameCount;
    }

    public String getEventHandler() {
        return eventHandler;
    }

    public ImageTranscoder getTranscoder() {
        return transcoder;
    }

    public LazyGenerator getLazyGenerator() {
        return lazyGenerator;
    }

    public boolean isBranded() {
        return isBranded;
    }

    public boolean isHighQuality() {
        return isHighQuality;
    }

    public String getValue(String strPath) {
        return contextDelegate.getValue(strPath);
    }

    public List getValueList(String strPath) {
        return contextDelegate.getValueList(strPath);
    }

    public static PhotoPulseContext findContext(Context ctx) {
        // Search for parent PhotoPulseContext
        while (ctx != null && !(ctx instanceof PhotoPulseContext))
            ctx = ctx.getParent();
        return (PhotoPulseContext)ctx;
    }
}