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
package com.stellaris;

import com.stellaris.script.*;
import java.io.PrintStream;
import java.util.*;
import javax.script.*;

/**
 *
 * @author donizyo
 */
public class FieldTypeBinding {

    private final Map<String, ScriptValue> map;

    public FieldTypeBinding(ScriptContext context) {
        Bindings bindings;
        Set<String> keySet;
        Object obj;
        ScriptValue value;

        map = new TreeMap<>();
        bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        keySet = bindings.keySet();
        for (String key : keySet) {
            obj = bindings.get(key);
            if (obj == null) {
                continue;
            }
            if (!(obj instanceof ScriptValue)) {
                continue;
            }
            value = (ScriptValue) obj;
            map.put(key, value);
        }
    }

    public void list(PrintStream out) {
        Set<String> keySet;
        ScriptValue value;
        Set<Type> set;

        keySet = map.keySet();
        for (String key : keySet) {
            value = map.get(key);
            set = value.getTypeSet();
            out.format("%s=%s%n", key, set);
        }
    }
}
