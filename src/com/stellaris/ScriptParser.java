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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Deque;
import java.util.ArrayDeque;

/**
 *
 * @author donizyo
 */
public class ScriptParser implements AutoCloseable {

    private static final int BUFFER_SIZE = 4096;

    private final BufferedReader reader;
    private CharBuffer buffer;
    private boolean hasNext;
    private final Deque<String> deque;

    public ScriptParser(Reader in) {
        reader = new BufferedReader(in);
        deque = new ArrayDeque<>(128);
        fill();
    }

    private static CharBuffer allocateBuffer() {
        return CharBuffer.allocate(BUFFER_SIZE);
    }

    /**
     * Fill up the whole buffer
     *
     * @throws IOException
     */
    private void fill() {
        CharBuffer copy;
        int res;

        if (buffer == null) {
            buffer = allocateBuffer();
        } else {
            copy = allocateBuffer();
            copy.put(buffer);
            buffer = copy;
        }

        try {
            res = reader.read(buffer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        buffer.flip();
        hasNext = res > 0;
    }

    public boolean hasNext() {
        return hasNext || !deque.isEmpty();
    }

    public String peek() {
        String res;

        if (deque.isEmpty()) {
            res = next0();
            deque.addLast(res);
        } else {
            res = deque.getFirst();
        }

        return res;
    }

    public String[] peek(int count) {
        String[] res;
        String str;
        int i;

        res = new String[count];
        i = 0;
        while (!deque.isEmpty() && i < count) {
            str = deque.getFirst();
            res[i++] = str;
        }
        while (i < count) {
            str = next0();
            res[i++] = str;
            deque.addLast(str);
        }
        return res;
    }

    /**
     * Dicard elements buffered in built-in deque
     * Will not ignore elements still in the stream
     * @param count 
     */
    public void discard(int count) {
        int i;

        i = 0;
        while (i++ < count) {
            deque.removeFirst();
        }
    }

    public String next() {
        if (!deque.isEmpty()) {
            return deque.removeFirst();
        }
        return next0();
    }

    private String next0() {
        StringBuilder sb;
        char c;
        int state;

        sb = new StringBuilder();
        // state:
        // 0 :      initial
        // 1 :      accept
        // 2 :      comment
        state = 0;
        while (hasNext()) {
            if (buffer.hasRemaining()) {
                c = buffer.get();
                if (Character.isWhitespace(c)) {
                    if (state == 0) {
                        // skip first few whitespace characters
                        continue;
                    } else if (state == 2) {
                        if (c == '\r' || c == '\n') {
                            break;
                        }
                    } else {
                        break;
                    }
                } else if (c == '#') {
                    state = 2;
                } else if (state != 2) {
                    state = 1;
                }
                sb.append(c);
            } else {
                fill();
                if (!hasNext()) {
                    break;
                }
            }
        }

        if (sb.length() == 0) {
            throw new IllegalStateException();
        }
        return sb.toString();
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
        }
    }
}
