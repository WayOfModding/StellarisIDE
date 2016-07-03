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

/**
 *
 * @author donizyo
 */
public enum Type {
    STRING,
    INTEGER,
    RANGE,
    FLOAT,
    STRUCT,
    BOOLEAN,
    VARIABLE,
    LIST,
    COLOR,
    COLORLIST,
    CONDITION
    ;

    public String toString() {
        return "$" + name().toLowerCase();
    }

    public static Type getType(String str) {
        Type[] types;
        String name;

        if (str == null) {
            return null;
        }
        types = Type.values();
        for (Type type : types) {
            name = type.name();
            if (name.equals(str)) {
                return type;
            }
        }
        return null;
    }
}
