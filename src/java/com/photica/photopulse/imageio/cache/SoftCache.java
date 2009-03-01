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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Andrew Wason, Mike Mills
 * info@photica.com
 *
 * ***** END LICENSE BLOCK ***** */
 
package com.photica.photopulse.imageio.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;

public class SoftCache<K,V> {
    private HashMap<K,SoftReference<V>> cache = new HashMap<K, SoftReference<V>>();

    public synchronized V get(K key) {
        SoftReference<V> ref = cache.get(key);
        if (ref == null)
            return null;
        V value = ref.get();
        if (value == null)
            cache.remove(key);
        return value;
    }

    public synchronized void put(K key, V value) {
        cache.put(key, new SoftReference<V>(value));
    }

    public synchronized boolean containsKey(K key) {
        SoftReference<V> ref = cache.get(key);
        if (ref == null)
            return false;
        if (ref.get() == null) {
            cache.remove(key);
            return false;
        }
        return true;
    }

    public synchronized void clear() {
        cache.clear();
    }
}
