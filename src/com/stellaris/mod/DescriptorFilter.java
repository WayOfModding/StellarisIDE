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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        try {
            try {
                // integer file name ==> subscribed mod descriptor
                Integer.parseInt(prefix);
                queue = queueRemote;
                // workshop mods can be disabled for debugging
                //queue = null;
                loader = new RemoteModLoader(pathHome, file);
            } catch (NumberFormatException ex) {
                queue = queueLocal;
                loader = new LocalModLoader(pathHome, file);
            }
            if (queue != null) {
                loader.handleMod();
                queue.add(loader);
            }
        } catch (IOException ex) {
            Logger.getLogger(DescriptorFilter.class.getName()).log(Level.SEVERE, filename, ex);
        }

        return false;
    }
}
