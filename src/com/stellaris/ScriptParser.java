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
import com.stellaris.util.DigestStore;

/**
 *
 * @author donizyo
 */
public final class ScriptParser extends AbstractParser {

    private static final int BUFFER_SIZE = 65536;
    private static final int CACHE_SIZE = 3;

    private LinkedList<String> queue;
    private Map<Integer, Queue<String>> map;
    private int bracket;

    public ScriptParser(File file) throws IOException {
        this(new BOMReader(file));
    }

    public ScriptParser(Reader in) throws IOException {
        super(in instanceof BufferedReader
                ? (BufferedReader) in
                : new BufferedReader(in),
                BUFFER_SIZE);
        queue = new LinkedList<>();
        map = new HashMap<>();
    }

    public void skipCurrentLine() {
        int idx;
        Queue<String> q;
        Map<Integer, Queue<String>> m;
        Queue<String> p;

        Debug.err.format("[INFO]\tSkip current line!%n");

        idx = getLineNumber();
        q = queue;
        // line - token mapping
        m = map;
        // retrieve all tokens in this line
        p = m.remove(idx);
        if (p != null) {
            q.removeAll(p);
        }
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
        Queue<String> q;
        Map<Integer, Queue<String>> m;
        boolean res;

        q = queue;
        m = map;
        while (q.size() < count) {
            res = tokenize(q, m);
            if (!res) {
                break;
            }
        }
        res = hasRemaining();
        return res;
    }

    public List<String> peekToken(int count)
            throws IOException, TokenException {
        List<String> res;
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
        String str;

        if (DEBUG && DEBUG_DISCARD) {
            Debug.err.format("[DSCD]\tcount=%d%n", count);
        }
        i = 0;
        while (i++ < count) {
            str = queue.remove();
            if (DEBUG && DEBUG_DISCARD) {
                Debug.err.format("[DSCD]\tstr=\"%s\"%n\tcache=%d %s%n",
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
    public String nextToken() throws IOException, NoSuchElementException {
        String res;

        if (!hasNextToken()) {
            throw new NoSuchElementException();
        }
        res = queue.remove();
        if (DEBUG && DEBUG_NEXT) {
            Debug.err.format("[NEXT]\tline=%d, next=\"%s\"%n\tcache=%d %s%n",
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
    private String cache(CharBuffer charBuffer,
            int src, int dst)
            throws AssertionError {
        int len;
        char[] buf;
        char c;
        String str;

        if (src == dst) {
            throw new AssertionError("Empty string");
        }
        len = dst - src;
        buf = charBuffer.array();
        if (len == 1) {
            c = buf[src];
            switch (c) {
                case '{':
                    ++bracket;
                    break;
                case '}':
                    --bracket;
                    break;
            }
        }
        str = new String(buf, src, len);
        if (DEBUG && DEBUG_CACHE) {
            Debug.err.format("[CACHE]\tline=%d, src=%d, dst=%d, str=\"%s\"%n"
                    + "\tcache=%d %s%n",
                    getLineNumber(), src, dst, str,
                    queue.size(), queue.toString()
            );
        }

        return str;
    }

    private boolean isTerminalCharacter(char c) {
        switch (c) {
            case '{':
            case '}':
            case '=':
            case '>':
            case '<':
                return true;
            default:
                return false;
        }
    }

    private void debugLine(CharBuffer buf) {
        if (Debug.DEBUG_LINE) {
            Debug.err.format("[LINE]\t%4d>\t%s%n",
                    buf.remaining(), buf);
        }
    }

    private boolean tokenize(Queue<String> q, Map<Integer, Queue<String>> m)
            throws IOException, TokenException {
        char c;
        int src, dst, pos;
        String res;
        int lineNumber;
        boolean isComment;
        boolean isString;
        CharBuffer buf;
        Queue<String> p;

        // skip empty line:         "\r?\n"
        // skip white-space line:   "\s+\r?\n"
        while (true) {
            // current line is empty
            // retrieve next line
            buf = nextLine();
            if (buf == null) {
                // hit EOF
                return false;
            }
            debugLine(buf);
            if (skipLeadingWhitespace(buf)) {
                break;
            }
        }

        lineNumber = getLineNumber();
        p = new LinkedList<>();
        do {
            isComment = false;
            c = buf.get();
            if (c == '#') {
                isComment = true;
                res = handleComment(buf);
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
                        pos = buf.mark().position();
                        c = buf.get();
                        if (isString) {
                            if (c == '\\') {
                                buf.get();
                                //continue;
                            } else if (c == '"') {
                                dst = pos + 1;
                                break;
                            }
                        } else if (Character.isWhitespace(c)) {
                            dst = pos;
                            break;
                        } else if (isTerminalCharacter(c)
                                || c == '#') {
                            // handle ending terminal characters
                            // handle immediate ending comment
                            // fuck those who don't have good coding habits
                            dst = pos;
                            buf.reset();
                            break;
                        }
                    }
                }
                res = cache(buf, src, dst);
            }

            if (!isComment || Debug.ACCEPT_COMMENT) {
                if (!q.add(res) || !p.add(res)) {
                    throw new IllegalStateException("Fail to add new token");
                }
            }
        } while (skipLeadingWhitespace(buf));

        // map the line with all tokens in this line
        if (!p.isEmpty()) {
            m.put(lineNumber, p);
        }

        return true;
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

    private String handleComment(CharBuffer buf) {
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
            buf.rewind().flip();
            return res;
        }
        res = "#";
        return res;
    }

    public void close() throws IOException {
        super.close();
        if (bracket != 0) {
            throw new TokenException(
                    String.format(
                            "Unmatching bracket pairs: %d",
                            bracket
                    )
            );
        }
    }

    public static void main(String[] args) {
        File file;

        if (args.length < 2) {
            return;
        }
        file = new File(args[0], args[1]);
        try (ScriptParser parser = new ScriptParser(file);) {
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
            Debug.out.format("File=\"%s\"%n"
                    + "Count['{']=%d%n"
                    + "Count['}']=%d%n"
                    + "Delta=%d%n",
                    DigestStore.getPath(file),
                    count0,
                    count1,
                    count0 - count1
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
