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

// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently,
// but are changed infrequently

#pragma once

#ifndef STRICT
#define STRICT
#endif


// Modify the following defines if you have to target a platform prior to the ones specified below.
// Refer to MSDN for the latest info on corresponding values for different platforms.
#ifndef WINVER              // Allow use of features specific to Windows 95 and Windows NT 4 or later.
#define WINVER 0x0400       // Change this to the appropriate value to target Windows 98 and Windows 2000 or later.
#endif

#ifndef _WIN32_WINNT        // Allow use of features specific to Windows NT 4 or later.
#define _WIN32_WINNT 0x0400 // Change this to the appropriate value to target Windows 2000 or later.
#endif                      

#ifndef _WIN32_WINDOWS      // Allow use of features specific to Windows 98 or later.
#define _WIN32_WINDOWS 0x0410 // Change this to the appropriate value to target Windows Me or later.
#endif

#ifndef _WIN32_IE           // Allow use of features specific to IE 4.0 or later.
#define _WIN32_IE 0x0400    // Change this to the appropriate value to target IE 5.0 or later.
#endif

#define _ATL_APARTMENT_THREADED
#define _ATL_NO_AUTOMATIC_NAMESPACE

#define _ATL_CSTRING_EXPLICIT_CONSTRUCTORS  // some CString constructors will be explicit

// turns off ATL's hiding of some common and often safely ignored warning messages
#define _ATL_ALL_WARNINGS

// Disable warning about throw clause
#pragma warning(disable : 4290)

// std
#include <new>

#include "resource.h"
#include <atlbase.h>
#include <atlcom.h>
#include <atlwin.h>
#include <atltypes.h>
#include <atlctl.h>
#include <atlhost.h>
#include <atlsimpcoll.h>
#include <atlstr.h>

#include <ocmm.h>

#import "progid:ShockwaveFlash.ShockwaveFlash" raw_interfaces_only

#include <wmsdk.h>

// Java
#include <jni.h>
#include <jni_md.h>
#include <pptrace.h>

#include <eh.h>

_COM_SMARTPTR_TYPEDEF(IWMProfileManager, __uuidof(IWMProfileManager));
_COM_SMARTPTR_TYPEDEF(IWMProfile, __uuidof(IWMProfile));
_COM_SMARTPTR_TYPEDEF(IWMWriter, __uuidof(IWMWriter));
_COM_SMARTPTR_TYPEDEF(IWMSyncReader, __uuidof(IWMSyncReader));
_COM_SMARTPTR_TYPEDEF(IWMInputMediaProps, __uuidof(IWMInputMediaProps));
_COM_SMARTPTR_TYPEDEF(IWMOutputMediaProps, __uuidof(IWMOutputMediaProps));
_COM_SMARTPTR_TYPEDEF(INSSBuffer, __uuidof(INSSBuffer));
_COM_SMARTPTR_TYPEDEF(IWMStreamConfig, __uuidof(IWMStreamConfig));
_COM_SMARTPTR_TYPEDEF(IWMMediaProps, __uuidof(IWMMediaProps));

using namespace ATL;