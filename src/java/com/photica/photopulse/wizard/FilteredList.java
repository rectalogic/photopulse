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

package com.photica.photopulse.wizard;

import java.util.AbstractList;
import java.util.BitSet;
import java.util.List;

// Wraps an immutable List, the underlying List is not modified - removing elements simply flags them as removed.
class FilteredList<E> extends AbstractList<E> {
    // Set bits indicate the List indices that "exist"
    private BitSet mask;
    private List<E> wrappedList;

    /**
     * @param list Underlying List
     */
    public FilteredList(List<E> list) {
        wrappedList = list;
        int size = list.size();
        mask = new BitSet(size);
        mask.set(0, size);
    }

    public int size() {
        return mask.cardinality();
    }

    public E get(int index) {
        return wrappedList.get(mapIndex(index));
    }

    public E remove(int index) {
        int maskIndex = mapIndex(index);
        mask.clear(maskIndex);
        return wrappedList.get(maskIndex);
    }

    // Map List index to underlying base List index.
    // List index N is the base List element corresponding to the Nth set bit in the mask.
    private int mapIndex(int listIndex) {
        int maskIndex = -1;
        int count = 0;
        for (maskIndex = mask.nextSetBit(0), count = 0;
                maskIndex >= 0 && count < listIndex;
                maskIndex = mask.nextSetBit(maskIndex + 1), count++) {
            // Do nothing
        }
        if (maskIndex < 0 || count != listIndex)
            throw new ArrayIndexOutOfBoundsException(listIndex);
        return maskIndex;
    }
}