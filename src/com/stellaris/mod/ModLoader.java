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
import com.stellaris.test.Debug;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class ModLoader {

    private static final String SUFFIX_MOD = ".mod";
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
    private String path;

    public ModLoader(File file) {
        try (FileReader reader = new FileReader(file);) {
            path = handleFile(reader);
            System.out.format("\tname=\"%s\"%n\tpath=\"%s\"%n", name, path);
            handleDirectory();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleDirectory() throws FileNotFoundException {
        File file;

        file = new File(DEFAULT_STELLARIS_DIRECTORY, path);
        if (!file.isDirectory()) {
            throw new FileNotFoundException();
        }
        System.out.format(">>>\tDirectory \"%s\" found, handling...%n", path);
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
                    return null;
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

    private static class DescriptorFilter implements FileFilter {

        private final Queue<ModLoader> queue;

        public DescriptorFilter(Queue<ModLoader> q) {
            queue = q;
        }

        @Override
        public boolean accept(File file) {
            String filename;
            int idx;
            String prefix;
            String suffix;
            ModLoader loader;

            if (!file.isFile()) {
                return false;
            }
            filename = file.getName();
            idx = filename.lastIndexOf('.');
            if (idx == -1) {
                throw new AssertionError(idx);
            }
            suffix = filename.substring(idx);
            if (!SUFFIX_MOD.equals(suffix)) {
                return false;
            }
            prefix = filename.substring(0, idx);
            try {
                Integer.parseInt(prefix);
                return false;
            } catch (NumberFormatException ex) {
            }

            System.out.format("[MOD]\tfile=\"%s\"%n", filename);
            loader = new ModLoader(file);
            queue.add(loader);

            return false;
        }
    }

    public static void main(String[] args) {
        Queue<ModLoader> q;

        Debug.DEBUG = false;
        q = getModLoaders();
        System.out.format("ModLoader count=%d%n", q.size());
    }
}
