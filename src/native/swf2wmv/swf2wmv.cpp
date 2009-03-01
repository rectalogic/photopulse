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
#include "swf2wmv.h"
#include "comex.h"
#include "swfrenderer.h"
#include "wmvencoder.h"


void LoadProfile(const CHAR *pszProfileFile, CAutoVectorPtr<BYTE> &apProfile) throw(...)
{
    HANDLE hFile = CreateFile(pszProfileFile, GENERIC_READ, FILE_SHARE_READ, 
            NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
    if (INVALID_HANDLE_VALUE == hFile)
        CE(HRESULT_FROM_WIN32(GetLastError()), "CreateFile(profile)");
    CHandle hProfile(hFile);

    DWORD dwLength = GetFileSize(hProfile, NULL);
    if (-1 == dwLength)
        CE(HRESULT_FROM_WIN32(GetLastError()), "GetFileSize(profile)");
    dwLength += sizeof(WCHAR);    

    if (!apProfile.Allocate(dwLength))
        CE(E_OUTOFMEMORY, "CAutoVectorPtr<BYTE>.Allocate(profile)");
    ZeroMemory(apProfile, dwLength);

    DWORD dwBytesRead = 0;
    if (!ReadFile(hProfile, apProfile, dwLength, &dwBytesRead, NULL))
        CE(HRESULT_FROM_WIN32(GetLastError()), "ReadFile(profile)");
}

// Called for C SEH exceptions, rethrow as C++ exception
// Must compile with /EHa
void SETrans(unsigned int uCode, EXCEPTION_POINTERS* pExp) throw(...)
{
    TRACE("Converting C SEH to C++ exception - %x\n", uCode);
    throw COMException(uCode, S_OK, "C SEH exception", __FILE__, __LINE__);
}

void ConvertSWF2WMV(const WCHAR *pwszFlashFile,
                    int nSwfWidth, int nSwfHeight, float fFramesPerSec,
                    const WCHAR *awszAudioFiles[], int nAudioFileCount,
                    const CHAR *pszProfileFile,
                    const WCHAR *pwszOutputVideoFile, IEncodingProgress *pProgress) throw(...)
{
    _se_translator_function seOld = _set_se_translator(SETrans);

    // Initialize renderer with Flash
    CFlashRenderer renderer;
    renderer.Init(pwszFlashFile, nSwfWidth, nSwfHeight, fFramesPerSec);

    // Get Flash bitmap specs for encoder
    BITMAPINFO *bmpInfo = renderer.GetBitmapInfo();

    // Load video profile Unicode data
    CAutoVectorPtr<BYTE> apProfile;
    LoadProfile(pszProfileFile, apProfile);

    // Initialize encoder with Flash bitmap specs and audio files
    CVideoEncoder encoder;
    encoder.Init(pwszOutputVideoFile, bmpInfo, fFramesPerSec,
        awszAudioFiles, nAudioFileCount, (WCHAR *)(BYTE *)apProfile, pProgress);

    // Get Flash bitmap bits
    PVOID pvBitmapBuffer = renderer.GetBitmapBuffer();
    DWORD dwBitmapBufferSize = bmpInfo->bmiHeader.biSizeImage;

    // Render each Flash frame into bitmap, then pass bitmap bits to encoder
    while (renderer.RenderFlashFrame()) {
        // Bail if user cancels
        if (!encoder.AppendVideoFrame(dwBitmapBufferSize, pvBitmapBuffer))
            break;
    }
    encoder.EndWriting(dwBitmapBufferSize, pvBitmapBuffer);

    _set_se_translator(seOld);
}
