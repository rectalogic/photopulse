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

#ifndef SPLASHTHREAD_H
#define SPLASHTHREAD_H

#define WINDOWS_LEAN_AND_MEAN
#include <windows.h>
#include "Event.h"

class CSplashThread
{
public:
    CSplashThread() : m_hinstApp(0), m_hwndSplash(0) {};

    ~CSplashThread() {
        HideSplash();
    };

    HRESULT ShowSplash(HINSTANCE hInst);
    void HideSplash();
    void ErrorMessageBox(DWORD dwMessageID, ...);

private:

    BOOL ShowSplashInternal();
    void MessageLoop();

    static LRESULT WINAPI SplashWndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
    static void ThreadProc(void *arg);

    CEvent m_event;
    HWND m_hwndSplash;
    HINSTANCE m_hinstApp;
    static WNDPROC g_lpfnOldWndProc;
};

#endif