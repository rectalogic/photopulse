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

import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collection;


/**
 *  A Model Used for PhotoPulse Lists. An interface is used so that an implementation
 * can mix in an interface compatible with
 */
public class ShowList extends ArrayList<PhotoEffects> implements ListModel {

    private EventListenerList listenerList = null;

    public ShowList() {
        super();
    }
    public ShowList(int sz) {
        super(sz);
    }

    ShowList(ShowList other) {
        super(other);
    }

    // wrap all setters to ensure that the proper events get fired ///////////////////
    public void add(int index, PhotoEffects effects) {
        super.add(index, effects);
        fireListDataEvent(ListDataEvent.INTERVAL_ADDED, index, index);
    }

    public boolean add(PhotoEffects effects) {
        int index = size();
        boolean ret = super.add(effects);
        fireListDataEvent(ListDataEvent.INTERVAL_ADDED, index, index);
        return ret;
    }

    public boolean addAll(Collection<? extends PhotoEffects> c) {
        int idx1 = size();
        boolean ret = super.addAll(c);
        int idx2 = size();
        fireListDataEvent(ListDataEvent.INTERVAL_ADDED, idx1, idx2);
        return ret;
    }

    public boolean addAll(int index, Collection<? extends PhotoEffects> c) {
        boolean ret = false;
        int idx1 = index;
        int idx2 = idx1 + c.size() - 1;
        ret = super.addAll(idx1, c);
        fireListDataEvent(ListDataEvent.INTERVAL_ADDED, idx1, idx2);
        return ret;
    }

    public void clear() {
        if (!isEmpty()) {
            int idx0 = 0;
            int idx1 = size() - 1;
            super.clear();
            fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, idx0, idx1);
        }
    }

    public PhotoEffects remove(int idx) {
        PhotoEffects effects = super.remove(idx);
        if (effects != null)
            fireListDataEvent(ListDataEvent.INTERVAL_REMOVED, idx, idx);
        return effects;
    }

    public PhotoEffects set(int index, PhotoEffects effects) {
        PhotoEffects ret = super.set(index, effects);
        fireListDataEvent(ListDataEvent.CONTENTS_CHANGED, index, index);
        return ret;
    }

    protected void removeRange(int idx0 , int idx1) {
        super.removeRange(idx0, idx1);
        // idx1 is exclusive but the event fires as inclusive so subtract 1
        fireListDataEvent(ListDataEvent.CONTENTS_CHANGED, idx0, idx1-1);
    }


    // END wrapping of all setters///////////////////////////////////////////



    // ListModel Interfaces
    public PhotoEffects getElementAt(int index) {
        return this.get(index);
    }

    public int getSize() {
        return this.size();
    }

    // ListDataListener Interfaces
    public void addListDataListener(ListDataListener l) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ListDataListener.class, l);
    }

    public void removeListDataListener(ListDataListener l) {
        if (listenerList == null)
            return;
        listenerList.remove(ListDataListener.class, l);
    }

    /**
     * Redefine equals() to be identity.
     * This breaks the List interface contract, but is necessary when using ShowList as a JList model.
     * When swapping ShowLists on the JList, it does not fire a property change event if the ShowLists are equals(),
     * and so the ListUI does not register listeners on the ShowList.
     * http://developer.java.sun.com/developer/bugParade/bugs/4528403.html
     * http://developer.java.sun.com/developer/bugParade/bugs/4257639.html
     */
    public boolean equals(Object o) {
        return this == o;
    }

    private void fireListDataEvent(int type, int index0, int index1) {
        if (listenerList == null)
            return;

        ListDataEvent event = null;
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                // Lazily create the event:
                if (event == null)
                    event = new ListDataEvent(this, type, index0, index1);

                switch (type) {
                case ListDataEvent.INTERVAL_ADDED:
                    ((ListDataListener)listeners[i + 1]).intervalAdded(event);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    ((ListDataListener)listeners[i + 1]).intervalRemoved(event);
                    break;
                case ListDataEvent.CONTENTS_CHANGED:
                    ((ListDataListener)listeners[i + 1]).contentsChanged(event);
                    break;
                }
            }
        }
    }
}
