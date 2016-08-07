/*
 * Copyright (C) 2016 donizyo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stellaris.script;

import com.stellaris.TokenException;
import com.stellaris.Type;

/**
 *
 * @author donizyo
 */
public abstract class ScriptBoolean extends ScriptValue {

    private static final String STR_TRUE = "yes";
    private static final String STR_FALSE = "no";

    private ScriptBoolean() {
    }

    protected Type getType() {
        return Type.BOOLEAN;
    }

    public abstract boolean get();

    public abstract String toString();

    public static final ScriptBoolean TRUE = new ScriptBoolean() {
        @Override
        public boolean get() {
            return true;
        }

        @Override
        public String toString() {
            return STR_TRUE;
        }
    };

    public static final ScriptBoolean FALSE = new ScriptBoolean() {
        @Override
        public boolean get() {
            return false;
        }

        @Override
        public String toString() {
            return STR_FALSE;
        }
    };
}
