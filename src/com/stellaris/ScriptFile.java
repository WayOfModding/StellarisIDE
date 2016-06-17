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

import com.stellaris.test.Debug;
import static com.stellaris.test.Debug.DEBUG;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;

/**
 * Generates AST (a.k.a abstract syntax tree) for Stellaris Script
 *
 * @author donizyo
 */
public class ScriptFile extends HashMap<Field, Type> {

    private ScriptParser parser;

    public static ScriptFile newInstance(File file) {
        try {
            return new ScriptFile(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ScriptFile(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    private ScriptFile(Reader reader) {
        parser = new ScriptParser(reader);
        analyze();
    }

    private void analyze() {
        int res;

        try {
            res = analyze(null, 0);
            if (res != 0) {
                throw new AssertionError(res);
            }
        } finally {
            parser.close();
        }
    }

    private int analyze(Field parent, int state) {
        String token, key;
        List<String> tokens;
        Field field;
        Type type;
        boolean isList;
        Patterns patterns;
        int newstate;

        if (DEBUG) {
            System.err.format("[PARSE]\tparent=%s, state=%d%n",
                    parent, state
            );
        }
        while (parser.hasNext()) {
            token = parser.next();
            // ignore comment token
            if (token.charAt(0) == '#') {
                continue;
            }
            // return
            if ("}".equals(token)) {
                return --state;
            }
            {
                if (handleColorList(token)) {
                    type = Type.COLORLIST;
                    put(parent, type);
                    return --state;
                } else {
                    key = token;
                    field = new Field(parent, key);
                }

                // operator
                // or list?
                token = parser.next();
                isList = !"=".equals(token)
                        && !">".equals(token)
                        && !"<".equals(token);

                if (isList) {
                    // handle single-element list
                    if ("}".equals(token)) {
                        type = Type.LIST;
                        put(parent, type);
                        return --state;
                    }

                    {
                        // handle multiple-element list
                        while (true) {
                            token = parser.next();
                            if ("}".equals(token)) {
                                type = Type.LIST;
                                put(parent, type);
                                return --state;
                            }
                            if ("{".equals(token)
                                    || "yes".equals(token)
                                    || "no".equals(token)) {
                                throw new TokenException(parent, token);
                            }
                            // number list is allowed
                            /*
                            try {
                                // integer
                                Integer.parseInt(token);
                                throw new TokenException(token);
                            } catch (NumberFormatException e1) {
                                // float
                                try {
                                    Float.parseFloat(token);
                                    throw new TokenException(token);
                                } catch (NumberFormatException e2) {
                                }
                            }
                            */
                        }
                    }
                } else {
                    // value
                    token = parser.next();
                    patterns = checkColorToken(token);
                    if (patterns != null) {
                        type = handleColorToken(patterns);
                    } else if ("{".equals(token)) {
                        tokens = parser.peek(7);
                        // { -> min = INTEGER max = INTEGER }
                        if (Patterns.PS_RANGE.matches(tokens)) {
                            type = Type.RANGE;
                            parser.discard(7);
                        } else {
                            // add 1 each time a struct is found
                            newstate = analyze(field, state + 1);
                            if (newstate != state) {
                                throw new AssertionError();
                            }
                            state = newstate;
                            type = get(field);
                            if (type != Type.LIST) {
                                type = Type.STRUCT;
                            }
                        }
                    } else if ("yes".equals(token)
                            || "no".equals(token)) {
                        type = Type.BOOLEAN;
                    } else {
                        try {
                            // integer
                            Integer.parseInt(token);
                            type = Type.INTEGER;
                        } catch (NumberFormatException e1) {
                            // float
                            try {
                                Float.parseFloat(token);
                                type = Type.FLOAT;
                            } catch (NumberFormatException e2) {
                                if (token.startsWith("\"")
                                        && token.endsWith("\"")) {
                                    type = Type.STRING;
                                } else {
                                    type = Type.ENUM;
                                }
                            }
                        }
                    }
                }

                // field - type binding
                put(field, type);
            }
        }

        return state;
    }

    private boolean handleColorList(String token) {
        Patterns patterns;
        List<String> tokens;

        // detect color list
        switch (token) {
            case "rgb":
                patterns = Patterns.PS_COLOR_RGB;
                break;
            case "hsv":
                patterns = Patterns.PS_COLOR_HSV;
                break;
            default:
                patterns = null;
                break;
        }

        if (patterns != null) {
            // handle color list
            tokens = parser.peek(5);
            while (true) {
                //System.err.format("Tokens=%s%nPatterns=%s%nMatches=%b%n
                //tokens, patterns, patterns.matches(tokens));
                if (patterns.matches(tokens)) {
                    parser.discard(5);
                    token = parser.next();
                    switch (token) {
                        case "rgb":
                            // new RGB color element
                            tokens = parser.peek(5);
                            patterns = Patterns.PS_COLOR_RGB;
                            continue;
                        case "hsv":
                            // new HSV color element
                            tokens = parser.peek(5);
                            patterns = Patterns.PS_COLOR_HSV;
                            continue;
                        case "}":
                            // exit color list
                            break;
                        default:
                            throw new TokenException(token);
                    }
                    // exit color list
                    break;
                } else {
                    throw new TokenException(tokens);
                }
            }

            return true;
        }

        return false;
    }

    private Type handleColorToken(Patterns patterns) {
        List<String> tokens;
        Type type;
        tokens = parser.peek(5);
        if (patterns.matches(tokens)) {
            type = Type.COLOR;
            parser.discard(5);
        } else {
            throw new TokenException("Color token exception");
        }
        return type;
    }

    private Patterns checkColorToken(String token) {
        Patterns patterns;
        switch (token) {
            case "hsv":
                patterns = Patterns.PS_COLOR_HSV;
                break;
            case "rgb":
                patterns = Patterns.PS_COLOR_RGB;
                break;
            default:
                patterns = null;
                break;
        }
        return patterns;
    }

    @Override
    public Type put(Field field, Type type) {
        Type old;

        if (field == null) {
            return null;
        }
        old = super.put(field, type);
        /*
        if (old == null
                || old == type
                || old == Type.INTEGER
                && type == Type.RANGE) {
            return old;
        }
        // put the old value back
        super.put(field, old);
        throw new IllegalArgumentException();
         */
        return old;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }
        Debug.DEBUG = true;
        System.out.format("Parsing file \"%s\"...%n", args[1]);
        ScriptFile.newInstance(new java.io.File(args[0], args[1]));
    }
}
