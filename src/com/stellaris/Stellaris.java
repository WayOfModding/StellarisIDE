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
import com.stellaris.util.DigestStore;
import java.io.File;
import java.io.FileFilter;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class Stellaris {

    private static final String[] BLACKLIST_ALL = {
        "common\\HOW_TO_MAKE_NEW_SHIPS.txt",
        "interface\\credits.txt",
        "interface\\reference.txt",
        "previewer_assets\\previewer_filefilter.txt",
        "pdx_launcher\\game\\motd.txt"
    };
    // skip when syntax analysis is ongoing
    private static final String[] BLACKLIST_SYN = {
        "common\\component_tags\\00_tags.txt"
    };
    private final DigestStore digestStore;
    private final FieldTypeBinding fields;

    public Stellaris() {
        fields = new FieldTypeBinding();
        digestStore = new DigestStore();
    }

    public void init(String path, boolean forceUpdate) {
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
                filename = DigestStore.getPath(file);
                for (String name : BLACKLIST_ALL) {
                    if (name.equals(filename)) {
                        continue mainloop;
                    }
                }
                for (String name : BLACKLIST_SYN) {
                    if (name.equals(filename)) {
                        continue mainloop;
                    }
                }
                if (!forceUpdate && digestStore.matches(file)) {
                    continue;
                }
                // refresh syntax table
                if (Debug.DEBUG && Debug.DEBUG_REFRESH) {
                    System.out.format("[REFRESH] %s%n", DigestStore.getPath(file));
                }
                try {
                    script = ScriptFile.newInstance(file);
                } catch (IllegalStateException | TokenException | AssertionError | BufferUnderflowException | BufferOverflowException ex) {
                    System.err.format("[ERROR] Found at file \"%s\"%n",
                            DigestStore.getPath(file));
                    continue;
                } catch (NoSuchElementException ex) {
                    throw new RuntimeException(
                            String.format(
                                    "A non-blacklisted file \"%s\" has serious error!",
                                    filename),
                            ex
                    );
                }
                fields.putAll(script);
            }
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
        st = null;
        try {
            st = new Stellaris();
            st.init(path, true);
        } finally {
            if (st != null) {
                st.digestStore.store();
                FieldTypeBinding.store(st.fields);
            }
        }
    }
}
