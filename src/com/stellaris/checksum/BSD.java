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
package com.stellaris.checksum;

import java.nio.*;
import java.util.zip.Checksum;

/**
 *
 * @author donizyo
 */
public class BSD implements Checksum {

    private int value;

    public BSD() {
        value = 0;
    }

    // typedef unsigned short  jchar;
    // typedef long jint;
    // typedef __int64 jlong;
    // typedef signed char jbyte;
    @Override
    public void update(int b) {
        if ((value & 1) == 1) {
            value |= 0x10000;
        }
        value = ((value >> 1) + b) & 0xffff;
    }

    @Override
    public void update(byte[] b, int off, int len) {
        int i;
        int dst;

        dst = off + len;
        for (i = off; i < dst; i++) {
            update(b[i]);
        }
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public void reset() {
        value = 0;
    }

}
