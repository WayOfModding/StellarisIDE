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
package com.stellaris.localisation;

import com.stellaris.test.Debug;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author donizyo
 */
public class LocalisationMap {

    private final Map<String, SortedMap<Integer, Entry<File, String>>> map;

    public LocalisationMap(Language language) {
        map = new HashMap<>();
    }

    public void put(File file, CharBuffer key, CharBuffer digit, CharBuffer value) {
        put(file,
                key.toString(),
                Integer.parseInt(
                        digit.toString()
                ),
                value.toString()
        );
    }

    public void put(File file, String key, int priority, String value) {
        SortedMap<Integer, Entry<File, String>> m;
        Entry<File, String> e;

        m = get(key);
        if (m == null) {
            m = new TreeMap<>();
            map.put(key, m);
        }
        e = m.get(priority);
        if (e == null) {
            e = new Pair(file, value);
            m.put(priority, e);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public SortedMap<Integer, Entry<File, String>> get(String key) {
        return map.get(key);
    }

    private static class Pair implements Entry<File, String> {

        private final File file;
        private final String str;

        private Pair(File file, String str) {
            this.file = file;
            this.str = str;
        }

        @Override
        public File getKey() {
            return file;
        }

        @Override
        public String getValue() {
            return str;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }

    }

    public void list(PrintStream out) {
        Set<String> keySet;
        SortedMap<Integer, Entry<File, String>> m;
        Integer firstKey;
        Entry<File, String> e;
        File file;
        String value;

        if (out == null) {
            out = Debug.out;
        }
        keySet = map.keySet();
        for (String key : keySet) {
            m = get(key);
            if (m == null) {
                throw new AssertionError("Null tree map");
            }
            if (m.isEmpty()) {
                throw new AssertionError("Empty tree map");
            }
            firstKey = m.firstKey();
            if (firstKey == null) {
                throw new AssertionError("Null integer first key");
            }
            e = m.get(firstKey);
            if (e == null) {
                throw new AssertionError("Null pair");
            }
            file = e.getKey();
            value = e.getValue();
            out.format("%s: \"%s\" @ %s%n", key, value, file.getName());
        }
    }

    public static void main(String[] args) {
        String root, path;
        File file;
        LangFilter langFilter;
        Queue<File> queue;
        LangFileReader reader;
        LocalisationMap map;

        if (args.length < 2) {
            return;
        }
        root = args[0];
        path = "localisation";
        file = new File(root, path);
        try {
            langFilter = LangFilterFactory.LANG_ENGLISH;
            file.listFiles(langFilter);
            queue = langFilter.getFiles();
            map = new LocalisationMap(langFilter);
            while (!queue.isEmpty()) {
                file = queue.remove();
                reader = new LangFileReader(langFilter, file);
                reader.loadInto(map);
            }
            //map.list(Debug.out);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
