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



public class RisingFallingStuffEffect extends Effect {

    public static enum Stuff {
        BUBBLE("Bubble"),
        HEART("Heart"),
        SNOWFLAKE("Snowflake"),
        LEAF("Leaf");

        private String oldName;

        Stuff(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    public static enum Direction {
        UP("up"),
        DOWN("down");

        private String oldName;

        Direction(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    private Direction direction = Direction.UP;
    private Stuff stuff = Stuff.BUBBLE;

    RisingFallingStuffEffect(String tag) {
        super(tag);
    }

    public int hashCode() {
        return stuff.hashCode() | direction.hashCode();
    }

    public boolean equals(Object other) {
        boolean ret = super.equals(other);
        if (ret) {
            RisingFallingStuffEffect rfs = (RisingFallingStuffEffect)other;
            ret = stuff == rfs.stuff && direction == rfs.direction;
        }
        return ret;
    }

    public RisingFallingStuffEffect cloneRisingFallingStuffEffect(Stuff stuff, Direction direction) {
        RisingFallingStuffEffect clone = (RisingFallingStuffEffect)clone();
        clone.direction = direction;
        clone.stuff = stuff;
        return clone;
    }

    public Stuff getStuff() {
        return stuff;
    }

    public Direction getDirection() {
        return direction;
    }

    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
