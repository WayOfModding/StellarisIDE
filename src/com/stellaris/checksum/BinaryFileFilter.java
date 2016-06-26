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
package com.stellaris.checksum;

import java.io.*;
import java.util.*;

/**
 *
 * @author donizyo
 */
public class BinaryFileFilter implements FileFilter {

    private final Queue<File> files;

    static {
        String osName;

        osName = System.getProperty("os.name");
        if (!osName.startsWith("Win")) {
            throw new UnsupportedOperationException(osName);
        }
    }

    public BinaryFileFilter() {
        files = new LinkedList<>();
    }

    public Queue<File> getFiles() {
        return files;
    }

    @Override
    public boolean accept(File file) {
        String name;

        if (!file.isFile()) {
            return false;
        }
        name = file.getName();
        if (name.endsWith(".exe")
                || name.endsWith(".dll")) {
            files.add(file);
        }
        return false;
    }

}
