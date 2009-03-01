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
 
package com.photica.photopulse.license;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import com.photica.photopulse.License;
/**
 * Simple tool to leverage license infrastructure to calculate expire vlue some time
 * in the future.
 */
public class DropDeadTool {
    public static void main(String args[] ) throws Exception {
        if( args.length != 1 )  {
            System.err.println("Usage: yyy-mm-dd");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse(args[0]);
        long millis = d.getTime();

        System.out.println("EXPIRE VAL: " + millis);
        System.out.println("EXPIRE Date:  " + d );
    }
}
