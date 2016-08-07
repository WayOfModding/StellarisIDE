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

import com.stellaris.ScriptParser;
import com.stellaris.ScriptLexer;
import com.stellaris.Stellaris;
import com.stellaris.Token;
import com.stellaris.TokenException;
import com.stellaris.script.*;
import com.stellaris.test.Debug;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import static javax.script.ScriptContext.ENGINE_SCOPE;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

/**
 *
 * @author donizyo
 */
public abstract class ModLoader extends SimpleEngine {

    private static final String DEFAULT_STELLARIS_DIRECTORY;

    static {
        String separator;
        StringBuilder sb;

        separator = System.getProperty("file.separator");
        sb = new StringBuilder();
        sb.append(System.getProperty("user.home"));
        sb.append(separator);
        sb.append("Documents");
        sb.append(separator);
        sb.append("Paradox Interactive");
        sb.append(separator);
        sb.append("Stellaris");
        // <user.home>/Documents/Paradox Interactive/Stellaris
        DEFAULT_STELLARIS_DIRECTORY = sb.toString();
    }

    protected final String pathHome;
    protected String path;
    protected String name;
    protected String supportedVersion;

    public ModLoader(String home, File file) {
        pathHome = home;
        try {
            path = handleFile(file);
        } catch (ZipException ex) {
            Logger.getLogger(ModLoader.class.getName()).log(Level.SEVERE, path, ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getDefaultPathHome() {
        return DEFAULT_STELLARIS_DIRECTORY;
    }

    public abstract void handleMod() throws IOException;

    protected void handleReader(String filename, Reader reader) throws IOException {
        ScriptContext engineContext;
        ScriptContext fileContext;
        Bindings bindings;
        SyntaxValidator validator;
        int scope;

        validator = new SyntaxValidator();
        engineContext = getContext();
        scope = ENGINE_SCOPE;
        // create a isolated context for current script file
        fileContext = new SimpleScriptContext();
        // routine: set FILENAME
        //fileContext.setAttribute(ScriptEngine.FILENAME, filename, scope);
        // parse the file
        try {
            ScriptParser.newInstance(reader, filename, fileContext);
        } catch (RuntimeException ex) {
            throw new ModException(filename, ex);
        }
        // retrieve field-type binding
        bindings = fileContext.getBindings(scope);
        // validate field-type binding
        validator.validate(bindings);
        // if it is accepted,
        // put all bindings of the file
        // into the engine context.
        // it will be used later to check
        // compatibility between mods
        engineContext.getBindings(scope).putAll(bindings);
    }

    /**
     * Parse descriptor.mod
     * @param file
     * @return
     * @throws IOException 
     */
    private String handleFile(File file) throws IOException {
        ScriptLexer parser;
        Token key;
        String sKey;
        Token token;
        String sToken;
        String _path;
        int len;

        parser = new ScriptLexer(file);
        _path = null;
        while (parser.hasNextToken()) {
            key = parser.nextToken();

            parser.nextToken();
            sKey = key.token;
            switch (sKey) {
                case "name":
                    token = parser.nextToken();
                    sToken = token.token;
                    len = sToken.length();
                    name = sToken.substring(1, len - 1);
                    break;
                case "tags":
                    token = parser.nextToken();
                    sToken = token.token;
                    if ("{".equals(sToken)) {
                        do {
                            token = parser.nextToken();
                            sToken = token.token;
                        } while (!"}".equals(sToken));
                        break;
                    }
                    throw new AssertionError(sToken);
                case "archive":
                case "path":
                    token = parser.nextToken();
                    sToken = token.token;
                    len = sToken.length();
                    sToken = sToken.substring(1, len - 1);
                    _path = sToken;
                    break;
                case "supported_version":
                    token = parser.nextToken();
                    sToken = token.token;
                    len = sToken.length();
                    sToken = sToken.substring(1, len - 1);
                    supportedVersion = sToken;
                    break;
                default:
                    parser.nextToken();
                    break;
            }
        }

        if (_path == null) {
            throw new AssertionError("Invalid descriptor file: path/archive field not found!");
        }

        return _path;
    }

    public static void getModLoaders(String pathHome,
            Queue<ModLoader> q,
            Queue<ModLoader> p) {
        File dir;
        FileFilter filter;

        if (pathHome == null) {
            pathHome = DEFAULT_STELLARIS_DIRECTORY;
        }
        dir = new File(pathHome, "mod");
        if (!dir.isDirectory()) {
            throw new AssertionError("Fail to locate stellaris mod directory!");
        }
        filter = new DescriptorFilter(pathHome, q, p);
        dir.listFiles(filter);
    }

    public static Queue<ModLoader> getModLoaders() {
        Queue<ModLoader> q;

        q = new LinkedList<>();
        getModLoaders(null, q, q);
        return q;
    }

    public String toString() {
        return name;
    }

    public static void main(String[] args) {
        String path;
        Queue<ModLoader> q;
        Stellaris main;

        if (args.length < 1) {
            return;
        }
        path = args[0];
        //Debug.DEBUG = true;
        main = new Stellaris();
        Stellaris.setDefault(main);
        main.init(path);
        main.scan(true);
        q = getModLoaders();
        Debug.err.flush();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
        }
        Debug.out.format("ModLoader count=%d%n", q.size());
        for (ModLoader ml : q) {
            Debug.out.format("%s - %s%n",
                    ml.name, ml.supportedVersion
            );
        }
    }
}
