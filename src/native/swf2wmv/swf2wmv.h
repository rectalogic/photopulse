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

void ConvertSWF2WMV(const WCHAR *pwszFlashFile,
                    int nSwfWidth, int nSwfHeight, float fFramesPerSec,
                    const WCHAR *awszAudioFiles[], int nAudioFileCount,
                    const CHAR *pszProfileFile,
                    const WCHAR *pwszOutputVideoFile, IEncodingProgress *pProgress) throw(...);
