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

import java.util.List;

/**
 *
 * @author donizyo
 */
public class TokenException extends RuntimeException {

    public TokenException(Field parent, String token) {
        this(String.format("parent=%s, token=%s", parent, token));
    }

    public TokenException(List<String> tokens) {
        this(tokens == null ? "[]" : tokens.toString());
    }

    public TokenException(String err) {
        super(err);
    }

    public TokenException() {
        super();
    }

}
