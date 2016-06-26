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

import java.util.Objects;

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

    public Field getParent() {
        return parent;
    }
    
    public String getParentName() {
        return parent == null ? null : parent.getName();
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

    @Override
    public int hashCode() {
        int hash;
        String pn;

        hash = 5;
        pn = getParentName();
        hash = 89 * hash + Objects.hashCode(pn);
        hash = 89 * hash + Objects.hashCode(name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj
                || obj != null
                && obj instanceof Field
                && equals((Field) obj);
    }

    public boolean equals(Field field) {
        String mp;
        String mn;
        String fp;
        String fn;

        if (this == field) {
            return true;
        }
        if (field == null) {
            return false;
        }

        mp = getParentName();
        fp = field.getParentName();
        mn = getName();
        fn = field.getName();

        return mp == null ? mp == null : mp.equals(fp) && mn.equals(fn);
    }
}
