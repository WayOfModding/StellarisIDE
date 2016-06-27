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

import com.stellaris.test.Debug;
import com.stellaris.util.BOMReader;
import java.io.*;
import java.nio.CharBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author donizyo
 */
public class LangFileReader {

    private static final int BUFFER_SIZE = 0x100000;
    private final String langId;
    private final File file;
    private CharBuffer buffer;

    public LangFileReader(Language language, File file) throws IOException {
        this.langId = language.getLanguageID();
        this.file = file;
        try (Reader reader = new BOMReader(file);) {
            buffer = CharBuffer.allocate(BUFFER_SIZE);
            reader.read(buffer);
            buffer.flip();
        }
    }

    public CharBuffer nextLine() {
        CharBuffer res;
        char c;
        int src, dst, pos;

        if (buffer.hasRemaining()) {
            res = buffer;
            do {
                c = res.get();
                if (!res.hasRemaining())
                    return null;
            } while (Character.isWhitespace(c));
            pos = res.position();
            src = pos - 1;
            res.position(src);
            res = res.slice();
            while (true) {
                if (!res.hasRemaining())
                    throwEOF(res);
                c = res.get();
                if (c == '\r') {
                    c = res.get();
                    if (c != '\n')
                        throw new IllegalStateException(String.valueOf(c));
                    break;
                } else if (c == '\n') {
                    break;
                }
            }
            buffer = res.slice();

            pos = res.position();
            dst = pos - 1;
            res.position(dst);
            res.flip();
        } else {
            res = null;
        }

        return res;
    }

    private static void logToken(String token, CharBuffer cbuf) {
        if (!Debug.DEBUG) {
            return;
        }
        System.out.format("[%s] pos=%d, limit=%d, str=\"%s\"%n",
                token,
                cbuf.position(),
                cbuf.limit(),
                cbuf.toString()
        );
    }

    private static void throwEOF(CharBuffer cbuf) {
        int pos = cbuf.position();
        int limit = cbuf.limit();
        cbuf.rewind();
        throw new IllegalStateException(String.format(
                "EOF: pos=%d, limit=%d, str=\"%s\"",
                pos,
                limit,
                cbuf.toString()
        ));
    }

    public void loadInto(LocalisationMap map) {
        CharBuffer line;
        String str;
        char c;
        CharBuffer key;
        CharBuffer digit;
        CharBuffer value;
        int pos;
        int dst;
        int state;

        line = nextLine();
        str = line.toString();
        if (!str.startsWith(langId)) {
            throw new AssertionError(str);
        }
        while ((line = nextLine()) != null) {
            logToken("Line", line);

            // key
            key = line.duplicate();
            c = line.get();
            if (c == '#') {
                continue;
            }
            while (true) {
                if (!key.hasRemaining()) {
                    throwEOF(key);
                }
                c = key.get();
                if (c >= 'a' && c <= 'z'
                        || c >= 'A' && c <= 'z'
                        || c >= '0' && c <= '9'
                        || c == '_'
                        || c == '.'
                        || c == '-') {
                    continue;
                }
                if (c == ':') {
                    digit = key.slice();

                    pos = key.position();
                    dst = pos - 1;
                    key.position(dst);
                    key.flip();
                    logToken("Key", key);
                    break;
                }
                // non-accepting state
                line.rewind();
                throw new IllegalStateException(String.format(
                        "Illegal character '%c' in%n%s%n",
                        c,
                        line.toString()
                ));
            }

            // priority
            while (true) {
                if (!digit.hasRemaining()) {
                    throwEOF(digit);
                }
                c = digit.get();
                if (c >= '0' && c <= '9') {
                    continue;
                }
                if (c != ' ') {
                    throw new IllegalStateException(String.valueOf(c));
                }
                pos = digit.position();
                dst = pos - 1;

                c = digit.get();
                if (c != '"') {
                    throw new IllegalStateException(String.valueOf(c));
                }

                value = digit.slice();

                digit.position(dst);
                digit.flip();
                logToken("Digit", digit);
                break;
            }

            // value
            state = 0;
            while (true) {
                if (!value.hasRemaining()) {
                    throwEOF(value);
                }
                c = value.get();

                if (c == '\\') {
                    state = 1;
                } else {
                    if (c == '"') {
                        if (state != 1) {
                            pos = value.position();
                            dst = pos - 1;

                            value.position(dst);
                            value.flip();
                            logToken("Value", value);
                            break;
                        }
                    }
                    state = 0;
                }
            }

            // put
            if (map != null) {
                map.put(file, key, digit, value);
            }
            // black-box debugging
            //*
            String result = String.format("%s:%s \"%s\"",
                    key,
                    digit,
                    value
            );
            line.rewind();
            String sLine = line.toString();
            if (!sLine.contains(result)) {
                System.err.println(result);
                throw new AssertionError(sLine);
            }
            //*/
            // white-box debugging
            /*
            System.out.format("%-64s %s \"%s\"%n",
                    key.toString(),
                    digit.toString(),
                    value.toString()
            );
            //*/
        }
    }

    public static void main(String[] args) {
        String root, path;
        File file;
        LangFilter langFilter;
        LangFileReader reader;
        LocalisationMap map;

        if (args.length < 2) {
            return;
        }
        root = args[0];
        path = args[1];
        file = new File(root, path);
        langFilter = LangFilterFactory.LANG_ENGLISH;
        map = new LocalisationMap(langFilter);
        try {
            //Debug.DEBUG = true;
            reader = new LangFileReader(langFilter, file);
            reader.loadInto(map);
        } catch (IOException ex) {
            Logger.getLogger(LangFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
