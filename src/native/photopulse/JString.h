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

#ifndef JSTRING_H
#define JSTRING_H


// Manages a Java jstring

class CJString
{
public:
    CJString(JNIEnv *pJNIEnv, jstring jstr) : m_pJNIEnv(pJNIEnv), m_jstrString(jstr), m_pszString(0) {};

    ~CJString() {
        if (m_jstrString != NULL && m_pszString != NULL)
            m_pJNIEnv->ReleaseStringUTFChars(m_jstrString, m_pszString);
    };

    const char *GetStringUTFChars() {
        if (m_jstrString == NULL)
            return NULL;
        m_pszString = m_pJNIEnv->GetStringUTFChars(m_jstrString, NULL);
        return m_pszString;
    };

private:
    JNIEnv *m_pJNIEnv;
    jstring m_jstrString;
    const char *m_pszString;
};

#endif