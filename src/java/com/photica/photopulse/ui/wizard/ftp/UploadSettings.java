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

package com.photica.photopulse.ui.wizard.ftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;


// Holds FTP settings, loads/saves from prefs
public class UploadSettings implements Cloneable {

    public static final String LOGIN_PROPERTY = "login";
    public static final String PRESET_PROPERTY = "preset";

    private PropertyChangeSupport changeSupport;

    private String login;
    private boolean savePassword;
    private String password;
    private Preset preset;

    private Preferences prefs = Preferences.userNodeForPackage(getClass());

    private static final String PREF_LOGIN = "login";
    private static final String PREF_SAVEPASSWORD = "savepassword";
    private static final String PREF_PASSWORD = "password";

    private static final String PREF_PRESETKEY = "preset";

    private static final String PREF_HOSTNAME = "hostname";
    private static final String PREF_PORT = "port";
    private static final String PREF_DIRECTORY = "directory";
    private static final String PREF_VIEWURLTEMPLATE = "urlprefix";

    public UploadSettings() {
        String presetKey = prefs.get(PREF_PRESETKEY, null);

        // If no preset key, construct Preset from individual prefs
        if (presetKey == null) {
            String hostname = prefs.get(PREF_HOSTNAME, null);
            int port = prefs.getInt(PREF_PORT, Preset.DEFAULT_PORT);
            String directory = prefs.get(PREF_DIRECTORY, null);
            String urlTemplate = prefs.get(PREF_VIEWURLTEMPLATE, null);

            preset = new Preset(hostname, port, directory, urlTemplate);
            loadCredentials();
        }
        // If we have a preset key, retrieve the corresponding Preset.
        // If it's gone, use an empty custom preset and do not load credentials.
        else {
            preset = PresetManager.getPreset(presetKey);
            if (preset != null)
                loadCredentials();
            else
                preset = new Preset();
        }

        savePassword = prefs.getBoolean(PREF_SAVEPASSWORD, false);
    }

    private void loadCredentials() {
        login = prefs.get(PREF_LOGIN, null);

        byte[] passwordBytes = prefs.getByteArray(PREF_PASSWORD, null);
        if (passwordBytes != null) {
            passwordBytes = decryptPassword(login, passwordBytes);
            if (passwordBytes != null)
                password = new String(passwordBytes);
        }
    }

    // Overrides Object
    public UploadSettings clone() {
        try {
            UploadSettings clone = (UploadSettings)super.clone();
            // Don't clone listeners
            clone.changeSupport = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    public void storePrefs() {
        // If we have a Preset, store it's key or individual prefs if custom
        if (preset.getKey() != null) {
            storePref(PREF_PRESETKEY, preset.getKey());
            prefs.remove(PREF_HOSTNAME);
            prefs.remove(PREF_PORT);
            prefs.remove(PREF_DIRECTORY);
            prefs.remove(PREF_VIEWURLTEMPLATE);
        }
        else {
            prefs.remove(PREF_PRESETKEY);
            storePref(PREF_HOSTNAME, preset.getHostname());
            if (preset.getPort() == Preset.DEFAULT_PORT)
                prefs.remove(PREF_PORT);
            else
                prefs.putInt(PREF_PORT, preset.getPort());
            storePref(PREF_DIRECTORY, preset.getDirectory());
            storePref(PREF_VIEWURLTEMPLATE, preset.getViewURLTemplate());
        }

        storePref(PREF_LOGIN, login);

        // Do not store password if user doesn't want to
        prefs.putBoolean(PREF_SAVEPASSWORD, savePassword);
        byte[] passwordBytes = null;
        if (savePassword && password != null)
            passwordBytes = encryptPassword(login, password.getBytes());

        if (passwordBytes != null)
            prefs.putByteArray(PREF_PASSWORD, passwordBytes);
        else
            prefs.remove(PREF_PASSWORD);

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
        }
    }

    private void storePref(String key, String value) {
        if (value == null || value.length() == 0)
            prefs.remove(key);
        else
            prefs.put(key, value);
    }

    /**
     * Return true if we have all we need to FTP upload
     */
    public boolean isValid() {
        return preset.getHostname() != null && login != null && password != null;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        String oldLogin = this.login;
        this.login = login;
        firePropertyChange(LOGIN_PROPERTY, oldLogin, login);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSavePassword() {
        return savePassword;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    public Preset getPreset() {
        return preset;
    }

    public void setPreset(Preset preset) {
        Preset oldPreset = this.preset;
        this.preset = preset;
        firePropertyChange(PRESET_PROPERTY, oldPreset, preset);
    }


    private byte[] encryptPassword(String login, byte[] passwordBytes) {
        try {
            // Encrypt
            cryptPassword(login, passwordBytes);

            // Compress encrypted password
            ByteArrayOutputStream baos = new ByteArrayOutputStream(passwordBytes.length);
            DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_COMPRESSION, true), passwordBytes.length);
            dos.write(passwordBytes);
            dos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    private byte[] decryptPassword(String login, byte[] passwordBytes) {
        try {
            // Since we use Inflater 'nowrap', have to provide extra dummy byte at end of input
            byte[] abInput = new byte[passwordBytes.length + 1];
            System.arraycopy(passwordBytes, 0, abInput, 0, passwordBytes.length);

            // Decompress
            Inflater inflater = new Inflater(true);
            inflater.setInput(abInput);

            ByteArrayOutputStream baos = new ByteArrayOutputStream(passwordBytes.length);
            // Reuse passwordBytes as output buffer
            int length = 0;
            while ((length = inflater.inflate(passwordBytes)) > 0) {
                baos.write(passwordBytes, 0, length);
            }
            baos.close();
            byte[] abInflated = baos.toByteArray();

            // Decrypt decompressed password
            cryptPassword(login, abInflated);
            return abInflated;
        } catch (IOException e) {
            return null;
        } catch (DataFormatException e) {
            return null;
        }
    }

    private void cryptPassword(String login, byte[] passwordBytes) {
        int hash = 0xdeadbeef;
        if (login != null)
            hash = login.hashCode();

        // XOR key with hash, use this to XOR with password bytes
        long lKey = 0xa3b291d4e5f687c7L ^ (((long)hash << 32) | ((long)hash & 0xffffffffL));

        long mask = 0xff;
        int keyShift = 0;
        for (int i = 0; i < passwordBytes.length; i++) {
            byte bytKey = (byte)(((mask << keyShift) & lKey) >>> keyShift);
            keyShift = (keyShift + 8) % 64;
            passwordBytes[i] = (byte)(passwordBytes[i] ^ bytKey);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null)
            return;
        if (changeSupport == null)
            changeSupport = new PropertyChangeSupport(this);

        changeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener == null)
            return;
        if (changeSupport == null)
            changeSupport = new PropertyChangeSupport(this);

        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (listener == null || changeSupport == null)
            return;
        changeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeSupport changeSupport = this.changeSupport;
        if (changeSupport == null || (oldValue != null && newValue != null && oldValue.equals(newValue)))
            return;
        changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
}
