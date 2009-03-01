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
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Decodes jar files.
 * Delegates most work to a FileURLConnection.
 */
final class JaxURLConnection extends URLConnection {

    // FileURLConnection delegate
    private URLConnection m_ucDelegate;

    protected JaxURLConnection(URL url) throws IOException {
        super(url);

        // Get a FileURLConnection to delegate to
        m_ucDelegate = new File(url.getFile().replace('/', File.separatorChar)).toURL().openConnection();
    }


    // Overrides URLConnection
    public synchronized void connect() throws IOException {
        m_ucDelegate.connect();
    }
    // Overrides URLConnection
    public boolean getAllowUserInteraction() {
        return m_ucDelegate.getAllowUserInteraction();
    }

    // Delegate to superclass so cached content will be used
    // public Object getContent() throws IOException

    // Overrides URLConnection
    public String getContentEncoding() {
        return m_ucDelegate.getContentEncoding();
    }
    // Overrides URLConnection
    public int getContentLength() {
        return m_ucDelegate.getContentLength();
    }
    // Overrides URLConnection
    public String getContentType() {
        return m_ucDelegate.getContentType();
    }
    // Overrides URLConnection
    public long getDate() {
        return m_ucDelegate.getDate();
    }
    // Overrides URLConnection
    public boolean getDefaultUseCaches() {
        return m_ucDelegate.getDefaultUseCaches();
    }
    // Overrides URLConnection
    public boolean getDoInput() {
        return m_ucDelegate.getDoInput();
    }
    // Overrides URLConnection
    public boolean getDoOutput() {
        return m_ucDelegate.getDoOutput();
    }
    // Overrides URLConnection
    public long getExpiration() {
        return m_ucDelegate.getExpiration();
    }
    // Overrides URLConnection
    public String getHeaderField(int n) {
        return m_ucDelegate.getHeaderField(n) ;
    }
    // Overrides URLConnection
    public String getHeaderField(String name) {
        return m_ucDelegate.getHeaderField(name);
    }
    // Overrides URLConnection
    public long getHeaderFieldDate(String name, long Default) {
        return m_ucDelegate.getHeaderFieldDate(name, Default);
    }
    // Overrides URLConnection
    public int getHeaderFieldInt(String name, int Default) {
        return m_ucDelegate.getHeaderFieldInt(name, Default);
    }
    // Overrides URLConnection
    public String getHeaderFieldKey(int n) {
        return m_ucDelegate.getHeaderFieldKey(n);
    }
    // Overrides URLConnection
    public long getIfModifiedSince() {
        return m_ucDelegate.getIfModifiedSince();
    }
    // Overrides URLConnection
    public InputStream getInputStream() throws IOException {
        PushbackInputStream pis = new PushbackInputStream(m_ucDelegate.getInputStream(), 1);
        // Return custom decoding stream if it contains the prefix
        int nByte = pis.read();
        if (nByte == JaxInputStream.JAX_PREFIX)
            return new JaxInputStream(pis);
        else {
            pis.unread(nByte);
            return pis;
        }
    }
    // Overrides URLConnection
    public long getLastModified() {
        return m_ucDelegate.getLastModified();
    }
    // Overrides URLConnection
    public OutputStream getOutputStream() throws IOException {
        return m_ucDelegate.getOutputStream();
    }
    // Overrides URLConnection
    public java.security.Permission getPermission() throws IOException {
        return m_ucDelegate.getPermission();
    }

    // Let superclass return the "jax:" URL
    // public URL getURL()

    // Overrides URLConnection
    public String getRequestProperty(String key) {
        return m_ucDelegate.getRequestProperty(key);
    }
    // Overrides URLConnection
    public boolean getUseCaches() {
        return m_ucDelegate.getUseCaches();
    }
    // Overrides URLConnection
    public void setAllowUserInteraction(boolean allowuserinteraction) {
        m_ucDelegate.setAllowUserInteraction(allowuserinteraction);
    }
    // Overrides URLConnection
    public void setDefaultUseCaches(boolean defaultusecaches) {
        m_ucDelegate.setDefaultUseCaches(defaultusecaches);
    }
    // Overrides URLConnection
    public void setDoInput(boolean doinput) {
        m_ucDelegate.setDoInput(doinput);
    }
    // Overrides URLConnection
    public void setDoOutput(boolean dooutput) {
        m_ucDelegate.setDoOutput(dooutput);
    }
    // Overrides URLConnection
    public void setIfModifiedSince(long ifmodifiedsince) {
        m_ucDelegate.setIfModifiedSince(ifmodifiedsince);
    }
    // Overrides URLConnection
    public void setRequestProperty(String key, String value) {
        m_ucDelegate.setRequestProperty(key, value);
    }
    // Overrides URLConnection
    public void setUseCaches(boolean usecaches) {
        m_ucDelegate.setUseCaches(usecaches);
    }
}
