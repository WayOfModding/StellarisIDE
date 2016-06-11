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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author donizyo
 */
public class DigestStore {

    private static final String PATH_STORE = "stellaris.dig";
    private final Properties prop;

    public DigestStore() {
        File file;

        prop = new Properties();
        file = getFileStore();
        if (!file.isFile()) {
            return;
        }
        try (FileInputStream finput = new FileInputStream(file);) {
            prop.load(finput);
        } catch (IOException ex) {
            Logger.getLogger(DigestStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static File getFileStore() {
        File file;
        String dirWorking;

        dirWorking = getUserDir();
        if (dirWorking == null) {
            throw new NullPointerException();
        }
        file = new File(dirWorking, PATH_STORE);
        return file;
    }

    public static String getPath(File file) {
        String path;

        path = file.getAbsolutePath();
        return getPath(path);
    }

    public static String getPath(String path) {
        int idx;

        idx = path.indexOf("Stellaris");
        return path.substring(idx);
    }

    public boolean matches(File file) {
        String path;
        Digest digest;
        String sum, res;

        path = getPath(file);
        sum = prop.getProperty(path);
        digest = new Digest(file);
        res = digest.digest();

        if (res.equals(sum)) {
            return true;
        }
        prop.setProperty(path, res);
        return false;
    }

    public void store() {
        File file;

        file = getFileStore();
        try (FileOutputStream fout = new FileOutputStream(file);) {
            prop.store(fout, "");
        } catch (IOException ex) {
            Logger.getLogger(DigestStore.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getUserDir() {
        return System.getProperty("user.dir");
    }

    public static void main(String[] args) {
        System.out.format("user.dir=%s%n", getUserDir());
    }
}
