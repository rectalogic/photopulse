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
 
package com.photica.photopulse.model;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import java.util.BitSet;

public class ShowSelectionModel implements Cloneable {

    public void addShowSelectionListener(ListSelectionListener l) {
        if (listeners == null)
            listeners = new EventListenerList();
        listeners.add(ListSelectionListener.class, l);
    }

    public void removeSelectionListener(ListSelectionListener l) {
        if (listeners == null)
            return;
        listeners.remove(ListSelectionListener.class, l);
    }


    /**
     * @param index0 - begin index must be <= index1
     * @param index1 - end index (inclusive) must be >= index0
     */
    public void addSelectionInterval(int index0, int index1) {
        modifySelectionInterval(index0, index1, true);
    }

    /**
     *
     * @param index0 - begin index must be <= index1
     * @param index1 - end index (inclusive) must be >= index0
     */
    public void removeSelectionInterval(int index0, int index1) {
        modifySelectionInterval(index0, index1, false);
    }


    /**
     *
     * @param index0 - begin index must be <= index1
     * @param index1 - end index (inclusive) must be >= index0
     * @param val - value to set
     */
    private void modifySelectionInterval(int index0, int index1, boolean val) {
        // convert from exclusive (bitset) to inclusive
        selection.set(index0, index1 + 1, val);
        fireListSelectionEvent(index0, index1);
    }

    public void toggleSelectionIndex(int index) {
        selection.flip(index);
        fireListSelectionEvent(index, index);

    }

    public void setSelection(BitSet newSelection) {
        int newL = newSelection.nextSetBit(0);
        int myL = selection.nextSetBit(0);
        int index0 = Math.min(newL, myL);
        int newH = newSelection.length();
        int myH = selection.length();
        // convert from exclusive (bitset) to inclusive
        int index1 = Math.max(newH, myH) - 1;
        selection.clear();
        selection.or(newSelection);
        fireListSelectionEvent(index0, index1);

    }
    /**
     * Indices are inclusive
     * @param newL - new lower bound of selection is <= newH
     * @param newH - new upper bound of selection is >= newL
     */
    public void setSelectionInterval(int newL, int newH) {
        if (newL == -1 || newH == -1)
            return;

        int myL = selection.nextSetBit(0);
        int index0 = myL == -1 ? newL : Math.min(newL, myL);

        int myH = selection.length();
        int index1 = myH == 0 ? newH : Math.max(newH, myH - 1);

        selection.clear();
        selection.set(newL, newH + 1);

        fireListSelectionEvent(index0, index1);
    }


    public void clearSelection() {
        int index0 = selection.nextSetBit(0);
        int index1 = selection.length() - 1;

        if (index0 == -1 || index1 == -1)
            return;

        selection.clear();
        fireListSelectionEvent(index0, index1);
    }

    public boolean isSelectionEmpty() {
        return selection.isEmpty();
    }

    public int getSelectionCount() {
        return selection.cardinality();
    }

    public boolean isSelectedIndex(int idx) {
        return selection.get(idx);
    }


    private void fireListSelectionEvent(int idx0, int idx1) {
        if (listeners == null)
            return;

        // Guaranteed to return a non-null array
        Object[] list = listeners.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        ListSelectionEvent evt = null;
        if (list.length > 0) {
            evt = new ListSelectionEvent(this, idx0, idx1, false);
            for (int i = list.length - 2; i >= 0; i -= 2) {
                ((ListSelectionListener) list[i + 1]).valueChanged(evt);
            }
        }
    }

    /**
     * Clone this selection model without keeping listeners.
     * @return
     */
    public Object clone() {
        ShowSelectionModel myClone = null;
        try {
            // get a shallow copy of all variables
            myClone = (ShowSelectionModel) super.clone();
            // Clone bitset
            myClone.selection = (BitSet)selection.clone();
            // No listeners
            myClone.listeners = null;
        } catch (CloneNotSupportedException ex) {
            throw new IllegalStateException("Clone Operation Failed");
        }
        return myClone;
    }

    private BitSet selection = new BitSet();
    private EventListenerList listeners = null;
}
