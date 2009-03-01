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

package com.photica.photopulse.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// MHTML support
// See section ftp://ftp.isi.edu/in-notes/rfc2557.txt

public class MHTML {

    private static final String MHTML_BOUNDARY =
        "----=_NextPart_000_0000_01C1F5CD.44B11000";

    private static final String MHTML_HEADER =
        "From: <Saved by Photica PhotoPulse>\n" +
        "Subject: Photica PhotoPulse Show\n" +
        "Date: {0}\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: multipart/related;\n" +
        "\tboundary=\"" + MHTML_BOUNDARY + "\";\n" +
        "\ttype=\"text/html\"\n" +
        // This makes the message editable in Outlook (save as *.eml)
        //"X-Unsent: 1\n" +
        "\n" +
        "This is a multi-part message in MIME format.\n";

    private static final String MHTML_PART =
        "\n--" + MHTML_BOUNDARY + "\n" +
        "Content-Type: {0}\n" +
        "Content-Transfer-Encoding: {1}\n" +
        "Content-Location: {2}\n\n";

    private static final String CHARSET_HTML = "ISO-8859-1";

    public static final String MIMETYPE_HTML = "text/html; charset=\"" + CHARSET_HTML + "\"";
    public static final String MIMETYPE_JPG = "image/jpeg";
    public static final String MIMETYPE_PNG = "image/png";
    public static final String MIMETYPE_SWF = "application/x-shockwave-flash";


    private Writer m_writer;
    private QuotedPrintableOutputStream m_qpos;
    private Base64OutputStream m_b64os;

    public MHTML(OutputStream os) throws IOException {
        m_writer = new OutputStreamWriter(os, "US-ASCII");
        m_qpos = new QuotedPrintableOutputStream(os);
        m_b64os = new Base64OutputStream(os);

        writeHeader();
    }

    private void writeHeader() throws IOException {
        // RFC822 date format
        // ftp://ftp.isi.edu/in-notes/rfc822.txt
        String strDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date());
        m_writer.write(MessageFormat.format(MHTML_HEADER, new Object[] { strDate }));
        m_writer.flush();
    }

    public void writeHTMLPart(String strHTML, String strURL) throws IOException {
        finishStreams();

        m_writer.write(MessageFormat.format(MHTML_PART,
            new Object[] { MIMETYPE_HTML, QuotedPrintableOutputStream.ENCODING, strURL }));
        m_writer.flush();

        m_qpos.write(strHTML.getBytes(CHARSET_HTML));
        m_qpos.finish();
    }

    /**
     * Writes base64 header. Caller should use the returned stream to write the data
     */
    public OutputStream writeBinaryPartHeader(String strURL, String strMimeType) throws IOException {
        finishStreams();

        m_writer.write(MessageFormat.format(MHTML_PART,
            new Object[] { strMimeType, Base64OutputStream.ENCODING, strURL }));
        m_writer.flush();

        return m_b64os;
    }

    public void writeFinish() throws IOException {
        finishStreams();

        m_writer.write("\n--");
        m_writer.write(MHTML_BOUNDARY);
        m_writer.write("--\n");
        m_writer.flush();
    }

    private void finishStreams() throws IOException {
        m_qpos.finish();
        m_b64os.finish();
    }
}