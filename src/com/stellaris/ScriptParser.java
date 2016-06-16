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

import com.stellaris.test.Debug;
import java.io.*;
import java.nio.*;
import java.util.*;
import static com.stellaris.test.Debug.*;

/**
 *
 * @author donizyo
 */
public class ScriptParser implements AutoCloseable {

    private static final int BUFFER_SIZE = 4096;
    private static final int REFILL_SIZE = 256;
    private static final int CACHE_SIZE = 3;

    private final BufferedReader reader;
    private CharBuffer buffer;
    private boolean hasMore;
    private final LinkedList<String> deque;
    private int lineCounter;

    public ScriptParser(Reader in) {
        reader = new BufferedReader(in);
        deque = new LinkedList<>();
        fill();
        lineCounter = 0;
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
        hasMore = res > 0 && !buffer.hasRemaining();
        buffer.flip();
    }

    private boolean hasRemaining() {
        return !deque.isEmpty();
    }

    public boolean hasNext() {
        boolean res;

        res = hasRemaining() || cache(CACHE_SIZE);
        return res;
    }

    /**
     * Fill the deque
     *
     * @param count
     */
    private boolean cache(int count) {
        String str;
        boolean res;

        while (deque.size() < count) {
            str = next0();
            if (str == null) {
                hasMore = false;
                break;
            }
        }
        res = hasRemaining();
        return res;
    }

    public List<String> peek(int count) {
        List<String> res;
        int size;

        cache(count);
        size = deque.size();
        if (size < count) {
            count = size;
        }
        res = deque.subList(0, count);
        res = Collections.unmodifiableList(res);
        return res;
    }

    /**
     * Dicard elements buffered in built-in deque Will not ignore elements still
     * in the stream
     *
     * @param count
     */
    public void discard(int count) {
        int i;
        String str;

        i = 0;
        while (i++ < count) {
            str = deque.remove();
            if (DEBUG) {
                System.err.format("[DISCARD]\tcache=%d %s%n",
                        deque.size(), deque.toString()
                );
            }
        }
    }

    /**
     * Get next token
     *
     * @return
     */
    public String next() {
        String res;

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        res = deque.remove();
        if (res == null) {
            throw new AssertionError();
        }
        if (DEBUG) {
            System.err.format("[NEXT]\tline=%d, next=\"%s\"%n\tcache=%d %s%n",
                    lineCounter, res, deque.size(), deque.toString()
            );
        }
        return res;
    }

    /**
     * Create a token string with char data from the buffer
     *
     * @param src
     * @param dst
     * @return
     */
    private String cache(int src, int dst) {
        int len;
        char[] buf;
        String str;
        boolean res;

        if (src == dst) {
            throw new AssertionError();
        }
        len = dst - src;
        buf = new char[len];
        buffer.position(src);
        buffer.get(buf);
        str = new String(buf);
        res = cache(str);
        if (DEBUG) {
            System.err.format("[CACHE]\tline=%d, src=%d, dst=%d, str=\"%s\"%n"
                    + "\tcache=%d %s%n",
                    lineCounter, src, dst, str,
                    deque.size(), deque.toString()
            );
        }

        return str;
    }

    private boolean cache(String str) {
        return deque.add(str);
    }

    private boolean refill() {
        int rem;

        rem = buffer.remaining();
        // check if there are enough remaining characters in the buffer
        if (rem < REFILL_SIZE) {
            if (hasMore) {
                // re-fill if there are more characters in the stream
                fill();
            } else if (rem == 0) {
                // if there ain't more characters in the stream
                // and if there ain't remaining charactes in the buffer
                // there won't be any possible token to be parsed
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the next token string
     *
     * @return
     */
    private String next0() {
        char c;
        int src, dst;
        String res;
        boolean isString;

        if (!refill()) {
            return null;
        }
        while (true) {
            if (buffer.hasRemaining()) {
                // skip the first few whitespace characters
                c = buffer.get();
                if (!Character.isWhitespace(c)) {
                    break;
                }
                if (c == '\r') {
                    buffer.mark();
                    if (buffer.get() != '\n') {
                        buffer.reset();
                    }
                    ++lineCounter;
                } else if (c == '\n') {
                    ++lineCounter;
                }
            } else {
                // if there's no remaining characters in the buffer
                return null;
            }
        }

        if (c == '#') {
            // find a comment token
            src = buffer.position() - 1;
            while (true) {
                if (!buffer.hasRemaining()) {
                    dst = buffer.position();
                    break;
                }
                c = buffer.get();
                if (c == '#') {
                    src = buffer.position() - 1;
                    continue;
                }
                if (c == '\r') {
                    dst = buffer.position() - 1;
                    buffer.mark();
                    if (buffer.get() != '\n') {
                        buffer.reset();
                    }
                    ++lineCounter;
                    break;
                } else if (c == '\n') {
                    dst = buffer.position() - 1;
                    ++lineCounter;
                    break;
                }
                if (buffer.hasRemaining()) {
                    continue;
                }
                throw new TokenException("Comment token is too long!");
            }
            if (dst - src == 1
                    && buffer.get(src) == '#') {
                res = next0();
            } else if (src < dst) {
                if (Debug.ACCEPT_COMMENT) {
                    res = cache(src, dst);
                } else {
                    res = next0();
                }
            } else {
                res = next0();
            }
        } else {
            // non-comment token
            src = buffer.position() - 1;
            isString = c == '"';
            while (true) {
                if (!buffer.hasRemaining()) {
                    dst = buffer.position();
                    break;
                }
                c = buffer.get();
                if (isString) {
                    if (c == '\\') {
                        buffer.get();
                        continue;
                    } else if (c == '"') {
                        dst = buffer.position();
                        break;
                    }
                } else if (Character.isWhitespace(c)) {
                    dst = buffer.position() - 1;
                    if (c == '\r') {
                        buffer.mark();
                        if (buffer.get() != '\n') {
                            buffer.reset();
                        }
                        ++lineCounter;
                    } else if (c == '\n') {
                        ++lineCounter;
                    }
                    break;
                } else if (c == '}') {
                    dst = buffer.position() - 1;
                    buffer.position(dst);
                    break;
                }
                if (buffer.hasRemaining()) {
                    continue;
                }
                throw new TokenException("Key token is too long!");
            }
            res = cache(src, dst);
        }

        return res;
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
        }
    }

    public static void main(String[] args) {
        //ScriptFile.newInstance(new java.io.File(args[0]));
        /*
        try (ScriptParser parser = new ScriptParser(
                new java.io.StringReader("\r\n \r \r\n \r\n \n"));) {
            parser.peek(1024);
            System.out.format("Line=%d%n", parser.lineCounter);
        }
        //*/
        //*
        try (ScriptParser parser = new ScriptParser(
                new java.io.FileReader(new java.io.File(args[0], args[1])));) {
            parser.peek(1024);
        } catch (FileNotFoundException ex) {
        }
        //*/
    }
}
