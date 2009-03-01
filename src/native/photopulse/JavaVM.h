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

#ifndef JAVAVM_H
#define JAVAVM_H

#define WINDOWS_LEAN_AND_MEAN
#include <windows.h>

#include <string.h>

#include <jni.h>



typedef jint (JNICALL * JNI_CREATEJAVAVMPROC)(JavaVM **pJavaVM, JNIEnv **pJNIEnv, void *vmargs);

class CJavaVM
{
public:

    CJavaVM() : m_pJavaVM(0), m_pJNIEnv(0), m_jclsMain(0), m_jmIDraisePhotoPulse(0) {
    }

    ~CJavaVM();

    HRESULT StartPhotoPulse(HINSTANCE hinstApp, int argc, char *argv[]);
    BOOL InvokeRaisePhotoPulse();
    void DestroyJavaVM();

private:

    JNI_CREATEJAVAVMPROC GetJavaVMProc(LPSTR lpszAppPath);
    BOOL QueryRegistryKey(HKEY hkeyRoot, LPCSTR lpszRegPath);
    BOOL QueryRegistryValue(HKEY hkeyRoot, LPCSTR lpszRegPath, LPCSTR lpszValueName, LPSTR lpszResultBuf, LPDWORD lpdwResultBufLen);
    HRESULT CreateJavaVM(JNI_CREATEJAVAVMPROC pfnCreateJavaVM, LPSTR lpszAppPath, JavaVM **ppJavaVM, JNIEnv **ppJNIEnv);
    HRESULT InvokeMainMethod(JNIEnv *pJNIEnv, jclass jclsMain, int argc, char *argv[]);
    jobjectArray NewStringArray(JNIEnv *pJNIEnv, char **strv, int strc);
    HRESULT RegisterNatives(JNIEnv *pJNIEnv, jclass jclsMain);
    HRESULT InitMethodCache(JNIEnv *pJNIEnv, jclass jclsLauncher, jmethodID jmidLoadClass);

    static jboolean JNICALL Native_HyperLabel_launchDocumentNative(JNIEnv *pJNIEnv, jobject joHyperLabel, jstring jstrDocument);

    JavaVM *m_pJavaVM;
    // This is only safe to use from the main thread
    JNIEnv *m_pJNIEnv;

    jmethodID m_jmIDraisePhotoPulse;
    jclass m_jclsMain;
};

#endif
