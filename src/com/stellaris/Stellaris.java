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

import com.stellaris.util.DigestStore;
import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class Stellaris {

    private static final String SUFFIX_TXT = ".txt";
    private static final String[] BLACKLIST = {
        "common\\HOW_TO_MAKE_NEW_SHIPS.txt"
    };
    private final DigestStore digestStore;
    private final Map<Field, Type> fields;

    public Stellaris() {
        fields = new HashMap<>();
        digestStore = new DigestStore();
    }

    public void init(String path) {
        File root;
        DirectoryFilter df;
        ScriptFilter sf;
        Queue<File> files, dirs;
        File file, dir;
        ScriptFile script;
        String filename;

        root = new File(path);
        df = new DirectoryFilter();
        root.listFiles(df);
        sf = new ScriptFilter(df.getDirs());
        dirs = sf.getDirs();

        while (!dirs.isEmpty()) {
            dir = dirs.remove();
            dir.listFiles(sf);

            files = sf.getFiles();
            mainloop:
            while (!files.isEmpty()) {
                file = files.remove();
                if (digestStore.matches(file)) {
                    continue;
                }
                // refresh syntax table
                System.out.format("[REFRESH] %s%n", DigestStore.getPath(file));
                try {
                    script = ScriptFile.newInstance(file);
                } catch (IllegalStateException | TokenException | AssertionError ex) {
                    System.err.format("[ERROR] Found at file \"%s\"%n",
                            DigestStore.getPath(file));
                    continue;
                } catch (NoSuchElementException ex) {
                    filename = DigestStore.getPath(file);
                    for (String name : BLACKLIST) {
                        if (name.equals(filename)) {
                            continue mainloop;
                        }
                    }
                    throw new RuntimeException(String.format(
                            "A non-blacklisted file \"%s\" has serious error!",
                            filename),
                            ex);
                }
                fields.putAll(script);
            }
        }
    }

    private class ScriptFilter implements FileFilter {

        private final Queue<File> files;
        private final Queue<File> dirs;

        public ScriptFilter(Queue<File> init) {
            files = new LinkedList<>();
            dirs = new LinkedList<>();
            if (init != null) {
                dirs.addAll(init);
            }
        }

        public Queue<File> getFiles() {
            return files;
        }

        public Queue<File> getDirs() {
            return dirs;
        }

        @Override
        public boolean accept(File file) {
            String name;

            if (file.isDirectory()) {
                dirs.add(file);
            } else if (file.isFile()) {
                name = file.getName();
                if (name.endsWith(SUFFIX_TXT)) {
                    files.add(file);
                }
            }
            return false;
        }

    }

    private class DirectoryFilter implements FileFilter {

        private final Queue<File> dirs;

        public DirectoryFilter() {
            dirs = new LinkedList<>();
        }

        public Queue<File> getDirs() {
            return dirs;
        }

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
            return false;
        }
    }

    public static void main(String[] args) {
        String path;
        Stellaris st;

        if (args.length < 1) {
            return;
        }
        path = args[0];
        System.out.format("Checkout directory \"%s\"...%n", path);
        st = new Stellaris();
        st.init(path);
        /*
        for (Field field : st.fields.keySet()) {
            System.out.format("%s%n\t\t%s",
                    field.toString(),
                    st.fields.get(field).name().toLowerCase());
        }
        //*/
    }
}
