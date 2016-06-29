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
package com.stellaris.io;

import java.io.*;
import java.nio.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author donizyo
 */
public class AbstractParser implements AutoCloseable {

    private Reader reader;
    private CharBuffer buffer;
    private CharBuffer bupher;
    private int line;
    private boolean isEOF;

    protected AbstractParser(Reader in) throws IOException {
        this(in, 65536);
    }

    protected AbstractParser(Reader in, int bufferSize) throws IOException {
        CharBuffer buf;

        reader = in;
        buf = CharBuffer.allocate(bufferSize);
        buffer = buf;
        fillBuffer(buf);
        bupher = CharBuffer.allocate(bufferSize);
        line = 0;
        isEOF = false;
    }

    public final void setReader(Reader in) throws IOException {
        int limit;
        int cap;

        if (in == null) {
            return;
        }
        close();
        reader = in;
        cap = buffer.capacity();
        buffer.rewind();
        limit = buffer.limit();
        if (limit != cap) {
            buffer.limit(cap);
        }
        cap = bupher.capacity();
        bupher.rewind();
        limit = bupher.limit();
        if (limit != cap) {
            bupher.limit(cap);
        }
        line = 0;
        isEOF = false;
    }

    private CharBuffer swapBuffer() {
        CharBuffer buf;

        buf = bupher;
        bupher = buffer;
        buffer = buf;
        return buf;
    }

    private int fillBuffer(CharBuffer buf) throws IOException {
        int nbits;

        if (buf == null) {
            throw new NullPointerException();
        }
        buf.rewind();
        nbits = reader.read(buf);
        buf.flip();
        return nbits;
    }

    protected final int getLineNumber() {
        return line;
    }

    protected final CharBuffer nextLine() throws IOException {
        CharBuffer buf;
        char c;
        int pos;
        int src;
        int dst;
        boolean isNewLine;
        boolean isSwapped;
        boolean isFull;
        int len;
        int cap;
        char[] hb;
        char[] tmp;
        int nbits;
        CharBuffer res;

        buf = buffer;
        if (buf == null) {
            throw new AssertionError("null");
        }
        ++line;
        isNewLine = false;
        isSwapped = false;
        src = buf.position();
        pos = -1;
        c = 0;

        if (isEOF) {
            return null;
        }
        while (true) {
            while (buf.hasRemaining()) {
                c = buf.get();
                if (c == '\r') {
                    // CR + LF
                    pos = buf.position();
                    if (buf.hasRemaining()) {
                        buf.mark();
                        c = buf.get();
                        if (c != '\n') {
                            // tolerate "\r[^\n]"
                            buf.reset();
                        }
                        isNewLine = true;
                        break;
                    }
                    break;
                } else if (c == '\n') {
                    // LF
                    pos = buf.position();
                    isNewLine = true;
                    break;
                }
            }
            if (isNewLine || isEOF) {
                if (isEOF) {
                    dst = buf.limit();
                } else {
                    // NEWLINE is found
                    dst = pos - 1;
                }
                if (dst < 0) {
                    throw new AssertionError(
                            String.format("Invalid NEWLINE index: %d", dst)
                    );
                }
                if (isSwapped) {
                    // swap back to the first buffer
                    buf = swapBuffer();
                    hb = buf.array();
                    len = hb.length;
                    if (pos == len) {
                        // if '\r' is at the end of the first buffer
                        // while '\n' is at the beginning of the second;
                        // then we don't need to swap back to the second buffer
                        len = dst - src;
                        res = CharBuffer.wrap(hb, src, len);
                        // swap back to the second buffer
                        // because the first is empty
                        swapBuffer();
                    } else if (dst == 0) {
                        // if "\r\n" is at the beginning of the second;
                        // then we don't need to swap back to the second buffer
                        len -= src;
                        res = CharBuffer.wrap(hb, src, len);
                        // swap back to the second buffer
                        // because the first is empty
                        swapBuffer();
                    } else {
                        // there are printable characters in the second buffer
                        len -= src;
                        cap = len + dst;
                        tmp = new char[cap];
                        System.arraycopy(hb, src, tmp, 0, len);
                        // clean up the first buffer
                        buf.rewind();

                        // swap back to the second buffer
                        buf = swapBuffer();
                        hb = buf.array();
                        System.arraycopy(hb, 0, tmp, len, dst);
                        res = CharBuffer.wrap(tmp, 0, cap);
                    }
                } else {
                    hb = buf.array();
                    len = dst - src;
                    res = CharBuffer.wrap(hb, src, len);
                }
                res.mark();
                return res;
            }
            if (!buf.hasRemaining()) {
                if (isSwapped) {
                    // fail to find NEWLINE throughout the second buffer
                    // try to skip the current line
                    while (true) {
                        nbits = fillBuffer(buf);
                        if (nbits < 0) {
                            break;
                        }
                        while (buf.hasRemaining()) {
                            c = buf.get();
                            if (c == '\n') {
                                break;
                            }
                        }
                    }
                    // with the current line skipped or not
                    // return the first buffer
                    buf = swapBuffer();
                    hb = buf.array();
                    len = hb.length;
                    len -= src;
                    res = CharBuffer.wrap(hb, src, len);
                    // hit EOF, return null
                    res.mark();
                    return res;
                }
                // fail to find NEWLINE throughout the first buffer
                buf = swapBuffer();
                // maintain bytes in the first buffer
                // and fill the second buffer to locate NEWLINE
                nbits = fillBuffer(buf);
                isFull = nbits == buf.capacity();
                if (nbits < 0) {
                    if (c == 0) {
                        // no printable characters in the first buffer
                        return null;
                    }
                    // EOF is in the first buffer
                    isEOF = true;
                    buf = swapBuffer();
                    continue;
                } else if (!isFull) {
                    // EOF is in the second buffer
                    isEOF = true;
                }
                isSwapped = true;
                continue;
            }
            throw new AssertionError("Unexpected situation");
        }
    }

    protected final String nextLineString() throws IOException {
        CharBuffer buf;
        String res;

        buf = nextLine();
        if (buf == null) {
            res = null;
        } else {
            res = buf.toString();
        }
        return res;
    }

    @Override
    public final void close() throws IOException {
        Reader in;

        in = reader;
        if (in != null) {
            in.close();
            reader = null;
        }
    }

    public static void main(String[] args) {
        String root, path;
        File file;
        AbstractParser parser;
        String line;
        int num, len;

        if (args.length < 2) {
            return;
        }
        root = args[0];
        path = args[1];
        file = new File(root, path);
        try (Reader reader = new FileReader(file);) {
            parser = new AbstractParser(reader);
            while (true) {
                line = parser.nextLineString();
                if (line == null) {
                    break;
                }
                num = parser.line;
                len = line.length();
                System.out.format("Line %4d:%-4d> %s%n", num, len, line);
            }
        } catch (IOException ex) {
            Logger.getLogger(AbstractParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
