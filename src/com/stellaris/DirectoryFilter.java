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

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class DirectoryFilter implements FileFilter {

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
