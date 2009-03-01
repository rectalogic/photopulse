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
 
package com.photica.photopulse.model.effects;

public abstract class BaseEffect implements Cloneable {
    private String tag;

    // Package private - cannot be constructed or subclassed outside package
    BaseEffect(String tag) {
        this.tag = tag;
    }

    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public String getTag() {
        return this.tag;
    }

    public abstract void accept(EffectVisitor visitor);

    public String toString() {
        return getClass().getName() + "[" + tag + "]";
    }

    /**
     * Override the hashcode method for transitions and effects to use the
     * tag as the hash. This provides a means for supplying different hashes
     * for each transition/effect without each one needed to override.
     * @return
     */
    public int hashCode() {
        return tag.hashCode();
    }
    
    /**
     * Override equals to declare objects of the same tag equal. Subclasses that
     * provide new tags MUST override equals to reflect the state of the sublcass.
     * @param other
     * @return
     */
    public boolean equals (Object other) {
        boolean ret = false;
        if ((other != null) && (other.getClass().equals(this.getClass()))) {
            BaseEffect be = (BaseEffect)other;
            return tag.equals(be.tag);
        }
        return ret;
    }
}
