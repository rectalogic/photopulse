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

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public final class JaxInputStream extends FilterInputStream {

    // Single byte prefix indicating stream is encoded
    public static final int JAX_PREFIX = 0x7A;

    // Incremented and xor'd for every byte read. Overflow just wraps around.
    private byte m_byPos = 7;

    public JaxInputStream(InputStream in) {
        super(in);
    }

    public synchronized int read() throws IOException {
        int nData = super.read();
        if (nData != -1) {
            nData ^= m_byPos++;
        }
        return nData;
    }

    public synchronized int read(byte[] abData, int nOffset, int nLength) throws IOException {
        int nTotal = super.read(abData, nOffset, nLength);
        if (nTotal != -1) {
            for (int i = nOffset; i < nLength; i++) {
                abData[i] ^= m_byPos++;
            }
        }
        return nTotal;
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readlimit) {
        // Do nothing
    }

    public void reset() throws IOException {
        throw new IOException();
    }
}