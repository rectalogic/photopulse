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

//#define WINDOWS_LEAN_AND_MEAN
//#include <windows.h>

#ifdef _DEBUG
// For Win32 console stuff
#include <io.h>
#include <fcntl.h>
#endif

#include "photopulse.h"
#include "resource.h"
#include "JavaVM.h"
#include "SplashThread.h"
#include "Mutex.h"
#include "Event.h"

void RaisePhotoPulse();
void ProcessRaiseEvents(CJavaVM *pJVM);
#ifdef _DEBUG
void InitDebugConsole();
#endif



int WINAPI WinMain(HINSTANCE hInst, HINSTANCE hPrevInst, LPSTR lpszCmdLine, int nCmdShow)
{
#ifdef _DEBUG
    InitDebugConsole();
#endif

    TRACE("Starting\n");

    // This is just so the installer doesn't run while we are running
    CMutex mtxInstance;
    mtxInstance.Create(INSTANCE_MUTEX_NAME);

    HRESULT hr = S_OK;
    CSplashThread splash;

    int nArgSkip = 1;

    // Don't display splash
    if (__argc > nArgSkip && lstrcmp(__argv[nArgSkip], "-nosplash") == 0)
        nArgSkip++;
    else {
        if (FAILED(hr = splash.ShowSplash(hInst))) {
            splash.ErrorMessageBox(IDS_ERRSTARTUP, hr);
            return 1;
        }
    }

    CJavaVM jvm;
    // Skip argv[0] (app name)
    if (FAILED(hr = jvm.StartPhotoPulse(hInst, __argc - nArgSkip, &__argv[nArgSkip]))) {
        splash.ErrorMessageBox(IDS_ERRSTARTUP, hr);
        return 1;
    }
    splash.HideSplash();

    // This blocks waiting for signals until Java exits via System.exit()
    // We do not use jvm.DestroyJavaVM()
    ProcessRaiseEvents(&jvm);

    return 0;
}

#define SPI_GETFOREGROUNDLOCKTIMEOUT 0x2000
#define SPI_SETFOREGROUNDLOCKTIMEOUT 0x2001

void ProcessRaiseEvents(CJavaVM *pJVM)
{
    // Auto-reset event
    CEvent evtRaiseEvent;
    if (!evtRaiseEvent.Create(RAISE_EVENT_NAME)) {
        TRACE("Failed to create raise event\n");
        return;
    }

    while (true) {
        TRACE("Waiting for event\n");
        if (evtRaiseEvent.WaitEvent()) {
            TRACE("Event signalled\n");

            // Java ends up calling SetForegroundWindow(), but this API has restrictions
            // and usually just ends up flashing the taskbar.
            // Resetting the foreground lock timeout seems to make it work.
            DWORD dwLockTimeout;
            BOOL bSuccess = SystemParametersInfo(SPI_GETFOREGROUNDLOCKTIMEOUT, 0, &dwLockTimeout, 0);
            if (bSuccess)
                SystemParametersInfo(SPI_SETFOREGROUNDLOCKTIMEOUT, 0, 0, 0);
            pJVM->InvokeRaisePhotoPulse();
            if (bSuccess)
                SystemParametersInfo(SPI_SETFOREGROUNDLOCKTIMEOUT, 0, (PVOID)dwLockTimeout, 0);
        }
    }
}

void RaisePhotoPulse()
{
    // Auto-reset event
    CEvent evtRaiseEvent;
    if (!evtRaiseEvent.Create(RAISE_EVENT_NAME)) {
        TRACE("Failed to create raise event\n");
        return;
    }

    TRACE("Firing event\n");

    // Signal the running instance raise itself
    if (!evtRaiseEvent.PulseEvent()) {
        TRACE("Failed to pulse raise event\n");
        return;
    }
}

#ifdef _DEBUG
// Send stdout to a console - so we can Ctrl-Break and see Java stacktraces.
// See KB article Q105305 for why this is so complicated
void InitDebugConsole()
{
    if (::AllocConsole()) {
        ::SetConsoleTitle("PhotoPulse Console");

        int hCRT = _open_osfhandle((long) ::GetStdHandle(STD_OUTPUT_HANDLE), _O_TEXT);
        *stdout = *_fdopen(hCRT, "w");
        setvbuf(stdout, NULL, _IONBF, 0);
    }
}
#endif
