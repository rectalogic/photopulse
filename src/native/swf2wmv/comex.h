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
 * Portions created by the Initial Developer are Copyright (C) 2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */

#pragma once

#define CE(hr,msg) CheckError(hr, S_OK, msg, __FILE__, __LINE__)
// Use CEC when HRESULT is custom
#define CEC(hr,hrc,msg) CheckError(hr, hrc, msg, __FILE__, __LINE__)

#define C_E_FLASHVERSION MAKE_HRESULT(1,FACILITY_NULL,1)

// Exception with informative message.
class COMException
{
public:
    COMException(HRESULT hr, HRESULT hrCustom, const char *pszMsg, const char *pszFile, int nLine)
        : m_hr(hr), m_hrCustom(hrCustom), m_strMsg(pszMsg)
    {
        m_strLocation.Format("%s:%d", pszFile, nLine);
    }

    HRESULT CustomError()
    {
        return m_hrCustom;
    }

    HRESULT Error()
    {
        return m_hr;
    }

    const CAtlString &ErrorMessage()
    {
        return m_strMsg;
    }

    const CAtlString &Location()
    {
        return m_strLocation;
    }

private:
    HRESULT m_hr;
    HRESULT m_hrCustom;
    CAtlString m_strMsg;
    CAtlString m_strLocation;
};

// Check HRESULT and throw exception if failed
inline void CheckError(HRESULT hr, HRESULT hrCustom, const char *pszMsg, const char *pszFile, int nLine) throw(...)
{
    if (FAILED(hr))
        throw COMException(hr, hrCustom, pszMsg, pszFile, nLine);
}