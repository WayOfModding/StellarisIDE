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
import com.stellaris.ScriptFile;
import com.stellaris.ScriptFilter;
import com.stellaris.ScriptParser;
import com.stellaris.TokenException;
import com.stellaris.test.Debug;
import com.stellaris.util.DigestStore;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class ModLoader {

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

    public ModLoader(File file) {
        String path;

        try (FileReader reader = new FileReader(file);) {
            path = handleFile(reader);
            System.out.format("\tname=\"%s\"%n\tpath=\"%s\"%n", name, path);
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
        ScriptFile script;

        root = new File(DEFAULT_STELLARIS_DIRECTORY, path);
        if (!root.isDirectory()) {
            throw new FileNotFoundException();
        }
        df = new DirectoryFilter();
        root.listFiles(df);
        sf = new ScriptFilter(df.getDirs());
        dirs = sf.getDirs();

        while (!dirs.isEmpty()) {
            dir = dirs.remove();
            dir.listFiles(sf);

            files = sf.getFiles();
            loop:
            while (!files.isEmpty()) {
                file = files.remove();
                filename = DigestStore.getPath(file);
                try {
                    script = ScriptFile.newInstance(file);
                    validateScript(script);
                } catch (IllegalStateException | TokenException | AssertionError | BufferUnderflowException | BufferOverflowException ex) {
                    System.err.format("[ERROR] Found at file \"%s\"%n", filename);
                } catch (NoSuchElementException ex) {
                    throw new RuntimeException(
                            String.format(
                                    "A non-blacklisted file \"%s\" has serious error!",
                                    filename),
                            ex
                    );
                }
            }
        }
    }

    private String handleFile(Reader reader) {
        ScriptParser parser;
        String key;
        String token;
        int idx;
        int len;

        parser = new ScriptParser(reader);
        while (parser.hasNext()) {
            key = parser.next();

            parser.next();
            switch (key) {
                case "name":
                    token = parser.next();
                    len = token.length();
                    name = token.substring(1, len - 1);
                    break;
                case "tags":
                    token = parser.next();
                    if ("{".equals(token)) {
                        do {
                            token = parser.next();
                        } while (!"}".equals(token));
                    }
                    break;
                case "archieve":
                    throw new TokenException("Unexpected token: archieve");
                case "path":
                    token = parser.next();
                    idx = token.indexOf('/');
                    len = token.length();
                    token = token.substring(idx + 1, len - 1);
                    return token;
                default:
                    parser.next();
                    break;
            }
        }
        throw new AssertionError("Invalid descriptor file: path/archieve field not found!");
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

    private void validateScript(ScriptFile script) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void main(String[] args) {
        Queue<ModLoader> q;

        Debug.DEBUG = false;
        q = getModLoaders();
        System.out.format("ModLoader count=%d%n", q.size());
    }
}
