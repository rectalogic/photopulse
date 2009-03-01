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
 * Portions created by the Initial Developer are Copyright (C) 2009
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.exporter;

import com.photica.photopulse.model.ShowModel;

import java.io.File;

public class ExporterFactory {
    public static ShowExporter getExporter(ShowModel.ShowType showType, File exportFile) throws ExportException {
        return getExporter(showType, isExporterHighQuality(showType), exportFile);
    }

    public static ShowExporter getExporter(ShowModel.ShowType showType, boolean isHighQuality, File exportFile) throws ExportException {
        switch (showType) {
        case EXE:
            return new EXEExporter(exportFile, showType, isHighQuality);
        case HTM:
            return new HTMExporter(exportFile, showType, isHighQuality);
        case SWF:
            return new SWFExporter(exportFile, showType, isHighQuality);
        case MHT:
            return new MHTExporter(exportFile, showType, isHighQuality);
        case WMV:
            return new WMVExporter(exportFile, showType, isHighQuality);
        default:
            throw new IllegalArgumentException(showType.name());
        }
    }

    /**
     * Return default exporter quality.
     */
    public static boolean isExporterHighQuality(ShowModel.ShowType showType) {
        switch (showType) {
        case EXE:
        case HTM:
        case SWF:
        case MHT:
            return false;
        case WMV:
            return true;
        default:
            throw new IllegalArgumentException(showType.name());
        }
    }

    public static PreviewExporter getPreviewExporter() throws ExportException {
        return new FlashPlayerPreviewExporter();
    }
}
