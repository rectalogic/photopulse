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
 
package com.photica.photopulse.ui.wizard;

import com.photica.photopulse.model.ShowModel;
import com.photica.photopulse.model.ShowSelectionModel;
import com.photica.photopulse.wizard.Wizard;
import com.photica.photopulse.wizard.WizardEffect;
import com.photica.photopulse.Branding;
import com.photica.photopulse.skin.SkinManager;

import java.awt.Point;
import java.io.File;
import java.util.BitSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.net.URI;
import java.net.URISyntaxException;

public class WizardPrefs {

    // Preference keys
    private static final String PREF_SAVEDIR = "SaveDir";
    private static final String PREF_SOUNDDIR = "SoundDir";
    private static final String PREF_IMAGEDIR = "ImageDir";
    private static final String PREF_PROJECTDIR = "ProjectDir";
    private static final String PREF_LOCATION = "Location";
    private static final String PREF_BEGINTRANS = "BeginTrans";
    private static final String PREF_EFFECTS = "Effects";
    private static final String PREF_ENDTRANS = "EndTrans";
    private static final String PREF_FIRSTRUN = "FirstRun";
    private static final String PREF_EXPERTMODE= "ExpertMode";
    private static final String PREF_SKINURI= "SkinURI";

    private static final String KEY_SEP = ",";

    private File photoDirectory;
    private File mp3Directory;
    private File exportDirectory;
    private File projectDirectory;
    private boolean isFirstRun;
    private Point location;
    private BitSet begintransSelection;
    private BitSet effectSelection;
    private BitSet endtransSelection;
    private boolean isExpertMode;
    private URI skinURI;

    public WizardPrefs() {
        Preferences prefs = getPreferences();

        isFirstRun = prefs.getBoolean(PREF_FIRSTRUN, true);
        isExpertMode = prefs.getBoolean(PREF_EXPERTMODE, Branding.DEFAULT_EXPERTMODE);

        String pref;

        pref = prefs.get(PREF_IMAGEDIR, null);
        if (pref != null)
            photoDirectory = new File(pref);

        pref = prefs.get(PREF_SOUNDDIR, null);
        if (pref != null)
            mp3Directory = new File(pref);

        pref = prefs.get(PREF_SAVEDIR, null);
        if (pref != null)
            exportDirectory = new File(pref);

        pref = prefs.get(PREF_PROJECTDIR, null);
        if (pref != null) {
            File projDir = new File(pref);
            // contract says always a directory - be paranoid and check to make sure
            // user did not tweak into a file object by hand and correct if possible..
            if( !projDir.isDirectory() ) projDir = projDir.getParentFile();
            if( projDir != null ) {
                projectDirectory = projDir;
            }
        }
        try {
            pref = prefs.get(PREF_LOCATION, null);
            if (pref != null) {
                long loc = Long.parseLong(pref);
                location = new Point((int)(loc >>> 32), (int)(loc & 0xFFFFFFFF));
            }
        } catch (NumberFormatException e) {
        }

        pref = prefs.get(PREF_SKINURI, null);
        if (pref != null) {
            try {
                skinURI = new URI(pref);
            } catch (URISyntaxException e) {
            }
        }

        //XXX deal with old prefs that used integer lists - may be OK, everything turns on

        begintransSelection = decodeSelection(prefs.get(PREF_BEGINTRANS, null), Branding.DEFAULT_BEGINTRANS_INCLUDES, Wizard.LIST_BEGINTRANSITIONS);
        effectSelection = decodeSelection(prefs.get(PREF_EFFECTS, null), Branding.DEFAULT_EFFECT_INCLUDES, Wizard.LIST_EFFECTS);
        endtransSelection = decodeSelection(prefs.get(PREF_ENDTRANS, null), Branding.DEFAULT_ENDTRANS_INCLUDES, Wizard.LIST_ENDTRANSITIONS);
    }

    /**
     * Encode a selection as a string of unselected (excluded) effect keys
     */
    private <WE extends WizardEffect> String encodeSelection(ShowSelectionModel selection, List<WE> effectList) {
        StringBuilder sb = new StringBuilder();
        int count = effectList.size();

        // Store effect keys for effects that are not selected
        for (int i = 0; i < count; i++) {
            if (!selection.isSelectedIndex(i)) {
                sb.append((effectList.get(i)).getKey());
                sb.append(KEY_SEP);
            }
        }

        // Delete trailing comma
        if (sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     * Decode a string of excluded effect keys and return BitSet of selected indices.
     * If effectString is not null, treat it as a list of excluded keys.
     * If effectString is null and defaultEffectString is not null, treat defaultEffectString as
     * a list of (exclusively) included keys. If both are null, include everything.
     * @param effectString String of excluded effect keys, or null or empty
     * @param defaultEffectString String of included effect keys, to be used if effectString is null
     * @param effectList List of WizardEffects
     */
    private <WE extends WizardEffect> BitSet decodeSelection(String effectString, String defaultEffectString, List<WE> effectList) {
        if (effectString == null)
            effectString = defaultEffectString;

        int count = effectList.size();
        BitSet bitset = new BitSet(count);
        // Everything selected by default
        bitset.set(0, count);

        // Everything selected if no exclusions
        if (effectString == null)
            return bitset;

        // StringTokenizer is much faster than String.split(regex)
        StringTokenizer st = new StringTokenizer(effectString, KEY_SEP);

        // Turn off selections for indices in prefs (prefs stores excluded effects)
        while (st.hasMoreTokens()) {
            Integer index = Wizard.findEffectIndex(effectList, st.nextToken());
            if (index != null)
                bitset.clear(index.intValue());
        }

        // If we used the default, flip every bit. The default is a list of included effects, not excluded.
        if (effectString == defaultEffectString)
            bitset.flip(0, bitset.size());

        return bitset;
    }

    public void storePrefs(ShowModel model) {
        Preferences prefs = getPreferences();

        try {
            prefs.putBoolean(PREF_FIRSTRUN, isFirstRun);
            prefs.putBoolean(PREF_EXPERTMODE, isExpertMode);

            // Save directories
            File d = null;
            d = model.getExportDirectory();
            if (d != null)
                prefs.put(PREF_SAVEDIR, d.getAbsolutePath());
            d = model.getMP3Directory();
            if (d != null)
                prefs.put(PREF_SOUNDDIR, d.getAbsolutePath());
            d = model.getPhotoDirectory();
            if (d != null)
                prefs.put(PREF_IMAGEDIR, d.getAbsolutePath());

            if( projectDirectory != null ) {
                prefs.put(PREF_PROJECTDIR, projectDirectory.getAbsolutePath());
            }

            // Save main window location
            if (location != null)
                prefs.putLong(PREF_LOCATION, ((long)location.x << 32) + location.y);

            prefs.put(PREF_SKINURI, SkinManager.getSkinURI(model.getSkin()).toString());

            // Save disabled effects/transitions keys
            prefs.put(PREF_BEGINTRANS, encodeSelection(model.getBeginTransitionSelectionModel(), Wizard.LIST_BEGINTRANSITIONS));
            prefs.put(PREF_EFFECTS, encodeSelection(model.getEffectSelectionModel(), Wizard.LIST_EFFECTS));
            prefs.put(PREF_ENDTRANS, encodeSelection(model.getEndTransitionSelectionModel(), Wizard.LIST_ENDTRANSITIONS));
        } catch (IllegalArgumentException e) {
        } catch (IllegalStateException e) {
        }
    }

    private Preferences getPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void setFirstRun(boolean firstRun) {
        this.isFirstRun = firstRun;
    }

    public boolean isExpertMode() {
        return isExpertMode;
    }

    public void setExpertMode(boolean expertMode) {
        isExpertMode = expertMode;
    }

    public Point getLocation() {
        if (location != null)
            return (Point)location.clone();
        else
            return null;
    }

    public void setLocation(Point location) {
        this.location = (Point)location.clone();
    }

    public URI getSkinURI() {
        return skinURI;
    }

    public File getPhotoDirectory() {
        return photoDirectory;
    }

    public File getMP3Directory() {
        return mp3Directory;
    }

    public File getExportDirectory() {
        return exportDirectory;
    }

    /**
     *
     * @return the project directory - always a directory, never a file
     */
    public File getProjectDirectory() {
        return projectDirectory;
    }
    /**
     * Set the project directory. If the File object is not a directory then its parent
     * is used. The value is not set if dir is null or if dir is not an dir and dir.getParent()
     * is null. This means that getProjectDirectory will in some cases return a
     * value that is different that what was provided to setProjectDirectory.
     * @param dir
     */
    public void setProjectDirectory(File dir) {
        if (dir == null)
            return;
        if (!dir.isDirectory())
            dir = dir.getParentFile();
        if (dir == null)
            return;
        projectDirectory = dir;
    }

    /**
     * The selection BitSet can be modified directly
     */
    public BitSet getBeginTransitionSelection() {
        return begintransSelection;
    }

    /**
     * The selection BitSet can be modified directly
     */
    public BitSet getEffectSelection() {
        return effectSelection;
    }

    /**
     * The selection BitSet can be modified directly
     */
    public BitSet getEndTransitionSelection() {
        return endtransSelection;
    }
}
