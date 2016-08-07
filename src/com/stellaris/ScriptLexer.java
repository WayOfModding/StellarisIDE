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

import com.stellaris.io.AbstractLexer;
import com.stellaris.test.Debug;
import java.io.*;
import java.nio.*;
import java.util.*;
import static com.stellaris.test.Debug.*;
import com.stellaris.util.BOMReader;
import com.stellaris.util.DigestStore;
import com.stellaris.util.ScriptPath;

/**
 *
 * @author donizyo
 */
public final class ScriptLexer extends AbstractLexer {

    private static final int BUFFER_SIZE = 65536;
    private static final int CACHE_SIZE = 3;

    private LinkedList<Token> queue;
    private SortedMap<Integer, Queue<Token>> map;
    private int cl, cr;

    public ScriptLexer(File file) throws IOException {
        this(new BOMReader(file));
    }

    public ScriptLexer(Reader in) throws IOException {
        super(in instanceof BufferedReader
                ? (BufferedReader) in
                : new BufferedReader(in),
                BUFFER_SIZE);
        queue = new LinkedList<>();
        map = new TreeMap<>();
        cl = 0;
        cr = 0;
    }

    public void skipCurrentLine() {
        int idx;
        Queue<Token> q;
        SortedMap<Integer, Queue<Token>> m;
        Queue<Token> p;

        Debug.err.format("[INFO]\tSkip current line!%n");

        m = map;
        idx = m.firstKey();
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
        Queue<Token> q;
        Map<Integer, Queue<Token>> m;
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

    public List<Token> peekToken(int count)
            throws IOException, TokenException {
        List<Token> res;
        List<Token> q;
        int size;

        q = queue;
        cache(count);
        size = q.size();
        if (size < count) {
            count = size;
        }
        res = q.subList(0, count);
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
        Queue<Token> q;
        Token token;

        if (DEBUG && DEBUG_DISCARD) {
            Debug.err.format("[DSCD]\tcount=%d%n", count);
        }
        i = 0;
        q = queue;
        while (i++ < count) {
            token = q.remove();
            cleanLine(token);
            if (DEBUG && DEBUG_DISCARD) {
                Debug.err.format("[DSCD]\tstr=\"%s\"%n\tcache=%d %s%n",
                        token.token, q.size(), q.toString()
                );
            }
        }
    }
    
    private void cleanLine(Token token) {
        int idx;
        SortedMap<Integer, Queue<Token>> m;
        Queue<Token> l;
        
        if (token == null)
            throw new NullPointerException("cleanLine");
        idx = token.line;
        // remove token in list
        m = map;
        l = m.get(idx);
        l.remove(token);
        // remove int-list binding from map
        if (l.isEmpty())
            m.remove(idx);
    }

    /**
     * Get next token
     *
     * @return
     * @throws java.io.IOException
     */
    public Token nextToken() throws IOException, NoSuchElementException {
        Queue<Token> q;
        Token res;
        int lineNumber;

        if (!hasNextToken()) {
            throw new NoSuchElementException();
        }
        q = queue;
        res = q.remove();
        lineNumber = res.line;
        cleanLine(res);
        if (DEBUG && DEBUG_NEXT) {
            Debug.err.format("[NEXT]\tline=%d, next=\"%s\"%n\tcache=%d %s%n",
                    lineNumber, res.token, q.size(), q.toString()
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
        int i;

        if (src == dst) {
            throw new AssertionError("Empty string");
        }
        len = dst - src;
        buf = charBuffer.array();
        for (i = 0; i < len; i++) {
            c = buf[src + i];
            if (c == '{') {
                ++cl;
                i = -1;
                break;
            } else if (c == '}') {
                ++cr;
                i = -1;
                break;
            }
        }
        str = new String(buf, src, len);
        if (i == -1 && len != 1)
            throw new AssertionError(
                    String.format("[WARN] Unexpected token: \"%s\"%n", str)
            );
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
            Debug.err.format("[LINE]\t%4d - %2d>\t%s%n",
                    getLineNumber(),
                    buf.remaining(),
                    buf);
        }
    }

    private static String compact(CharBuffer buf) {
        CharBuffer tmp;
        char c;
        boolean isString;

        tmp = CharBuffer.allocate(buf.remaining());
        buf.mark();
        isString = false;
        while (buf.hasRemaining()) {
            c = buf.get();
            if (c == '"') {
                isString = !isString;
            }
            if (Character.isWhitespace(c) && !isString) {
                continue;
            }
            tmp.put(c);
        }
        buf.reset();
        tmp.flip();
        return tmp.toString();
    }

    private boolean tokenize(Queue<Token> q, Map<Integer, Queue<Token>> m)
            throws IOException, TokenException {
        char c;
        int src, dst, pos;
        String res;
        Token token;
        int lineNumber;
        boolean isComment;
        boolean isString;
        CharBuffer buf;
        Queue<Token> p;
        String line;
        StringBuilder sb;
        String lex;

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
        line = compact(buf);
        sb = new StringBuilder();
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

            sb.append(res);
            if (!isComment || Debug.ACCEPT_COMMENT) {
                token = new Token(res, lineNumber);
                if (!q.add(token) || !p.add(token)) {
                    throw new IllegalStateException("Fail to add new token");
                }
            }
        } while (skipLeadingWhitespace(buf));

        lex = sb.toString();
        if (!lex.equals(line) && !isComment) {
            throw new AssertionError(
                    String.format(
                            "Lexical analysis exception @ %d:%n"
                            + "\tLine:  %s%n"
                            + "\tToken: %s%n",
                            lineNumber,
                            line,
                            lex
                    )
            );
        }

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
        int total;
        
        super.close();
        total = cl - cr;
        //System.out.format("Closing lexer (%d, %d, %d)...%n", cl, cr, total);
        if (total != 0) {
            throw new TokenException(
                    String.format(
                            "Unmatching bracket pairs: %d('{'=%d, '}'=%d)",
                            total, cl, cr
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
        try (ScriptLexer parser = new ScriptLexer(file);) {
            int cl, clt; // count {
            int cr, crt; // count }
            CharBuffer buffer;
            char c;

            cl = cr = clt = crt = 0;
            mainloop:
            while ((buffer = parser.nextLine()) != null) {
                //buffer.mark();
                while (buffer.hasRemaining()) {
                    c = buffer.get();
                    switch (c) {
                        case '#':
                            while (buffer.hasRemaining()) {
                                c = buffer.get();
                                if (c == '{') {
                                    ++clt;
                                } else if (c == '}') {
                                    ++crt;
                                }
                            }
                            break;
                        case '{':
                            ++cl;
                            break;
                        case '}':
                            ++cr;
                            break;
                    }
                }
            }
            clt += cl;
            crt += cr;
            Debug.out.format("File=\"%s\"%n"
                    + "Count['{']=%d / %d%n"
                    + "Count['}']=%d / %d%n"
                    + "Delta=%d%n",
                    ScriptPath.getPath(file),
                    cl, clt,
                    cr, crt,
                    cl - cr
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
