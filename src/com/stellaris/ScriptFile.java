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

import com.stellaris.script.ScriptColor;
import com.stellaris.script.ScriptHSVColor;
import com.stellaris.script.ScriptList;
import com.stellaris.script.ScriptRGBColor;
import com.stellaris.script.ScriptValue;
import com.stellaris.test.Debug;
import static com.stellaris.test.Debug.DEBUG;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.script.*;

/**
 * Generates AST (a.k.a abstract syntax tree) for Stellaris Script
 *
 * @author donizyo
 */
public class ScriptFile extends FieldTypeBinding {

    private ScriptParser parser;
    private boolean isCore;
    private ScriptValue cache;

    public static ScriptFile newInstance(File file) {
        return newInstance(file, null);
    }

    public static ScriptFile newInstance(File file, ScriptContext context) {
        try {
            return new ScriptFile(file, context);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ScriptFile newInstance(Reader reader, ScriptContext context) {
        return new ScriptFile(reader, context);
    }

    private static boolean isCoreFile(File file) {
        Stellaris main;
        File root;

        main = Stellaris.getDefault();
        root = main.getRootDirectory();
        return isCoreFile(file, root);
    }

    private static boolean isCoreFile(File file, File root) {
        String proot, pfile;

        proot = root.getPath();
        pfile = file.getPath();
        return pfile.startsWith(proot);
    }

    private ScriptFile(ScriptParser parser, boolean isCoreFile, ScriptContext context) {
        this.parser = parser;
        this.isCore = isCoreFile;
        analyze(context);
    }

    private ScriptFile(File file, ScriptContext context) throws IOException {
        this(new ScriptParser(file), isCoreFile(file), context);
    }

    private ScriptFile(Reader reader, ScriptContext context) {
        this(new ScriptParser(reader), false, context);
    }

    private void analyze(ScriptContext context) {
        int res;

        try {
            res = analyze(null, 0, context);
            if (res != 0) {
                throw new AssertionError(res);
            }
        } finally {
            parser.close();
            parser = null;
        }
    }

    private int analyze(Field parent, int state, ScriptContext context) {
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
                    // TODO do something to map field and value
                    // @see: 'cache'
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
                    if (handlePlainList(token)) {
                        type = Type.LIST;
                        put(parent, type);
                        return --state;
                    } else {
                        throw new AssertionError();
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
                            newstate = analyze(field, state + 1, context);
                            if (newstate != state) {
                                throw new AssertionError(String.format("old_state=%d, new_state=%d", state, newstate));
                            }
                            state = newstate;
                            type = get(field);
                            if (type != Type.LIST) {
                                type = Type.STRUCT;
                            } else {
                                // skip binding procedure
                                type = null;
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
                                    type = Type.VARIABLE;
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

    private Bindings getBindings(ScriptContext context) {
        if (isCore) {
            return context.getBindings(ScriptContext.GLOBAL_SCOPE);
        } else {
            return context.getBindings(ScriptContext.ENGINE_SCOPE);
        }
    }

    private boolean handleColorList(String token) {
        final int len = 5;
        boolean isRGB;
        Patterns patterns;
        List<String> tokens;
        List<String> output;
        String[] data;
        int r, g, b;
        float h, s, v;
        ScriptColor color;
        ScriptList<ScriptColor> colorList;

        // detect color list
        switch (token) {
            case "rgb":
                patterns = Patterns.PS_COLOR_RGB;
                isRGB = true;
                break;
            case "hsv":
                patterns = Patterns.PS_COLOR_HSV;
                isRGB = false;
                break;
            default:
                return false;
        }

        colorList = new ScriptList<>();
        // handle color list
        while (true) {
            tokens = parser.peek(len);
            //System.err.format("Tokens=%s%nPatterns=%s%nMatches=%b%n
            //tokens, patterns, patterns.matches(tokens));
            data = new String[len];
            output = new ArrayList<>(len);
            if (patterns.matches(tokens, output)) {
                output.toArray(data);
                if (isRGB) {
                    r = Integer.parseInt(data[1]);
                    g = Integer.parseInt(data[2]);
                    b = Integer.parseInt(data[3]);
                    color = new ScriptRGBColor(r, g, b);
                } else {
                    h = Float.parseFloat(data[1]);
                    s = Float.parseFloat(data[2]);
                    v = Float.parseFloat(data[3]);
                    color = new ScriptHSVColor(h, s, v);
                }
                colorList.add(color);
                // prevent loitering
                data = null;
                output = null;
                parser.discard(len);

                token = parser.next();
                switch (token) {
                    case "rgb":
                        // new RGB color element
                        patterns = Patterns.PS_COLOR_RGB;
                        isRGB = true;
                        continue;
                    case "hsv":
                        // new HSV color element
                        patterns = Patterns.PS_COLOR_HSV;
                        isRGB = false;
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

        cache = colorList;
        return true;
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

    private boolean handlePlainList(String token) {
        // handle single-element list
        if ("}".equals(token)) {
            return true;
        }

        // handle multiple-element list
        while (true) {
            token = parser.next();
            if ("}".equals(token)) {
                return true;
            }
            if ("{".equals(token)
                    || "yes".equals(token)
                    || "no".equals(token)) {
                throw new TokenException(token);
            }
        }
    }

    public static void main(String[] args) {
        String sparent, sname;
        File dir;
        File file;

        if (args.length < 2) {
            return;
        }
        Debug.DEBUG = true;
        sparent = args[0];
        sname = args[1];
        dir = new File(sparent);
        file = new File(dir, sname);
        System.out.format("Parsing file \"%s\"...%n", sname);
        ScriptFile.newInstance(file, null);
    }
}
