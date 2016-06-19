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
package com.stellaris.mod;

import com.stellaris.Field;
import com.stellaris.FieldTypeBinding;
import com.stellaris.ScriptFile;
import com.stellaris.Type;
import java.util.Set;

/**
 *
 * @author donizyo
 */
public class SyntaxValidator {

    private final FieldTypeBinding syntax;

    public SyntaxValidator(FieldTypeBinding ftb) {
        if (ftb == null) {
            throw new NullPointerException();
        }
        syntax = ftb;
    }

    public void validate(ScriptFile script) {
        Set<Field> keyset;
        Set<Type> typeset;
        Set<Type> bindset;

        if (script == null) {
            throw new NullPointerException();
        }
        keyset = script.keyset();
        for (Field key : keyset) {
            bindset = syntax.getAll(key);
            if (bindset == null) {
                //throw new SyntaxException(String.format("Variable \"%s\" is invalid!", key));
                continue;
            }
            typeset = script.getAll(key);
            if (typeset == null) {
                throw new NullPointerException();
            }
            for (Type type : typeset) {
                if (!bindset.contains(type)) {
                    throw new SyntaxException(
                            String.format("Type \"%s\" is not found in %s",
                                    type, bindset
                            )
                    );
                }
            }
        }
    }
}
