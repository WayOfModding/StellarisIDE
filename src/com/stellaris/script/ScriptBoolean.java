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
public class ScriptBoolean extends ScriptValue {

    private static final String TRUE = "yes";
    private static final String FALSE = "no";
    private boolean boolValue;

    public ScriptBoolean(boolean value) {
        boolValue = value;
    }

    public void set(boolean newValue) {
        boolValue = newValue;
    }

    public boolean get() {
        return boolValue;
    }

    public void parse(String s) {
        switch (s) {
            case TRUE:
                set(true);
                return;
            case FALSE:
                set(false);
                return;
        }
        throw new TokenException(s);
    }

    protected Type getType() {
        return Type.BOOLEAN;
    }

    public String toString() {
        return get() ? TRUE : FALSE;
    }
}
