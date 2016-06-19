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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

/**
 *
 * @author donizyo
 */
public class BOMReader extends BufferedReader {

    public static final String DEFAULT_ENCODING = "UTF-8";

    public BOMReader(File file) throws IOException {
        this(file, DEFAULT_ENCODING);
    }

    private BOMReader(File file, String encoding) throws IOException {
        this(new FileInputStream(file), encoding);
    }

    private BOMReader(FileInputStream input, String encoding) throws IOException {
        this(new BOMInputStream(input), encoding);
    }

    private BOMReader(BOMInputStream input, String encoding) throws IOException {
        super(new InputStreamReader(input, getCharset(input, encoding)));
    }

    private static String getCharset(BOMInputStream bomInput, String encoding) throws IOException {
        ByteOrderMark bom;

        bom = bomInput.getBOM();
        return bom == null ? encoding : bom.getCharsetName();
    }
}
