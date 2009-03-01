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

#include <pptrace.h>

#define FPCJNI_CREATE_FLASHCONTROL      (WM_APP + 0x1700)

typedef struct WndProcData {
    WNDPROC lpOldWndProc;
    LONG lpOldUserData;
} WndProcData;

// JAWT_GetAwt prototype
typedef jboolean (JNICALL *PJAWT_GETAWT)(JNIEnv*, JAWT*);

LRESULT APIENTRY CanvasSubclassProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
void RestoreCanvasWndProc(HWND hwndCanvas, WndProcData *lpData);

// Globals
PJAWT_GETAWT g_JAWT_GetAWT = NULL;
HANDLE g_hModuleDLL = NULL;
jfieldID g_jfidHwnd = 0;
BOOL g_bFlashRegistered = FALSE;



/*
 * Class:     com_photica_ui_FlashPlayerControl
 * Method:    initializeControl
 * Signature: (Ljava/lang/String;)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_photica_ui_FlashPlayerControl_initializeControl
    (JNIEnv *env, jclass fpClass, jstring jstrJawtLibPath)
{
    // Cache hwnd field ID
    g_jfidHwnd = env->GetFieldID(fpClass, "hwndControl", "I");

    // The path to the jawt.dll library is passed in.
    // Attempt to load it.
    const char *szJawtLibPath = env->GetStringUTFChars(jstrJawtLibPath, NULL);
    if (!szJawtLibPath) {
        TRACE("GetStringUTFChars failed for JAWT_LIB\n");
        return;
    }
    HMODULE hAWTModule = ::LoadLibrary(szJawtLibPath);
    env->ReleaseStringUTFChars(jstrJawtLibPath, szJawtLibPath);
    if (!hAWTModule) {
        TRACE("Failed to load jawt.dll\n");
        return;
    }

    // Find the JAWT_GetAwt entry point in the DLL
    g_JAWT_GetAWT = (PJAWT_GETAWT)::GetProcAddress(hAWTModule, "_JAWT_GetAWT@8");
    if (!g_JAWT_GetAWT) {
        TRACE("Failed to find JAWT_GetAWT proc\n");
        return;
    }
}

/*
 * Class:     com_photica_ui_FlashPlayerControl
 * Method:    attachControl
 * Signature: ()Z
 */
extern "C" JNIEXPORT jboolean JNICALL Java_com_photica_ui_FlashPlayerControl_attachControl
    (JNIEnv *env, jobject jControl)
{
    if (!g_JAWT_GetAWT) {
        TRACE("JAWT_GetAWT function pointer not set\n");
        return JNI_FALSE;
    }

    // Get the AWT
    JAWT awt;
    awt.version = JAWT_VERSION_1_4;
    if (g_JAWT_GetAWT(env, &awt) == JNI_FALSE) {
        TRACE("JAWT_GetAWT failed\n");
        return JNI_FALSE;
    }

    // Get the drawing surface
    JAWT_DrawingSurface *ds = awt.GetDrawingSurface(env, jControl);
    if (!ds) {
        TRACE("GetDrawingSurface failed\n");
        return JNI_FALSE;
    }

    // Lock the drawing surface
    jint lock = ds->Lock(ds);
    if ((lock & JAWT_LOCK_ERROR) != 0) {
        awt.FreeDrawingSurface(ds);
        TRACE("Lock failed - JAWT_LOCK_ERROR\n");
        return JNI_FALSE;
    }

    // Get the drawing surface info
    JAWT_DrawingSurfaceInfo *dsi = ds->GetDrawingSurfaceInfo(ds);
    if (!dsi) {
        ds->Unlock(ds);
        awt.FreeDrawingSurface(ds);
        TRACE("GetDrawingSurfaceInfo failed\n");
        return JNI_FALSE;
    }

    // Get the Canvas HWND
    HWND hwndCanvas = ((JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo)->hwnd;

    // Subclass the canvas HWND.
    // We are currently on the "AWT-EventQueue-0" EDT thread, but the message loop
    // is on the "AWT-Windows" thread. We need the flash HWND to be created on that thread.
    // So subclass the canvas, and send it a synchronous special message so
    // our subclassed WNDPROC can create the flash control on that thread.
    // Save the previous WNDPROC and USERDATA since Java needs those restored.
    WndProcData data;
    data.lpOldWndProc =
        (WNDPROC)::SetWindowLong(hwndCanvas, GWL_WNDPROC, (LONG)CanvasSubclassProc); 
    data.lpOldUserData = ::SetWindowLong(hwndCanvas, GWL_USERDATA, (LONG)&data);
    HWND hwndFlashPlayer = (HWND)::SendMessage(hwndCanvas, FPCJNI_CREATE_FLASHCONTROL, (WPARAM)0, (LPARAM)0);
    // If our message was handled, the WNDPROC was restored, but restore again just in case
    RestoreCanvasWndProc(hwndCanvas, &data);

    // Now our flash HWND is created, store it in the Java object field
    env->SetIntField(jControl, g_jfidHwnd, (jint)hwndFlashPlayer);

    // Free the drawing surface info
    ds->FreeDrawingSurfaceInfo(dsi);

    // Unlock the drawing surface
    ds->Unlock(ds);

    // Free the drawing surface
    awt.FreeDrawingSurface(ds);

    return hwndFlashPlayer ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     com_photica_ui_FlashPlayerControl
 * Method:    resizeControl
 * Signature: (II)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_photica_ui_FlashPlayerControl_resizeControl
    (JNIEnv *env, jobject jControl, jint width, jint height)
{
    HWND hwnd = (HWND)env->GetIntField(jControl, g_jfidHwnd);
    if (!hwnd) {
        TRACE("resizeControl - no hwnd\n");
        return;
    }
    // Resize the flash control to match the passed size
    ::MoveWindow(hwnd, 0, 0, width, height, TRUE);
}

/*
 * Class:     com_photica_ui_FlashPlayerControl
 * Method:    loadMovieControl
 * Signature: ([BII)V
 */
extern "C" JNIEXPORT void JNICALL Java_com_photica_ui_FlashPlayerControl_loadMovieControl
    (JNIEnv *env, jobject jControl, jbyteArray swfArray, jint nOffset, jint nLength)
{
    HWND hwnd = (HWND)env->GetIntField(jControl, g_jfidHwnd);
    if (!hwnd) {
        TRACE("loadMovieControl - no hwnd\n");
        return;
    }

    // Bounds check the passed byte[] array
    jbyte* swfBuf = env->GetByteArrayElements(swfArray, NULL);

    // Pass the byte[] array region to flash
    SFPCPutMovieFromMemory sFPCPutMovieFromMemory;
    sFPCPutMovieFromMemory.lpData = (LPVOID)&swfBuf[nOffset];
    sFPCPutMovieFromMemory.dwSize = nLength;
    ::SendMessage(hwnd, FPCM_PUTMOVIEFROMMEMORY, 0, (LPARAM)&sFPCPutMovieFromMemory);

    env->ReleaseByteArrayElements(swfArray, swfBuf, JNI_ABORT);
}

/*
 * Canvas HWND subclassed WNDPROC
 */
LRESULT APIENTRY CanvasSubclassProc
    (HWND hwndCanvas, UINT uMsg, WPARAM wParam, LPARAM lParam) 
{
    // Handle our special message
    if (uMsg == FPCJNI_CREATE_FLASHCONTROL) {

        // Restore canvas before creating a child flash window,
        // since that generates more messages reentrantly and canvas needs to handle them.
        WndProcData *lpData = (WndProcData*)::GetWindowLong(hwndCanvas, GWL_USERDATA);
        RestoreCanvasWndProc(hwndCanvas, lpData);

        // Register the flash control the first time
        if (!g_bFlashRegistered) {
            if (!RegisterFlashWindowClass())
                return NULL;
            g_bFlashRegistered = TRUE;
        }

        // Create flash control child window of same size as canvas
        RECT rc = { 0 };
        ::GetClientRect(hwndCanvas, &rc);
        HWND hwndFlashPlayer = ::CreateWindow(WC_FLASH,
            NULL, WS_CHILD | WS_VISIBLE,
            rc.left, rc.top,
            rc.right - rc.left, rc.bottom - rc.top,
            hwndCanvas, NULL,
            (HINSTANCE)g_hModuleDLL,
            NULL);

        // Return created control
        return (LRESULT)hwndFlashPlayer;
    }

    // If we are here, then we are handling some other window message.
    // Restore the canvas before calling its old WNDPROC so it can handle it.
    WndProcData *lpData = (WndProcData*)::GetWindowLong(hwndCanvas, GWL_USERDATA);
    RestoreCanvasWndProc(hwndCanvas, lpData);

    // Let the old canvas WNDPROC handle it
    LRESULT result = CallWindowProc(lpData->lpOldWndProc, hwndCanvas, uMsg, wParam, lParam);

    // Subclass again in preparation for handling our special message
    ::SetWindowLong(hwndCanvas, GWL_WNDPROC, (LONG)CanvasSubclassProc);
    ::SetWindowLong(hwndCanvas, GWL_USERDATA, (LONG)lpData);

    return result;
} 

/*
 * Restore canvas HWND WNDPROC and USERDATA
 */
void RestoreCanvasWndProc(HWND hwndCanvas, WndProcData *lpData)
{
    ::SetWindowLong(hwndCanvas, GWL_WNDPROC, (LONG)lpData->lpOldWndProc);
    ::SetWindowLong(hwndCanvas, GWL_USERDATA, (LONG)lpData->lpOldUserData);
}

BOOL APIENTRY DllMain
    (HANDLE hModule, DWORD  ul_reason_for_call, LPVOID lpReserved)
{
    switch (ul_reason_for_call)
    {
    case DLL_PROCESS_ATTACH:
        g_hModuleDLL = hModule;
        break;
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        g_hModuleDLL = NULL;
        break;
    }
    return TRUE;
}