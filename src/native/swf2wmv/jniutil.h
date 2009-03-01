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

#pragma once

#define LINK_EXCEPTION "java/lang/UnsatisfiedLinkError"
#define METHOD_EXCEPTION "java/lang/NoSuchMethodError"
#define OOM_EXCEPTION "java/lang/OutOfMemoryError"

#define INSTANTIATION_EXCEPTION "java/lang/InstantiationException"

#define ENCODE_EXCEPTION "com/photica/photopulse/exporter/WMVExporter$WMVEncodeException"


// Throw exception szClass with message szMessage
inline void JNIThrow(JNIEnv *env, const char *szClass, const char *szMessage)
{
    TRACE("JNIThrow %s: %s\n", szClass, szMessage);
    jclass jcEx = env->FindClass(szClass);
    if (jcEx)
        env->ThrowNew(jcEx, szMessage);
}

// Throw ENCODE_EXCEPTION with specified message and HRESULT
void JNIThrowEncodeException(JNIEnv *env, const char *szMessage, HRESULT hr, const char *szLocation)
{
    TRACE("JNIThrowEncodeException: %x %s - %s\n", hr, szMessage, szLocation);
    jclass jcEx = env->FindClass(ENCODE_EXCEPTION);
    if (jcEx) {
        jstring jstrMessage = env->NewStringUTF(szMessage);
        jstring jstrLocation = NULL;
        if (szLocation)
            jstrLocation = env->NewStringUTF(szLocation);
        if (jstrMessage) {
            jmethodID jmidCtor = env->GetMethodID(jcEx, "<init>", "(Ljava/lang/String;ILjava/lang/String;)V");
            if (jmidCtor) {
                jthrowable joEx = (jthrowable)env->NewObject(jcEx, jmidCtor, jstrMessage, (jint)hr, jstrLocation);
                if (joEx)
                    env->Throw(joEx);
            }
        }
    }
}

// Manage creating/releasing Java strings
class CJavaString
{
public:
    CJavaString(JNIEnv *env, jstring jstr) throw(...) :
        m_jchar(NULL)
    {
        m_jniEnv = env;
        m_jchar = m_jniEnv->GetStringChars(jstr, NULL);
        if (!m_jchar)
            throw std::bad_alloc("JNI GetStringChars failed");
    }

    ~CJavaString()
    {
        if (m_jchar)
            m_jniEnv->ReleaseStringChars(m_jstr, m_jchar);
    }

    const jchar *GetString()
    {
        return m_jchar;
    }

private:
    JNIEnv *m_jniEnv;
    jstring m_jstr;
    const jchar *m_jchar;
};

// Manage creating/releasing Java strings
class CJavaStringUTF
{
public:
    CJavaStringUTF(JNIEnv *env, jstring jstr) throw(...) :
        m_jchar(NULL)
    {
        m_jniEnv = env;
        m_jchar = m_jniEnv->GetStringUTFChars(jstr, NULL);
        if (!m_jchar)
            throw std::bad_alloc("JNI GetStringUTFChars failed");
    }

    ~CJavaStringUTF()
    {
        if (m_jchar)
            m_jniEnv->ReleaseStringUTFChars(m_jstr, m_jchar);
    }

    const char *GetString()
    {
        return m_jchar;
    }

private:
    JNIEnv *m_jniEnv;
    jstring m_jstr;
    const char *m_jchar;
};

// Manage array of Java strings
class CJavaStringArray
{
public:
    CJavaStringArray(JNIEnv *env, jobjectArray jstrArray) throw(...)
    {
        m_jniEnv = env;
        m_jstrArray = jstrArray;
        m_nArrayLength = jstrArray ? m_jniEnv->GetArrayLength(jstrArray) : 0;
        if (m_nArrayLength > 0) {
            m_jcharArray = new const jchar * [m_nArrayLength];
            for (int i = 0; i < m_nArrayLength; i++) {
                jstring jstr = (jstring)m_jniEnv->GetObjectArrayElement(m_jstrArray, i);
                if (!jstr)
                    throw std::bad_alloc("null jstring array element");
                m_jcharArray[i] = m_jniEnv->GetStringChars(jstr, NULL);
                if (!m_jcharArray[i])
                    throw std::bad_alloc("JNI GetStringChars failed");
            }
        }
        else
            m_jcharArray = NULL;
    }

    ~CJavaStringArray()
    {
        if (m_jcharArray) {
            for (int i = 0; i < m_nArrayLength; i++) {
                if (m_jcharArray[i])
                    m_jniEnv->ReleaseStringChars((jstring)m_jniEnv->GetObjectArrayElement(m_jstrArray, i), m_jcharArray[i]);
            }
            delete[] m_jcharArray;
        }
    }

    int GetArrayLength()
    {
        return m_nArrayLength;
    }

    const jchar **GetStringArray()
    {
        return m_jcharArray;
    }

private:
    JNIEnv *m_jniEnv;
    int m_nArrayLength;
    jobjectArray m_jstrArray;
    const jchar ** m_jcharArray;
};