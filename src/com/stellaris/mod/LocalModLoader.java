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

import com.stellaris.DirectoryFilter;
import com.stellaris.ScriptFilter;
import com.stellaris.Stellaris;
import com.stellaris.test.Debug;
import com.stellaris.util.ScriptPath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author donizyo
 */
class LocalModLoader extends ModLoader {

    public LocalModLoader(String home, File file) {
        super(home, file);
    }

    public void handleMod() throws IOException {
        File file;

        file = new File(pathHome, path);
        handleDirectory(file);
    }

    private void handleDirectory(final File root) throws IOException {
        File dir, file;
        DirectoryFilter df;
        ScriptFilter sf;
        Queue<File> dirs, files;
        String filename;
        String msg;
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
        df = new DirectoryFilter();
        root.listFiles(df);
        sf = new ScriptFilter(df.getDirs());
        dirs = sf.getDirs();

        while (!dirs.isEmpty()) {
            dir = dirs.remove();
            dir.listFiles(sf);
            // filter directories
            filename = ScriptPath.getPath(dir);
            if (filename == null) {
                throw new NullPointerException();
            }
            doParseFile = set.contains(filename);
            if (!doParseFile) {
                continue;
            }

            files = sf.getFiles();
            loop:
            while (!files.isEmpty()) {
                file = files.remove();
                filename = ScriptPath.getModFilePath(file);
                //System.out.format("Mod: %s%n", filename);
                if (filename.endsWith(".txt")) {
                    try (FileReader reader = new FileReader(file);) {
                        handleReader(filename, reader);
                    } catch (SyntaxException ex) {
                        msg = ex.getMessage();
                        if (msg == null) {
                            msg = "";
                        }
                        Debug.err.format(
                                "[MOD]\tfile=\"%s\"%n"
                                + "\tname=\"%s\"%n"
                                + "\tsupported_version=\"%s\"%n"
                                + "\tSyntaxException: %s%n",
                                filename,
                                name,
                                supportedVersion,
                                msg
                        );
                    }
                }
            }
        }
    }
}
