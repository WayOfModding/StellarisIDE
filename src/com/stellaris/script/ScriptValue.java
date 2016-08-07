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

import com.stellaris.Type;
import com.stellaris.TypeComparator;
import java.io.StringReader;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author donizyo
 */
public abstract class ScriptValue {

    protected Set<Type> type;

    public ScriptValue() {
    }

    /**
     * Invoked by ScriptValue.updateTypeInfo
     *
     * @return
     */
    protected Type getType() {
        return null;
    }

    public void updateTypeInfo(ScriptValue oldValue) {
        Set<Type> set;
        Type t;

        if (oldValue == null) {
            return;
        }
        getTypeSet();
        set = oldValue.type;
        if (set == null || set.isEmpty()) {
            t = oldValue.getType();
            if (t != null) {
                type.add(t);
            }
        } else {
            type.addAll(set);
        }
    }

    public Set<Type> getTypeSet() {
        Type t;

        if (type == null) {
            type = new TreeSet<>(TypeComparator.DEFAULT_COMPARATOR);
            t = getType();
            if (t != null) {
                type.add(t);
            }
        }
        return type;
    }

    public static ScriptValue parseString(String str) {
        int iVal;
        float fVal;

        if (str == null)
            throw new NullPointerException();
        if ("none".equals(str))
            return new ScriptNull();
        if ("yes".equals(str))
            return ScriptBoolean.TRUE;
        if ("no".equals(str))
            return ScriptBoolean.FALSE;
        if (str.startsWith("\"") && str.endsWith("\""))
            return new ScriptString(str);
        if (str.startsWith("{") && str.endsWith("}")) {
            //try (StringReader reader = new StringReader(str);) {
                // TODO Range, Struct, List, Color-list
            //}
            throw new UnsupportedOperationException("ScriptStruct");
        }
        if (str.startsWith("rgb") || str.startsWith("hsv")) {
            throw new UnsupportedOperationException("ScriptColor");
        }
        try {
            iVal = Integer.parseInt(str);
            return new ScriptInteger(iVal);
        } catch (NumberFormatException ex) {
        }
        try {
            fVal = Float.parseFloat(str);
            return new ScriptFloat(fVal);
        } catch (NumberFormatException ex) {
        }
        return new ScriptReference(str);
    }
}
