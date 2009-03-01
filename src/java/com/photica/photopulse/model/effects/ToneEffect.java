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
 
package com.photica.photopulse.model.effects;


public class ToneEffect extends Effect {

    public static enum Tone {
        SEPIA("sepia"),
        GRAY("gray");

        private String oldName;

        Tone(String oldName) {
            this.oldName = oldName;
        }

        // Must use toString() and not name() when generating XML
        public String toString() {
            return oldName;
        }
    };

    private Tone tone = Tone.SEPIA;

    public boolean equals(Object other) {
        boolean ret = super.equals(other);
        if (ret) {
            ToneEffect te = (ToneEffect)other;
            return tone == te.tone;
        }
        return ret;
    }

    ToneEffect(String tag) {
        super(tag);
    }

    public ToneEffect cloneToneEffect(Tone tone) {
        ToneEffect clone = (ToneEffect)clone();
        clone.tone = tone;
        return clone;
    }

    public Tone getTone() {
        return tone;
    }

    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
