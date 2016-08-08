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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author donizyo
 */
public class VersionScanner {

    private final File game;
    private String gameVersion;

    public VersionScanner(String path) {
        game = new File(path, "stellaris.exe");
    }

    public VersionScanner(File path) {
        game = new File(path, "stellaris.exe");
    }

    private String scanGameVersion(Reader reader) throws IOException {
        int bufSize;
        CharBuffer buf;
        CharBuffer out;
        char[] pattern;
        int len;
        char c;
        int state;

        bufSize = 4096;
        buf = CharBuffer.allocate(bufSize);
        out = CharBuffer.allocate(16);
        state = 0;
        pattern = "Stellaris v".toCharArray();
        len = pattern.length;
        while (reader.read((CharBuffer) buf.clear()) > 0) {
            buf.flip();
            while (state < len && buf.hasRemaining()) {
                c = buf.get();
                if (pattern[state] != c) {
                    state = 0;
                } else {
                    state++;
                }
                if (state == len) {
                    break;
                }
            }
            if (state < len) {
                continue;
            }
            while (buf.hasRemaining()) {
                c = buf.get();
                if (c >= '0' && c <= '9' || c == '.') {
                    out.put(c);
                } else {
                    break;
                }
            }
            break;
        }

        out.flip();
        if (state == len) {
            // version string is found
            return out.toString();
        } else {
            throw new IllegalStateException(
                    "Version string is not found in 'stellaris.exe': " + state
            );
        }
    }

    public String getGameVersion()
            throws IOException {
        File g;

        g = game;
        if (gameVersion != null) {
            return gameVersion;
        }
        if (!g.isFile()) {
            throw new FileNotFoundException();
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(g));) {
            return gameVersion = scanGameVersion(reader);
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            return;
        }
        String example = "Stellaris v1.2.3.23711";
        String path = args[0];
        VersionScanner scanner = new VersionScanner(path);
        try (StringReader reader = new StringReader(example);) {
            System.out.format("Example:\t%s%n", scanner.scanGameVersion(reader));
        } catch (IOException ex) {
            Logger.getLogger(VersionScanner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
