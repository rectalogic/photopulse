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




public class PanoramaEffect extends Effect {

    public static enum Direction {
        LEFT("left"),
        RIGHT("right");

        private String oldName;

        Direction(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    // XXX support UP/DOWN one day
    private Direction direction = Direction.LEFT;

    /**
     * Override equals to take into account the direction.
     * @param other
     * @return
     */
    public boolean equals(Object other) {
        boolean ret = super.equals(other);
        if( ret ) {
            return direction == ((PanoramaEffect)other).direction;
        }
        return ret;
    }

    PanoramaEffect(String tag) {
        super(tag);
    }

    public PanoramaEffect clonePanoramaEffect(Direction direction) {
        PanoramaEffect clone = (PanoramaEffect)clone();
        clone.direction = direction;
        return clone;
    }

    public Direction getDirection() {
        return direction;
    }

    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
