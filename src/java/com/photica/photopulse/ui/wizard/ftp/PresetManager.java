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
 
package com.photica.photopulse.ui.wizard.ftp;

import com.photica.photopulse.PhotoPulse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

class PresetManager {
    private static final File PRESETDIR = new File(PhotoPulse.INSTALLDIR, "ftp");
    // Map filename to Preset
    private static Map<String,Preset> KEY2PRESET = null;
    // Sorted by description
    private static Preset[] PRESETS;

    public static Preset[] getPresets() {
        buildPresets();
        return PRESETS;
    }

    public static Preset getPreset(String key) {
        buildPresets();
        return KEY2PRESET.get(key);
    }

    private static void buildPresets() {
        if (KEY2PRESET != null)
            return;

        Map<String,Preset> map = new HashMap<String, Preset>();
        KEY2PRESET = Collections.unmodifiableMap(map);

        try {
            File[] presetFiles = PRESETDIR.listFiles();
            if (presetFiles == null || presetFiles.length == 0)
                return;

            for (int i = 0; i < presetFiles.length; i++) {
                if (!presetFiles[i].getName().endsWith(".ftp"))
                    continue;
                try {
                    Properties props = new Properties();
                    props.load(new FileInputStream(presetFiles[i]));
                    map.put(presetFiles[i].getName(), new Preset(presetFiles[i].getName(), props));
                } catch (IOException e) {
                    // Skip this preset
                    PhotoPulse.logException(e);
                }
            }
        } finally {
            PRESETS = new Preset[KEY2PRESET.size()];
            KEY2PRESET.values().toArray(PRESETS);
            Arrays.sort(PRESETS, new Comparator<Preset>() {
                public int compare(Preset p1, Preset p2) {
                    return (p1).getDescription().compareTo((p2).getDescription());
                }
            });
        }
    }
}
