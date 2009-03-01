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

#ifndef EVENT_H
#define EVENT_H

#define WINDOWS_LEAN_AND_MEAN
#include <windows.h>

#define RAISE_EVENT_NAME "PhotoPulseRaiseEvent{CC7D313F-E101-4f4c-AD6A-D245C4B9DE76}"


// Manages an manual-reset named event

class CEvent
{
public:
    CEvent() : m_hEvent(0) {};

    ~CEvent() {
        Close();
    };

    BOOL Create(LPSTR lpszName) {
        // Auto-reset event
        m_hEvent = ::CreateEvent(NULL, TRUE, FALSE, lpszName);
        return m_hEvent != NULL;
    };

    BOOL WaitEvent() {
        if (m_hEvent == NULL)
            return FALSE;
        return ::WaitForSingleObject(m_hEvent, INFINITE) == WAIT_OBJECT_0;
    };

    BOOL PulseEvent() {
        if (m_hEvent == NULL)
            return FALSE;
        return ::PulseEvent(m_hEvent);
    };

    BOOL SetEvent() {
        if (m_hEvent == NULL)
            return FALSE;
        return ::SetEvent(m_hEvent);
    };

    void Close() {
        if (m_hEvent != NULL) {
            ::CloseHandle(m_hEvent);
            m_hEvent = NULL;
        }
    };

private:
    HANDLE m_hEvent;
};

#endif