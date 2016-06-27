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
package com.stellaris.localisation;

import java.io.File;
import java.io.FileFilter;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author donizyo
 */
public class LangFilter implements FileFilter, Language {

    private static final String YAML_SUFFIX = ".yml";
    private final Queue<File> files;
    private final String langName;
    private final String langId;

    public LangFilter(String name, String id) {
        files = new LinkedList<>();
        langName = name;
        langId = id;
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
        if (!name.endsWith(YAML_SUFFIX)) {
            return false;
        }
        if (name.contains(getLanguageID())) {
            files.add(file);
        }
        return false;
    }

    @Override
    public String getLanguageName() {
        return langName;
    }

    @Override
    public String getLanguageID() {
        return langId;
    }

}
