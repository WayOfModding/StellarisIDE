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
import java.io.FileFilter;

/**
 *
 * @author donizyo
 */
public final class RootFilter implements FileFilter {
    
    private File dirRoot;
    
    private RootFilter() {
    }
    
    public static File getGameDirectory() {
        File[] roots;
        RootFilter filter;
        File res;
        
        roots = File.listRoots();
        filter = new RootFilter();
        for (File root : roots) {
            res = filter.openDirectory(root);
            if (res != null)
                return res;
        }
        return (File) null;
    }
    
    private File openDirectory(File root) {
        File game;
        
        game = new File(root, "SteamLibrary/steamapps/common/Stellaris");
        if (game.isDirectory())
            return game;
        game = new File(root, "Program Files/Steam/steamapps/common/Stellaris");
        if (game.isDirectory())
            return game;
        root.listFiles(this);
        return dirRoot;
    }

    @Override
    public boolean accept(File dir) {
        File game;
        
        if (dirRoot != null)
            return false;
        if (!dir.isDirectory())
            return false;
        game = new File(dir, "steamapps/common/Stellaris");
        if (game.isDirectory())
            dirRoot = game;
        return false;
    }
    
}
