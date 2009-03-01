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


import com.iv.flash.api.FlashFile;
import com.iv.flash.api.Script;
import com.iv.flash.api.sound.MP3SoundStreamBlock;
import com.iv.flash.api.sound.SoundStreamBuilder;
import com.iv.flash.api.sound.SoundStreamHead;
import com.iv.flash.parser.DataMarker;
import com.iv.flash.util.FlashBuffer;
import com.iv.flash.util.FlashOutput;
import com.iv.flash.util.GeomHelper;
import com.iv.flash.util.IVException;
import com.iv.flash.util.PropertyManager;
import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.Util;
import com.photica.photopulse.flash.ShowGenerator;
import com.photica.photopulse.model.MP3;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Load and validate an MP3 or M3U playlist, and make its duration available.
 * M3U spec see http://hanna.pyxidis.org/tech/m3u.html
 * http://forums.winamp.com/showthread.php?s=dbec47f3a05d10a3a77959f17926d39c&threadid=65772
 */
public class MP3Data {

    public static final String EXTENSION_M3U = ".m3u";

    private float frameRate;
    private int frameCount;
    // List of MP3Sound for each mp3 in playlist
    private LinkedList<MP3Sound> mp3List = new LinkedList<MP3Sound>();
    private MP3 mp3;

    public MP3Data(float frameRate, File file) throws IOException, IVException, URISyntaxException {
        this.frameRate = frameRate;
        loadURI(file.toURI());
        frameCount = computeMP3Length();
        // Build model MP3 with duration
        mp3 = new MP3(file, (double)frameCount / frameRate);
    }

    private int computeMP3Length() throws IVException {
        int length = 0;
        Iterator<MP3Sound> iter = iterateMP3s();
        while (iter.hasNext()) {
            MP3Sound mp3Blocks = iter.next();
            length += mp3Blocks.getSoundStreamBlockCount();
        }

        // Validate MP3 length
        if (length > ShowGenerator.MAXFRAMES) {
            throw new IVException(SystemMessages.getBundle(), SystemMessages.ERR_MP3LENGTH,
                    new Object[] { com.photica.photopulse.Util.formatDuration((double)length / frameRate),
                                   com.photica.photopulse.Util.formatDuration((double)ShowGenerator.MAXFRAMES / frameRate) });
        }

        return length;
    }

    // Load m3u or mp3
    private void loadURI(URI uri) throws IOException, IVException, URISyntaxException {
        if (uri.getPath().endsWith(EXTENSION_M3U))
            loadM3U(uri);
        else {
            mp3List.add(new MP3Sound(uri, frameRate));
        }
    }

    // m3u may contain relative or absolute URIs to mp3s or other m3us
    private void loadM3U(URI m3uURI) throws IOException, IVException, URISyntaxException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(m3uURI.toURL().openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore comments (deals with EXTM3U format files too)
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;

                // If the m3u is a file, the line could be a relative or absolute file, or a URI
                if ("file".equals(m3uURI.getScheme())) {
                    File file = new File(line);
                    if (file.isAbsolute() && file.exists()) {
                        loadURI(file.toURI());
                        continue;
                    }
                    else {
                        // Attempt to resolve file relative to m3u, fall through if fails
                        file = new File(new File(m3uURI).getParentFile(), line);
                        if (file.exists()) {
                            loadURI(file.toURI());
                            continue;
                        }
                    }
                }

                // Fall through from above and treat as URI if it couldn't be parsed as a File.
                // We want to try File first above to avoid filenames like "f%2dck" that contain valid URI escapes.
                URI uri = new URI(line);
                if (uri.isAbsolute())
                    loadURI(uri);
                else
                    loadURI(m3uURI.resolve(uri));
            }
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    public MP3 getMP3() {
        return mp3;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public int getFrameCount() {
        return frameCount;
    }

    /**
     * Iterate over MP3Sound objects, one per MP3
     */
    public Iterator<MP3Sound> iterateMP3s() {
        return mp3List.iterator();
    }

    /**
     * Number of MP3Sound objects (MP3s)
     */
    public int getMP3Count() {
        return mp3List.size();
    }

    /**
     * Add MP3 data to script starting at frameNum
     * @return the frame after the last frame containing MP3 data
     */
    public int addMP3Frames(Script script, int frameNum) {
        Iterator<MP3Sound> iterMP3 = iterateMP3s();
        while (iterMP3.hasNext()) {
            MP3Sound mp3Blocks = iterMP3.next();
            script.getFrameAt(frameNum).addFlashObject(mp3Blocks.getSoundStreamHead());
            Iterator<MP3SoundStreamBlock> iterBlocks = mp3Blocks.iterateSoundStreamBlocks();
            while (iterBlocks.hasNext()) {
                script.getFrameAt(frameNum).addFlashObject(iterBlocks.next());
                frameNum++;
            }
        }
        return frameNum;
    }

    /**
     *  Create and generate SWF containing the MP3 for external MP3 swfs.
     */
    public FlashOutput createExternalSWF() throws IVException {
        // Create empty swf FlashFile
        FlashFile ffMP3 = new FlashFile();
        ffMP3.setVersion(4);
        ffMP3.setFrameSize(GeomHelper.newRectangle(0, 0, ShowGenerator.TWIPS_PER_PIXEL*ShowGenerator.TWIPS_PER_PIXEL,
                ShowGenerator.TWIPS_PER_PIXEL*ShowGenerator.TWIPS_PER_PIXEL));
        ffMP3.setFrameRate(Util.convertFrameRate(getFrameRate()));
        ffMP3.setFileSize(100);
        ffMP3.setEncoding(PropertyManager.defaultEncoding);
        // Set these so FlashFile.getFullName() doesn't crash.
        ffMP3.setFileName("mp3");
        ffMP3.setFileDir("");

        // Add main single frame script
        Script script = new Script(1);
        script.setMain();
        ffMP3.setMainScript(script);

        // Add mp3 data
        // Later processing may insert empty frames at beginning
        int nEndFrame = addMP3Frames(script, 0);

        // Stop mp3 on first and last frame
        script.getFrameAt(0).addStopAction();
        script.getFrameAt(nEndFrame - 1).addStopAction();

        return ffMP3.generate();
    }

    /**
     * @return Compute external swf filename based on host filename (which could be exe, html etc.)
     */
    public static String computeFileName(File file) {
        return com.photica.photopulse.Util.getBaseName(file) + "-mp3.swf";
    }

    /**
     * Manages block list data for a single MP3.
     */
    public static class MP3Sound {
        private URI uri;
        // Total byte size of this mp3
        private int byteCount = 0;
        private SoundStreamHead soundStreamHead;
        // List of MP3SoundStreamBlock
        private LinkedList<MP3SoundStreamBlock> streamBlocksList = new LinkedList<MP3SoundStreamBlock>();

        MP3Sound(URI uri, float frameRate) throws IOException, IVException {
            this.uri = uri;
            byte[] buf = readFully(uri);
            FlashBuffer fb = new FlashBuffer(buf);
            // Must pass SWF format framerate
            SoundStreamBuilder ssb = SoundStreamBuilder.newSoundStreamBuilder(fb,
                Util.convertFrameRate(frameRate));
            soundStreamHead = ssb.getSoundStreamHead();
            MP3SoundStreamBlock block;
            while ((block = (MP3SoundStreamBlock)ssb.getNextSoundStreamBlock()) != null)
                addSoundStreamBlock(block);
        }

        private void addSoundStreamBlock(MP3SoundStreamBlock block) {
            streamBlocksList.add(block);
            byteCount += block.data.length();
        }

        SoundStreamHead getSoundStreamHead() {
            return soundStreamHead;
        }

        /**
         * Total byte size of mp3
         */
        public int getSoundStreamByteCount() {
            return byteCount;
        }

        /**
         * Iterator of DataMarker block buffers
         */
        public Iterator<DataMarker> iterateSoundStreamBytes() {
            return new Iterator<DataMarker>() {
                private Iterator<MP3SoundStreamBlock> iter = iterateSoundStreamBlocks();
                public boolean hasNext() {
                    return iter.hasNext();
                }

                public DataMarker next() {
                    // Note: MP3SoundStreamBlock contains a hiding definition of SoundStreamBlock.data
                    return iter.next().data;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        int getSoundStreamBlockCount() {
            return streamBlocksList.size();
        }

        /**
         * Iterator of SoundStreamBlocks
         */
        Iterator<MP3SoundStreamBlock> iterateSoundStreamBlocks() {
            return streamBlocksList.iterator();
        }

        public URI getURI() {
            return uri;
        }

        // Read contents fully into memory
        private byte[] readFully(URI uri) throws IOException {
            if ("file".equals(uri.getScheme())) {
                RandomAccessFile raf = null;
                try {
                    raf = new RandomAccessFile(new File(uri), "r");
                    byte[] fileBuf = new byte[(int)raf.length()];
                    raf.readFully(fileBuf);
                    return fileBuf;
                } finally {
                    if (raf != null)
                        raf.close();
                }
            }
            else {
                InputStream is = null;
                try {
                    URLConnection conn = uri.toURL().openConnection();
                    is = conn.getInputStream();
                    int length = conn.getContentLength();
                    if (length != -1) {
                        byte[] buf = new byte[length];
                        int pos = 0;
                        int count = 0;
                        while (pos < length && (count = is.read(buf, pos, length - pos)) != -1) {
                            pos += count;
                        }
                        // Check if we really got Content-Length bytes?
                        return buf;
                    }
                    else {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream(512 * 1024);
                        is = new BufferedInputStream(is);
                        int b;
                        while ((b = is.read()) != -1)
                            bos.write(b);
                        return bos.toByteArray();
                    }
                } finally {
                    if (is != null)
                        is.close();
                }
            }
        }
    }
}