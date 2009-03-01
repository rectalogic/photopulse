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

// MIME Quoted Printable encoder
// See section 5.1 ftp://ftp.isi.edu/in-notes/rfc1521.txt

// Not threadsafe
class QuotedPrintableOutputStream extends MimeOutputStream {

    public static final String ENCODING = "quoted-printable";

    private static final byte HEXMAP[] = {
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5',
        (byte)'6', (byte)'7', (byte)'8', (byte)'9',
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F'
    };

    static final byte CRLF[] = new byte[] { (byte)'\r', (byte)'\n' };
    private static final byte EQCRLF[] = new byte[] { (byte)'=', (byte)'\r', (byte)'\n' };
    private static final byte EQCRLFCRLF[] = new byte[] { (byte)'=', (byte)'\r', (byte)'\n', (byte)'\r', (byte)'\n' };


    private boolean m_bWhitespace;
    private int m_nLastChar;
    private int m_nColumn;

    public QuotedPrintableOutputStream(OutputStream out) {
        super(out);
        init();
    }

    private void init() {
        m_nColumn = 0;
        m_bWhitespace = false;
        m_nLastChar = '\0';
    }

    // Overrides FilterOutputStream
    public void write(int nChar) throws IOException {

        // No escaping if we are tab or in the printable range and are not '=',
        // and are not '.' or 'F' in column 0 ('F' because of 'From' header)
        if ((nChar >= ' ' && nChar < 127
                && nChar != '='
                && (m_nColumn != 0 || (nChar != '.' && nChar != 'F')))
            || nChar == '\t') {
            out.write(nChar);
            m_nColumn++;
            m_bWhitespace = (nChar == ' ' || nChar == '\t');
        }
        else if (nChar == '\n' && m_nLastChar == '\r') {
            // We already processed this linebreak;
            // ignore the LF in the CRLF pair.
        }
        // Start of linebreak sequence
        else if (nChar == '\r' || nChar == '\n') {
            // Whitespace cannot be allowed to occur at the end of the line.
            // So we encode " \n" as " =\n\n", that is, the whitespace, a
            // soft line break, and then a hard line break.
            if (m_bWhitespace) {
                out.write(EQCRLFCRLF);
                m_bWhitespace = false;
            }
            else
                out.write(CRLF);
            m_nColumn = 0;
        }
        // Escape special character
        else {
            out.write((byte)'=');
            out.write((byte)HEXMAP[0xF & (nChar >> 4)]);
            out.write((byte)HEXMAP[0xF & nChar]);
            m_nColumn += 3;
            m_bWhitespace = false;
        }

        m_nLastChar = nChar;

        if (m_nColumn >= 73) {
            out.write(EQCRLF);
            m_nColumn = 0;
            m_bWhitespace = false;
        }
    }

    // Overrides MimeOutputStream
    public void finish() throws IOException {
        if (m_nColumn != 0)
            out.write(EQCRLF);
        init();
    }

    // XXX debugging
    /*
    public static void main(String args[]) throws Exception {
        QuotedPrintableOutputStream qpos = new QuotedPrintableOutputStream(System.out);
        java.io.FileInputStream fis = new java.io.FileInputStream(args[0]);
        int nByte;
        while ((nByte = fis.read()) != -1)
            qpos.write(nByte);
        qpos.finish();
    }
    */
}