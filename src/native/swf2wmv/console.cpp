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

// Console application. Not included in DLL build.
//

#include <stdafx.h>
#include <stdlib.h>
#include <stdio.h>

#include "swf2wmv.h"
#include "wmvencoder.h"
#include "comex.h"

class CSWF2WMVModule : public CAtlExeModuleT< CSWF2WMVModule >
{};

CSWF2WMVModule _AtlModule;

class CConsoleProgress : public IEncodingProgress
{
public:
    DWORD GetProgressInterval()
    {
        // Every 5 frames
        return 5;
    }

    BOOL ReportProgress(DWORD dwFrameNum)
    {
        printf("%d.", dwFrameNum);
        return FALSE;
    }
};

int main(int argc, CHAR *argv[ ])
{
    if (argc != 7) {
        fprintf(stderr, "Usage: %s <input.swf> <width> <height> <framerate> <profile.prx> <output.wmv>\n", argv[0]);
        return -1;
    }

    try {
        CVideoEncoder::InitLibrary();

        CConsoleProgress progress;

        CAtlStringW strFlashPath(argv[1]);
        CAtlStringW strOutputPath(argv[6]);

        ConvertSWF2WMV(strFlashPath,
                        atoi(argv[2]), atoi(argv[3]), (float)atof(argv[4]),
                        NULL, 0,
                        argv[5],
                        strOutputPath, &progress);

    } catch (COMException &e) {
        CAtlStringA str;
        str.Format("COMException: %x - %s", e.Error(), (LPCTSTR)e.ErrorMessage());
        fprintf(stderr, "%s\n", (const char *)str);
    } catch (_com_error &e) {
        CAtlStringA str;
        str.Format("_com_error: %x", e.Error());
        fprintf(stderr, "%s\n", (const char *)str);
    } catch (CAtlException &e) {
        CAtlStringA str;
        str.Format("CAtlException: %x", (HRESULT)e);
        fprintf(stderr, "%s\n", (const char *)str);
    } catch (...) {
        fprintf(stderr, "unknown exception\n");
    }
    return 0;
}

