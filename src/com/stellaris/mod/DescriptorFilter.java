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

import com.stellaris.TokenException;
import com.stellaris.test.Debug;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author donizyo
 */
public class DescriptorFilter implements FileFilter {

    private static final String SUFFIX_MOD = ".mod";

    private final Queue<ModLoader> queueLocal, queueRemote;
    private final String path;

    public DescriptorFilter(String pathHome,
            Queue<ModLoader> q, Queue<ModLoader> p) {
        path = pathHome;
        queueLocal = q;
        queueRemote = p;
    }

    @Override
    public boolean accept(File file) {
        String pathHome;
        String filename;
        int idx;
        String prefix;
        String suffix;
        ModLoader loader;
        Queue<ModLoader> queue;
        Pattern pRemoteMod;
        Matcher m;

        pathHome = path;
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
        // filter remote mod
        pRemoteMod = Pattern.compile("(?:ugc_)?\\d+");
        m = pRemoteMod.matcher(prefix);
        if (m.find()) {
            // is remote mod
            queue = queueRemote;
            // workshop mods can be disabled for debugging
            //queue = null;
            loader = new RemoteModLoader(pathHome, file);
        } else {
            // is local mod
            queue = queueLocal;
            loader = new LocalModLoader(pathHome, file);
        }
        try {
            if (queue != null) {
                try {
                    loader.handleMod();
                } catch (ModException ex) {
                    Debug.err.format("[ERROR] Found at mod \"%s\"%n"
                            + "\tname=%s%n"
                            + "\tpath=%s%n"
                            + "\tsver=%s%n"
                            + "\t%s%n",
                            ex.getMessage(),
                            loader.name,
                            loader.path,
                            loader.supportedVersion,
                            ex.getCause());
                }
                queue.add(loader);
            }
        } catch (IOException ex) {
            Logger.getLogger(DescriptorFilter.class.getName()).log(Level.SEVERE, filename, ex);
        }

        return false;
    }
}
