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

package com.photica.photopulse.launcher.jax;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;


// Java needs to be invoked with -Djava.protocol.handler.pkgs=com.photica.photopulse.launcher

public final class Handler extends URLStreamHandler {

    // Overrides URLStreamHandler
    protected URLConnection openConnection(URL url) throws IOException {
        String strHost = url.getHost();
        if (strHost == null || strHost.length() == 0)
            return new JaxURLConnection(url);
        else {
            // Handle UNC pathnames - they get misparsed
            URL url2 = new URL("jax", "", "//" + strHost + url.getPath());
            return new JaxURLConnection(url2);
        }
    }

    // Overrides URLStreamHandler
    protected void parseURL(URL u, String spec, int start, int limit) {
        super.parseURL(u, spec.replace(File.separatorChar, '/'), start, limit);
    }

}
