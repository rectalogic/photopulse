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

#ifndef MUTEX_H
#define MUTEX_H

#define WINDOWS_LEAN_AND_MEAN
#include <windows.h>

// Manages a named mutex

class CMutex
{
public:
    CMutex() : m_hMutex(0), m_bLocked(FALSE) {};

    ~CMutex() {
        Close();
    };

    BOOL Create(LPSTR lpszName) {
        m_hMutex = ::CreateMutex(NULL, FALSE, lpszName);

        // Check if we are the first instance to create this mutex
        if (m_hMutex != NULL && ERROR_ALREADY_EXISTS == ::GetLastError())
            m_bFirstCreator = FALSE;
        else
            m_bFirstCreator = TRUE;

        return m_hMutex != NULL;
    };

    BOOL IsFirstCreator() {
        return m_bFirstCreator;
    };

    BOOL Lock() {
        // WAIT_ABANDONED or WAIT_OBJECT_0 are both fine.
        m_bLocked = ::WaitForSingleObject(m_hMutex, INFINITE) != WAIT_FAILED;
        return m_bLocked;
    };

    void Unlock() {
        if (m_bLocked) {
            ::ReleaseMutex(m_hMutex);
            m_bLocked = FALSE;
        }
    };

    void Close() {
        if (m_hMutex != NULL) {
            Unlock();
            ::CloseHandle(m_hMutex);
            m_hMutex = NULL;
        }
    };

private:
    HANDLE m_hMutex;
    BOOL m_bFirstCreator;
    BOOL m_bLocked;
};

#endif