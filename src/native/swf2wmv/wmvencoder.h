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

#include "progress.h"

typedef HRESULT (WINAPI *WMCreateProfileManagerProc)(IWMProfileManager** ppProfileManager);
typedef HRESULT (WINAPI *WMCreateWriterProc)(IUnknown* pUnkReserved,IWMWriter** ppWriter);
typedef HRESULT (WINAPI *WMCreateSyncReaderProc)(IUnknown* pUnkCert, DWORD dwRights, IWMSyncReader** ppSyncReader);

// Handle encoding to WMV video
class CVideoEncoder
{
public:
    CVideoEncoder() :
        m_dwVideoInputNum(0),
        m_dwAudioInputNum(0),
        m_wAudioOutputStreamNum(0),
        m_cnsVideoSampleDuration(0),
        m_cnsAudioSampleTime(0),
        m_cnsVideoSampleTime(0),
        m_cnsSilentAudioSampleDuration(0),
        m_pProgress(NULL),
        m_dwProgressInterval(0),
        m_dwFrameNum(0),
        m_bCanceled(FALSE)
    {
        ZeroMemory(&m_wfexAudio, sizeof(WAVEFORMATEX));
        ZeroMemory(&m_wmtAudio, sizeof(WM_MEDIA_TYPE));
    }

    static void InitLibrary() throw(...);

    void Init(const WCHAR *pwszVideoOutputFile, BITMAPINFO *bmpInfo, FLOAT fFramesPerSec,
        const WCHAR *apwszAudioFiles[], int nAudioFilesCount,
        const WCHAR *pwszProfileData, IEncodingProgress *pProgress) throw(...);

    BOOL AppendVideoFrame(DWORD dwInputBufferSize, PVOID pvInputVideoBuffer) throw(...);
    void EndWriting(DWORD dwInputBufferSize, PVOID pvInputVideoBuffer) throw(...);

    // Video width/height must be even, adjust so it is
    static inline int AdjustDimension(int nDimension)
    {
        return nDimension % 2 == 0 ? nDimension : nDimension + 1;
    }

private:

    void InitAudioFormat();
    void FindInputFormat(IWMWriterPtr &spWriter, GUID guidRequiredInputType,
            DWORD *dwInputNum, IWMInputMediaPropsPtr &spInputProps) throw(...);
    void FindOutputFormat(IWMSyncReaderPtr &spSyncReader, GUID guidRequiredOutputType,
            DWORD *dwOutputNum, IWMOutputMediaPropsPtr &spOutputProps) throw(...);
    void ConfigureAudioInput() throw(...);
    void ConfigureVideoInput(BITMAPINFO *bmpInfo, FLOAT fFramesPerSec) throw(...);
    void ReconfigureProfileVideoStreams(IWMProfilePtr &spProfile, BITMAPINFO *bmpInfo, FLOAT fFramesPerSec) throw(...);
    void CloseCurrentAudioFile() throw(...);
    BOOL OpenNextAudioFile() throw(...);
    void WriteAudioSamples() throw(...);
    void AllocateSilentAudioSample() throw(...);

    static WMCreateProfileManagerProc s_lpfnWMCreateProfileManager;
    static WMCreateWriterProc s_lpfnWMCreateWriter;
    static WMCreateSyncReaderProc s_lpfnWMCreateSyncReader;


    IEncodingProgress *m_pProgress;
    DWORD m_dwProgressInterval;
    DWORD m_dwFrameNum;
    BOOL m_bCanceled;

    IWMWriterPtr m_spWriter;
    DWORD m_dwVideoInputNum;
    DWORD m_dwAudioInputNum;
    LONGLONG m_cnsVideoSampleDuration;
    QWORD m_cnsVideoSampleTime;

    CSimpleArray<const WCHAR *> m_awszAudioFiles;
    IWMSyncReaderPtr m_spSyncReader;
    WORD m_wAudioOutputStreamNum;
    QWORD m_cnsAudioSampleTime;
    INSSBufferPtr m_spSilentAudioSample;
    QWORD m_cnsSilentAudioSampleDuration;

    WM_MEDIA_TYPE m_wmtAudio;
    WAVEFORMATEX m_wfexAudio;

    static DWORD const AUDIO_FREQUENCY = 44100;
    static WORD const AUDIO_CHANNELS = 2;
    static WORD const AUDIO_BITS = 16;
};