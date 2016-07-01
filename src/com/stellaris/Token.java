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
public class Token {

    private final String str;
    private final int line;

    public Token(String value, int index) {
        str = value;
        line = index;
    }

    public String getValue() {
        return str;
    }

    public int getLineNumber() {
        return line;
    }

    public String toString() {
        return str;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Token) {
            return str.equals(((Token) o).str);
        } else if (o instanceof String) {
            return str.equals((String) o);
        }
        return false;
    }

}
