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

import com.stellaris.script.ScriptBoolean;
import com.stellaris.script.ScriptColor;
import com.stellaris.script.ScriptColorList;
import com.stellaris.script.ScriptFloat;
import com.stellaris.script.ScriptHSVColor;
import com.stellaris.script.ScriptInteger;
import com.stellaris.script.ScriptList;
import com.stellaris.script.ScriptNull;
import com.stellaris.script.ScriptRGBColor;
import com.stellaris.script.ScriptRange;
import com.stellaris.script.ScriptReference;
import com.stellaris.script.ScriptString;
import com.stellaris.script.ScriptStruct;
import com.stellaris.script.ScriptValue;
import com.stellaris.test.Debug;
import static com.stellaris.test.Debug.DEBUG;
import static com.stellaris.test.Debug.SKIP_LINE;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.*;

/**
 * Generates AST (a.k.a abstract syntax tree) for Stellaris Script
 *
 * @author donizyo
 */
public class ScriptFile extends ScriptValue {

    private ScriptParser scriptParser;
    private boolean isCore;
    private ScriptContext context;

    public static ScriptFile newInstance(File file, ScriptContext context) {
        try {
            return new ScriptFile(file, context);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static ScriptFile newInstance(Reader reader, ScriptContext context) throws IOException {
        return new ScriptFile(reader, context);
    }

    private static boolean isCoreFile(File file) {
        Stellaris main;
        File root;

        main = Stellaris.getDefault();
        if (main == null) {
            Debug.err.format("[WARN]\tStellaris instance is null!%n");
            return true;
        }
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
        this.scriptParser = parser;
        this.isCore = isCoreFile;
        this.context = context;
        analyze();
    }

    private ScriptFile(File file, ScriptContext context) throws IOException {
        this(new ScriptParser(file), isCoreFile(file), context);
    }

    private ScriptFile(Reader reader, ScriptContext context) throws IOException {
        this(new ScriptParser(reader), false, context);
    }

    private void analyze() {
        int res;

        try {
            res = analyze(null, 0, 0);
            if (res != 0) {
                throw new TokenException(
                        String.format(
                                "Invalid parsing state: %d\t(expecting: 0)",
                                res
                        )
                );
            }
        } catch (IOException ex) {
            Logger.getLogger(ScriptFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                scriptParser.close();
            } catch (IOException ex) {
                Logger.getLogger(ScriptFile.class.getName()).log(Level.SEVERE, null, ex);
            }
            scriptParser = null;
        }
    }

    public void put(Field field, ScriptValue value) {
        Bindings bindings;
        Field parent;
        String fieldName;
        Object obj;
        ScriptValue old;

        bindings = getBindings(context);
        if (bindings == null) {
            throw new NullPointerException();
        }
        if (field == null) {
            throw new UnsupportedOperationException("Accessing ScriptFile as ScriptValue");
        }
        parent = field.getParent();
        fieldName = field.getName();
        if (parent != null) {
            bindings = (ScriptStruct) get(parent);
        }
        if (value == null) {
            value = new ScriptNull();
        }
        obj = bindings.get(fieldName);
        if (obj != null && obj instanceof ScriptValue) {
            old = (ScriptValue) obj;
            value.updateTypeInfo(old);
        }
        bindings.put(fieldName, value);
    }

    public ScriptValue get(Field field) {
        Stack<String> stack;
        Field parent;
        String name;
        Bindings bindings;
        Object obj;
        ScriptValue value;

        if (field == null) {
            throw new NullPointerException();
        }
        stack = new Stack<>();
        parent = field;
        while (parent != null) {
            name = parent.getName();
            parent = parent.getParent();
            stack.push(name);
        }
        // root node
        try {
            name = stack.pop();
        } catch (EmptyStackException ex) {
            throw new AssertionError(ex);
        }
        bindings = getBindings(context);
        if (bindings == null) {
            throw new NullPointerException();
        }
        obj = bindings.get(name);
        if (obj == null) {
            throw new NullPointerException();
        }
        if (!(obj instanceof ScriptValue)) {
            throw new AssertionError(obj.getClass());
        }
        value = (ScriptValue) obj;
        // leaf node
        while (!stack.isEmpty()) {
            name = stack.pop();
            try {
                bindings = (ScriptStruct) value;

                obj = bindings.get(name);
                if (obj == null) {
                    if (stack.isEmpty()) {
                        return null;
                    } else {
                        throw new NullPointerException();
                    }
                }
                value = (ScriptValue) obj;
            } catch (NullPointerException ex) {
                throw new AssertionError(ex);
            } catch (ClassCastException ex) {
                throw new AssertionError(value.getClass().toString(), ex);
            }
        }

        return value;
    }

    // remember to skip the current line when TokenException is thrown
    private int analyze(Field parent, int state, int index) throws IOException {
        ScriptParser parser;
        Token token, key;
        String sToken, sKey;
        List<Token> tokens;
        List<String> output;
        Iterator<String> itr;
        Field field;
        //Type type;
        boolean isRange;
        boolean isList;
        Patterns patterns;
        int newstate;
        int min, max;
        ScriptList<ScriptValue> scriptList;
        ScriptColor scriptColor;

        if (DEBUG) {
            Debug.err.format("[PARSE]\tparent=%s, state=%d, index=%d%n",
                    parent, state, index
            );
        }
        index = 0;
        parser = scriptParser;
        while (parser.hasNextToken()) {
            try {
                token = parser.nextToken();
                sToken = token.getValue();
            } catch (TokenException ex) {
                if (SKIP_LINE) {
                    Logger.getLogger(Stellaris.class.getName()).log(
                            Level.SEVERE, parser.skipCurrentLine(), ex
                    );
                    continue;
                }
                throw ex;
            }
            // ignore comment token
            if (sToken.charAt(0) == '#') {
                continue;
            }
            // return
            if ("}".equals(sToken)) {
                //put(parent, cache); cache = null;
                return --state;
            }
            try {
                if (handleColorList(parent, token)) {
                    //type = Type.COLORLIST;
                    return --state;
                } else {
                    key = token;
                    sKey = key.getValue();
                }
            } catch (TokenException | NumberFormatException ex) {
                if (SKIP_LINE) {
                    Logger.getLogger(Stellaris.class.getName()).log(
                            Level.SEVERE, parser.skipCurrentLine(), ex
                    );
                    continue;
                }
                throw ex;
            }

            // operator
            // or list?
            try {
                token = parser.nextToken();
                sToken = token.getValue();
            } catch (TokenException ex) {
                if (SKIP_LINE) {
                    Logger.getLogger(Stellaris.class.getName()).log(
                            Level.SEVERE, parser.skipCurrentLine(), ex
                    );
                    continue;
                }
                throw ex;
            }
            isList = !"=".equals(sToken)
                    && !">".equals(sToken)
                    && !"<".equals(sToken);
            // update for Stellaris v1.2
            if (checkColorToken(token) != null) {
                try {
                    throw new TokenException("Unexpected color token");
                } catch (TokenException ex) {
                    if (SKIP_LINE) {
                        Logger.getLogger(Stellaris.class.getName()).log(
                                Level.SEVERE, parser.skipCurrentLine(), ex
                        );
                        continue;
                    }
                    throw ex;
                }
            }

            if (isList) {
                // list entries: key, token, ...
                scriptList = new ScriptList<>(
                        ScriptValue.parseString(sKey)
                );
                try {
                    isList = handlePlainList(scriptList, token);
                } catch (TokenException ex) {
                    if (SKIP_LINE) {
                        Logger.getLogger(Stellaris.class.getName()).log(
                                Level.SEVERE, parser.skipCurrentLine(), ex
                        );
                        continue;
                    }
                    throw ex;
                }
                if (isList) {
                    //type = Type.LIST;
                    //put(parent, type);
                    put(parent, scriptList);
                    scriptList = null;
                    return --state;
                } else {
                    throw new AssertionError();
                }
            } else {
                field = new Field(parent, sKey);
                if (Debug.DEBUG_FIELD) {
                    Debug.err.format("[FIELD]\tparent=%s, key=%s, index=%d%n",
                            parent, key, index);
                }
                ++index;
                // value
                try {
                    token = parser.nextToken();
                    sToken = token.getValue();
                } catch (TokenException ex) {
                    if (SKIP_LINE) {
                        Logger.getLogger(Stellaris.class.getName()).log(
                                Level.SEVERE, parser.skipCurrentLine(), ex
                        );
                        continue;
                    }
                    throw ex;
                }
                patterns = checkColorToken(token);
                if (patterns != null) {
                    try {
                        scriptColor = handleColorToken(patterns);
                    } catch (TokenException | NumberFormatException ex) {
                        if (SKIP_LINE) {
                            Logger.getLogger(Stellaris.class.getName()).log(
                                    Level.SEVERE, parser.skipCurrentLine(), ex
                            );
                            continue;
                        }
                        throw ex;
                    }
                    put(field, scriptColor);
                    scriptColor = null;
                } else if ("{".equals(sToken)) {
                    try {
                        tokens = parser.peekToken(7);
                    } catch (TokenException ex) {
                        if (SKIP_LINE) {
                            Logger.getLogger(Stellaris.class.getName()).log(
                                    Level.SEVERE, parser.skipCurrentLine(), ex
                            );
                            continue;
                        }
                        throw ex;
                    }
                    output = new ArrayList<>(2);
                    // { -> min = INTEGER max = INTEGER }
                    patterns = Patterns.PS_RANGE;
                    isRange = patterns.matches(tokens, output);
                    if (isRange) {
                        itr = output.iterator();

                        sToken = itr.next();
                        min = Integer.parseInt(sToken);
                        sToken = itr.next();
                        max = Integer.parseInt(sToken);

                        put(field, new ScriptRange(min, max));
                        parser.discardToken(7);
                    } else {
                        // add 1 each time a struct is found
                        put(field, new ScriptStruct());
                        newstate = analyze(field, state + 1, index - 1);
                        if (newstate != state) {
                            throw new TokenException(
                                    String.format(
                                            "Invalid parsing state: %d\t(expecting: %d)",
                                            newstate, state
                                    )
                            );
                        }
                        state = newstate;
                    }
                } else if ("yes".equals(sToken)) {
                    put(field, new ScriptBoolean(true));
                } else if ("no".equals(sToken)) {
                    //type = Type.BOOLEAN;
                    put(field, new ScriptBoolean(false));
                } else {
                    try {
                        // integer
                        put(field, new ScriptInteger(Integer.parseInt(sToken)));
                        //type = Type.INTEGER;
                    } catch (NumberFormatException e1) {
                        // float
                        try {
                            put(field, new ScriptFloat(Float.parseFloat(sToken)));
                        } catch (NumberFormatException e2) {
                            if (sToken.startsWith("\"")
                                    && sToken.endsWith("\"")) {
                                put(field, new ScriptString(sToken));
                            } else {
                                put(field, new ScriptReference(sToken));
                            }
                        }
                    }
                }
            }
        }

        return state;
    }

    private Bindings getBindings(ScriptContext context) {
        Bindings bindings;

        if (context == null) {
            throw new NullPointerException();
        }
        if (isCore) {
            bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        } else {
            bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
            if (bindings == null) {
                context.getBindings(ScriptContext.GLOBAL_SCOPE);
            }
        }

        return bindings;
    }

    private boolean handleColorList(Field parent, Token token) throws IOException {
        Patterns patterns;
        ScriptColor color;
        ScriptList<ScriptColor> colorList;
        ScriptParser parser;
        String sToken;

        // detect color list
        patterns = checkColorToken(token);
        if (patterns == null) {
            return false;
        }

        colorList = new ScriptColorList();
        parser = scriptParser;
        // handle color list
        while (true) {
            color = handleColorToken(patterns);
            colorList.add(color);

            token = parser.nextToken();
            patterns = checkColorToken(token);
            if (patterns != null) {
                continue;
            }
            sToken = token.getValue();
            switch (sToken) {
                case "}":
                    // exit color list
                    break;
                default:
                    throw new TokenException(sToken);
            }
            // exit color list
            break;
        }

        put(parent, colorList);
        return true;
    }

    // rgb -> { INT INT INT }
    // rgb -> { INT INT INT INT }
    private ScriptColor handleColorToken(Patterns patterns)
            throws IOException, TokenException {
        ScriptParser parser;
        int len;
        List<Token> tokens;
        String[] data;
        List<String> output;
        ScriptColor color;
        String sa;
        int r, g, b, a0;
        float h, s, v, a1;
        boolean isColor;

        if (patterns == null) {
            throw new NullPointerException();
        }
        parser = scriptParser;
        len = 6;
        tokens = parser.peekToken(len);
        output = new ArrayList<>(len);
        isColor = patterns.matches(tokens, output);
        if (isColor) {
            len = output.size();
            parser.discardToken(len + 2);
            data = new String[len];

            output.toArray(data);
            if (patterns == Patterns.PS_COLOR_RGB) {
                r = Integer.parseInt(data[0]);
                g = Integer.parseInt(data[1]);
                b = Integer.parseInt(data[2]);
                try {
                    sa = data[3];
                    a0 = Integer.parseInt(sa);
                    color = new ScriptRGBColor(r, g, b, a0);
                } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
                    color = new ScriptRGBColor(r, g, b);
                }
            } else if (patterns == Patterns.PS_COLOR_HSV) {
                h = Float.parseFloat(data[0]);
                s = Float.parseFloat(data[1]);
                v = Float.parseFloat(data[2]);
                try {
                    sa = data[3];
                    a1 = Float.parseFloat(sa);
                    color = new ScriptHSVColor(h, s, v, a1);
                } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
                    color = new ScriptHSVColor(h, s, v);
                }
            } else {
                throw new AssertionError(patterns.getClass());
            }
        } else {
            throw new TokenException("Color token exception");
        }
        return color;
    }

    private Patterns checkColorToken(Token token) {
        Patterns patterns;
        String sToken;

        sToken = token.getValue();
        switch (sToken) {
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

    private boolean handlePlainList(ScriptList list, Token token) throws IOException {
        ScriptParser parser;
        String sToken;

        sToken = token.getValue();
        // handle single-element list
        if ("}".equals(sToken)) {
            return true;
        }

        parser = scriptParser;
        list.add(ScriptValue.parseString(sToken));
        // handle multiple-element list
        while (true) {
            token = parser.nextToken();
            sToken = token.getValue();
            if ("}".equals(sToken)) {
                return true;
            }
            if ("{".equals(sToken)
                    || "yes".equals(sToken)
                    || "no".equals(sToken)) {
                throw new TokenException(sToken);
            }
            list.add(ScriptValue.parseString(sToken));
        }
    }

    public static void main(String[] args) {
        String sparent, sname;
        File dir;
        File file;
        File log;
        Stellaris st;
        ScriptEngine engine;
        ScriptContext context;

        if (args.length < 2) {
            return;
        }
        sparent = args[0];
        sname = args[1];
        {
            //st = new Stellaris();
            //Stellaris.setDefault(st);
            //st.init(sparent, true);
            //engine = st.getScriptEngine();
            //context = engine.getContext();
            context = new SimpleScriptContext();
            context.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
        }
        dir = new File(sparent);
        file = new File(dir, sname);
        log = new File("log", sname);
        log.getParentFile().mkdirs();
        try (PrintStream logStream = new PrintStream(new FileOutputStream(log));) {
            Debug.out = Debug.err = logStream;
            Debug.DEBUG = true;
            Debug.DEBUG_NEXT = true;
            Debug.DEBUG_DISCARD = true;
            System.out.format("Parsing file \"%s\"...%n", sname);
            Debug.out.format("Parsing file \"%s\"...%n", sname);
            ScriptFile.newInstance(file, context);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ScriptFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
