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

import com.stellaris.test.Debug;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author donizyo
 */
public class Digest {

    public static final String DEFAULT_ALGORITHM = "MD5";
    private byte[] result;

    public Digest(File file) {
        this(file, DEFAULT_ALGORITHM);
    }

    public Digest(File file, String algorithm) {
        this(file, newMessageDigest(algorithm));
    }

    private static MessageDigest newMessageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Digest(File file, Checksum cs) {
        int bufsize;
        byte[] buffer;
        int len;

        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException();
        }
        bufsize = 1024;
        buffer = new byte[bufsize];
        try (FileInputStream finput = new FileInputStream(file);) {
            while ((len = finput.read(buffer)) > 0) {
                cs.update(buffer, 0, len);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Digest(File file, MessageDigest md) {
        int bufsize;
        byte[] buffer;

        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException();
        }
        bufsize = 1024;
        buffer = new byte[bufsize];
        try (FileInputStream finput = new FileInputStream(file);
                DigestInputStream dinput = new DigestInputStream(finput, md);) {
            while (dinput.read(buffer) > 0);
            result = md.digest();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String toString(byte[] array) {
        return DatatypeConverter.printHexBinary(array);
    }

    public Digest(String path) {
        this(new File(path));
    }

    public byte[] getResult() {
        return result;
    }

    public String digest() {
        String digest;

        digest = toString(result);
        return digest;
    }

    public String toString() {
        return digest();
    }

    public static void main(String[] args) {
        String path;
        Digest digest;
        String result;
        MessageDigest md;

        if (args.length < 1) {

            return;
        }
        path = args[0];
        digest = new Digest(path);
        result = digest.digest();
        Debug.out.format("%s checksum of file \"%s\":\"%s\"%n",
                Digest.DEFAULT_ALGORITHM, path, result);
    }
}
