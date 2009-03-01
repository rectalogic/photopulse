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
#include "wmvencoder.h"


WMCreateProfileManagerProc CVideoEncoder::s_lpfnWMCreateProfileManager = NULL;
WMCreateWriterProc CVideoEncoder::s_lpfnWMCreateWriter = NULL;
WMCreateSyncReaderProc CVideoEncoder::s_lpfnWMCreateSyncReader = NULL;

// Load wmvcore.dll and initialize function pointers
void CVideoEncoder::InitLibrary() throw(...)
{
    HMODULE hDLL = LoadLibrary(_T("wmvcore.dll"));
    CE(hDLL ? S_OK : E_FAIL, "LoadLibrary");

    s_lpfnWMCreateProfileManager = (WMCreateProfileManagerProc)GetProcAddress(hDLL, _T("WMCreateProfileManager"));
    CE(s_lpfnWMCreateProfileManager ? S_OK : E_FAIL, "GetProcAddress(WMCreateProfileManager)");

    s_lpfnWMCreateWriter = (WMCreateWriterProc)GetProcAddress(hDLL, _T("WMCreateWriter"));
    CE(s_lpfnWMCreateWriter ? S_OK : E_FAIL, "GetProcAddress(WMCreateWriter)");

    s_lpfnWMCreateSyncReader = (WMCreateSyncReaderProc)GetProcAddress(hDLL, _T("WMCreateSyncReader"));
    CE(s_lpfnWMCreateSyncReader ? S_OK : E_FAIL, "GetProcAddress(WMCreateSyncReader)");
}

void CVideoEncoder::InitAudioFormat()
{
    m_wfexAudio.wFormatTag = WAVE_FORMAT_PCM;
    m_wfexAudio.nChannels = AUDIO_CHANNELS;
    m_wfexAudio.nSamplesPerSec = AUDIO_FREQUENCY;
    m_wfexAudio.wBitsPerSample = AUDIO_BITS;
    m_wfexAudio.nBlockAlign = (WORD)((AUDIO_CHANNELS * AUDIO_BITS) >> 3);
    m_wfexAudio.nAvgBytesPerSec = AUDIO_FREQUENCY * m_wfexAudio.nBlockAlign;
    m_wfexAudio.cbSize = 0;

    m_wmtAudio.majortype = WMMEDIATYPE_Audio;
    m_wmtAudio.subtype = WMMEDIASUBTYPE_PCM;
    m_wmtAudio.bFixedSizeSamples = TRUE;
    m_wmtAudio.bTemporalCompression = FALSE;
    m_wmtAudio.lSampleSize = (AUDIO_CHANNELS * AUDIO_BITS) >> 3;
    m_wmtAudio.formattype = WMFORMAT_WaveFormatEx;
    m_wmtAudio.pUnk = NULL;
    m_wmtAudio.cbFormat = sizeof(WAVEFORMATEX);
    m_wmtAudio.pbFormat = (BYTE *)&m_wfexAudio;
}

void CVideoEncoder::Init(const WCHAR *pwszVideoOutputFile, BITMAPINFO *bmpInfo,
        FLOAT fFramesPerSec, const WCHAR *apwszAudioFiles[], int nAudioFilesCount,
        const WCHAR *pwszProfileData, IEncodingProgress *pProgress) throw(...)
{
    ATLASSERT(s_lpfnWMCreateProfileManager);
    ATLASSERT(s_lpfnWMCreateWriter);
    ATLASSERT(s_lpfnWMCreateSyncReader);

    // Get progress info
    m_pProgress = pProgress;
    if (pProgress)
        m_dwProgressInterval = pProgress->GetProgressInterval();

    // Number of 100ns units per frame
    m_cnsVideoSampleDuration = (LONGLONG)(10000L * 1000L / fFramesPerSec);

    // Create profile manager and load a profile from prx string
    IWMProfileManagerPtr spProfileManager;
    CE(s_lpfnWMCreateProfileManager(&spProfileManager), "WMCreateProfileManager");

    // Load profile from string data
    IWMProfilePtr spProfile;
    CE(spProfileManager->LoadProfileByData(pwszProfileData, &spProfile),
        "IWMProfile::LoadProfileByData");

    // Modify profile so video stream framerates and frame sizes match source
    ReconfigureProfileVideoStreams(spProfile, bmpInfo, fFramesPerSec);

    // Create writer and assign it our profile
    CE(s_lpfnWMCreateWriter(NULL, &m_spWriter), "WMCreateWriter");
    CE(m_spWriter->SetProfile(spProfile),
        "IWMWriter::SetProfile");

    // Configure writer video input to accept our sample data
    ConfigureVideoInput(bmpInfo, fFramesPerSec);

    // Setup for audio encoding if any audio files specified
    if (nAudioFilesCount > 0) {
        // Add files to our list
        for (int i = 0; i < nAudioFilesCount; i++)
            m_awszAudioFiles.Add(apwszAudioFiles[i]);

        // Initialize audio format sample specs
        InitAudioFormat();

        // Configure writer audio input to accept our sample data
        ConfigureAudioInput();

        // Create the reader
        CE(s_lpfnWMCreateSyncReader(NULL, 0, &m_spSyncReader), "WMCreateSyncReader");

        // Open and configure the first audio file
        OpenNextAudioFile();
    }

    // Set output filename on writer
    CE(m_spWriter->SetOutputFilename(pwszVideoOutputFile),
        "IWMWriter::SetOutputFilename");

    // Initialize writing on writer
    CE(m_spWriter->BeginWriting(),
        "IWMWriter::BeginWriting");
}

// Find input number and media props on writer for a given media type
void CVideoEncoder::FindInputFormat(IWMWriterPtr &spWriter, GUID guidRequiredInputType,
        DWORD *dwInputNum, IWMInputMediaPropsPtr &spInputProps) throw(...)
{
    DWORD dwInputCount = 0;
    CE(spWriter->GetInputCount(&dwInputCount),
        "IWMWriter::GetInputCount");

    for (DWORD i = 0; i < dwInputCount; i++) {

        CE(spWriter->GetInputProps(i, &spInputProps),
            "IWMWriter::GetInputProps");

        GUID guidInputType;
        CE(spInputProps->GetType(&guidInputType),
            "IWMInputMediaProps::GetType");

        // If this input handles our input type, break and use it
        if (guidRequiredInputType == guidInputType) {
            *dwInputNum = i;
            break;
        }
        else
            spInputProps = NULL;
    }

    // Profile does not accept input of required type
    if (NULL == spInputProps)
        CE(NS_E_INVALID_INPUT_FORMAT, "IWMInputMediaProps::GetType(no supported input)");
}

// Find output number and media props on reader for a given media type
void CVideoEncoder::FindOutputFormat(IWMSyncReaderPtr &spSyncReader, GUID guidRequiredOutputType,
        DWORD *dwOutputNum, IWMOutputMediaPropsPtr &spOutputProps) throw(...)
{
    DWORD dwOutputCount = 0;
    CE(spSyncReader->GetOutputCount(&dwOutputCount),
        "IWMSyncReader::GetOutputCount");

    for (DWORD i = 0; i < dwOutputCount; i++) {

        CE(spSyncReader->GetOutputProps(i, &spOutputProps),
            "IWMSyncReader::GetOutputProps");

        GUID guidOutputType;
        CE(spOutputProps->GetType(&guidOutputType),
            "IWMOutputMediaProps::GetType");

        // If this output handles our output type, break and use it
        if (guidRequiredOutputType == guidOutputType) {
            *dwOutputNum = i;
            break;
        }
        else
            spOutputProps = NULL;
    }

    // Profile does not accept input of required type
    if (NULL == spOutputProps)
        CE(NS_E_INVALID_INPUT_FORMAT, "IWMOutputMediaProps::GetType(no supported output)");
}

void CVideoEncoder::ConfigureAudioInput() throw(...)
{
    // Search for audio input on writer
    IWMInputMediaPropsPtr spAudioInputProps;
    FindInputFormat(m_spWriter, WMMEDIATYPE_Audio, &m_dwAudioInputNum, spAudioInputProps);

    // Configure audio input to accept our sample data
    CE(spAudioInputProps->SetMediaType(&m_wmtAudio),
        "IWMInputMediaProps::SetMediaType");

    // Set video input props on writer
    CE(m_spWriter->SetInputProps(m_dwAudioInputNum, spAudioInputProps),
        "IWMWriter::SetInputProps");
}

void CVideoEncoder::ConfigureVideoInput(BITMAPINFO *bmpInfo, FLOAT fFramesPerSec) throw(...)
{
    // Search for video input on writer
    IWMInputMediaPropsPtr spVideoInputProps;
    FindInputFormat(m_spWriter, WMMEDIATYPE_Video, &m_dwVideoInputNum, spVideoInputProps);

    // Setup video info based on bitmap
    WMVIDEOINFOHEADER wmvInfo;
    ZeroMemory(&wmvInfo, sizeof(WMVIDEOINFOHEADER));
    wmvInfo.rcSource.left = 0;
    wmvInfo.rcSource.top = 0;
    wmvInfo.rcSource.right = bmpInfo->bmiHeader.biWidth;
    wmvInfo.rcSource.bottom = abs(bmpInfo->bmiHeader.biHeight);
    wmvInfo.rcTarget = wmvInfo.rcSource;
    wmvInfo.dwBitRate = (DWORD)(bmpInfo->bmiHeader.biSizeImage * fFramesPerSec);
    wmvInfo.dwBitErrorRate = 0;
    // Time per frame in 100ns units
    wmvInfo.AvgTimePerFrame = m_cnsVideoSampleDuration;
    wmvInfo.bmiHeader = bmpInfo->bmiHeader;

    // Setup input video media type descriptor
    WM_MEDIA_TYPE wmtVideo;
    ZeroMemory(&wmtVideo, sizeof(WM_MEDIA_TYPE));
    wmtVideo.majortype = WMMEDIATYPE_Video;
    wmtVideo.subtype = WMMEDIASUBTYPE_RGB24;
    wmtVideo.bFixedSizeSamples = TRUE;
    wmtVideo.bTemporalCompression = FALSE;
    wmtVideo.lSampleSize = bmpInfo->bmiHeader.biSizeImage;
    wmtVideo.formattype = WMFORMAT_VideoInfo;
    wmtVideo.pUnk = NULL;
    wmtVideo.cbFormat = sizeof(WMVIDEOINFOHEADER);
    wmtVideo.pbFormat = (BYTE*)&wmvInfo;

    // Set video descriptor on input video props
    CE(spVideoInputProps->SetMediaType(&wmtVideo),
        "IWMInputMediaProps::SetMediaType");

    // Set video input props on writer
    CE(m_spWriter->SetInputProps(m_dwVideoInputNum, spVideoInputProps),
        "IWMWriter::SetInputProps");
}

// Reconfigure profile to match framerate and frame size
void CVideoEncoder::ReconfigureProfileVideoStreams(IWMProfilePtr &spProfile, BITMAPINFO *bmpInfo, FLOAT fFramesPerSec) throw(...)
{
    // Get total number of streams in profile
    DWORD dwStreamCount = 0;
    CE(spProfile->GetStreamCount(&dwStreamCount),
        "IWMProfile::GetStreamCount");

    // Check each stream, reconfigure video streams
    for (DWORD i = 0; i < dwStreamCount; i++) {
        // Get stream config for this index
        IWMStreamConfigPtr spStreamConfig;
        CE(spProfile->GetStream(i, &spStreamConfig),
            "IWMProfile::GetStream");

        // Check that this stream is video
        GUID guidStreamType;
        CE(spStreamConfig->GetStreamType(&guidStreamType),
            "IWMStreamConfig::GetStreamType");

        if (WMMEDIATYPE_Video != guidStreamType)
            continue;

        // Get media props for this video stream
        IWMMediaPropsPtr spMediaProps;
        CE(spStreamConfig->QueryInterface(&spMediaProps),
            "IWMStreamConfig::QueryInterface");

        // Find out how much memory is needed for media data
        DWORD cbMediaSize = 0;
        CE(spMediaProps->GetMediaType(NULL, &cbMediaSize),
            "IWMMediaProps::GetMediaType");

        // Allocate buffer for WM_MEDIA_TYPE
        CAutoVectorPtr<BYTE> apMediaData(new BYTE[cbMediaSize]);
        if (apMediaData == NULL)
            CE(E_OUTOFMEMORY, "CAutoVectorPtr<BYTE>");
        ZeroMemory(apMediaData, cbMediaSize);

        // Populate allocated WM_MEDIA_TYPE buffer
        CE(spMediaProps->GetMediaType((WM_MEDIA_TYPE *)(BYTE *)apMediaData, &cbMediaSize),
            "IWMMediaProps::GetMediaType");

        // Ignore if this is not normal video format for some reason
        if (WMFORMAT_VideoInfo != ((WM_MEDIA_TYPE *)(BYTE *)apMediaData)->formattype)
            continue;

        // Reconfigure video size and framerate
        WMVIDEOINFOHEADER *pwmvInfo = (WMVIDEOINFOHEADER *)(((WM_MEDIA_TYPE *)(BYTE *)apMediaData)->pbFormat);
        // Set rects if not specified in profile
        if (pwmvInfo->rcSource.right == pwmvInfo->rcSource.bottom == 0) {
            pwmvInfo->rcSource.right = pwmvInfo->rcSource.left + bmpInfo->bmiHeader.biWidth;
            pwmvInfo->rcSource.bottom = pwmvInfo->rcSource.top + abs(bmpInfo->bmiHeader.biHeight);
        }
        if (pwmvInfo->rcTarget.right == pwmvInfo->rcTarget.bottom == 0) {
            pwmvInfo->rcTarget.right = pwmvInfo->rcTarget.left + bmpInfo->bmiHeader.biWidth;
            pwmvInfo->rcTarget.bottom = pwmvInfo->rcTarget.top + abs(bmpInfo->bmiHeader.biHeight);
        }
        pwmvInfo->AvgTimePerFrame = m_cnsVideoSampleDuration;
        pwmvInfo->bmiHeader.biHeight = abs(bmpInfo->bmiHeader.biHeight);
        pwmvInfo->bmiHeader.biWidth = bmpInfo->bmiHeader.biWidth;

        // Store modified media data back
        CE(spMediaProps->SetMediaType((WM_MEDIA_TYPE *)(BYTE *)apMediaData),
            "IWMMediaProps::SetMediaType");

        // Notify profile of changed stream config
        CE(spProfile->ReconfigStream(spStreamConfig),
            "IWMProfile::ReconfigStream");
    }
}


void CVideoEncoder::CloseCurrentAudioFile() throw(...)
{
    if (NULL == m_spSyncReader)
        return;

    CE(m_spSyncReader->Close(), "IWMSyncReader::Close");
}

// Open and configure next audio file in list
BOOL CVideoEncoder::OpenNextAudioFile() throw(...)
{
    if (NULL == m_spSyncReader)
        return FALSE;

    // No more files
    if (m_awszAudioFiles.GetSize() == 0)
        return FALSE;

    // Get and remove head audio file from list
    const WCHAR *pwszAudioFile = m_awszAudioFiles[0];
    m_awszAudioFiles.RemoveAt(0);

    // Open the MP3 file.
    // Special handling so we can include filename in error.
    HRESULT hr = m_spSyncReader->Open(pwszAudioFile);
    if (FAILED(hr))
        CE(hr, CAtlString(L"IWMSyncReader::Open-") + pwszAudioFile);

    // Find the audio output we will read from
    IWMOutputMediaPropsPtr spOutputProps;
    DWORD dwAudioOutputNum = 0;
    FindOutputFormat(m_spSyncReader, WMMEDIATYPE_Audio, &dwAudioOutputNum, spOutputProps);

    // Get stream number for output, used when reading samples
    CE(m_spSyncReader->GetStreamNumberForOutput(dwAudioOutputNum, &m_wAudioOutputStreamNum),
        "IWMSyncReader::GetStreamNumberForOutput");

    // Configure audio output to return our sample format
    CE(spOutputProps->SetMediaType(&m_wmtAudio),
        "IWMOutputMediaProps::SetMediaType");

    // Set audio output props on reader
    CE(m_spSyncReader->SetOutputProps(dwAudioOutputNum, spOutputProps),
        "IWMSyncReader::SetOutputProps");

    return TRUE;
}

// Write any audio samples needed to sync up to the current video sample time
void CVideoEncoder::WriteAudioSamples() throw(...)
{
    if (NULL == m_spSyncReader)
        return;

    INSSBufferPtr spSample;

    while (m_cnsAudioSampleTime < m_cnsVideoSampleTime) {
        // Ref the sample we use - either the sample read or the silent sample
        INSSBufferPtr &spSampleRef = spSample;
        DWORD dwFlags = 0;
        QWORD cnsDuration = 0;

        // If we don't have a silent sample, then read the next audio sample
        if (NULL == m_spSilentAudioSample) {
            QWORD cnsSampleTime = 0;
            HRESULT hr = m_spSyncReader->GetNextSample(m_wAudioOutputStreamNum, &spSample,
                &cnsSampleTime, &cnsDuration, &dwFlags, NULL, NULL);
            // If no more samples, then open the next audio file.
            // If no more files, then allocate the silent sample and write it from now on.
            if (hr == NS_E_NO_MORE_SAMPLES) {
                dwFlags = WM_SF_CLEANPOINT;
                CloseCurrentAudioFile();
                if (!OpenNextAudioFile()) {
                    AllocateSilentAudioSample();
                    spSampleRef = m_spSilentAudioSample;
                    cnsDuration = m_cnsSilentAudioSampleDuration;
                }
                // Loop around again to read first sample from next file
                else
                    continue;
            }
            else {
                CE(hr, "IWMSyncReader::GetNextSample");
                spSampleRef = spSample;
            }
        }
        else {
            spSampleRef = m_spSilentAudioSample;
            cnsDuration = m_cnsSilentAudioSampleDuration;
        }

        // Write the audio sample (either the read sample or silence)
        CE(m_spWriter->WriteSample(m_dwAudioInputNum, m_cnsAudioSampleTime, dwFlags, spSampleRef),
            "IWMWriter::WriteSample(audio)");

        m_cnsAudioSampleTime += cnsDuration;
    }
}

// Allocate an audio sample of silence
void CVideoEncoder::AllocateSilentAudioSample() throw(...)
{
    // Compute duration (in 100ns units) of a single sample
    float fSilentSampleDuration = (10000.0f * 1000.0f / (float)AUDIO_FREQUENCY);
    // Figure out how many samples needed to equal one video sample
    DWORD dwSampleCount = (DWORD)((float)m_cnsVideoSampleDuration / fSilentSampleDuration);
    // Compute more accurately the overall duration of that many samples
    m_cnsSilentAudioSampleDuration = (LONGLONG)(dwSampleCount * fSilentSampleDuration);

    // Allocate space for the computed number of samples
    CE(m_spWriter->AllocateSample(m_wmtAudio.lSampleSize * dwSampleCount, &m_spSilentAudioSample),
        "IWMWriter::AllocateSample");

    // Get the byte buffer from the sample
    BYTE *pbAudioBuffer = NULL;
    DWORD cbAudioBuffer = 0;
    CE(m_spSilentAudioSample->GetBufferAndLength(&pbAudioBuffer, &cbAudioBuffer),
        "INSSBuffer::GetBufferAndLength");

    // For 16bit unsigned PCM, silence is zero
    ZeroMemory(pbAudioBuffer, cbAudioBuffer);
}

// Add video bits to a new frame, also add needed audio samples to current frame.
// Return FALSE if user canceled.
BOOL CVideoEncoder::AppendVideoFrame(DWORD dwInputBufferSize, PVOID pvInputVideoBuffer) throw(...)
{
    // Allocate a video sample
    INSSBufferPtr spSample;
    CE(m_spWriter->AllocateSample(dwInputBufferSize, &spSample),
        "IWMWriter::AllocateSample");

    // Get the byte buffer from the sample
    BYTE *pbVideoBuffer = NULL;
    DWORD cbVideoBuffer = 0;
    CE(spSample->GetBufferAndLength(&pbVideoBuffer, &cbVideoBuffer),
        "INSSBuffer::GetBufferAndLength");

    // Copy input video bits into sample byte buffer
    CopyMemory(pbVideoBuffer, pvInputVideoBuffer, cbVideoBuffer);

    // Write the sample for the computed time
    CE(m_spWriter->WriteSample(m_dwVideoInputNum, m_cnsVideoSampleTime, 0, spSample),
        "IWMWriter::WriteSample(video)");

    // Update sample time in 100ns units
    m_cnsVideoSampleTime += m_cnsVideoSampleDuration;

    // Write any audio samples needed for current frame
    WriteAudioSamples();

    m_dwFrameNum++;

    // Report progress
    if (m_pProgress && (m_dwFrameNum % m_dwProgressInterval) == 0) {
        m_bCanceled = m_pProgress->ReportProgress(m_dwFrameNum);
    }

    return !m_bCanceled;
}

void CVideoEncoder::EndWriting(DWORD dwInputBufferSize, PVOID pvInputVideoBuffer) throw(...)
{
    TRACE("CVideoEncoder::EndWriting\n");

    if (NULL != m_spWriter) {
        if (!m_bCanceled) {
            // If we have audio, continue appending the same video frame
            // until we start appending silence.
            // This takes care of when the audio is longer than the video.
            if (NULL != m_spSyncReader) {
                while (NULL == m_spSilentAudioSample) {
                    AppendVideoFrame(dwInputBufferSize, pvInputVideoBuffer);
                }
            }
        }

        CE(m_spWriter->EndWriting(), "IWMWriter::EndWriting");
    }
}