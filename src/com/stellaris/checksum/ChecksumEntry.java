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

import com.stellaris.ScriptFilter;
import java.io.File;
import java.io.FileFilter;
import java.util.Set;

/**
 *
 * @author donizyo
 */
public class ChecksumEntry extends ScriptFilter implements FileFilter {

    private final boolean recursive;
    private final String extension;

    public ChecksumEntry(boolean recursive, Set<String> extensions) {
        super(extensions, null);
        this.recursive = recursive;
        this.extension = null;
    }

    public ChecksumEntry(boolean recursive, String extension) {
        super(extension, null);
        this.recursive = recursive;
        this.extension = extension;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public boolean accept(File entry) {
        if (!isRecursive() && entry.isDirectory()) {
            return false;
        }

        return super.accept(entry);
    }
}
