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

import com.stellaris.DirectoryFilter;
import com.stellaris.ScriptParser;
import com.stellaris.ScriptFilter;
import com.stellaris.ScriptLexer;
import com.stellaris.Stellaris;
import com.stellaris.TokenException;
import com.stellaris.script.*;
import com.stellaris.test.Debug;
import com.stellaris.util.DigestStore;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import javax.script.Bindings;
import javax.script.ScriptContext;
import static javax.script.ScriptContext.ENGINE_SCOPE;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

/**
 *
 * @author donizyo
 */
public class ModLoader extends SimpleEngine {

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
        sb.append(separator);
        sb.append("mod");
        DEFAULT_STELLARIS_DIRECTORY = sb.toString();
    }

    private String name;
    private String supportedVersion;

    public ModLoader(File file) {
        String path;

        try {
            path = handleFile(file);
            handleDirectory(path);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleDirectory(String path) throws FileNotFoundException {
        File root, dir, file;
        DirectoryFilter df;
        ScriptFilter sf;
        Queue<File> dirs, files;
        String filename;
        ScriptContext engineContext;
        ScriptContext fileContext;
        Bindings bindings;
        SyntaxValidator validator;
        int scope;
        String msg;

        root = new File(DEFAULT_STELLARIS_DIRECTORY, path);
        if (!root.isDirectory()) {
            throw new FileNotFoundException();
        }
        df = new DirectoryFilter();
        root.listFiles(df);
        sf = new ScriptFilter(df.getDirs());
        dirs = sf.getDirs();
        validator = new SyntaxValidator();
        engineContext = getContext();
        scope = ENGINE_SCOPE;

        while (!dirs.isEmpty()) {
            dir = dirs.remove();
            dir.listFiles(sf);

            files = sf.getFiles();
            loop:
            while (!files.isEmpty()) {
                file = files.remove();
                filename = DigestStore.getPath(file);
                try (FileReader reader = new FileReader(file);) {
                    // create a isolated context for current script file
                    fileContext = new SimpleScriptContext();
                    // parse the file
                    ScriptParser.newInstance(reader, fileContext);
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
                } catch (IOException ex) {
                    throw new RuntimeException(filename, ex);
                } catch (SyntaxException ex) {
                    msg = ex.getMessage();
                    if (msg == null) {
                        msg = "";
                    }
                    Debug.err.format(
                            "[MOD]\tfile=\"%s\"%n"
                            + "\tname=\"%s\"%n"
                            + "\tsupported_version=\"%s\"%n"
                            + "\tSyntaxException: %s%n",
                            filename,
                            name,
                            supportedVersion,
                            msg
                    );
                }
            }
        }
    }

    private String handleFile(File file) throws IOException {
        ScriptLexer parser;
        String key;
        String token;
        String path;
        int idx;
        int len;

        parser = new ScriptLexer(file);
        path = null;
        while (parser.hasNextToken()) {
            key = parser.nextToken();

            parser.nextToken();
            switch (key) {
                case "name":
                    token = parser.nextToken();
                    len = token.length();
                    name = token.substring(1, len - 1);
                    break;
                case "tags":
                    token = parser.nextToken();
                    if ("{".equals(token)) {
                        do {
                            token = parser.nextToken();
                        } while (!"}".equals(token));
                        break;
                    }
                    throw new AssertionError(token);
                case "archieve":
                    throw new TokenException("Unexpected token: archieve");
                case "path":
                    token = parser.nextToken();
                    idx = token.indexOf('/');
                    len = token.length();
                    token = token.substring(idx + 1, len - 1);
                    path = token;
                    break;
                case "supported_version":
                    token = parser.nextToken();
                    len = token.length();
                    token = token.substring(1, len - 1);
                    supportedVersion = token;
                    break;
                default:
                    parser.nextToken();
                    break;
            }
        }

        if (path == null) {
            throw new AssertionError("Invalid descriptor file: path/archieve field not found!");
        }

        return path;
    }

    public static Queue<ModLoader> getModLoaders() {
        File dir;
        FileFilter filter;
        Queue<ModLoader> res;

        dir = new File(DEFAULT_STELLARIS_DIRECTORY);
        if (!dir.isDirectory()) {
            throw new AssertionError("Fail to locate stellaris mod directory!");
        }
        res = new LinkedList<>();
        filter = new DescriptorFilter(res);
        dir.listFiles(filter);

        return res;
    }

    public static void main(String[] args) {
        Queue<ModLoader> q;
        Stellaris main;

        Debug.DEBUG = false;
        main = new Stellaris();
        Stellaris.setDefault(main);
        q = getModLoaders();
        Debug.out.format("ModLoader count=%d%n", q.size());
    }
}
