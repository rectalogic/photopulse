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

#ifndef PHOTOPULSE_H
#define PHOTOPULSE_H

#define WINDOWS_LEAN_AND_MEAN
#include <windows.h>

#include "pptrace.h"
#include "ppshared.h"

// This name is also used in the installer photopulse.iss script
#define INSTANCE_MUTEX_NAME "PhotoPulseMutex{E0893C13-1962-45fb-8E71-3CEEFC4F6CA4}"

// HRESULT facility codes
#define FACILITY_MAIN 0x606
#define FACILITY_JAVA 0x303
#define FACILITY_SPLASH 0x404


#endif
