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

import com.stellaris.io.AbstractParser;
import com.stellaris.test.Debug;
import java.io.*;
import java.nio.*;
import java.util.*;
import static com.stellaris.test.Debug.*;
import com.stellaris.util.BOMReader;

/**
 *
 * @author donizyo
 */
public final class ScriptParser extends AbstractParser {

    private static final int BUFFER_SIZE = 65536;
    private static final int CACHE_SIZE = 3;

    private LinkedList<Token> queue;
    // refrigerator for leftover
    private CharBuffer line;

    public ScriptParser(File file) throws IOException {
        this(new BOMReader(file));
    }

    public ScriptParser(Reader in) throws IOException {
        super(in instanceof BufferedReader
                ? (BufferedReader) in
                : new BufferedReader(in),
                BUFFER_SIZE);
        queue = new LinkedList<>();
        line = null;
    }

    public String skipCurrentLine() {
        CharBuffer buf;
        String res;
        int idx;
        Queue<Token> q;
        Queue<Token> p;
        Token t;
        int tl;

        buf = line;
        buf.reset();
        res = buf.toString();
        line = null;

        idx = getLineNumber();
        q = queue;
        if (!q.isEmpty()) {
            p = new LinkedList<>();
            while (!q.isEmpty()) {
                t = q.remove();
                tl = t.getLineNumber();
                if (tl == idx) {
                    continue;
                }
                p.add(t);
            }
            q = p;
        }

        return res;
    }

    private boolean hasRemaining() {
        return !queue.isEmpty();
    }

    public boolean hasNextToken() throws IOException, TokenException {
        boolean res;

        res = hasRemaining() || cache(CACHE_SIZE);
        return res;
    }

    private boolean cache(int count)
            throws IOException, TokenException {
        Queue<Token> q;
        Token str;
        boolean res;

        q = queue;
        while (q.size() < count) {
            str = next();
            if (str == null) {
                break;
            }
        }
        res = hasRemaining();
        return res;
    }

    private boolean cache(Token s) {
        Queue<Token> q;

        if (s == null) {
            return false;
        }
        q = queue;
        return q.add(s);
    }

    public List<Token> peekToken(int count)
            throws IOException, TokenException {
        List<Token> res;
        int size;

        cache(count);
        size = queue.size();
        if (size < count) {
            count = size;
        }
        res = queue.subList(0, count);
        res = Collections.unmodifiableList(res);
        return res;
    }

    /**
     * Dicard elements buffered in built-in deque Will not ignore elements still
     * in the stream
     *
     * @param count
     */
    public void discardToken(int count) {
        int i;
        Token str;

        if (DEBUG && DEBUG_DISCARD) {
            System.err.format("[DSCD]\tcount=%d%n", count);
        }
        i = 0;
        while (i++ < count) {
            str = queue.remove();
            if (DEBUG && DEBUG_DISCARD) {
                System.err.format("[DSCD]\tstr=\"%s\"%n\tcache=%d %s%n",
                        str, queue.size(), queue.toString()
                );
            }
        }
    }

    /**
     * Get next token
     *
     * @return
     * @throws java.io.IOException
     */
    public Token nextToken() throws IOException, NoSuchElementException {
        Token res;

        if (!hasNextToken()) {
            throw new NoSuchElementException();
        }
        res = queue.remove();
        if (DEBUG && DEBUG_NEXT) {
            System.err.format("[NEXT]\tline=%d, next=\"%s\"%n\tcache=%d %s%n",
                    getLineNumber(), res, queue.size(), queue.toString()
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
    private Token cache(CharBuffer charBuffer, int src, int dst)
            throws AssertionError {
        int len;
        char[] buf;
        String str;

        if (src == dst) {
            throw new AssertionError("Empty string");
        }
        len = dst - src;
        buf = charBuffer.array();
        str = new String(buf, src, len);
        if (DEBUG && DEBUG_CACHE) {
            System.err.format("[CACHE]\tline=%d, src=%d, dst=%d, str=\"%s\"%n"
                    + "\tcache=%d %s%n",
                    getLineNumber(), src, dst, str,
                    queue.size(), queue.toString()
            );
        }

        return new Token(str, getLineNumber());
    }

    private boolean isTerminalCharacter(char c) {
        return c == '{'
                || c == '}'
                || c == '='
                || c == '>'
                || c == '<';
    }

    /**
     * Retrieves the next token string
     *
     * @return
     */
    private Token next()
            throws IOException, TokenException {
        char c;
        int src, dst, pos;
        Token res;
        boolean isComment;
        boolean isString;
        CharBuffer buf;

        buf = line;
        if (buf == null) {
            // no leftover available
            // get a new line
            buf = nextLine();
            // mark the beginning of the new buffer
        }
        // skip empty line:         "\r?\n"
        // skip white-space line:   "\s+\r?\n"
        while (buf != null
                //&& !buf.hasRemaining()
                && !skipLeadingWhitespace(buf)) {
            // empty line
            buf = nextLine();
        }
        if (buf == null) {
            // hit EOF
            return null;
        }
        //System.err.format("[LINE]\t%s%n", buf);

        isComment = false;
        c = buf.get();
        if (c == '#') {
            isComment = true;
            res = handleComment(buf);
            if (Debug.ACCEPT_COMMENT) {
                cache(res);
            }
        } else {
            // non-comment token

            // handle leading terminal characters
            pos = buf.position();
            src = pos - 1;
            if (isTerminalCharacter(c)) {
                dst = pos;
            } else {
                isString = c == '"';
                while (true) {
                    if (!buf.hasRemaining()) {
                        if (isString) {
                            throw new TokenException("String is not closed");
                        }
                        // NEWLINE or EOF
                        dst = buf.position();
                        break;
                    }
                    c = buf.get();
                    if (isString) {
                        if (c == '\\') {
                            buf.get();
                            //continue;
                        } else if (c == '"') {
                            dst = buf.position();
                            break;
                        }
                    } else if (Character.isWhitespace(c)) {
                        dst = buf.position() - 1;
                        break;
                    } else if (isTerminalCharacter(c)
                            || c == '#') {
                        // handle ending terminal characters
                        // handle immediate ending comment
                        // fuck those who don't have good coding habits
                        dst = buf.position() - 1;
                        buf.position(dst);
                        break;
                    }
                }
            }
            res = cache(buf, src, dst);
            cache(res);
        }

        if (!buf.hasRemaining() || isComment) {
            buf = null;
        }
        line = buf;

        return res;
    }

    /**
     *
     * @param buf
     * @return true, if line is not empty
     */
    private boolean skipLeadingWhitespace(CharBuffer buf) {
        char c;
        int pos;

        while (true) {
            if (buf.hasRemaining()) {
                // skip the first few whitespace characters
                c = buf.get();
                if (Character.isWhitespace(c)) {
                    continue;
                }
                pos = buf.position();
                buf.position(pos - 1);
                return true;
            } else {
                // if there's no remaining characters in the buffer
                return false;
            }
        }
    }

    private Token handleComment(CharBuffer buf) {
        int src;
        char c;
        String res;

        // find a comment token
        while (buf.hasRemaining()) {
            c = buf.get();
            if (c == '#') {
                continue;
            }
            src = buf.position() - 2;
            buf.position(src);
            res = buf.toString();
            return new Token(res, getLineNumber());
        }
        res = "#";
        return new Token(res, getLineNumber());
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            return;
        }
        try (ScriptParser parser = new ScriptParser(new File(args[0], args[1]));) {
            int count0; // count {
            int count1; // count }
            CharBuffer buffer;
            char c;

            count0 = count1 = 0;
            while ((buffer = parser.nextLine()) != null) {
                while (buffer.hasRemaining()) {
                    c = buffer.get();
                    switch (c) {
                        case '{':
                            ++count0;
                            break;
                        case '}':
                            ++count1;
                            break;
                    }
                }
            }
            System.out.format("Count['{']=%d%nCount['}']=%d%nDelta=%d%n",
                    count0, count1, count0 - count1);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
