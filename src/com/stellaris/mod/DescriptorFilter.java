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

import com.stellaris.test.Debug;
import java.io.File;
import java.io.FileFilter;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class DescriptorFilter implements FileFilter {

    private static final String SUFFIX_MOD = ".mod";

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

        loader = new ModLoader(file);
        queue.add(loader);

        return false;
    }
}
