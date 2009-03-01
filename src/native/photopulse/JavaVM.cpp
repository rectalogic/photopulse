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

#include <crtdbg.h>
#include "JavaVM.h"
#include "JString.h"
#include "photopulse.h"

#define JVM_PATH "\\jre\\bin\\client\\jvm.dll"

#define CLASS_LAUNCHER "com/photica/photopulse/launcher/Launcher"
#define CLASS_STRING "java/lang/String"

#define CLASS_DOTTED_HYPERLABEL "com.photica.photopulse.ui.wizard.HyperLabel"

// Encoded
// "com.photica.photopulse.License"
#define CLASS_DOTTED_LICENSE \
    "\x54\x58\x5a\x19\x47\x5f\x58\x43\x5e\x54\x56\x19\x47\x5f\x58" \
    "\x43\x58\x47\x42\x5b\x44\x52\x19\x7b\x5e\x54\x52\x59\x44\x52"

// Encoded
// "com.photica.photopulse.ui.wizard.Main"
#define CLASS_DOTTED_MAIN \
    "\x54\x58\x5a\x19\x47\x5f\x58\x43\x5e\x54\x56\x19\x47\x5f\x58" \
    "\x43\x58\x47\x42\x5b\x44\x52\x19\x42\x5e\x19\x40\x5e\x4d\x56" \
    "\x45\x53\x19\x7a\x56\x5e\x59"

#define JNI_METHOD_main "main"
#define JNI_METHOD_SIG_main "([Ljava/lang/String;)V"

#define JNI_METHOD_loadClass "loadClass"
#define JNI_METHOD_SIG_loadClass "(Ljava/lang/String;)Ljava/lang/Class;"

#define JNI_METHOD_launchDocumentNative "launchDocumentNative"
#define JNI_METHOD_SIG_launchDocumentNative "(Ljava/lang/String;)Z"

#define JNI_METHOD_raisePhotoPulse "raisePhotoPulse"
#define JNI_METHOD_SIG_raisePhotoPulse "()V"

// HRESULT errors
#define JAVA_E_MODULENAME MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x01)
#define JAVA_E_LOADJVMPROC MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x02)
#define JAVA_E_CREATEJVM MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x03)
#define JAVA_E_LOADLAUNCHER MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x04)
#define JAVA_E_FINDMETHOD_LOADCLASS MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x05)
#define JAVA_E_NEWSTRING MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x06)
#define JAVA_E_INVOKE_LOADCLASS_HYPERLABEL MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x07)
#define JAVA_E_REGISTERNATIVES_HYPERLABEL MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x08)
#define JAVA_E_REGISTERNATIVES_LICENSE MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x09)
#define JAVA_E_INVOKE_LOADCLASS_LICENSE MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0a)
#define JAVA_E_INVOKE_LOADCLASS_MAIN MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0b)
#define JAVA_E_GETMETHOD_RAISEPHOTOPULSE MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0c)
#define JAVA_E_GETMETHOD_MAIN MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0d)
#define JAVA_E_NEWSTRINGARRAY MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0e)
#define JAVA_E_INVOKE_MAIN MAKE_HRESULT(SEVERITY_ERROR, FACILITY_JAVA, 0x0f)




HRESULT CJavaVM::StartPhotoPulse(HINSTANCE hinstApp, int argc, char *argv[])
{
    // Get full EXE pathname
    CHAR szAppPath[_MAX_PATH];
    if (GetModuleFileName(hinstApp, szAppPath, sizeof(szAppPath)/sizeof(CHAR)) == 0) {
        TRACE("Failed to find module filename\n");
        return JAVA_E_MODULENAME;
    }

    // Get JVM create proc
    JNI_CREATEJAVAVMPROC pfnCreateJavaVM = GetJavaVMProc(szAppPath);
    if (pfnCreateJavaVM == NULL) {
        TRACE("Failed to load JVM proc\n");
        return JAVA_E_LOADJVMPROC;
    }

    HRESULT hr = S_OK;

    // Create JavaVM.
    if (FAILED((hr = CreateJavaVM(pfnCreateJavaVM, szAppPath, &m_pJavaVM, &m_pJNIEnv)))) {
        TRACE("Failed to create JVM\n");
        return hr;
    }

    // Load main Launcher class
    jclass jclsLauncher = m_pJNIEnv->FindClass(CLASS_LAUNCHER);
    if (jclsLauncher == NULL) {
        TRACE("Failed to load Launcher class\n");
        return JAVA_E_LOADLAUNCHER;
    }

    // Register native methods
    if (FAILED(hr = RegisterNatives(m_pJNIEnv, jclsLauncher))) {
        TRACE("RegisterNatives failed\n");
        return hr;
    }

    // Call static (private) Launcher.main() method, passing arguments
    if (FAILED(hr = InvokeMainMethod(m_pJNIEnv, jclsLauncher, argc, argv))) {
        TRACE("Invoke main failed\n");
        return hr;
    }

    return S_OK;
}

void CJavaVM::DestroyJavaVM()
{
    if (m_pJavaVM != NULL) {
        // This call will block until all Java threads exit
        m_pJavaVM->DestroyJavaVM();
        m_pJavaVM = NULL;
        m_pJNIEnv = NULL;
    }
}

CJavaVM::~CJavaVM()
{
    DestroyJavaVM();
    TRACE("~CJavaVM finished\n");
}

JNI_CREATEJAVAVMPROC CJavaVM::GetJavaVMProc(LPSTR lpszAppPath)
{
    // Find last path element
    LPSTR lpszLastSep = strrchr(lpszAppPath, '\\');

    CHAR szJrePath[_MAX_PATH];
    // Copy application directory
    strcpy(szJrePath, lpszAppPath);
    // Append relative path to jvm DLL
    strcpy(&szJrePath[lpszLastSep - lpszAppPath], JVM_PATH);

    // Load jvm.dll
    HINSTANCE hinstJavaDLL = ::LoadLibrary(szJrePath);
    if (hinstJavaDLL == NULL) {
        TRACE("Failed to load %s\n", szJrePath);
        return NULL;
    }

    // Get JVM create proc
    return (JNI_CREATEJAVAVMPROC)GetProcAddress(hinstJavaDLL, "JNI_CreateJavaVM");
}

BOOL CJavaVM::QueryRegistryKey(HKEY hkeyRoot, LPCSTR lpszRegPath)
{
    BOOL bResult = FALSE;
    HKEY hkey;
    if (RegOpenKeyEx(hkeyRoot, lpszRegPath, 0, KEY_QUERY_VALUE, &hkey) == ERROR_SUCCESS) {
        bResult = TRUE;
        RegCloseKey(hkey);
    }
    return bResult;
}

BOOL CJavaVM::QueryRegistryValue(HKEY hkeyRoot, LPCSTR lpszRegPath, LPCSTR lpszValueName, LPSTR lpszResultBuf, LPDWORD lpdwResultBufLen)
{
    BOOL bResult = FALSE;
    HKEY hkey;
    if (RegOpenKeyEx(hkeyRoot, lpszRegPath, 0, KEY_QUERY_VALUE, &hkey) == ERROR_SUCCESS) {
        DWORD dwType;
        if (RegQueryValueEx(hkey, lpszValueName, NULL, &dwType,
                (LPBYTE)lpszResultBuf, lpdwResultBufLen) == ERROR_SUCCESS
                && dwType == REG_SZ) {
            bResult = TRUE;
        }
        RegCloseKey(hkey);
    }
    return bResult;
}

HRESULT CJavaVM::CreateJavaVM(JNI_CREATEJAVAVMPROC pfnCreateJavaVM, LPSTR lpszAppPath, JavaVM **ppJavaVM, JNIEnv **ppJNIEnv)
{
    // Get special, optional JVM tweaks from the registry - maxmem and noddraw
    LPSTR lpszMaxMemVal = "128M";
    CHAR szMaxMem[7];
    LPSTR lpszNoDDrawVal = NULL;
    HKEY hkeyPhotoPulse;
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, "Software\\JavaSoft\\Prefs\\com\\photica\\photopulse",
            0, KEY_QUERY_VALUE, &hkeyPhotoPulse) == ERROR_SUCCESS) {
        DWORD dwMaxMemBufLen = sizeof(szMaxMem)/sizeof(CHAR);
        DWORD dwType;
        if (RegQueryValueEx(hkeyPhotoPulse, "maxmem", NULL, &dwType,
                (LPBYTE)szMaxMem, &dwMaxMemBufLen) == ERROR_SUCCESS
                && dwType == REG_SZ) {
            lpszMaxMemVal = szMaxMem;
        }
        if (RegQueryValueEx(hkeyPhotoPulse, "noddraw", NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
            lpszNoDDrawVal = "true";
        }
        RegCloseKey(hkeyPhotoPulse);
    }

    // Try to locate neoDVD application path
    LPSTR lpszNeoDVDPath = NULL;
    CHAR szNeoDVDPath[_MAX_PATH+1];
    // Do an initial check if any MedioStream software is installed
    if (QueryRegistryKey(HKEY_LOCAL_MACHINE, "SOFTWARE\\MedioStream")) {
        DWORD dwNeoDVDPathBufLen = sizeof(szNeoDVDPath)/sizeof(CHAR);
        // First look for neoDVDplus in App Paths - this is how to find the neoSTUDIO6
        // version that DirectorsTribute is redistributing.
        if (QueryRegistryValue(HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\neoDVDplus.exe", "",
                szNeoDVDPath, &dwNeoDVDPathBufLen)) {
            lpszNeoDVDPath = szNeoDVDPath;
        }
        // If that fails, look for NeoDVDPath - neoDVD5 sets this (neoSTUDIO6 sets it too, but wrong)
        else {
            // Reset length
            dwNeoDVDPathBufLen = sizeof(szNeoDVDPath)/sizeof(CHAR);
            if (QueryRegistryValue(HKEY_LOCAL_MACHINE,
                    "SOFTWARE\\MedioStream\\neoDVD", "NeoDVDPath",
                    szNeoDVDPath, &dwNeoDVDPathBufLen)) {
                lpszNeoDVDPath = szNeoDVDPath;
            }
        }
    }

    // The _JAVA_OPTIONS env var can be used with the JavaSoft VM to set additional VM options
    // Disable this feature.
    _putenv("_JAVA_OPTIONS=");

    JavaVMInitArgs jvmInitArgs;
    JavaVMOption jvmOptions[6];
    jvmInitArgs.version = JNI_VERSION_1_4;
    jvmInitArgs.options = jvmOptions;
    jvmInitArgs.nOptions = 0;

    // Classpath, the launch jar is appended to the application EXE
    // "-Djava.class.path=%.*s"
    CHAR szOptClasspathEncoded[] =
        "\x1a\x73\x5d\x56\x41\x56\x19\x54\x5b\x56\x44\x44\x19\x47\x56"
        "\x43\x5f\xa\x12\x19\x1d\x44"
        ;
    DecodeString(szOptClasspathEncoded, sizeof(szOptClasspathEncoded)/sizeof(CHAR) - 1, XOR_DECODE);
    char szClasspathOpt[_MAX_PATH + sizeof(szOptClasspathEncoded)];
    sprintf(szClasspathOpt, szOptClasspathEncoded, _MAX_PATH, lpszAppPath);
    jvmOptions[jvmInitArgs.nOptions].optionString = szClasspathOpt;
    jvmInitArgs.nOptions++;

    // Max memory
    #define OPT_MAXMEM "-Xmx%.*s"
    char szMaxMemOpt[40 + sizeof(OPT_MAXMEM)];
    sprintf(szMaxMemOpt, OPT_MAXMEM, 40, lpszMaxMemVal);
    jvmOptions[jvmInitArgs.nOptions].optionString = szMaxMemOpt;
    jvmInitArgs.nOptions++;

    // No DDRAW
    #define OPT_NODDRAW "-Dsun.java2d.noddraw=%.*s"
    char szNoDDrawOpt[10 + sizeof(OPT_NODDRAW)];
    if (lpszNoDDrawVal != NULL) {
        sprintf(szNoDDrawOpt, OPT_NODDRAW, 10, lpszNoDDrawVal);
        jvmOptions[jvmInitArgs.nOptions].optionString = szNoDDrawOpt;
        jvmInitArgs.nOptions++;
    }

    // neoDVD path
    #define OPT_NEODVD "-Dneodvd.path=%.*s"
    char szNeoDVDOpt[_MAX_PATH + sizeof(OPT_NEODVD)];
    if (lpszNeoDVDPath != NULL) {
        sprintf(szNeoDVDOpt, OPT_NEODVD, _MAX_PATH, lpszNeoDVDPath);
        jvmOptions[jvmInitArgs.nOptions].optionString = szNeoDVDOpt;
        jvmInitArgs.nOptions++;
    }

    // Protocol handler
    // "-Djava.protocol.handler.pkgs=com.photica.photopulse.launcher"
    CHAR szOptHandlerEncoded[] =
        "\x1a\x73\x5d\x56\x41\x56\x19\x47\x45\x58\x43\x58\x54\x58\x5b"
        "\x19\x5f\x56\x59\x53\x5b\x52\x45\x19\x47\x5c\x50\x44\xa\x54"
        "\x58\x5a\x19\x47\x5f\x58\x43\x5e\x54\x56\x19\x47\x5f\x58\x43"
        "\x58\x47\x42\x5b\x44\x52\x19\x5b\x56\x42\x59\x54\x5f\x52\x45"
        ;
    DecodeString(szOptHandlerEncoded, sizeof(szOptHandlerEncoded)/sizeof(CHAR) - 1, XOR_DECODE);
    jvmOptions[jvmInitArgs.nOptions].optionString = szOptHandlerEncoded;
    jvmInitArgs.nOptions++;

    // photopulse.home property
    #define OPT_HOME "-Dphotopulse.home="
    char szHomeOpt[_MAX_PATH + sizeof(OPT_HOME) + 2] = OPT_HOME;
    int nLength = _MAX_PATH;
    char *pszEnd = strrchr(lpszAppPath, '\\');
    if (pszEnd != NULL)
        nLength = pszEnd - lpszAppPath + 1;
    lstrcpyn(&szHomeOpt[sizeof(OPT_HOME) - 1], lpszAppPath, nLength);
    jvmOptions[jvmInitArgs.nOptions].optionString = szHomeOpt;
    jvmInitArgs.nOptions++;

    _ASSERTE(jvmInitArgs.nOptions <= sizeof(jvmOptions)/sizeof(JavaVMOption));

#ifdef _DEBUG
    for (int i = 0; i < jvmInitArgs.nOptions; i++) {
        TRACE("jvmOption=%s\n", jvmOptions[i].optionString);
    }
#endif

    // Create JVM
    if ((*pfnCreateJavaVM)(ppJavaVM, ppJNIEnv, &jvmInitArgs) != JNI_OK)
        return JAVA_E_CREATEJVM;
    return S_OK;
}

HRESULT CJavaVM::InitMethodCache(JNIEnv *pJNIEnv, jclass jclsLauncher, jmethodID jmidLoadClass)
{
    // Main class
    CHAR szClassNameEncoded[] = CLASS_DOTTED_MAIN;
    DecodeString(szClassNameEncoded, sizeof(szClassNameEncoded)/sizeof(CHAR) - 1, XOR_DECODE);
    jstring jstrClassName = pJNIEnv->NewStringUTF(szClassNameEncoded);
    if (jstrClassName == NULL) {
        TRACE("NewStringUTF failed\n");
        return JAVA_E_NEWSTRING;
    }

    // Invoke Launcher.loadClass method to load Main
    m_jclsMain = (jclass)pJNIEnv->CallStaticObjectMethod(jclsLauncher, jmidLoadClass, jstrClassName);
    if (pJNIEnv->ExceptionOccurred()) {
        TRACE("loadClass exception\n");
        pJNIEnv->ExceptionClear();
        return JAVA_E_INVOKE_LOADCLASS_MAIN;
    }

    // Cache raisePhotoPulse method ID
    m_jmIDraisePhotoPulse = pJNIEnv->GetStaticMethodID(m_jclsMain, JNI_METHOD_raisePhotoPulse,
            JNI_METHOD_SIG_raisePhotoPulse);
    if (m_jmIDraisePhotoPulse == NULL) {
        TRACE("no raisePhotoPulse method ID\n");
        return JAVA_E_GETMETHOD_RAISEPHOTOPULSE;
    }

    return S_OK;
}

HRESULT CJavaVM::InvokeMainMethod(JNIEnv *pJNIEnv, jclass jclsLauncher, int argc, char *argv[])
{
    // Get main() method
    jmethodID jmIDMain = pJNIEnv->GetStaticMethodID(jclsLauncher, JNI_METHOD_main, JNI_METHOD_SIG_main);
    if (jmIDMain == NULL)
        return JAVA_E_GETMETHOD_MAIN;

    // Build argument array
    jobjectArray joaArgs = NewStringArray(pJNIEnv, argv, argc);
    if (joaArgs == NULL)
        return JAVA_E_NEWSTRINGARRAY;

    // Invoke main method.
    pJNIEnv->CallStaticVoidMethod(jclsLauncher, jmIDMain, joaArgs);
    if (pJNIEnv->ExceptionOccurred()) {
        pJNIEnv->ExceptionClear();
        return JAVA_E_INVOKE_MAIN;
    }

    return S_OK;
}

HRESULT CJavaVM::RegisterNatives(JNIEnv *pJNIEnv, jclass jclsLauncher)
{
    // Get loadClass() method
    jmethodID jmidLoadClass = pJNIEnv->GetStaticMethodID(jclsLauncher, JNI_METHOD_loadClass,
            JNI_METHOD_SIG_loadClass);
    if (jmidLoadClass == NULL) {
        TRACE("no loadClass method ID\n");
        return JAVA_E_FINDMETHOD_LOADCLASS;
    }

    // HyperLabel class
    jstring jstrHyperLabelClassName = pJNIEnv->NewStringUTF(CLASS_DOTTED_HYPERLABEL);
    if (jstrHyperLabelClassName == NULL) {
        TRACE("NewStringUTF failed\n");
        return JAVA_E_NEWSTRING;
    }

    // Invoke Launcher.loadClass method to load HyperLabel
    jclass jclsHyperLabel = (jclass)pJNIEnv->CallStaticObjectMethod(jclsLauncher, jmidLoadClass, jstrHyperLabelClassName);
    if (pJNIEnv->ExceptionOccurred()) {
        TRACE("loadClass exception\n");
        pJNIEnv->ExceptionClear();
        return JAVA_E_INVOKE_LOADCLASS_HYPERLABEL;
    }

    // HyperLabel methods
    JNINativeMethod jnmHyperLabelMethods[] = {
        { JNI_METHOD_launchDocumentNative, JNI_METHOD_SIG_launchDocumentNative, Native_HyperLabel_launchDocumentNative },
    };
    if (pJNIEnv->RegisterNatives(jclsHyperLabel, jnmHyperLabelMethods, sizeof(jnmHyperLabelMethods)/sizeof(JNINativeMethod)) < 0) {
        TRACE("RegisterNatives failed\n");
        return JAVA_E_REGISTERNATIVES_HYPERLABEL;
    }

    // License class
    CHAR szLicenseClassNameEncoded[] = CLASS_DOTTED_LICENSE;
    DecodeString(szLicenseClassNameEncoded, sizeof(szLicenseClassNameEncoded)/sizeof(CHAR) - 1, XOR_DECODE);
    jstring jstrLicenseClassName = pJNIEnv->NewStringUTF(szLicenseClassNameEncoded);
    if (jstrLicenseClassName == NULL) {
        TRACE("NewStringUTF failed\n");
        return JAVA_E_NEWSTRING;
    }

    // Invoke Launcher.loadClass method to load License
    jclass jclsLicense = (jclass)pJNIEnv->CallStaticObjectMethod(jclsLauncher, jmidLoadClass, jstrLicenseClassName);
    if (pJNIEnv->ExceptionOccurred()) {
        TRACE("loadClass exception\n");
        pJNIEnv->ExceptionClear();
        return JAVA_E_INVOKE_LOADCLASS_LICENSE;
    }

    // Cache methods we need to call later
    HRESULT hr = S_OK;
    if (FAILED(hr = InitMethodCache(pJNIEnv, jclsLauncher, jmidLoadClass))) {
        TRACE("Init method cache failed\n");
        return hr;
    }

    return S_OK;
}

BOOL CJavaVM::InvokeRaisePhotoPulse()
{
    m_pJNIEnv->CallStaticVoidMethod(m_jclsMain, m_jmIDraisePhotoPulse);
    if (m_pJNIEnv->ExceptionOccurred()) {
        m_pJNIEnv->ExceptionClear();
        return FALSE;
    }

    return TRUE;
}

jboolean JNICALL CJavaVM::Native_HyperLabel_launchDocumentNative(JNIEnv *pJNIEnv, jobject joHyperLabel, jstring jstrDocument)
{
    TRACE("Native_HyperLabel_launchDocumentNative\n");

    CJString jsDocument(pJNIEnv, jstrDocument);
    const char *pszDocument = jsDocument.GetStringUTFChars();
    if (pszDocument == NULL)
        return JNI_FALSE;
    TRACE("document %s\n", pszDocument);

    jboolean jbResult = JNI_FALSE;
    if ((int)ShellExecute(NULL, "open", pszDocument, NULL, NULL, SW_SHOWNORMAL) > 32)
        jbResult = JNI_TRUE;

    return jbResult;
}

/*
 * Returns a new array of Java string objects for the specified
 * array of platform strings.
 */
// Lifted from JDK1.4 src.zip launcher\java.c
jobjectArray CJavaVM::NewStringArray(JNIEnv *pJNIEnv, char **argv, int argc)
{
    jclass jclsString = pJNIEnv->FindClass(CLASS_STRING);
    if (jclsString == NULL)
        return NULL;
    jobjectArray jarrString = pJNIEnv->NewObjectArray(argc, jclsString, 0);
    if (jarrString == NULL)
        return NULL;

    for (int i = 0; i < argc; i++) {
        jstring jstr = pJNIEnv->NewStringUTF(*argv++);
        if (jstr == NULL)
            return NULL;
        pJNIEnv->SetObjectArrayElement(jarrString, i, jstr);
        pJNIEnv->DeleteLocalRef(jstr);
    }
    return jarrString;
}
