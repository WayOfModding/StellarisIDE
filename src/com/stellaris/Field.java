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
public final class Field {

    public static final char SEPERATOR = '.';
    private final Field parent;
    private final String name;

    public Field(Field parent, String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder sb;

        if (parent == null) {
            return name;
        }
        sb = new StringBuilder();
        sb.append(parent.getName());
        sb.append(SEPERATOR);
        sb.append(name);

        return sb.toString();
    }

    public boolean equals(Field field) {
        Field p;
        String mp;
        String mn;
        String fp;
        String fn;

        if (field == null) {
            return false;
        }

        p = field.parent;
        mp = parent == null ? null : parent.getName();
        fp = p == null ? null : p.getName();
        mn = getName();
        fn = field.getName();

        return mp == null ? mp == null : mp.equals(fp) && mn.equals(fn);
    }
}
