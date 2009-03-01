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

#include "SplashThread.h"
#include <process.h>
#include "photopulse.h"
#include "resource.h"

WNDPROC CSplashThread::g_lpfnOldWndProc = NULL;

// HRESULT errors
#define SPLASH_E_BEGINTHREAD MAKE_HRESULT(SEVERITY_ERROR, FACILITY_SPLASH, 0x01)

HRESULT CSplashThread::ShowSplash(HINSTANCE hinstApp)
{
    m_event.Create(NULL);
    m_hinstApp = hinstApp;

    if (::_beginthread(ThreadProc, 0, this) == -1)
        return SPLASH_E_BEGINTHREAD;
    return S_OK;
}

void CSplashThread::ThreadProc(void *arg)
{
    CSplashThread *splash = (CSplashThread *)arg;

    BOOL result = splash->ShowSplashInternal();

    // Signal event so HideSplash() knows it is safe to close the HWND
    splash->m_event.SetEvent();

    if (!result) {
        TRACE("Failed to load splash, exiting SplashThread\n");
        return;
    }

    splash->MessageLoop();

    TRACE("SplashThread MessageLoop exited\n");
}

void CSplashThread::HideSplash()
{
    // We may be called from any thread to terminate the message loop
    // Wait for event so we know HWND exists.
    // This will return immediately if event never created (ShowSplash not called).
    m_event.WaitEvent();
    m_event.Close();
    if (m_hwndSplash != NULL)
        ::PostMessage(m_hwndSplash, WM_CLOSE, 0, 0);
}

BOOL CSplashThread::ShowSplashInternal()
{
    // Load bitmap from file and set in STATIC control
    HBITMAP hBitmap = (HBITMAP)::LoadImage(m_hinstApp, "splash.bmp", IMAGE_BITMAP, 0, 0, LR_LOADFROMFILE);
    if (hBitmap == NULL)
        return false;

    // Create toplevel STATIC control, it knows how to render bitmaps
    m_hwndSplash = ::CreateWindowEx(
        0,
        "STATIC",
        "PhotoPulse",
        SS_BITMAP | WS_POPUP,
        0,
        0,
        0,
        0,
        NULL,
        NULL,
        m_hinstApp,
        NULL);
    ::SendMessage(m_hwndSplash, STM_SETIMAGE, IMAGE_BITMAP, (LPARAM)hBitmap);

    // Save old wndproc and set our own
    g_lpfnOldWndProc = (WNDPROC)::GetWindowLong(m_hwndSplash, GWL_WNDPROC);
    ::SetWindowLong(m_hwndSplash, GWL_WNDPROC, (LONG)SplashWndProc);

    // Center the splash screen and display it.
    RECT rect;
    ::GetWindowRect(m_hwndSplash, &rect);
    int nScreenWidth = ::GetSystemMetrics(SM_CXSCREEN);
    int nScreenHeight = ::GetSystemMetrics(SM_CYSCREEN);
    int x = (nScreenWidth - (rect.right - rect.left)) / 2;
    int y = (nScreenHeight - (rect.bottom - rect.top)) / 2;
    ::SetWindowPos(m_hwndSplash, 0, x, y, 0, 0, SWP_NOZORDER | SWP_NOSIZE | SWP_NOACTIVATE);
    ::ShowWindow(m_hwndSplash, SW_SHOW);

    return true;
}

void CSplashThread::MessageLoop()
{
    MSG msg;
    // Loop until we get a WM_QUIT message
    while (::GetMessage(&msg, NULL, 0, 0)) {
        ::TranslateMessage(&msg);
        ::DispatchMessage(&msg);
    }

    m_hwndSplash = NULL;
}


LRESULT WINAPI CSplashThread::SplashWndProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
    switch (uMsg) {
        case WM_NCHITTEST:
            return HTCLIENT;
        case WM_CLOSE:
            ::DestroyWindow(hwnd);
            break;
        case WM_DESTROY:
            HBITMAP hBitmap = (HBITMAP)::SendMessage(hwnd, STM_GETIMAGE, IMAGE_BITMAP, (LPARAM)0);
            if (hBitmap != NULL)
                ::DeleteObject(hBitmap);
            ::PostQuitMessage(0);
            break;
    }
    // Call original WndProc for STATIC controls
    return ::CallWindowProc(g_lpfnOldWndProc, hwnd, uMsg, wParam, lParam);
}

void CSplashThread::ErrorMessageBox(DWORD dwMessageID, ...)
{
    va_list vaArgs;
    va_start(vaArgs, dwMessageID);

    // Format message with arguments.
    // We must explicitly load the string since it is in a string table resource,
    // not a message table
    CHAR szMessage[4096] = "Internal error.";   // Resource strings are < 4096 chars
    ::LoadString(m_hinstApp, dwMessageID, szMessage, sizeof(szMessage)/sizeof(CHAR));
    if (::FormatMessage(
            FORMAT_MESSAGE_FROM_STRING,
            szMessage,
            dwMessageID,
            0,
            szMessage,
            sizeof(szMessage)/sizeof(CHAR),
            &vaArgs) == 0) {
        DWORD dwErr = ::GetLastError();
    }
    va_end(vaArgs);

    // Load error caption
    CHAR szCaption[256];
    LPSTR lpszCaption = szCaption;
    if (::LoadString(m_hinstApp, IDS_ERRCAPTION, szCaption, sizeof(szCaption)/sizeof(CHAR)) == 0)
        lpszCaption = NULL;

    // Display message box
    ::MessageBox(m_hwndSplash, szMessage, lpszCaption, MB_ICONERROR);
}