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

#include "jniutil.h"
#include "swf2wmv.h"
#include "wmvencoder.h"
#include "comex.h"


// Globals
jmethodID g_jmidUpdateProgress = 0;

class CEncodingProgress : public IEncodingProgress
{
public:
    CEncodingProgress(JNIEnv *env, jobject jExporter)
    {
        m_jniEnv = env;
        m_jExporter = jExporter;
    }

    DWORD GetProgressInterval()
    {
        // Every 5 frames
        return 5;
    }

    BOOL ReportProgress(DWORD dwFrameNum)
    {
        return m_jniEnv->CallBooleanMethod(m_jExporter, g_jmidUpdateProgress, dwFrameNum);
    }

private:
    JNIEnv *m_jniEnv;
    jobject m_jExporter;
};


/*
 * Class:     com_photica_photopulse_exporter_WMVExporter
 * Method:    initIDs
 * Signature: ()V
 */
extern "C" JNIEXPORT void JNICALL Java_com_photica_photopulse_exporter_WMVExporter_initIDs
    (JNIEnv *env, jclass jcExporter)
{
    // Cache progress callback
    g_jmidUpdateProgress = env->GetMethodID(jcExporter, "updateProgress", "(I)Z");
    if (!g_jmidUpdateProgress) {
        JNIThrow(env, METHOD_EXCEPTION, "updateProgress");
        return;
    }

    try {
        CVideoEncoder::InitLibrary();
    } catch (COMException &e) {
        JNIThrow(env, LINK_EXCEPTION, e.ErrorMessage());
        return;
    }
}


/*
 * Class:     com_photica_photopulse_exporter_WMVExporter
 * Method:    encodeWMV
 * Signature: (Ljava/lang/String;IIF[Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_photica_photopulse_exporter_WMVExporter_encodeWMV
    (JNIEnv *env, jobject jExporter, jstring jstrSwfFile, jint jiSwfWidth, jint jiSwfHeight,
        jfloat jfFrameRate, jobjectArray joaAudioFiles,
        jstring jstrProfilePath, jstring jstrVideoOutputFile)
{
    try {
        try {
            CEncodingProgress progress(env, jExporter);

            CJavaString jsSwfFile(env, jstrSwfFile);
            CJavaStringUTF jsProfilePath(env, jstrProfilePath);
            CJavaString jsVideoOutputFile(env, jstrVideoOutputFile);
            CJavaStringArray jsaAudioFiles(env, joaAudioFiles);

            ConvertSWF2WMV((const WCHAR *)jsSwfFile.GetString(), jiSwfWidth, jiSwfHeight, jfFrameRate,
                        (const WCHAR **)jsaAudioFiles.GetStringArray(), jsaAudioFiles.GetArrayLength(),
                        jsProfilePath.GetString(),
                        (const WCHAR *)jsVideoOutputFile.GetString(), &progress);

        } catch (COMException &e) {
            TRACE("COMException: %x: %s - %s\n", e.Error(), (LPCTSTR)e.ErrorMessage(), (LPCTSTR)e.Location());
            // Throw different Java exceptions based on custom HRESULT
            HRESULT hrCustom = e.CustomError();
            if (FAILED(hrCustom)) {
                switch(hrCustom) {
                case C_E_FLASHVERSION:
                    CAtlStringA str;
                    str.Format("COMException: %x: %s - %s",
                        e.Error(), (LPCTSTR)e.ErrorMessage(), (LPCTSTR)e.Location());
                    JNIThrow(env, INSTANTIATION_EXCEPTION, str);
                    return;
                }
            }
            JNIThrowEncodeException(env, (LPCTSTR)e.ErrorMessage(), e.Error(), (LPCTSTR)e.Location());
        } catch (_com_error &e) {
            TRACE("_com_error: %x\n", e.Error());
            JNIThrowEncodeException(env, "_com_error", e.Error(), NULL);
        } catch (CAtlException &e) {
            TRACE("CAtlException: %x\n", (HRESULT)e);
            JNIThrowEncodeException(env, "CAtlException", (HRESULT)e, NULL);
        } catch (...) {
            TRACE("unknown exception\n");
            JNIThrowEncodeException(env, "unknown exception", S_OK, NULL);
        }
    } catch (std::bad_alloc &e) {
        TRACE("std::bad_alloc: %s\n", e.what());
        // Don't format a string since that can throw bad_alloc too
        JNIThrow(env, OOM_EXCEPTION, e.what());
    }
}
