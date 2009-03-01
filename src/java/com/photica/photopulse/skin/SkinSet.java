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

package com.photica.photopulse.skin;

import com.photica.photopulse.SystemMessages;
import com.photica.photopulse.Util;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

/**
 * A set of similar skins, but at different sizes
 */
public class SkinSet implements Comparable<SkinSet> {

    static final String SKINMANIFEST = "skin.txt";

    private URI uri;
    private File file;
    private String displayName;
    private String description;
    private ArrayList<Skin> skins = new ArrayList<Skin>();

    /**
     * This constructor is for the BUILTIN SkinSet
     */
    SkinSet(URI uri, String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
        this.uri = uri;
        this.file = null;
    }

    /**
     * Process an image, zip file or directory based skinset
     * @param skinsetFile Image, directory or zip file name in skins directory
     */
    SkinSet(URI uri, File skinsetFile) throws SkinException {
        try {
            this.uri = uri;
            this.file = skinsetFile;

            // Process skinset directory
            if (skinsetFile.isDirectory())
                processSkinSet(skinsetFile, null);
            // Attempt to process as skinset zip file
            else {
                JarFile skinsetJar = new JarFile(skinsetFile, false);
                processSkinSet(skinsetFile, skinsetJar);
            }
        } catch (ZipException e) {
            // Treat as a background or foreground image
            processSkinSet(skinsetFile);
        } catch (IOException e) {
            // Invalid skinset
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_LOADSKIN, e.getMessage()), e);
        }
    }

    /**
     * Process zip file or directory based skinset
     * @param skinsetFile Directory or zip file
     * @param skinsetJar null if skinsetFile is a directory
     */
    private void processSkinSet(File skinsetFile, JarFile skinsetJar) throws SkinException {

        // Load skin properties
        Properties skinsetProperties = new Properties();
        try {
            // Pull manifest from jar file
            if (skinsetJar != null) {
                JarEntry manifest = skinsetJar.getJarEntry(SKINMANIFEST);
                if (manifest == null)
                    throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, SKINMANIFEST));
                InputStream manifestStream = skinsetJar.getInputStream(manifest);
                skinsetProperties.load(manifestStream);
                manifestStream.close();
            }
            // Lookup manifest in directory
            else {
                InputStream is = new BufferedInputStream(new FileInputStream(new File(skinsetFile, SKINMANIFEST)));
                skinsetProperties.load(is);
            }
        } catch (IOException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_LOADSKIN, e.getMessage()), e);
        }

        // Get skinset name, use filename if none
        displayName = skinsetProperties.getProperty("name");
        if (displayName == null)
            displayName = skinsetFile.getName();

        // Get skinset description
        description = skinsetProperties.getProperty("description");

        // Skin properties are of this form:
        // name=Wedding Skin
        // description=Flowery background with silver bells in each corner
        // skins=foo,bar,baz
        //# Optional external flash files to be added (e.g. font files)
        // foo.externals=Arial.fft,BITimesGNewGRoman.fft
        // foo.foregrounds=zoo,zar,zaz
        // foo.foreground.zoo=zipentryname
        // foo.foreground.zoo.region=x,y[,w,h]
        // foo.foreground.zoo.mask=x,y,w,h (cuts a hole)
        // foo.foreground.zoo.rotation=<0-360degrees>
        // foo.foreground.zoo.tile=true|false (tiles only if bitmap, must have [w,h] in region)
        //# zipentry name may begin with "../" to reference files outside the skin zip/directory
        // foo.foreground.zar=zipentryname
        // foo.foreground.zar.region=x,y[,w,h]
        // foo.foreground.zaz=zipentryname
        // foo.foreground.zaz.region=x,y[,w,h]
        // foo.backgrounds=goo
        // foo.background.goo=zipentryname
        // foo.background.goo.region=x,y[,w,h] (must have [w,h] to tile)
        // foo.background.goo.tile=true|false (tiles only if bitmap)
        // foo.size=w,h
        // foo.stage=x,y,w,h
        // foo.name=Display name override - otherwise a name is generated based on stage size
        //
        //# Flash4 absolute path to an MC labeled frame "eventHandler" that can be call()ed.
        //# Frame invoked with "showBegin", "photo0-photoN" and "showEnd" events set in "eventType" var
        //# and "eventArg" var set to additional info
        //# (caption for "photoX" eventType and total photo count for "showBegin" eventType).
        //# Skin element MCs are named with their manifest names.
        // foo.eventHandler=foreground/zar
        //
        //# application/x-www-form-urlencoded JGenerator parameters used by this skin
        //# Names must start with "sp" ("skin param")
        // foo.params=spName1=value1&spName2=value2
        //
        // bar.foregrounds=fee
        // bar.foreground.fee=zipentryname
        // bar.backgrounds=gee
        // bar.background.gee=zipentryname
        // bar.size=w,h
        // bar.stage=x,y,w,h
        // baz...
        //
        // size must be specified for a skin.
        // When tiling, rotation is applied to the tile not the element.


        // Get list of skins
        String skinNames = skinsetProperties.getProperty("skins");
        if (skinNames == null)
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "skins"));

        int validSkinCount = 0;
        StringTokenizer stSkins = new StringTokenizer(skinNames, ",");

        while (stSkins.hasMoreTokens()) {
            String skinName = stSkins.nextToken().trim();

            Dimension skinSize = Util.parseSize(skinsetProperties.getProperty(skinName + ".size"), ",");

            // Invalid skin if no size
            if (skinSize == null)
                continue;

            Rectangle2D stageBounds = Util.parseRect(skinsetProperties.getProperty(skinName + ".stage"));

            String displayName = skinsetProperties.getProperty(skinName + ".name");

            SkinElement[] foregroundElements = processSkinElements(skinsetFile,
                skinsetJar, skinsetProperties,
                skinsetProperties.getProperty(skinName + ".foregrounds"),
                skinName + ".foreground.");

            SkinElement[] backgroundElements = processSkinElements(skinsetFile,
                skinsetJar, skinsetProperties,
                skinsetProperties.getProperty(skinName + ".backgrounds"),
                skinName + ".background.");

            SkinExternal[] externalElements = processSkinExternals(skinsetFile,
                skinsetJar, skinsetProperties.getProperty(skinName + ".externals"));

            Map<String,String> skinParams = parseParams(skinsetProperties.getProperty(skinName + ".params"));

            new Skin(this, skinName, displayName, skinParams, foregroundElements, backgroundElements, externalElements,
                    skinSize, stageBounds, skinsetProperties.getProperty(skinName + ".eventHandler"));
            validSkinCount++;
        }

        if (validSkinCount <= 0)
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "skins"));
    }

    /**
     * Parse application/x-www-form-urlencoded skin parameters
     */
    private Map<String,String> parseParams(String params) throws SkinException {
        if (params == null || params.length() == 0)
            return null;

        Map<String,String> skinParams = new HashMap<String, String>();

        try {
            StringTokenizer st = new StringTokenizer(params, "&");
            while (st.hasMoreTokens()) {
                String param = st.nextToken();
                int eq = param.indexOf("=");
                if (eq == -1)
                    throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "params"));
                String name = URLDecoder.decode(param.substring(0, eq), "UTF-8");
                if (!name.startsWith("sp"))
                    throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "params"));
                String value = URLDecoder.decode(param.substring(eq + 1), "UTF-8");
                skinParams.put(name, value);
            }
        } catch (IllegalArgumentException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "params"));
        } catch (UnsupportedEncodingException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, "params"));
        }

        return skinParams;
    }

    private SkinElement[] processSkinElements(File skinsetFile, JarFile skinsetJar,
            Properties skinProperties, String elementNames, String prefix) throws SkinException {

        if (elementNames == null)
            return null;

        SkinElement[] elements = null;

        StringTokenizer stElements = new StringTokenizer(elementNames, ",");
        int elementCount = stElements.countTokens();
        if (elementCount == 0)
            return null;

        // Collect skin elements
        elements = new SkinElement[elementCount];
        for (int i = 0; i < elementCount; i++) {
            String elementName = stElements.nextToken().trim();

            String elementEntry = skinProperties.getProperty(prefix + elementName);
            // Bail if element was in list but doesn't exist - bad skin
            if (elementEntry == null)
                throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, prefix + elementName));

            // Get the element region
            Rectangle2D elementBounds = Util.parseRect(skinProperties.getProperty(prefix + elementName + ".region"), false);
            if (elementBounds == null)
                elementBounds = new Rectangle();

            // Get the element mask
            Rectangle2D elementMaskBounds = Util.parseRect(skinProperties.getProperty(prefix + elementName + ".mask"), true);

            // Get the element rotation
            double elementRotation = 0.0;
            String rotationString = skinProperties.getProperty(prefix + elementName + ".rotation");
            if (rotationString != null) {
                try {
                    elementRotation = Math.toRadians(Double.parseDouble(rotationString));
                } catch (NumberFormatException e) {
                }
            }

            // Get element tile flag
            boolean isTiled = Boolean.valueOf(skinProperties.getProperty(prefix + elementName + ".tile")).booleanValue();;

            try {
                URL url = processURL(skinsetFile, skinsetJar, elementEntry);
                elements[i] = new SkinElement(elementName, url, elementBounds, elementMaskBounds,
                    elementRotation, isTiled);
            } catch (MalformedURLException e) {
                throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, e.getMessage()));
            }
        }

        return elements;
    }

    private SkinExternal[] processSkinExternals(File skinsetFile, JarFile skinsetJar, String externalNames) throws SkinException {
        if (externalNames == null)
            return null;

        SkinExternal[] externals = null;

        StringTokenizer stExternals = new StringTokenizer(externalNames, ",");
        int externalCount = stExternals.countTokens();
        if (externalCount == 0)
            return null;

        // Collect skin externals
        externals = new SkinExternal[externalCount];
        for (int i = 0; i < externalCount; i++) {
            String externalEntry = stExternals.nextToken().trim();
            try {
                URL url = processURL(skinsetFile, skinsetJar, externalEntry);
                externals[i] = new SkinExternal(url);
            } catch (MalformedURLException e) {
                throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, e.getMessage()));
            }
        }

        return externals;
    }

    private URL processURL(File skinsetFile, JarFile skinsetJar, String entryName) throws MalformedURLException {
        // Java caches jar files internally,
        // so jar: URLs will refer to the same underlying JarFile object

        // Special case entries beginning with "../" - they are paths relative to the zip
        if (skinsetJar == null || entryName.startsWith("../"))
            return new File(skinsetFile, entryName).toURI().toURL();
        else
            return new URL("jar:" + skinsetFile.toURI() + "!/" + entryName);
    }

    // Simple single image file skinset
    private void processSkinSet(File skinsetFile) throws SkinException {
        // Create a skinset named after the file
        displayName = skinsetFile.getName();
        try {
            // The skinset contains one skin with the file as foreground if swf, otherwise as background
            SkinElement[] element = new SkinElement[] { new SkinElement(null, skinsetFile.toURI().toURL()) };
            boolean isForeground = skinsetFile.getName().endsWith(".swf");
            // Name it "image" since there is only one skin
            new Skin(this, "image", null, null,
                    isForeground ? element : null,
                    isForeground ? null : element, null, null, null, null);
        } catch (MalformedURLException e) {
            throw new SkinException(SystemMessages.getMessage(SystemMessages.ERR_BADSKIN, e.getMessage()), e);
        }
    }

    void addSkin(Skin skin) {
        skins.add(skin);
    }

    public List<Skin> getSkins() {
        // init each skin so sizes are valid.
        // Remove invalid skins
        int size = skins.size();
        for (int i = size - 1; i >= 0; i--) {
            Skin skin = skins.get(i);
            try {
                skin.init();
            } catch (SkinException e) {
                skins.remove(i);
            }
        }
        return skins;
    }

    public URI getURI() {
        return uri;
    }

    /**
     * Skinset zip file or directory, or null
     */
    public File getFile() {
        return file;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    // Implements Comparable
    public int compareTo(SkinSet skinSet) {
        return getDisplayName().compareTo(skinSet.getDisplayName());
    }

    public String toString() {
        return getDisplayName();
    }
}