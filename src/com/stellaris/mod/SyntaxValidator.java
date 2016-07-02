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

import com.stellaris.Stellaris;
import com.stellaris.Type;
import com.stellaris.script.ScriptValue;
import com.stellaris.script.SimpleFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.script.Bindings;

/**
 *
 * @author donizyo
 */
public class SyntaxValidator {

    public SyntaxValidator() {
    }

    private boolean isValidType(Type type, Set<Type> bindset) {
        if (type == null) {
            throw new IllegalArgumentException("Parameter 'type' is null!");
        }
        if (bindset == null) {
            throw new IllegalArgumentException("Parameter 'bindset' is null!");
        }
        if (bindset.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'bindset' is empty set!");
        }
        if (bindset.contains(type)) {
            return true;
        }
        // lower-ordinal type may be in fact a high-ordinal type
        switch (type) {
            case STRUCT:
                // when a LIST or a COLORLIST is empty,
                // it's highly propable that it will be
                // interpreted as a simple STRUCT
                //
                // however RANGE type cannot be empty,
                // which means a STRUCT can never
                // be an empty RANGE type, so is COLOR type
                if (bindset.contains(Type.LIST) || bindset.contains(Type.COLORLIST)) {
                    return true;
                }
                break;
            case INTEGER:
                // a FLOAT value without decimal part
                // is highly propable to be interpreted
                // as an INTEGER
                if (bindset.contains(Type.RANGE) || bindset.contains(Type.FLOAT)) {
                    return true;
                }
                // RANGE type is in fact INTEGER type
                if (bindset.contains(Type.RANGE)) {
                    return true;
                }
                break;
            case RANGE:
                // RANGE type is in fact INTEGER type
                if (bindset.contains(Type.INTEGER)) {
                    return true;
                }
                break;
            case VARIABLE:
                // a VARIABLE can be anything
                return true;
        }

        return false;
    }

    public void validate(Bindings script) throws SyntaxException {
        SimpleFactory factory;
        Bindings bindings;
        Set<String> keyset;
        Set<Type> typeset;
        Set<Type> bindset;
        ScriptValue syntax;
        ScriptValue value;

        factory = (SimpleFactory) Stellaris.getDefault();
        bindings = factory.getBindings();
        keyset = script.keySet();
        for (String key : keyset) {
            syntax = (ScriptValue) bindings.get(key);
            if (syntax == null) {
                // no available syntax binding
                throw new SyntaxException(
                        String.format("Unknown field name \"%s\"", key)
                );
            }
            bindset = syntax.getTypeSet();
            value = (ScriptValue) script.get(key);
            typeset = value.getTypeSet();
            for (Type type : typeset) {
                if (!isValidType(type, bindset)) {
                    throw new SyntaxException(
                            String.format("Field \"%s\" has type \"%s\","
                                    + " which is not found in %s",
                                    key, typeset, bindset
                            )
                    );
                }
            }
        }
    }
}
