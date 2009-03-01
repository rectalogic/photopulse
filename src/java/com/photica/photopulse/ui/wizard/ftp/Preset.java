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

import com.photica.photopulse.Util;
import com.photica.photopulse.ui.wizard.UIMessages;

import java.io.IOException;
import java.util.Properties;

/**
 * FTP hosting provider presets.
 * Immutable.
 */
public class Preset {
    public static final int DEFAULT_PORT = 21;

    private static final String PROP_DEFAULT = "default";
    private static final String PROP_DESCRIPTION = "description";
    private static final String PROP_SIGNUPURL = "signupURL";
    private static final String PROP_HOSTNAME = "hostname";
    private static final String PROP_PORT = "port";
    private static final String PROP_DIRECTORY = "directory";
    private static final String PROP_VIEWURLTEMPLATE = "viewURLTemplate";
    private static final String PROP_HOOKURLTEMPLATE = "hookURLTemplate";
    private static final String PROP_MANAGEURLTEMPLATE = "manageURLTemplate";

    private static final String PATTERN_USERNAME = "${userName}";
    private static final String PATTERN_SHOWNAME = "${showName}";
    private static final String PATTERN_SHOWEXT = "${showExt}";

    private static final String CUSTOM_DESCRIPTION = UIMessages.getMessage(UIMessages.UI_FTPS_CUSTOMDESC);

    private String key;
    private boolean isDefault;
    private String description;
    private String signupURL;
    private String hostname;
    private int port = DEFAULT_PORT;
    private String directory;
    private String viewURLTemplate;
    private String hookURLTemplate;
    private String manageURLTemplate;

    public Preset(String key, Properties props) throws IOException {
        this.key = key;
        isDefault = Boolean.valueOf(props.getProperty(PROP_DEFAULT)).booleanValue();
        description = props.getProperty(PROP_DESCRIPTION);
        hostname = props.getProperty(PROP_HOSTNAME);
        signupURL = props.getProperty(PROP_SIGNUPURL);
        if (description == null || hostname == null)
            throw new IOException(key);

        String p = props.getProperty(PROP_PORT);
        if (p != null) {
            try {
                port = Integer.parseInt(p);
            } catch (NumberFormatException e) {
                throw new IOException(key);
            }
        }
        else
            port = DEFAULT_PORT;

        directory = props.getProperty(PROP_DIRECTORY);
        viewURLTemplate = props.getProperty(PROP_VIEWURLTEMPLATE);
        hookURLTemplate = props.getProperty(PROP_HOOKURLTEMPLATE);
        manageURLTemplate = props.getProperty(PROP_MANAGEURLTEMPLATE);
    }

    // For constructing custom preset
    public Preset(String hostname, int port, String directory, String viewURLTemplate) {
        this.description = CUSTOM_DESCRIPTION;
        this.hostname = hostname;
        this.port = port;
        this.directory = directory;
        this.viewURLTemplate = viewURLTemplate;
        // Make this a default preset if non-empty
        this.isDefault = hostname != null;
    }

    // For constructing custom preset
    public Preset() {
        this(null, DEFAULT_PORT, null, null);
    }

    public boolean isCustom() {
        return key == null;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public String getSignupURL() {
        return signupURL;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getDirectory() {
        return directory;
    }

    /**
     * FTP directory.
     * May contain ${userName} substitution var.
     */
    public String populateDirectory(String login) {
        if (directory == null)
            return null;
        Util.encodeURL(login);
        StringBuilder template = new StringBuilder(directory);
        if (!replacePattern(template, PATTERN_USERNAME, login))
            return directory;
        else
            return template.toString();
    }

    /**
     * Template URL use to view uploaded show.
     * May contain ${showName}, ${showExt} and ${userName} template parameters.
     */
    public String getViewURLTemplate() {
        return viewURLTemplate;
    }

    public String populateURLTemplate(String template, String login, String showFilename) {
        if (template == null)
            return null;

        String userName = Util.encodeURL(login);
        String showName = null;
        String showExt = null;

        if (showFilename != null) {
            int index = showFilename.lastIndexOf(".");
            if (index != -1) {
                showName = Util.encodeURL(showFilename.substring(0, index));
                showExt = Util.encodeURL(showFilename.substring(index));
            }
            else
                showName = Util.encodeURL(showFilename);
        }

        boolean isReplaced = false;
        StringBuilder templateBuilder = new StringBuilder(template);

        isReplaced = replacePattern(templateBuilder, PATTERN_SHOWNAME, showName) || isReplaced;
        isReplaced = replacePattern(templateBuilder, PATTERN_USERNAME, userName) || isReplaced;
        isReplaced = replacePattern(templateBuilder, PATTERN_SHOWEXT, showExt) || isReplaced;

        // Append show/ext if no substitution variables found
        if (!isReplaced && showName != null && showExt != null) {
            if (templateBuilder.charAt(templateBuilder.length() - 1) != '/')
                templateBuilder.append("/");
            templateBuilder.append(showName).append(showExt);
        }

        return templateBuilder.toString();
    }

    private boolean replacePattern(StringBuilder template, String pattern, String value) {
        boolean found = false;
        int index = 0;
        while ((index = template.indexOf(pattern, index)) != -1) {
            if (value != null) {
                template.replace(index, index + pattern.length(), value);
                index += value.length();
            }
            else
                template.delete(index, index + pattern.length());

            found = true;
        }
        return found;
    }

    /**
     * URL to GET after uploading show to notify server of upload.
     * May need to authenticate using credentials.
     */
    public String getHookURLTemplate() {
        return hookURLTemplate;
    }

    /**
     * URL for user to go to to manage hosting of the show.
     */
    public String getManageURLTemplate() {
        return manageURLTemplate;
    }

    public String toString() {
        return description;
    }
}
