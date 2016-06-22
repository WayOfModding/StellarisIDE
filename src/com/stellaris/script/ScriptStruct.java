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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptEngine;

/**
 *
 * @author donizyo
 */
public class ScriptStruct extends ScriptValue implements Bindings {

    private Bindings structValue;

    public ScriptStruct() {
        super();
    }

    @Override
    public Object put(String name, Object value) {
        return structValue.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        structValue.putAll(toMerge);
    }

    @Override
    public boolean containsKey(Object key) {
        return structValue.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return structValue.get(key);
    }

    @Override
    public Object remove(Object key) {
        return structValue.remove(key);
    }

    @Override
    public int size() {
        return structValue.size();
    }

    @Override
    public boolean isEmpty() {
        return structValue.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return structValue.containsValue(value);
    }

    @Override
    public void clear() {
        structValue.clear();
    }

    @Override
    public Set<String> keySet() {
        return structValue.keySet();
    }

    @Override
    public Collection<Object> values() {
        return structValue.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return structValue.entrySet();
    }
}
