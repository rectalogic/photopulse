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

#include "timer.h"

// Render Flash frame by frame into an HBITMAP
class CFlashRenderer
{
public:

    CFlashRenderer() :
        m_hBitmap(NULL),
        m_hdcBitmap(NULL),
        m_pspFlashTimer(NULL),
        m_lTotalFrames(0),
        m_lCurrentFrame(-1) // Start at -1 since we increment to 0 first time
    {
    }

    ~CFlashRenderer();

    void Init(const WCHAR *pwszFlashFile, int nSwfWidth, int nSwfHeight, FLOAT fFramesPerSec) throw(...);
    BOOL RenderFlashFrame() throw(...);
    
    PVOID GetBitmapBuffer()
    {
        return m_pvBitmap;
    }

    BITMAPINFO *GetBitmapInfo()
    {
        return &m_bmpInfo;
    }
    
private:

    void CreateFlashControl(const WCHAR *pwszFlashFile, FLOAT fFramesPerSec, int nWidth, int nHeight) throw(...);
    void CreateBitmap(int nWidth, int nHeight) throw(...);
    
    CAxWindow m_axFlash;
    ShockwaveFlashObjects::IShockwaveFlashPtr m_spFlash;
    IViewObjectPtr m_spFlashView;
    CComObject<CFlashTimer> *m_pspFlashTimer;

    // Bitmap info for WMV encoding
    BITMAPINFO m_bmpInfo;
    // Device context for IViewObject::Draw
    HDC m_hdcBitmap;
    // Bitmap handle, so we can free it
    HBITMAP m_hBitmap;
    // Bitmap bits for WMV encoding
    PVOID m_pvBitmap;
    // Flash rect, for IViewObject::Draw
    RECTL m_rcFlash;
    // Total number of Flash frames
    long m_lTotalFrames;
    // Current Flash frame (zero based)
    long m_lCurrentFrame;
};