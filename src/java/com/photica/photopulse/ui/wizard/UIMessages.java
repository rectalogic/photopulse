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

import com.photica.photopulse.Branding;

import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.text.MessageFormat;
import java.util.ListResourceBundle;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class UIMessages extends ListResourceBundle {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(UIMessages.class.getName());

    // Message IDs.
    // Using the same name of the identifier as the string saves space
    // in the class file.

    public static final String UI_FILTER_EXE = "UI_FILTER_EXE";
    public static final String UI_FILTER_HTM = "UI_FILTER_HTM";
    public static final String UI_FILTER_SWF = "UI_FILTER_SWF";
    public static final String UI_FILTER_MP3 = "UI_FILTER_MP3";
    public static final String UI_FILTER_MHT = "UI_FILTER_MHT";
    public static final String UI_FILTER_WMV = "UI_FILTER_WMV";
    public static final String UI_FILTER_PPP = "UI_FILTER_PPP";
    public static final String UI_FILTER_M3U = "UI_FILTER_M3U";

    public static final String UI_PROGRESS_LABEL = "UI_PROGRESS_LABEL";
    public static final String UI_PROGRESS_PERCENT = "UI_PROGRESS_PERCENT";
    public static final String UI_BUTTON_CANCEL = "UI_BUTTON_CANCEL";
    public static final String UI_PROGRESS_TITLE = "UI_PROGRESS_TITLE";
    public static final String UI_PROGRESS_IND_TITLE = "UI_PROGRESS_IND_TITLE";

    public static final String UI_ERROR_TITLE = "UI_ERROR_TITLE";
    public static final String UI_WARNING_TITLE = "UI_WARNING_TITLE";
    public static final String UI_INFO_TITLE = "UI_INFO_TITLE";

    public static final String UI_FRAME_TITLE = "UI_FRAME_TITLE";
    public static final String UI_FRAME_UNTITLED = "UI_FRAME_UNTITLED";
    public static final String UI_OPEN_LABEL = "UI_OPEN_LABEL";
    public static final String UI_BUTTON_OPEN = "UI_BUTTON_OPEN";
    public static final String UI_SAVE_LABEL = "UI_SAVE_LABEL";
    public static final String UI_BUTTON_SAVE = "UI_BUTTON_SAVE";
    public static final String UI_SOUND_LABEL = "UI_SOUND_LABEL";
    public static final String UI_BUTTON_SOUND = "UI_BUTTON_SOUND";
    public static final String UI_LABEL_SPEED_SLOW = "UI_LABEL_SPEED_SLOW";
    public static final String UI_LABEL_SPEED_FAST = "UI_LABEL_SPEED_FAST";
    public static final String UI_TAB_BEGINTRANS = "UI_TAB_BEGINTRANS";
    public static final String UI_TAB_EFFECTS = "UI_TAB_EFFECTS";
    public static final String UI_TAB_ENDTRANS = "UI_TAB_ENDTRANS";
    public static final String UI_TAB_PHOTOS = "UI_TAB_PHOTOS";
    public static final String UI_TAB_SETTINGS = "UI_TAB_SETTINGS";
    public static final String UI_IMAGECHOOSER_TITLE = "UI_IMAGECHOOSER_TITLE";
    public static final String UI_SOUNDCHOOSER_TITLE = "UI_SOUNDCHOOSER_TITLE";
    public static final String UI_SOUNDCHOOSER_LISTENLINK = "UI_SOUNDCHOOSER_LISTENLINK";
    public static final String UI_EXPORTCHOOSER_TITLE = "UI_EXPORTCHOOSER_TITLE";
    public static final String UI_EXPORTCHOOSER_HIQUALITY = "UI_EXPORTCHOOSER_HIQUALITY";
    public static final String UI_EXPORTCHOOSER_HIQUALITY_INFO = "UI_EXPORTCHOOSER_HIQUALITY_INFO";
    public static final String UI_EXPORTCHOOSER_TOGGLE_TITLE = "UI_EXPORTCHOOSER_TOGGLE_TITLE";
    public static final String UI_EXPORTCHOOSER_HTM_TOGGLE = "UI_EXPORTCHOOSER_HTM_TOGGLE";
    public static final String UI_EXPORTCHOOSER_WMV_TOGGLE = "UI_EXPORTCHOOSER_WMV_TOGGLE";
    public static final String UI_EXPORTCHOOSER_EXE_TOGGLE = "UI_EXPORTCHOOSER_EXE_TOGGLE";
    public static final String UI_COLORCHOOSER_TITLE = "UI_COLORCHOOSER_TITLE";
    public static final String UI_FTPUPLOADCHOOSER_TITLE = "UI_FTPUPLOADCHOOSER_TITLE";
    public static final String UI_OPEN_PROJECTCHOOSER_TITLE = "UI_OPEN_PROJECTCHOOSER_TITLE";
    public static final String UI_SAVE_PROJECTCHOOSER_TITLE = "UI_SAVE_PROJECTCHOOSER_TITLE";
    public static final String UI_TRANSITION_SPEED_TITLE = "UI_TRANSITION_SPEED_TITLE";
    public static final String UI_EFFECT_SPEED_TITLE = "UI_EFFECT_SPEED_TITLE";
    public static final String UI_EFFECT_DURATION_LABEL = "UI_EFFECT_DURATION_LABEL";
    public static final String UI_TRANSITION_DURATION_LABEL = "UI_TRANSITION_DURATION_LABEL";
    public static final String UI_SECONDS_LABEL = "UI_SECONDS_LABEL";
    public static final String UI_FRAMERATE = "UI_FRAMERATE";
    public static final String UI_FPS = "UI_FPS";
    public static final String UI_LABEL_COLOR = "UI_LABEL_COLOR";
    public static final String UI_LABEL_COLOR_ELLIPSIS = "UI_LABEL_COLOR_ELLIPSIS";
    public static final String UI_LABEL_ABOUT = "UI_LABEL_ABOUT";
    public static final String UI_LABEL_COPYRIGHT = "UI_LABEL_COPYRIGHT";
    public static final String UI_LABEL_VERSION = "UI_LABEL_VERSION";
    public static final String UI_LABEL_MEMORY = "UI_LABEL_MEMORY";
    public static final String UI_NOTICES = "UI_NOTICES";
    public static final String UI_BUTTON_OK = "UI_BUTTON_OK";
    public static final String UI_BUTTON_CLOSE = "UI_BUTTON_CLOSE";
    public static final String UI_LABEL_PREV = "UI_LABEL_PREV";
    public static final String UI_LABEL_NEXT = "UI_LABEL_NEXT";

    public static final String UI_LABEL_WEBSITE = "UI_LABEL_WEBSITE";
    public static final String UI_LABEL_VIEWLICENSE = "UI_LABEL_VIEWLICENSE";
    public static final String UI_ABOUT_TITLE = "UI_ABOUT_TITLE";
    public static final String UI_LABEL_LAUNCH = "UI_LABEL_LAUNCH";
    public static final String UI_LABEL_MANAGE = "UI_LABEL_MANAGE";
    public static final String UI_LICENSE_TITLE = "UI_LICENSE_TITLE";
    public static final String UI_LABEL_LICENSE = "UI_LABEL_LICENSE";
    public static final String UI_LABEL_LICENSEKEY = "UI_LABEL_LICENSEKEY";
    public static final String UI_LABEL_CUT = "UI_LABEL_CUT";
    public static final String UI_LABEL_COPY = "UI_LABEL_COPY";
    public static final String UI_LABEL_PASTE = "UI_LABEL_PASTE";
    public static final String UI_LABEL_CLEAR = "UI_LABEL_CLEAR";
    public static final String UI_LABEL_DELETE = "UI_LABEL_DELETE";
    public static final String UI_LABEL_SERIAL_UNREG = "UI_LABEL_SERIAL_UNREG";
    public static final String UI_LABEL_SERIAL_REG = "UI_LABEL_SERIAL_REG";
    public static final String UI_LABEL_PURCHASE = "UI_LABEL_PURCHASE";
    public static final String UI_LABEL_LOGLINK = "UI_LABEL_LOGLINK";
    public static final String UI_LABEL_REGISTER = "UI_LABEL_REGISTER";
    public static final String UI_TOOLTIP_SOUNDFILE = "UI_TOOLTIP_SOUNDFILE";
    public static final String UI_LABEL_PHOTOCOUNT = "UI_LABEL_PHOTOCOUNT";
    public static final String UI_LABEL_COMPUTED_PHOTOCOUNT = "UI_LABEL_COMPUTED_PHOTOCOUNT";
    public static final String UI_QUICKSTART_TITLE = "UI_QUICKSTART_TITLE";
    public static final String UI_TRIAL_TITLE = "UI_TRIAL_TITLE";
    public static final String UI_BUTTON_TRIAL_LICENSE = "UI_BUTTON_TRIAL_LICENSE";
    public static final String UI_LABEL_SELECTION = "UI_LABEL_SELECTION";
    public static final String UI_BUTTON_OPENALL = "UI_BUTTON_OPENALL";
    public static final String UI_TOGGLE_THUMBNAIL = "UI_TOGGLE_THUMBNAIL";
    public static final String UI_CONFIRMONE_TITLE = "UI_CONFIRMONE_TITLE";
    public static final String UI_BOX_THEME_TITLE = "UI_BOX_THEME_TITLE";
    public static final String UI_CUSTOMSKIN_TITLE = "UI_CUSTOMSKIN_TITLE";
    public static final String UI_CUSTOMSKIN_WIDTH = "UI_CUSTOMSKIN_WIDTH";
    public static final String UI_CUSTOMSKIN_HEIGHT = "UI_CUSTOMSKIN_HEIGHT";
    public static final String UI_CUSTOMSKIN_TOOLTIP = "UI_CUSTOMSKIN_TOOLTIP";
    public static final String UI_CUSTOMSKIN_TOOLTIP_SIZE = "UI_CUSTOMSKIN_TOOLTIP_SIZE";
    public static final String UI_CUSTOMSIZE_LABEL = "UI_CUSTOMSIZE_LABEL";
    public static final String UI_RADIO_SIZE_LARGE = "UI_RADIO_SIZE_LARGE";
    public static final String UI_RADIO_SIZE_SMALL = "UI_RADIO_SIZE_SMALL";
    public static final String UI_CUSTOMSKIN_BUTTON = "UI_CUSTOMSKIN_BUTTON";
    public static final String UI_CUSTOMSKIN_MSG = "UI_CUSTOMSKIN_MSG";
    public static final String UI_CUSTOMSKIN_THEME = "UI_CUSTOMSKIN_THEME";
    public static final String UI_CUSTOMSKIN_SIZE = "UI_CUSTOMSKIN_SIZE";
    public static final String UI_BOX_MP3_TITLE = "UI_BOX_MP3_TITLE";
    public static final String UI_BOX_ENDSHOW_TITLE = "UI_BOX_ENDSHOW_TITLE";
    public static final String UI_DISABLE_SOUND = "UI_DISABLE_SOUND";
    public static final String UI_EXTERNAL_SOUND = "UI_EXTERNAL_SOUND";
    public static final String UI_INTERNAL_SOUND = "UI_INTERNAL_SOUND";
    public static final String UI_ENDSHOW_LOOP = "UI_ENDSHOW_LOOP";
    public static final String UI_ENDSHOW_PAUSE = "UI_ENDSHOW_PAUSE";
    public static final String UI_ENDSHOW_STOP = "UI_ENDSHOW_STOP";
    public static final String UI_ENDSHOW_CLOSE = "UI_ENDSHOW_CLOSE";
    public static final String UI_SAVE_EXISTS_TITLE = "UI_SAVE_EXISTS_TITLE";
    public static final String UI_NEW_IMAGELIST_TITLE = "UI_NEW_IMAGELIST_TITLE";
    public static final String UI_ENDTRANS_LAYER_TOP = "UI_ENDTRANS_LAYER_TOP";
    public static final String UI_ENDTRANS_LAYER_BOTTOM = "UI_ENDTRANS_LAYER_BOTTOM";
    public static final String UI_VIEWER_TITLE = "UI_VIEWER_TITLE";
    public static final String UI_PREVIEWER_TITLE = "UI_PREVIEWER_TITLE";
    public static final String UI_VIEWER_LABEL = "UI_VIEWER_LABEL";
    public static final String UI_VIEWER_TITLE_NO_PHOTO = "UI_VIEWER_TITLE_NO_PHOTO";
    public static final String UI_PHOTO_TOOLTIP = "UI_PHOTO_TOOLTIP";
    public static final String UI_TOOLTIP_VIEWPHOTO = "UI_TOOLTIP_VIEWPHOTO";
    public static final String UI_TOOLTIP_PREVIEW = "UI_TOOLTIP_PREVIEW";
    public static final String UI_TOOLTIP_FULL_PREVIEW = "UI_TOOLTIP_FULL_PREVIEW";
    public static final String UI_CUSTOM_EFFECT = "UI_CUSTOM_EFFECT";
    public static final String UI_LABEL_MULTIPLE = "UI_LABEL_MULTIPLE";
    public static final String UI_LABEL_NONE = "UI_LABEL_NONE";
    public static final String UI_LABEL_MP3_DURATION = "UI_LABEL_MP3_DURATION";
    public static final String UI_LABEL_SHOW_DURATION = "UI_LABEL_SHOW_DURATION";
    public static final String UI_POPUP_PROMPT = "UI_POPUP_PROMPT";
    public static final String UI_LABEL_PHOTOCAPTION = "UI_LABEL_PHOTOCAPTION";
    public static final String UI_LOCK_TOGGLE = "UI_LOCK_TOGGLE";
    public static final String UI_LOCK_TOOLTIP = "UI_LOCK_TOOLTIP";
    public static final String UI_CONFIRMSAVE_TITLE = "UI_CONFIRMSAVE_TITLE";

    public static final String UI_CUSTOMEFFECT_LABEL = "UI_CUSTOMEFFECT_LABEL";
    public static final String UI_CUSTOMEFFECT_BUTTON = "UI_CUSTOMEFFECT_BUTTON";
    public static final String UI_CUSTOMEFFECT_TOOLTIP = "UI_CUSTOMEFFECT_TOOLTIP";
    public static final String UI_PZ_DIALOGTITLE = "UI_PZ_DIALOGTITLE";
    public static final String UI_PZ_ADDKEYFRAME = "UI_PZ_ADDKEYFRAME";
    public static final String UI_PZ_DELKEYFRAME = "UI_PZ_DELKEYFRAME";
    public static final String UI_PZ_TIMELINE = "UI_PZ_TIMELINE";
    public static final String UI_PZ_PREVIEW = "UI_PZ_PREVIEW";
    public static final String UI_PZ_TRANSLATION = "UI_PZ_TRANSLATION";
    public static final String UI_PZ_TRANSX = "UI_PZ_TRANSX";
    public static final String UI_PZ_TRANSY = "UI_PZ_TRANSY";
    public static final String UI_PZ_SCALE = "UI_PZ_SCALE";
    public static final String UI_PZ_ROTATE = "UI_PZ_ROTATE";
    public static final String UI_PZ_SPINLEFT = "UI_PZ_SPINLEFT";
    public static final String UI_PZ_SPINUP = "UI_PZ_SPINUP";
    public static final String UI_PZ_SPINRIGHT = "UI_PZ_SPINRIGHT";
    public static final String UI_PZ_EASING = "UI_PZ_EASING";
    public static final String UI_PZ_CURVE = "UI_PZ_CURVE";
    public static final String UI_PZ_DEGREES = "UI_PZ_DEGREES";
    public static final String UI_PZ_TIMELINE_TIME = "UI_PZ_TIMELINE_TIME";

    public static final String UI_TOOL_UPLOAD_LABEL = "UI_TOOL_UPLOAD_LABEL";
    public static final String UI_TOOL_UPLOAD_MENU_LABEL = "UI_TOOL_UPLOAD_MENU_LABEL";
    public static final String UI_TOOL_MANAGE_SHOW_MENU_LABEL = "UI_TOOL_MANAGE_SHOW_MENU_LABEL";
    public static final String UI_TOOL_OSVIEWER_LABEL = "UI_TOOL_OSVIEWER_LABEL";
    public static final String UI_TOOL_OSVIEWER_MENU_LABEL = "UI_TOOL_OSVIEWER_MENU_LABEL";
    public static final String UI_TOOL_NEODVD_LABEL = "UI_TOOL_NEODVD_LABEL";
    public static final String UI_TOOL_NEODVD_MENU_LABEL = "UI_TOOL_NEODVD_MENU_LABEL";
    public static final String UI_SHOW_CREATED_MESSAGE = "UI_SHOW_CREATED_MESSAGE";
    public static final String UI_TOOL_DIALOG_TITLE = "UI_TOOL_DIALOG_TITLE";

    public static final String UI_MENU_FILE = "UI_MENU_FILE";
    public static final String UI_MENU_FILE_PROJECT_NEW = "UI_MENU_FILE_PROJECT_NEW";
    public static final String UI_MENU_FILE_PROJECT_OPEN = "UI_MENU_FILE_PROJECT_OPEN";
    public static final String UI_MENU_FILE_PROJECT_SAVE = "UI_MENU_FILE_PROJECT_SAVE";
    public static final String UI_MENU_FILE_PROJECT_SAVE_AS = "UI_MENU_FILE_PROJECT_SAVE_AS";
    public static final String UI_MENU_FILE_EXIT = "UI_MENU_FILE_EXIT";
    public static final String UI_MENU_EDIT = "UI_MENU_EDIT";
    public static final String UI_MENU_EDIT_SELECTALL = "UI_MENU_EDIT_SELECTALL";
    public static final String UI_MENU_EDIT_DESELECTALL = "UI_MENU_EDIT_DESELECTALL";
    public static final String UI_MENU_EDIT_FTPSETTINGS = "UI_MENU_EDIT_FTPSETTINGS";
    public static final String UI_MENU_EDIT_RANDOMIZE = "UI_MENU_EDIT_RANDOMIZE";
    public static final String UI_MENU_EDIT_REDIST_TIME = "UI_MENU_EDIT_REDIST_TIME";
    public static final String UI_MENU_VIEW = "UI_MENU_VIEW";
    public static final String UI_MENU_VIEW_EXPERTMODE = "UI_MENU_VIEW_EXPERTMODE";
    public static final String UI_MENU_TOOLS = "UI_MENU_TOOLS";
    public static final String UI_MENU_TOOLS_BUILDM3U = "UI_MENU_TOOLS_BUILDM3U";
    public static final String UI_MENU_HELP = "UI_MENU_HELP";
    public static final String UI_MENU_HELP_ABOUT = "UI_MENU_HELP_ABOUT";
    public static final String UI_MENU_HELP_QUICKSTART = "UI_MENU_HELP_QUICKSTART";
    public static final String UI_MENU_HELP_LICENSE = "UI_MENU_HELP_LICENSE";
    public static final String UI_MENU_HELP_HELP = "UI_MENU_HELP_HELP";
    public static final String UI_MENU_HELP_REGISTER = "UI_MENU_HELP_REGISTER";
    public static final String UI_MENU_HELP_PURCHASE = "UI_MENU_HELP_PURCHASE";

    public static final String UI_FTPS_TITLE = "UI_FTPS_TITLE";
    public static final String UI_FTPS_INFO = "UI_FTPS_INFO";
    public static final String UI_FTPS_PROVIDER = "UI_FTPS_PROVIDER";
    public static final String UI_FTPS_SIGNUP = "UI_FTPS_SIGNUP";
    public static final String UI_FTPS_CUSTOMSETTINGS = "UI_FTPS_CUSTOMSETTINGS";
    public static final String UI_FTPS_CUSTOMDESC = "UI_FTPS_CUSTOMDESC";
    public static final String UI_FTPS_HOSTNAME = "UI_FTPS_HOSTNAME";
    public static final String UI_FTPS_PORT = "UI_FTPS_PORT";
    public static final String UI_FTPS_LOGIN = "UI_FTPS_LOGIN";
    public static final String UI_FTPS_PW = "UI_FTPS_PW";
    public static final String UI_FTPS_DIR = "UI_FTPS_DIR";
    public static final String UI_FTPS_URL = "UI_FTPS_URL";
    public static final String UI_FTPS_SAVEPW = "UI_FTPS_SAVEPW";

    public static final String UI_FTPU_INFO = "UI_FTPU_INFO";
    public static final String UI_FTPU_TITLE = "UI_FTPU_TITLE";

    public static final String UI_M3U_EDITOR_TITLE = "UI_M3U_EDITOR_TITLE";
    public static final String UI_M3U_NEW = "UI_M3U_NEW";
    public static final String UI_M3U_CHOOSER_TITLE = "UI_M3U_CHOOSER_TITLE";
    public static final String UI_M3U_SAVE_TITLE = "UI_M3U_SAVE_TITLE";
    public static final String UI_M3U_CONFIRMSAVE_TITLE = "UI_M3U_CONFIRMSAVE_TITLE";
    public static final String UI_M3U_ADD = "UI_M3U_ADD";
    public static final String UI_M3U_REMOVE = "UI_M3U_REMOVE";
    public static final String UI_M3U_LOAD = "UI_M3U_LOAD";
    public static final String UI_M3U_SAVE = "UI_M3U_SAVE";
    public static final String UI_M3U_MOVEUP = "UI_M3U_MOVEUP";
    public static final String UI_M3U_MOVEDOWN = "UI_M3U_MOVEDOWN";

    public static final String UI_LABEL_FTPSUCCESS = "UI_LABEL_FTPSUCCESS";
    public static final String UI_LABEL_FTPSUCCESS_NOURL = "UI_LABEL_FTPSUCCESS_NOURL";
    public static final String UI_TIP_FTPSUCCESS_COPYURL = "UI_TIP_FTPSUCCESS_COPYURL";

    public static final String I_UI_MN_FILE = "I_UI_MN_FILE";
    public static final String I_UI_MN_FILE_PROJECT_NEW = "I_UI_MN_FILE_PROJECT_NEW";
    public static final String I_UI_MN_FILE_PROJECT_OPEN = "I_UI_MN_FILE_PROJECT_OPEN";
    public static final String I_UI_MN_FILE_PROJECT_SAVE = "I_UI_MN_FILE_PROJECT_SAVE";
    public static final String I_UI_MN_FILE_PROJECT_SAVE_AS = "I_UI_MN_FILE_PROJECT_SAVE_AS";
    public static final String I_UI_MN_FILE_PHOTOS = "I_UI_MN_FILE_PHOTOS";
    public static final String I_UI_MN_FILE_MUSIC = "I_UI_MN_FILE_MUSIC";
    public static final String I_UI_MN_FILE_CREATE = "I_UI_MN_FILE_CREATE";
    public static final String I_UI_MN_FILE_EXIT = "I_UI_MN_FILE_EXIT";
    public static final String I_UI_MN_EDIT = "I_UI_MN_EDIT";
    public static final String I_UI_MN_EDIT_SELECTALL = "I_UI_MN_EDIT_SELECTALL";
    public static final String I_UI_MN_EDIT_DESELECTALL = "I_UI_MN_EDIT_DESELECTALL";
    public static final String I_UI_MN_EDIT_FTPSETTINGS = "I_UI_MN_EDIT_FTPSETTINGS";
    public static final String I_UI_MN_EDIT_RANDOMIZE = "I_UI_MN_EDIT_RANDOMIZE";
    public static final String I_UI_MN_EDIT_REDIST_TIME = "I_UI_MN_EDIT_REDIST_TIME";
    public static final String I_UI_MN_VIEW = "I_UI_MN_VIEW";
    public static final String I_UI_MN_VIEW_EXPERTMODE = "I_UI_MN_VIEW_EXPERTMODE";
    public static final String I_UI_MN_TOOLS = "I_UI_MN_TOOLS";
    public static final String I_UI_MN_TOOLS_BUILDM3U = "I_UI_MN_TOOLS_BUILDM3U";
    public static final String I_UI_MN_TOOLS_UPLOAD = "I_UI_MN_TOOLS_UPLOAD";
    public static final String I_UI_MN_TOOLS_MANAGE_SHOW = "I_UI_MN_TOOLS_MANAGE_SHOW";
    public static final String I_UI_MN_TOOLS_VIEWSHOW = "I_UI_MN_TOOLS_VIEWSHOW";
    public static final String I_UI_MN_TOOLS_CREATEDVD = "I_UI_MN_TOOLS_CREATEDVD";
    public static final String I_UI_MN_HELP = "I_UI_MN_HELP";
    public static final String I_UI_MN_HELP_ABOUT = "I_UI_MN_HELP_ABOUT";
    public static final String I_UI_MN_HELP_QUICKSTART = "I_UI_MN_HELP_QUICKSTART";
    public static final String I_UI_MN_HELP_LICENSE = "I_UI_MN_HELP_LICENSE";
    public static final String I_UI_MN_HELP_HELP = "I_UI_MN_HELP_HELP";
    public static final String I_UI_MN_HELP_REGISTER = "I_UI_MN_HELP_REGISTER";
    public static final String I_UI_MN_HELP_PURCHASE = "I_UI_MN_HELP_PURCHASE";
    public static final String I_UI_MN_CUT = "I_UI_MN_CUT";
    public static final String I_UI_MN_COPY = "I_UI_MN_COPY";
    public static final String I_UI_MN_PASTE = "I_UI_MN_PASTE";
    public static final String I_UI_MN_DELETE = "I_UI_MN_DELETE";

    public static final String I_UI_ACCEL_CUT = "I_UI_ACCEL_CUT";
    public static final String I_UI_ACCEL_COPY = "I_UI_ACCEL_COPY";
    public static final String I_UI_ACCEL_PASTE = "I_UI_ACCEL_PASTE";
    public static final String I_UI_ACCEL_DELETE = "I_UI_ACCEL_DELETE";

    public static final String URL_PHOTOPULSE = "URL_PHOTOPULSE";
    public static final String URL_PURCHASE = "URL_PURCHASE";
    public static final String URL_REGISTER = "URL_REGISTER";

    public static final String RSRC_TRIAL = "RSRC_TRIAL";
    public static final String RSRC_QUICKSTART = "RSRC_QUICKSTART";

    public static final String ERR_INTERNAL_ERROR = "ERR_INTERNAL_ERROR";
    public static final String ERR_LOG_MESSAGES = "ERR_LOG_MESSAGES";
    public static final String ERR_IO_GENERATE = "ERR_IO_GENERATE";
    public static final String ERR_INVALID_IMAGES = "ERR_INVALID_IMAGES";
    public static final String ERR_IO_MP3 = "ERR_IO_MP3";
    public static final String ERR_INVALID_MP3 = "ERR_INVALID_MP3";
    public static final String ERR_NOMEMORY = "ERR_NOMEMORY";
    public static final String ERR_LAUNCH_FAILED = "ERR_LAUNCH_FAILED";
    public static final String ERR_TOOL_FAILED = "ERR_TOOL_FAILED";
    public static final String ERR_TOOL_NOFILE = "ERR_TOOL_NOFILE";
    public static final String ERR_DROPDEAD = "ERR_DROPDEAD";
    public static final String ERR_INVALID_LICENSE = "ERR_INVALID_LICENSE";
    public static final String ERR_EXPIRED_LICENSE = "ERR_EXPIRED_LICENSE";
    public static final String ERR_LICENSE_OK = "ERR_LICENSE_OK";
    public static final String ERR_QUICKSTART_LOAD = "ERR_QUICKSTART_LOAD";
    public static final String ERR_TRIAL_LOAD = "ERR_TRIAL_LOAD";
    public static final String ERR_BADSKIN = "ERR_BADSKIN";
    public static final String ERR_FTP_FAILED = "ERR_FTP_FAILED";
    public static final String ERR_FTPEX_CONNECT = "ERR_FTPEX_CONNECT";
    public static final String ERR_FTPEX_LOGIN = "ERR_FTPEX_LOGIN";
    public static final String ERR_FTPEX_HOST = "ERR_FTPEX_HOST";
    public static final String ERR_FTPEX_PORT = "ERR_FTPEX_PORT";
    public static final String ERR_FTPEX_CD = "ERR_FTPEX_CD";
    public static final String ERR_FTPEX_FILE = "ERR_FTPEX_FILE";
    public static final String ERR_FTPS_REQUIRED = "ERR_FTPS_REQUIRED";
    public static final String ERR_AWT_EXCEPTION = "ERR_AWT_EXCEPTION";
    public static final String ERR_SAVE_EXISTS = "ERR_SAVE_EXISTS";
    public static final String ERR_EXPORT_DELETE = "ERR_EXPORT_DELETE";
    public static final String ERR_NEW_IMAGELIST = "ERR_NEW_IMAGELIST";
    public static final String ERR_SHOW_LENGTH = "ERR_SHOW_LENGTH";
    public static final String ERR_LOAD_MODEL_NOFILE = "ERR_LOAD_MODEL_NOFILE";
    public static final String ERR_LOAD_MODEL_INVALIDPHOTOS = "ERR_LOAD_MODEL_INVALIDPHOTOS";
    public static final String ERR_LOAD_MODEL_INVALIDSKIN = "ERR_LOAD_MODEL_INVALIDSKIN";
    public static final String ERR_LOAD_MODEL_IO = "ERR_LOAD_MODEL_IO";
    public static final String ERR_LOAD_MODEL_FAILED = "ERR_LOAD_MODEL_FAILED";
    public static final String ERR_SAVE_MODEL_IO = "ERR_SAVE_MODEL_IO";
    public static final String ERR_SAVE_MODEL_FAILED = "ERR_SAVE_MODEL_FAILED";
    public static final String ERR_CONFIRM_ONE = "ERR_CONFIRM_ONE";
    public static final String ERR_CONFIRMSAVE = "ERR_CONFIRMSAVE";
    public static final String ERR_M3U_CONFIRMSAVE = "ERR_M3U_CONFIRMSAVE";
    public static final String ERR_M3U_LOAD_IO = "ERR_M3U_LOAD_IO";
    public static final String ERR_M3U_LOAD_ERR = "ERR_M3U_LOAD_ERR";
    public static final String ERR_M3U_SAVE_IO = "ERR_M3U_SAVE_IO";
    public static final String ERR_LOAD_PHOTO = "ERR_LOAD_PHOTO";

    private static final Object[][] contents = {
        { UI_FILTER_EXE,
            "Windows application ({0})" },
        { UI_FILTER_HTM,
            "Web page ({0})" },
        { UI_FILTER_SWF,
            "Flash file ({0})" },
        { UI_FILTER_MP3,
            "MP3 file ({0})" },
        { UI_FILTER_MHT,
            "Email attachment ({0})" },
        { UI_FILTER_WMV,
            "Windows Media Video file ({0})" },
        { UI_FILTER_PPP,
            Branding.PRODUCT_NAME + " Project ({0})" },
        { UI_FILTER_M3U,
            "MP3 playlist ({0})" },

        { UI_PROGRESS_LABEL,
            Branding.PRODUCT_NAME + " export progress" },
        { UI_PROGRESS_PERCENT,
            "{0,number,0.00%}" },
        { UI_BUTTON_CANCEL,
            "Cancel" },
        { UI_PROGRESS_TITLE,
            Branding.PRODUCT_NAME + " Progress" },
        { UI_PROGRESS_IND_TITLE,
            "Processing..." },

        { UI_ERROR_TITLE,
            Branding.PRODUCT_NAME + " Error" },
        { UI_WARNING_TITLE,
            Branding.PRODUCT_NAME + " Warning" },
        { UI_INFO_TITLE,
            Branding.PRODUCT_NAME + " Information" },

        { UI_FRAME_TITLE,
            Branding.PRODUCT_NAME + " - [{0}]" },
        { UI_FRAME_UNTITLED,
            "New Project" },
        { UI_OPEN_LABEL,
            "Choose photos" },
        { UI_BUTTON_OPEN,
            "Photos..." },
        { UI_SAVE_LABEL,
            "Create show" },
        { UI_BUTTON_SAVE,
            "Create..." },
        { UI_SOUND_LABEL,
            "Choose MP3 music file" },
        { UI_BUTTON_SOUND,
            "Music..." },
        { UI_LABEL_SPEED_SLOW,
            "slow" },
        { UI_LABEL_SPEED_FAST,
            "fast" },
        { UI_TAB_BEGINTRANS,
            "Transitions In" },
        { UI_TAB_EFFECTS,
            "Effects" },
        { UI_TAB_ENDTRANS,
            "Transitions Out" },
        { UI_TAB_PHOTOS,
            "Photos" },
        { UI_TAB_SETTINGS,
            "Settings" },
        { UI_IMAGECHOOSER_TITLE,
            "Choose Photos" },
        { UI_SOUNDCHOOSER_TITLE,
            "Choose MP3 Sound File" },
        { UI_SOUNDCHOOSER_LISTENLINK,
            "Listen" },
        { UI_EXPORTCHOOSER_TITLE,
            "Create " + Branding.PRODUCT_NAME + " Show" },
        { UI_EXPORTCHOOSER_HIQUALITY,
            "High quality" },
        { UI_EXPORTCHOOSER_HIQUALITY_INFO,
            "<html>High quality mode takes longer to create and results in larger file sizes.<br>Recommended primarily for video." },
        { UI_EXPORTCHOOSER_TOGGLE_TITLE,
            "Show type" },
        { UI_EXPORTCHOOSER_HTM_TOGGLE,
            "<html><div style='width:140pt'>Create a web page. May be hosted on a web site." },
        { UI_EXPORTCHOOSER_WMV_TOGGLE,
            "<html><div style='width:140pt'>Create a video. May be used to burn a DVD." },
        { UI_EXPORTCHOOSER_EXE_TOGGLE,
            "<html><div style='width:140pt'>Create a self contained player application. Also suitable for previewing the other formats." },
        { UI_COLORCHOOSER_TITLE,
            "Background Color Chooser" },
        { UI_FTPUPLOADCHOOSER_TITLE,
            "Choose " + Branding.PRODUCT_NAME + " Show to Upload" },
        { UI_OPEN_PROJECTCHOOSER_TITLE,
            "Open " + Branding.PRODUCT_NAME + " Project" },
        { UI_SAVE_PROJECTCHOOSER_TITLE,
            "Save " + Branding.PRODUCT_NAME + " Project" },

        { UI_TRANSITION_SPEED_TITLE,
            "Transition speed" },
        { UI_EFFECT_SPEED_TITLE,
            "Effect speed" },
        { UI_LABEL_COLOR,
            "Background color" },
        { UI_LABEL_COLOR_ELLIPSIS,
            "..." },
        { UI_BUTTON_OK,
            "OK" },
        { UI_BUTTON_CLOSE,
            "Close" },
        { UI_EFFECT_DURATION_LABEL,
            "Default effect duration" },
        { UI_TRANSITION_DURATION_LABEL,
            "Default transition duration" },
        { UI_SECONDS_LABEL,
            "seconds" },
        { UI_FRAMERATE,
            "Frame rate" },
        { UI_FPS,
            "frames/second" },

        { UI_LABEL_WEBSITE,
            Branding.PRODUCT_NAME + " Online" },
        { UI_LABEL_LAUNCH,
            "View Show" },
        { UI_LABEL_MANAGE,
            "Manage Show" },
        { UI_TOOLTIP_SOUNDFILE,
            "<html>{0}<br>{1} minutes" },
        { UI_LABEL_PHOTOCOUNT,
            "{0} photos total" },
        { UI_LABEL_COMPUTED_PHOTOCOUNT,
            "{0} displayed" },
        { UI_QUICKSTART_TITLE,
            Branding.PRODUCT_NAME + " Quickstart" },
        { UI_TRIAL_TITLE,
            Branding.PRODUCT_NAME + " Trial" },
        { UI_BUTTON_TRIAL_LICENSE,
            "Enter license key..." },
        { UI_LABEL_SELECTION,
            "<html>Control-click to select<br>multiple individual photos." },
        { UI_BUTTON_OPENALL,
            "Open all" },
        { UI_TOGGLE_THUMBNAIL,
            "Preview" },
        { UI_CONFIRMONE_TITLE,
            "Confirm selection" },
        { UI_CUSTOMSKIN_TITLE,
            "Theme Chooser" },
        { UI_CUSTOMSKIN_WIDTH,
            "Width" },
        { UI_CUSTOMSKIN_HEIGHT,
            "Height" },
        { UI_CUSTOMSKIN_TOOLTIP,
            "{0} - {1}" },
        { UI_CUSTOMSKIN_TOOLTIP_SIZE,
            "Custom Size" },
        { UI_CUSTOMSIZE_LABEL,
            "Custom size..." },
        { UI_BOX_THEME_TITLE,
            "Show theme" },
        { UI_RADIO_SIZE_LARGE,
            "Large" },
        { UI_RADIO_SIZE_SMALL,
            "Small" },
        { UI_CUSTOMSKIN_BUTTON,
            "Custom ..." },
        { UI_CUSTOMSKIN_MSG,
            "<html><div style='width:400pt'>Select a show theme and photo size. Sizes that do not match your photos' common aspect ratio are dimmed, but can still be selected.</div>" },
        { UI_CUSTOMSKIN_THEME,
            "Theme" },
        { UI_CUSTOMSKIN_SIZE,
            "Photo size" },
        { UI_BOX_MP3_TITLE,
            "MP3" },
        { UI_DISABLE_SOUND,
            "No sound" },
        { UI_EXTERNAL_SOUND,
            "External sound" },
        { UI_INTERNAL_SOUND,
            "Internal sound" },
        { UI_BOX_ENDSHOW_TITLE,
            "End of show" },
        { UI_ENDSHOW_LOOP,
            "Loop" },
        { UI_ENDSHOW_PAUSE,
            "Pause" },
        { UI_ENDSHOW_STOP,
            "Stop" },
        { UI_ENDSHOW_CLOSE,
            "Close window" },
        { UI_SAVE_EXISTS_TITLE,
            "Confirm overwrite files" },
        { UI_NEW_IMAGELIST_TITLE,
            "Confirm load photos" },
        { UI_ENDTRANS_LAYER_TOP,
            "Above next" },
        { UI_ENDTRANS_LAYER_BOTTOM,
            "Below next" },
        { UI_VIEWER_TITLE,
            Branding.PRODUCT_NAME + " Photo Viewer" },
        { UI_VIEWER_LABEL,
            "{0} ({1} X {2})" },
        { UI_PREVIEWER_TITLE,
            Branding.PRODUCT_NAME + " Preview" },
        { UI_PHOTO_TOOLTIP,
            "<html><b>{0}</b> ({1})<br><img src='trans-in.gif' width='16' height='16'> {2}<br><img src='effect.gif' width='16' height='16'> {3}<br><img src='trans-out.gif' width='16' height='16'> {4} ({5})" },
        { UI_TOOLTIP_VIEWPHOTO,
            "View photo" },
        { UI_TOOLTIP_PREVIEW,
            "Simple effect preview" },
        { UI_TOOLTIP_FULL_PREVIEW,
            "Full effect preview (with theme)" },
        { UI_CUSTOM_EFFECT,
            "<i>Custom</i>" },
        { UI_LABEL_MULTIPLE,
            "<Multiple>" },
        { UI_LABEL_NONE,
            "<No Selection>" },
        { UI_LABEL_MP3_DURATION,
            "MP3 Duration " },
        { UI_LABEL_SHOW_DURATION,
            "Show Duration " },
        { UI_POPUP_PROMPT,
            "<Type title here>" },
        { UI_LABEL_PHOTOCAPTION,
            "Photo Caption" },
        { UI_LOCK_TOGGLE,
            "Lock" },
        { UI_LOCK_TOOLTIP,
            "<html>Lock transitions and effect<br>so they do not change" },
        { UI_CONFIRMSAVE_TITLE,
            "Confirm save project" },

        { UI_CUSTOMEFFECT_LABEL,
            "Customized Effect" },
        { UI_CUSTOMEFFECT_BUTTON,
            "Customize..." },
        { UI_CUSTOMEFFECT_TOOLTIP,
            "Create a customized Pan & Zoom effect" },
        { UI_PZ_DIALOGTITLE,
            "Pan & Zoom Editor" },
        { UI_PZ_ADDKEYFRAME,
            "Add" },
        { UI_PZ_DELKEYFRAME,
            "Remove" },
        { UI_PZ_TIMELINE,
            "Keyframe Timeline" },
        { UI_PZ_PREVIEW,
            "Preview" },
        { UI_PZ_TRANSLATION,
            "Position" },
        { UI_PZ_TRANSX,
            "X:" },
        { UI_PZ_TRANSY,
            "Y:" },
        { UI_PZ_SCALE,
            "Zoom" },
        { UI_PZ_ROTATE,
            "Rotation" },
        { UI_PZ_SPINLEFT,
            "Rotate 90\u00B0 counter clockwise" },
        { UI_PZ_SPINUP,
            "No rotation" },
        { UI_PZ_SPINRIGHT,
            "Rotate 90\u00B0 clockwise" },
        { UI_PZ_EASING,
            "<html>Easing<table width='230'>Slow animation speed when passing through this keyframe" },
        { UI_PZ_CURVE,
            "<html>Smoothing<table width='230'>Smooth animation path (curved path) when passing through this keyframe" },
        { UI_PZ_DEGREES,
            "{0,number,0.0\u00B0}" },
        { UI_PZ_TIMELINE_TIME,
            "{0,number,0.0 sec}" },

        { UI_TOOL_UPLOAD_LABEL,
            "Upload show" },
        { UI_TOOL_UPLOAD_MENU_LABEL,
            "Upload Show..." },
        { UI_TOOL_MANAGE_SHOW_MENU_LABEL,
            "Manage Uploaded Shows" },
        { UI_TOOL_OSVIEWER_LABEL,
            "View show" },
        { UI_TOOL_OSVIEWER_MENU_LABEL,
            "View Show" },
        { UI_TOOL_NEODVD_LABEL,
            "Create DVD using NeoDVD" },
        { UI_TOOL_NEODVD_MENU_LABEL,
            "Create DVD" },
        { UI_SHOW_CREATED_MESSAGE,
            "The show {0} has been successfully created." },
        { UI_TOOL_DIALOG_TITLE,
            "Show Tools" },

        { UI_FTPS_TITLE,
            "Upload Settings" },
        { UI_FTPS_INFO,
            "<html>Enter your login and password for the selected hosting provider."
            + " If you do not yet have a login, click <b>Signup...</b> to create a new account."
            + "<p><p>To enter settings for uploading to a server that supports the File Transfer Protocol (FTP), choose the <b>Custom</b> provider." },
        { UI_FTPS_PROVIDER,
            "Hosting Provider" },
        { UI_FTPS_SIGNUP,
            "Signup..." },
        { UI_FTPS_CUSTOMSETTINGS,
            "Custom Settings" },
        { UI_FTPS_CUSTOMDESC,
            "Custom" },
        { UI_FTPS_HOSTNAME,
            "FTP server hostname (e.g. ftp.photica.com)" },
        { UI_FTPS_PORT,
            "Port" },
        { UI_FTPS_LOGIN,
            "Login" },
        { UI_FTPS_PW,
            "Password" },
        { UI_FTPS_DIR,
            "<html>Server directory in which to upload web pages<br>(e.g. mylogin)" },
        { UI_FTPS_URL,
            "<html>URL for the directory above on your web site<br>(e.g. http://www.photica.com/mylogin/)" },
        { UI_FTPS_SAVEPW,
            "Save" },

        { UI_FTPU_TITLE,
            "Show Upload" },
        { UI_FTPU_INFO,
            "<html><div style='width:400pt'>Uploading " + Branding.PRODUCT_NAME + " show files for {0} to FTP server {1}</div>" },

        { UI_LABEL_FTPSUCCESS_NOURL,
            "<html><div style='width:400pt'>" +
            "Your " + Branding.PRODUCT_NAME + " show <i>{0}</i> has been successfully uploaded." +
            "</div></html>"
         },
        { UI_LABEL_FTPSUCCESS,
            "<html><table style='width:400pt'>" +
            "<tr><td>Your " + Branding.PRODUCT_NAME + " show <i>{0}</i> has been successfully uploaded.</td></tr>" +
            "<tr><td style='font-weight:normal'>You can access your show online now, or you can copy the URL to the clipboard. " +
            "You can then easily paste the URL into another application " +
            "(e.g. an email message)." +
            "</td></tr></table></html>"
         },
        { UI_TIP_FTPSUCCESS_COPYURL,
            "Copy URL" },

        { UI_M3U_EDITOR_TITLE,
            "MP3 Playlist Editor" },
        { UI_M3U_NEW,
            "<New Playlist>" },
        { UI_M3U_CHOOSER_TITLE,
            "Choose MP3 Playlist" },
        { UI_M3U_SAVE_TITLE,
            "Save MP3 Playlist" },
        { UI_M3U_CONFIRMSAVE_TITLE,
            "Confirm Save Playlist" },
        { UI_M3U_ADD,
            "Add..." },
        { UI_M3U_REMOVE,
            "Remove" },
        { UI_M3U_LOAD,
            "Load..." },
        { UI_M3U_SAVE,
            "Save..." },
        { UI_M3U_MOVEUP,
            "Move Up" },
        { UI_M3U_MOVEDOWN,
            "Move Down" },
        { UI_MENU_FILE,
            "File" },
        { UI_MENU_FILE_PROJECT_NEW,
            "New Project" },
        { UI_MENU_FILE_PROJECT_OPEN,
            "Open Project..." },
        { UI_MENU_FILE_PROJECT_SAVE,
            "Save Project" },
        { UI_MENU_FILE_PROJECT_SAVE_AS,
            "Save Project As..." },
        { UI_MENU_FILE_EXIT,
            "Exit" },
        { UI_MENU_EDIT,
            "Edit" },
        { UI_MENU_EDIT_SELECTALL,
            "Select All" },
        { UI_MENU_EDIT_DESELECTALL,
            "Deselect All" },
        { UI_MENU_EDIT_FTPSETTINGS,
            "Upload Settings..." },
        { UI_MENU_EDIT_RANDOMIZE,
            "Randomize Effects" },
        { UI_MENU_EDIT_REDIST_TIME,
            "Redistribute Time" },
        { UI_MENU_VIEW,
            "View" },
        { UI_MENU_VIEW_EXPERTMODE,
            "Expert Mode" },
        { UI_MENU_TOOLS,
            "Tools" },
        { UI_MENU_TOOLS_BUILDM3U,
            "Build MP3 Playlist..." },
        { UI_MENU_HELP,
            "Help" },
        { UI_MENU_HELP_ABOUT,
            "About " + Branding.PRODUCT_NAME },
        { UI_MENU_HELP_QUICKSTART,
            "Quickstart Guide" },
        { UI_MENU_HELP_LICENSE,
            "Enter License Key..." },
        { UI_MENU_HELP_HELP,
            "Help" },
        { UI_MENU_HELP_REGISTER,
            "Register" },
        { UI_MENU_HELP_PURCHASE,
            "Purchase..." },

        { I_UI_MN_FILE,
            new Integer('F') },
        { I_UI_MN_FILE_PROJECT_NEW,
            new Integer('N') },
        { I_UI_MN_FILE_PROJECT_OPEN,
            new Integer('O') },
        { I_UI_MN_FILE_PROJECT_SAVE,
            new Integer('S') },
        { I_UI_MN_FILE_PROJECT_SAVE_AS,
            new Integer('A') },
        { I_UI_MN_FILE_PHOTOS,
            new Integer('P') },
        { I_UI_MN_FILE_MUSIC,
            new Integer('M') },
        { I_UI_MN_FILE_CREATE,
            new Integer('C') },
        { I_UI_MN_FILE_EXIT,
            new Integer('x') },
        { I_UI_MN_EDIT,
            new Integer('E') },
        { I_UI_MN_EDIT_SELECTALL,
            new Integer('S') },
        { I_UI_MN_EDIT_DESELECTALL,
            new Integer('D') },
        { I_UI_MN_EDIT_FTPSETTINGS,
            new Integer('U') },
        { I_UI_MN_EDIT_RANDOMIZE,
            new Integer('R') },
        { I_UI_MN_EDIT_REDIST_TIME,
            new Integer('T') },
        { I_UI_MN_VIEW,
            new Integer('V') },
        { I_UI_MN_VIEW_EXPERTMODE,
            new Integer('E') },
        { I_UI_MN_TOOLS,
            new Integer('T') },
        { I_UI_MN_TOOLS_BUILDM3U,
            new Integer('M') },
        { I_UI_MN_TOOLS_UPLOAD,
            new Integer('U') },
        { I_UI_MN_TOOLS_MANAGE_SHOW,
            new Integer('S') },
        { I_UI_MN_TOOLS_VIEWSHOW,
            new Integer('V') },
        { I_UI_MN_TOOLS_CREATEDVD,
            new Integer('D') },
        { I_UI_MN_HELP,
            new Integer('H') },
        { I_UI_MN_HELP_ABOUT,
            new Integer('A') },
        { I_UI_MN_HELP_QUICKSTART,
            new Integer('Q') },
        { I_UI_MN_HELP_LICENSE,
            new Integer('L') },
        { I_UI_MN_HELP_HELP,
            new Integer('H') },
        { I_UI_MN_HELP_REGISTER,
            new Integer('R') },
        { I_UI_MN_HELP_PURCHASE,
            new Integer('P') },
        { I_UI_MN_CUT,
            new Integer('t') },
        { I_UI_MN_COPY,
            new Integer('C') },
        { I_UI_MN_PASTE,
            new Integer('P') },
        { I_UI_MN_DELETE,
            new Integer('D') },

        { I_UI_ACCEL_CUT,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) },
        { I_UI_ACCEL_COPY,
            KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) },
        { I_UI_ACCEL_PASTE,
            KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) },
        { I_UI_ACCEL_DELETE,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0) },

        { UI_LABEL_ABOUT,
            "Photica " + Branding.PRODUCT_NAME + "\u2122 digital photo sharing software" },
        { UI_LABEL_COPYRIGHT,
            "Copyright \u00A9 Photica Inc. 2002. All rights reserved." },
        { UI_LABEL_VERSION,
            "Version {0}" },
        { UI_LABEL_MEMORY,
            "Memory {0,number} KB" },
        { UI_NOTICES,
            "This product includes software developed by Dmitry Skavish (skavish@usa.net, http://www.flashgap.com/)."
            + " This product includes  software developed  by the  Apache Software Foundation  (http://www.apache.org/)."
            + " This product includes code licensed from RSA Security, Inc."
            + " Some portions licensed from IBM are available at http://oss.software.ibm.com/icu4j/."
            + "\n[" + Branding.PRODUCT_ID + " build]"
            },
        { UI_LABEL_VIEWLICENSE,
            "View license details" },
        { UI_ABOUT_TITLE,
            "About " + Branding.PRODUCT_NAME },
        { UI_LABEL_SERIAL_UNREG,
            "Trial version" },
        { UI_LABEL_SERIAL_REG,
            "Serial number {0}" },
        { UI_LABEL_PURCHASE,
            "Purchase license" },
        { UI_LABEL_LOGLINK,
            "View application log" },
        { UI_LABEL_REGISTER,
            "Register for free updates and add-ons" },
        { UI_LICENSE_TITLE,
            Branding.PRODUCT_NAME + " License" },
        { UI_LABEL_LICENSE,
            "<html>Enter your license key exactly as received.<br>Copy the full license key and paste it below." },
        { UI_LABEL_LICENSEKEY,
            "Key:" },
        { UI_LABEL_CUT,
            "Cut" },
        { UI_LABEL_COPY,
            "Copy" },
        { UI_LABEL_PASTE,
            "Paste" },
        { UI_LABEL_CLEAR,
            "Clear" },
        { UI_LABEL_DELETE,
            "Delete" },
        { UI_LABEL_PREV,
            "Previous"},
        { UI_LABEL_NEXT,
            "Next"},

        { URL_PHOTOPULSE,
            Branding.PRODUCT_URL },
        { URL_PURCHASE,
            Branding.PURCHASE_URL },
        { URL_REGISTER,
            Branding.REGISTER_URL },

        { RSRC_TRIAL,
            "resources/trial.htm" },
        { RSRC_QUICKSTART,
            "resources/quickstart.htm" },

        { ERR_INTERNAL_ERROR,
            "An internal error has occurred.\nPlease send the file {0} to customer support.\nThe detailed message was:\n{1}" },
        { ERR_LOG_MESSAGES,
            "Internal errors were generated during show creation.  The detailed message was:\n{0}" },
        { ERR_IO_GENERATE,
            "Failed to write to the specified Flash output file.\n{0}" },
        { ERR_INVALID_IMAGES,
            "No valid images were selected." },
        { ERR_IO_MP3,
            "The MP3 sound file specified could not be read.\n{0}" },
        { ERR_INVALID_MP3,
            "The MP3 sound file is not in a supported format.\n{0}" },
        { ERR_NOMEMORY,
            "There is not enough memory to create the show.\n{0}" },
        { ERR_LAUNCH_FAILED,
            "Failed to launch the document \"{0}\"." },
        { ERR_TOOL_FAILED,
            "Failed to invoke the selected tool with the file \"{0}\"." },
        { ERR_TOOL_NOFILE,
            "The file \"{0}\" no longer exists." },
        { ERR_DROPDEAD,
            "This early access release has been deactivated.  Please visit " + Branding.PRODUCT_URL + " for more information." },
        { ERR_INVALID_LICENSE,
            "The license key entered is not valid.  Please check the key and try again.  (license error {0,number,#})" },
        { ERR_EXPIRED_LICENSE,
            "The license key entered is no longer valid.  Please visit " + Branding.PRODUCT_URL + " to revalidate your license." },
        { ERR_LICENSE_OK,
            "Thank you for purchasing " + Branding.PRODUCT_NAME + "." },
        { ERR_QUICKSTART_LOAD,
            "Failed to load Quickstart guide." },
        { ERR_TRIAL_LOAD,
            "Failed to load trial message." },
        { ERR_BADSKIN,
            "The selected skin could not be loaded.\n{0}" },
        { ERR_FTP_FAILED,
            "The upload has failed.  {0}\n\nDo you want to confirm your upload settings and try again?" },
        { ERR_FTPEX_CONNECT,
            "Unable to connect to the FTP server.\nError message was: \"{0}\"" },
        { ERR_FTPEX_LOGIN,
            "Unable to login to the FTP server.  Please confirm that your login and password are correct.\nError message was: \"{0}\"" },
        { ERR_FTPEX_HOST,
            "Unable to connect to the FTP server.  Please confirm that the FTP server hostname is correct.\nError message was: \"{0}\"" },
        { ERR_FTPEX_PORT,
            "Unable to connect to the FTP server.  The server may be down, or the specified server name or port number may be incorrect.\nError message was: \"{0}\"" },
        { ERR_FTPEX_CD,
            "Unable to change to the remote server directory {0}.\nError message was: \"{1}\"" },
        { ERR_FTPEX_FILE,
            "Unable to upload file {0}.\nError message was: \"{1}\"" },
        { ERR_FTPS_REQUIRED,
            "The login, password and server name fields must all be specified." },
        { ERR_AWT_EXCEPTION,
            "An internal error has occurred ({0}).\nPlease shut down the application and contact customer support." },
        { ERR_SAVE_EXISTS,
            "The following file{0,choice,1#|1<s} exist{0,choice,1#s|1<} and will be overwritten if you proceed:\n{1}\nDo you want to overwrite {0,choice,1#this|1<these} file{0,choice,1#|1<s}?" },
        { ERR_EXPORT_DELETE,
            "The following file could not be deleted. Please make sure it is not currently open in another application and try again.\n{0}" },
        { ERR_NEW_IMAGELIST,
            Branding.PRODUCT_NAME + " is loading a set of photos. After the photos are loaded, you can choose an optional MP3 music file and create a show." },
        { ERR_SHOW_LENGTH,
            "The show duration will be {0} minutes, this exceeds the maximum duration of {1} minutes.\nPlease increase the transition and effect speeds (decreasing their durations), reduce the frame rate, or reduce the number of photos in the show." },
        { ERR_LOAD_MODEL_NOFILE,
            "Failed to load the project file {0}. Please make sure the file exists and try again.\n{1}" },
        { ERR_LOAD_MODEL_INVALIDPHOTOS,
            "The following photos in the project could not be found and were ignored:\n{0}" },
        { ERR_LOAD_MODEL_INVALIDSKIN,
            "The project contained a theme ({0}) which could not be found and was ignored." },
        { ERR_LOAD_MODEL_IO,
            "Failed to load the project file {0}. Please make sure the file is readable and try again.\n{1}" },
        { ERR_LOAD_MODEL_FAILED,
            "Failed to load the project file {0}. The file may be corrupt or is not a " + Branding.PRODUCT_NAME + " project." },
        { ERR_SAVE_MODEL_IO,
            "Failed to save the project to {0}. Please make sure the file is writable and try again.\n{1}" },
        { ERR_SAVE_MODEL_FAILED,
            "Failed to save the project to {0} due to an internal error. Please contact customer support." },
        { ERR_CONFIRM_ONE,
            "Only one photo was selected. Do you want to select all photos in the folder instead?" },
        { ERR_CONFIRMSAVE,
            "The current project has been modified. Do you want to save the current project?" },
        { ERR_M3U_CONFIRMSAVE,
            "The playlist has been modified. Do you want to save the current MP3 playlist?" },
        { ERR_M3U_LOAD_IO,
            "Failed to load the MP3 playlist file {0}. Please make sure the file is readable and try again.\n{1}" },
        { ERR_M3U_LOAD_ERR,
            "Failed to load the MP3 playlist file {0}. Please make sure the file is a valid playlist and try again." },
        { ERR_M3U_SAVE_IO,
            "Failed to save the MP3 playlist to {0}. Please make sure the file is writable and try again.\n{1}" },
        { ERR_LOAD_PHOTO,
            "Failed to load the photo {0}.\n{1}" },
    };

    // Overrides ListResourceBundle
    protected Object[][] getContents() {
        return contents;
    }

    public static String getMessage(String key) throws MissingResourceException {
        return RESOURCE_BUNDLE.getString(key);
    }

    public static String getMessage(String key, Object ... arguments) throws MissingResourceException {
        return MessageFormat.format(getMessage(key), arguments);
    }

    public static Object getResource(String key) throws MissingResourceException {
        return RESOURCE_BUNDLE.getObject(key);
    }

    public static int getInteger(String key) throws MissingResourceException {
        return ((Integer)getResource(key)).intValue();
    }
}
