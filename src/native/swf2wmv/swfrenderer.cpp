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

#include "stdafx.h"
#include "comex.h"
#include "swfrenderer.h"
#include "wmvencoder.h"

#define FLASH_QUALITY_HIGH 1


CFlashRenderer::~CFlashRenderer()
{
    if (m_hBitmap)
        DeleteObject(m_hBitmap);
    if (m_hdcBitmap)
        DeleteDC(m_hdcBitmap);
    if (m_pspFlashTimer)
        m_pspFlashTimer->Release();
    
    // Release these before destroying window.
    // Otherwise can get access violations under some circumstances.
    m_spFlash = NULL;
    m_spFlashView = NULL;

    if (m_axFlash.IsWindow())
        m_axFlash.DestroyWindow();
}

void CFlashRenderer::Init(const WCHAR *pwszFlashFile, int nSwfWidth, int nSwfHeight, FLOAT fFramesPerSec) throw(...)
{
    CreateFlashControl(pwszFlashFile, fFramesPerSec, nSwfWidth, nSwfHeight);

    // Video dimensions must be even, so pad bitmap if needed
    CreateBitmap(CVideoEncoder::AdjustDimension(nSwfWidth),
        CVideoEncoder::AdjustDimension(nSwfHeight));
}

// Draw the current frame into the bitmap, then increment to next frame.
// Return FALSE if no more frames to render.
BOOL CFlashRenderer::RenderFlashFrame() throw(...)
{
    // If current frame is last, we are done
    if (m_lCurrentFrame == m_lTotalFrames - 1)
        return FALSE;

    // Increment frame (initialized to -1)
    m_lCurrentFrame++;

    // Loop until Flash increments to our desired frame
    long lFlashFrame = 0;
    CE(m_spFlash->CurrentFrame(&lFlashFrame), "IShockwaveFlash::CurrentFrame");
    while (lFlashFrame < m_lCurrentFrame) {
        m_pspFlashTimer->NextTimer();
        CE(m_spFlash->CurrentFrame(&lFlashFrame), "IShockwaveFlash::CurrentFrame");
    }

    // Render current frame to bitmap
    CE(m_spFlashView->Draw(DVASPECT_CONTENT, -1, NULL, NULL, NULL, m_hdcBitmap, &m_rcFlash, NULL, NULL, NULL),
        "IViewObject::Draw");

    m_pspFlashTimer->NextTimer();

    return TRUE;
}


// Create hosted Flash control
void CFlashRenderer::CreateFlashControl(const WCHAR *pwszFlashFile, FLOAT fFramesPerSec, int nWidth, int nHeight) throw(...)
{
    // Save rect for IViewObject::Draw
    m_rcFlash.top = m_rcFlash.left = 0;
    m_rcFlash.bottom = nHeight;
    m_rcFlash.right = nWidth;

    // Create hosting HWND of same size.
    RECT rc = {0, 0, nWidth, nHeight};
    if (NULL == m_axFlash.Create(NULL, &rc, NULL, 0))
        CE(E_FAIL, "CAxWindow::Create");
    AdjustWindowRect(&rc, m_axFlash.GetWindowLong(GWL_STYLE), FALSE);
    m_axFlash.SetWindowPos(NULL, 0, 0, rc.right - rc.left, rc.bottom - rc.top,
        SWP_NOMOVE|SWP_NOREDRAW|SWP_DEFERERASE|SWP_NOACTIVATE
        |SWP_NOCOPYBITS|SWP_NOOWNERZORDER|SWP_NOSENDCHANGING|SWP_NOZORDER);

    // Creates Flash ActiveX control using the progid
    CEC(m_axFlash.CreateControl(OLESTR("ShockwaveFlash.ShockwaveFlash")),
        C_E_FLASHVERSION, "CAxWindow::CreateControl");

    // Get Flash interface
    CE(m_axFlash.QueryControl(&m_spFlash),
        "CAxWindow::QueryControl");

    // Need Flash v5 or higher for the ITimerService stuff to work.
    // Versions less than 4 may not even have the FlashVersion property.
    long lFlashVersion = 0;
    CEC(m_spFlash->FlashVersion(&lFlashVersion),
        C_E_FLASHVERSION, "IShockwaveFlash::FlashVersion");
    if (lFlashVersion < 0x050000) {
        CAtlStringA str;
        str.Format("IShockwaveFlash::FlashVersion(0x%x)", lFlashVersion);
        CEC(E_FAIL, C_E_FLASHVERSION, str);
    }

    // Create our ITimerService COM object and initialize framerate
    CE(CComObject<CFlashTimer>::CreateInstance(&m_pspFlashTimer),
        "CComObject<CFlashTimer>::CreateInstance");
    m_pspFlashTimer->AddRef();
    m_pspFlashTimer->SetFrameRate(fFramesPerSec);

    // QI the ActiveX host for IObjectWithSite
    IObjectWithSitePtr spObjectSite;
    CE(m_axFlash.QueryHost(&spObjectSite),
        "CAxWindow::QueryHost");

    // Get our timers IUnknown and set it as the site.
    // CAxWindow will then use it as it's IServiceProvider implementation.
    IUnknownPtr spUnknown;
    CE(m_pspFlashTimer->QueryInterface(&spUnknown),
        "CFlashTimer::QueryInterface");
    CE(spObjectSite->SetSite(spUnknown),
        "IObjectWithSite::SetSite");

    // Save IViewObject so we can use it to draw into the bitmap
    CE(m_axFlash.QueryControl(&m_spFlashView),
        "CAxWindow::QueryControl");

    // Load the Flash SWF movie file
    CE(m_spFlash->put_Loop(VARIANT_FALSE),
        "IShockwaveFlash::put_Loop");
    CE(m_spFlash->put_Quality(FLASH_QUALITY_HIGH),
        "IShockwaveFlash::put_Quality");
    CE(m_spFlash->put_Movie(_bstr_t(pwszFlashFile)),
        "IShockwaveFlash::put_Movie");

    // This fails with E_PENDING if swf did not load
    CE(m_spFlash->get_TotalFrames(&m_lTotalFrames),
        "IShockwaveFlash::get_TotalFrames");

    TRACE("TotalFrames=%d\n", m_lTotalFrames);
}

// Create bitmap for Flash to draw into
void CFlashRenderer::CreateBitmap(int nWidth, int nHeight) throw(...)
{
    // Setup bitmap to be compatible with WMV encoding
    ZeroMemory(&m_bmpInfo, sizeof(BITMAPINFO));
    m_bmpInfo.bmiHeader.biSize = sizeof(BITMAPINFO);
    m_bmpInfo.bmiHeader.biPlanes = 1;
    m_bmpInfo.bmiHeader.biBitCount = 24;
    m_bmpInfo.bmiHeader.biCompression = BI_RGB;
    // Negative height makes image top-down
    m_bmpInfo.bmiHeader.biHeight = -nHeight;
    m_bmpInfo.bmiHeader.biWidth = nWidth;
    // Scanlines are padded to nearest 4 bytes (we have 3 bytes per pixel, 24bit)
    m_bmpInfo.bmiHeader.biSizeImage = (((nWidth * 3) + 3) & ~3) * nHeight;

    // Create an HDC for Flash to render to
    m_hdcBitmap = CreateCompatibleDC(NULL);
    if (NULL == m_hdcBitmap)
        CE(E_FAIL, "CreateCompatibleDC");
    // Create bitmap and select into HDC
    m_hBitmap = CreateDIBSection(m_hdcBitmap, &m_bmpInfo, DIB_RGB_COLORS, &m_pvBitmap, NULL, 0);
    if (NULL == m_hBitmap)
        CE(E_FAIL, "CreateDIBSection");
    if (NULL == SelectObject(m_hdcBitmap, m_hBitmap))
        CE(E_FAIL, "SelectObject");
}