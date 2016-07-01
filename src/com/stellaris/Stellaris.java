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

import com.stellaris.mod.ModLoader;
import com.stellaris.script.SimpleEngine;
import com.stellaris.script.SimpleFactory;
import com.stellaris.test.Debug;
import com.stellaris.util.DigestStore;
import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.*;

/**
 *
 * @author donizyo
 */
public class Stellaris extends SimpleFactory {

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

    private static Stellaris stellaris;
    private final DigestStore digestStore;
    private final ScriptEngine scriptEngine;
    private File dirRoot;

    public Stellaris() {
        digestStore = new DigestStore();
        scriptEngine = super.getScriptEngine();
    }

    public File getRootDirectory() {
        return dirRoot;
    }

    public static void setDefault(Stellaris val) {
        stellaris = val;
    }

    public static Stellaris getDefault() {
        return stellaris;
    }

    public void init(String path, boolean forceUpdate) {
        DirectoryFilter df;
        ScriptFilter sf;
        Queue<File> files, dirs;
        File file, dir;
        ScriptParser script;
        String filename;

        dirRoot = new File(path);
        if (!dirRoot.isDirectory()) {
            throw new RuntimeException();
        }
        df = new DirectoryFilter();
        dirRoot.listFiles(df);
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
                    Debug.out.format("[REFRESH] %s%n", DigestStore.getPath(file));
                }
                try {
                    script = ScriptParser.newInstance(file, scriptEngine.getContext());
                } catch (IllegalStateException | AssertionError | BufferUnderflowException | BufferOverflowException ex) {
                    Debug.err.format("[ERROR] Found at file \"%s\"%n", filename);
                } catch (TokenException ex) {
                    Debug.err.format("[ERROR] Found at file \"%s\"%n\t%s%n",
                            filename, ex);
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

    private static void printCopyrightMessage() {
        Debug.out.format("\tStellarisIDE is an open-source software licensed under GPLv3.%n"
                + "\tIt is aimed to help people create non-commercial mods%n"
                + "\tfor Stellaris (R), which is a game developed by Paradox Interactive.%n%n"
                + "\tCopyright (C) 2016  donizyo%n%n");
    }

    private static void printHelpMessage() {
        String jarName;

        jarName = new File(Stellaris.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        Debug.out.format("Usage:%n\tjava -jar %s <StellarisPath>%n%n",
                jarName);
    }

    public static void main(String[] args) {
        String path;
        Stellaris st;
        ScriptEngine se;
        ScriptContext sc;
        FieldTypeBinding ftb;

        if (args.length < 1) {
            printHelpMessage();
            printCopyrightMessage();
            return;
        }

        path = args[0];
        Debug.out.format("Checkout directory \"%s\"...%n", path);
        st = null;
        try {
            st = new Stellaris();
            Stellaris.setDefault(st);
            st.init(path, true);
            //ModLoader.getModLoaders();
            se = st.scriptEngine;
            sc = se.getContext();
            ftb = new FieldTypeBinding(sc);
            try (FileOutputStream fout = new FileOutputStream("ftb.log");
                    PrintStream out = new PrintStream(fout);) {
                ftb.list(out);
            } catch (IOException ex) {
                Logger.getLogger(Stellaris.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            if (st != null) {
                st.digestStore.store();
            }
        }
    }
}
