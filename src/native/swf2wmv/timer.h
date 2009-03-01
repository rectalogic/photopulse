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

#include "comex.h"

// ITimerService COM object.
// Also implements ITimer and IServiceProvider.
// Flash QIs for IServiceProvider, then QS that for ITimerService.
// Then gets an ITimer from that. The ITimer can be used to control the framerate.
class CFlashTimer :
    public CComObjectRoot,
    public IServiceProviderImpl<CFlashTimer>,
    public ITimerService,
    public ITimer
{
public:
    CFlashTimer() :
        m_fSecondsPerFrame(0),
        m_dwCurrentFrame(0)
    {
        m_vTime.ulVal = 0;
        m_vTime.vt = VT_UI4;
    }

#ifdef _DEBUG
    void FinalRelease()
    {
        TRACE("CFlashTimer::FinalRelease\n");
    }
#endif

    BEGIN_COM_MAP(CFlashTimer)
        COM_INTERFACE_ENTRY(IServiceProvider)
        COM_INTERFACE_ENTRY(ITimerService)
        COM_INTERFACE_ENTRY(ITimer)
    END_COM_MAP()

    BEGIN_SERVICE_MAP(CFlashTimer)
        SERVICE_ENTRY(SID_STimerService)
    END_SERVICE_MAP()

    // ITimerService
    HRESULT STDMETHODCALLTYPE CreateTimer(ITimer *pReferenceTimer, ITimer **ppNewTimer)
    {
        // QI ourself for ITimer
        return QueryInterface(IID_ITimer, (void **)ppNewTimer);
    }

    // ITimerService
    HRESULT STDMETHODCALLTYPE GetNamedTimer(REFGUID rguidName, ITimer **ppTimer)
    {
        return E_NOTIMPL;
    }

    // ITimerService
    HRESULT STDMETHODCALLTYPE SetNamedTimerReference(REFGUID rguidName, ITimer *pReferenceTimer)
    {
        return E_NOTIMPL;
    }

    // ITimer
    HRESULT STDMETHODCALLTYPE Advise(VARIANT vtimeMin, VARIANT vtimeMax, VARIANT vtimeInterval,
            DWORD dwFlags, ITimerSink *pTimerSink, DWORD *pdwCookie)
    {
        TRACE("CFlashTimer::Advise\n");
        if (NULL != m_spTimerSink)
            return E_FAIL;
        *pdwCookie = COOKIE;
        m_spTimerSink = pTimerSink;
        return S_OK;
    }

    // ITimer
    HRESULT STDMETHODCALLTYPE Freeze(BOOL fFreeze)
    {
        return S_OK;
    }

    // ITimer
    HRESULT STDMETHODCALLTYPE GetTime(VARIANT *pvTime)
    {
        VariantCopy(pvTime, &m_vTime);
        return S_OK;
    }

    // ITimer
    HRESULT STDMETHODCALLTYPE Unadvise(DWORD dwCookie)
    {
        if (dwCookie == COOKIE)
            m_spTimerSink = NULL;
        return S_OK;
    }

    void SetFrameRate(FLOAT fFramesPerSec)
    {
        m_fSecondsPerFrame = 1.0f / fFramesPerSec;
    }

    // Adjust time to sync to the next frame.
    // Report this new time to Flash.
    // Flash seems to use its own internal clock, so this method must be
    // called repeatedly until Flash reports the desired CurrentFrame.
    void NextTimer() throw(...)
    {
        if (NULL == m_spTimerSink)
            CE(E_POINTER, "ITimerSink(NULL)");

        // Report time in milliseconds
        m_vTime.ulVal = (ULONG)(m_dwCurrentFrame * m_fSecondsPerFrame * 1000);

        CE(m_spTimerSink->OnTimer(m_vTime), "ITimerSink::OnTimer");

        m_dwCurrentFrame++;
    }

private:
    ITimerSinkPtr m_spTimerSink;
    // DWORD variant, current time in milliseconds
    CComVariant m_vTime;
    FLOAT m_fSecondsPerFrame;
    DWORD m_dwCurrentFrame;

    static DWORD const COOKIE = 1;
};
