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

import java.util.*;

/**
 *
 * @author donizyo
 */
public class FieldTypeBinding {

    private static final Comparator<Type> DEFAULT_COMPARATOR
            = new TypeComparator();
    private final Map<Field, SortedSet<Type>> map;

    public FieldTypeBinding() {
        map = new HashMap<>();
    }

    public SortedSet<Type> getAll(Field field) {
        if (field == null) {
            return null;
        }
        return map.get(field);
    }

    public Type get(Field field) {
        SortedSet<Type> set;

        if (field == null) {
            return null;
        }
        set = getAll(field);
        return set.first();
    }

    public boolean put(Field field, Type type) {
        SortedSet<Type> set;

        if (field == null) {
            return false;
        }
        if (type == null) {
            return false;
        }
        set = map.get(field);
        if (set == null) {
            set = new TreeSet<>(DEFAULT_COMPARATOR);
            map.put(field, set);
        }
        return set.add(type);
    }

    private static class TypeComparator implements Comparator<Type> {

        @Override
        public int compare(Type o1, Type o2) {
            return o1.ordinal() - o2.ordinal();
        }
    }
}
