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

import com.stellaris.Stellaris;
import com.stellaris.util.ScriptPath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Set;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 *
 * @author donizyo
 */
class RemoteModLoader extends ModLoader {

    private static final String DEFAULT_ENTRY_NAME_DESCRIPTOR = "descriptor.mod";

    public RemoteModLoader(String home, File file) {
        super(home, file);
    }

    public void handleMod() throws IOException {
        File file;

        file = new File(path);
        if (!file.isFile())
            file = new File(pathHome, path);
        handleArchive(file);
    }

    private static String getParentEntryName(String entryName) {
        int idx;

        idx = entryName.lastIndexOf('/');
        if (idx == -1)
            return "";
        return entryName.substring(0, idx);
    }

    private void handleArchive(final File file) throws IOException {
        Enumeration<? extends ZipArchiveEntry> entries;
        ZipArchiveEntry entry;
        String entryName;
        String parentEntryName;
        String filename;
        Stellaris main;
        Set<String> set;
        boolean doParseFile;

        main = Stellaris.getDefault();
        if (main == null) {
            throw new NullPointerException("Stellaris instance not found!");
        }
        set = main.getDirectories();
        if (set == null) {
            throw new NullPointerException("Script directories not found!");
        }
        if (set.isEmpty()) {
            throw new IllegalStateException("Script directories not found!");
        }
        try (ZipFile zf = new ZipFile(file);) {
            entries = zf.getEntries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                entryName = entry.getName();
                parentEntryName = getParentEntryName(entryName);
                // filter entries
                doParseFile = set.contains(parentEntryName);
                if (!doParseFile) {
                    continue;
                }
                filename = ScriptPath.getModArchivePath(file, entryName);
                if (DEFAULT_ENTRY_NAME_DESCRIPTOR.equals(entryName)) {
                    continue;
                }
                if (entryName.endsWith(".txt")) {
                    try (InputStream input = zf.getInputStream(entry);
                            Reader reader = new InputStreamReader(input);) {
                        handleReader(filename, reader);
                    }
                }
            }
        }
    }
}
