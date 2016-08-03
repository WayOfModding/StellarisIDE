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
package com.stellaris.util;

import java.io.File;

/**
 *
 * @author donizyo
 */
public class ScriptPath {

    /**
     *
     * @param file the value of file
     * @return 
     */
    public static String getPath(File file) {
        String path;
        path = file.getAbsolutePath();
        return getPath(path);
    }

    /**
     *
     * @param path the value of path
     * @return 
     */
    public static String getPath(String path) {
        final String sp = "Stellaris\\";
        int len;
        int idx;
        idx = path.indexOf(sp);
        len = sp.length();
        idx += len;
        return path.substring(idx).replace('\\', '/');
    }
    
    public static String getModFilePath(File file) {
        String res;
        
        res = getPath(file);
        return res;
    }

    public static String getModArchivePath(File file, String entryName) {
        StringBuilder sb;
        
        sb = new StringBuilder();
        sb.append(getPath(file));
        sb.append("!/");
        sb.append(entryName);
        return sb.toString();
    }
}
