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

// MIME BASE64 encoder
// See section 5.2 ftp://ftp.isi.edu/in-notes/rfc1521.txt

// Not threadsafe
class Base64OutputStream extends MimeOutputStream {

    public static final String ENCODING = "base64";

    private static final byte BASE64MAP[] = {
        (byte)'A', (byte)'B', (byte)'C', (byte)'D', (byte)'E', (byte)'F', (byte)'G', (byte)'H',
        (byte)'I', (byte)'J', (byte)'K', (byte)'L', (byte)'M', (byte)'N', (byte)'O', (byte)'P',
        (byte)'Q', (byte)'R', (byte)'S', (byte)'T', (byte)'U', (byte)'V', (byte)'W', (byte)'X',
        (byte)'Y', (byte)'Z', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f',
        (byte)'g', (byte)'h', (byte)'i', (byte)'j', (byte)'k', (byte)'l', (byte)'m', (byte)'n',
        (byte)'o', (byte)'p', (byte)'q', (byte)'r', (byte)'s', (byte)'t', (byte)'u', (byte)'v',
        (byte)'w', (byte)'x', (byte)'y', (byte)'z', (byte)'0', (byte)'1', (byte)'2', (byte)'3',
        (byte)'4', (byte)'5', (byte)'6', (byte)'7', (byte)'8', (byte)'9', (byte)'+', (byte)'/',
    };

    // Holds 3 bytes
    private int m_nBuf;
    // Number of bytes in nBuf
    private int m_nBufBytes;
    private int m_nColumn;

    public Base64OutputStream(OutputStream out) {
        super(out);
        init();
    }

    private void init() {
        m_nBuf = 0;
        m_nBufBytes = 0;
        m_nColumn = 0;
    }

    // Overrides FilterOutputStream
    public void write(int nByte) throws IOException {
        switch (m_nBufBytes) {
        case 0:
            m_nBuf = (m_nBuf & 0x00FFFF) | (nByte << 16);
            break;
        case 1:
            m_nBuf = (m_nBuf & 0xFF00FF) | ((nByte << 8) & 0x00FF00);
            break;
        default:
            m_nBuf = (m_nBuf & 0xFFFF00) | (nByte & 0x0000FF);
            break;
        }
        m_nBufBytes++;

        // Buffer is full, write it out
        if (m_nBufBytes == 3) {

            // Encode a 3 byte token, 4 sextets
            out.write(BASE64MAP[0x3F & (m_nBuf >> 18)]);
            out.write(BASE64MAP[0x3F & (m_nBuf >> 12)]);
            out.write(BASE64MAP[0x3F & (m_nBuf >> 6)]);
            out.write(BASE64MAP[0x3F & m_nBuf]);
            m_nColumn += 4;

            m_nBuf = 0;
            m_nBufBytes = 0;

            // New line
            if (m_nColumn >= 72) {
                out.write(QuotedPrintableOutputStream.CRLF);
                m_nColumn = 0;
            }
        }
    }

    // Overrides MimeOutputStream
    public void finish() throws IOException {
        // Encode partial token
        if (m_nBufBytes != 0) {
            // Sextet 1
            out.write(BASE64MAP[0x3F & (m_nBuf >> 18)]);
            // Sextet 2
            out.write(BASE64MAP[0x3F & (m_nBuf >> 12)]);

            if (m_nBufBytes == 1)
                out.write((byte)'=');
            else {
                // Sextet 3
                out.write(BASE64MAP[0x3F & (m_nBuf >> 6)]);
            }

            if (m_nBufBytes <= 2)
                out.write((byte)'=');
            else {
                // Sextet 4
                out.write(BASE64MAP[0x3F & m_nBuf]);
            }

            // New line
            out.write(QuotedPrintableOutputStream.CRLF);
        }

        init();
    }

    // XXX debugging
    /*
    public static void main(String args[]) throws Exception {
        Base64OutputStream b64os = new Base64OutputStream(System.out);
        java.io.FileInputStream fis = new java.io.FileInputStream(args[0]);
        int nByte;
        while ((nByte = fis.read()) != -1)
            b64os.write(nByte);
        b64os.finish();
    }
    */
}

