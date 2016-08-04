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

import com.stellaris.util.SoloSet;
import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author donizyo
 */
public class ScriptFilter implements FileFilter {

    private static final Set<String> DEFAULT_SUFFIXES;

    static {
        DEFAULT_SUFFIXES = new HashSet<>(4);
        DEFAULT_SUFFIXES.add(".txt");
        DEFAULT_SUFFIXES.add(".gui");
        DEFAULT_SUFFIXES.add(".gfx");
    }

    private final Set<String> ext;
    protected final Queue<File> files;
    protected final Queue<File> dirs;

    public ScriptFilter(Queue<File> init) {
        this(DEFAULT_SUFFIXES, init);
    }

    public ScriptFilter(Set<String> suffixes, Queue<File> init) {
        ext = suffixes;
        files = new LinkedList<>();
        dirs = new LinkedList<>();
        if (init != null) {
            dirs.addAll(init);
        }
    }

    public ScriptFilter(String suffix, Queue<File> init) {
        this(toSet(suffix), init);
    }

    private static Set<String> toSet(String suffix) {
        Set<String> suffixes;

        suffixes = new SoloSet<>(suffix);
        return suffixes;
    }

    public Queue<File> getFiles() {
        return files;
    }

    public Queue<File> getDirs() {
        return dirs;
    }

    @Override
    public boolean accept(File file) {
        String name;
        Set<String> s;
        Queue<File> q;

        s = ext;
        q = files;
        if (file.isDirectory()) {
            dirs.add(file);
        } else if (file.isFile()) {
            name = file.getName();
            for (String suffix : s) {
                if (name.endsWith(suffix)) {
                    q.add(file);
                    break;
                }
            }
        }
        return false;
    }

}
