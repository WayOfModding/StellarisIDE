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

import com.stellaris.TokenException;
import com.stellaris.util.Digest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.*;

/**
 *
 * @author donizyo
 */
public class ChecksumManifest {

    private static final int DEFAULT_INITSIZE = 16;
    private static final String DEFAULT_FILENAME = "checksum_manifest.txt";

    private String scan(String line, Pattern pattern, String skey) {
        Matcher matcher;
        String key, value;

        if (line == null) {
            throw new NullPointerException();
        }
        matcher = pattern.matcher(line);
        if (!matcher.find()) {
            throw new TokenException();
        }
        key = matcher.group(1);
        if (!key.equals(skey)) {
            throw new TokenException(key);
        }
        value = matcher.group(2);

        return value;
    }

    private Digest digest(File file, Checksum checksum) {
        Digest digest;

        if (checksum == null) {
            digest = new Digest(file);
        } else {
            digest = new Digest(file, checksum);
        }
        return digest;
    }

    private Digest digest(File file, String algorithm) {
        Digest digest;

        digest = new Digest(file, algorithm);
        return digest;
    }

    private void createFilter(Map<String, List<ChecksumEntry>> map,
            String type, String name, boolean recursive, String extension) {
        ChecksumEntry entry;
        List<ChecksumEntry> list;

        entry = new ChecksumEntry(recursive, extension);
        list = map.get(name);
        if (list == null) {
            list = new LinkedList<>();
            map.put(name, list);
        }
        list.add(entry);
    }

    private void parse(File file, Map<String, List<ChecksumEntry>> map) {
        String line;
        Pattern pattern;
        String value;
        // construct
        String type;
        String name;
        boolean recursive;
        String extension;

        pattern = Pattern.compile("(\\w+)\\s*=\\s*(\\.?\\w+)");
        try (FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);) {
            while (true) {
                // entry type
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains("directory")) {
                    type = line;
                } else {
                    throw new UnsupportedOperationException(line);
                }
                // name
                line = reader.readLine();
                name = scan(line, pattern, "name");
                // recursive
                line = reader.readLine();
                value = scan(line, pattern, "sub_directories");
                switch (value) {
                    case "yes":
                        recursive = true;
                        break;
                    case "no":
                        //recursive = false;
                        throw new UnsupportedOperationException();
                    default:
                        throw new AssertionError(value);
                }
                // extension
                line = reader.readLine();
                extension = scan(line, pattern, "file_extension");
                // empty line
                reader.readLine();

                createFilter(map, type, name, recursive, extension);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ChecksumEntry integrate(List<ChecksumEntry> list, boolean recursive) {
        ChecksumEntry res;
        Set<String> exts;

        exts = new HashSet<>(DEFAULT_INITSIZE);
        for (ChecksumEntry entry : list) {
            //if (entry.getRecursive() != recursive) continue;
            exts.add(entry.getExtension());
        }

        res = new ChecksumEntry(recursive, exts);
        return res;
    }

    private void integrate(Map<String, List<ChecksumEntry>> map,
            Map<String, ChecksumEntry> out) {
        List<ChecksumEntry> list;
        Set<String> keyset;

        if (out == null) {
            return;
        }
        keyset = map.keySet();
        for (String key : keyset) {
            list = map.get(key);
            if (list == null) {
                continue;
            }
            out.put(key, integrate(list, true));
            //out.add(integrate(list, false));
        }
    }

    public void load(String path) {
        File root;
        File file;
        Map<String, List<ChecksumEntry>> map;
        Map<String, ChecksumEntry> entries;

        root = new File(path);
        map = new HashMap<>(DEFAULT_INITSIZE);
        entries = new HashMap<>(DEFAULT_INITSIZE);
        // load checksum_manifest.txt
        file = new File(root, DEFAULT_FILENAME);
        if (!file.isFile()) {
            return;
        }
        parse(file, map);
        // generate file filters
        integrate(map, entries);
        // dig into root directory
        filter(root, entries);
    }

    private void filter(File root, Map<String, ChecksumEntry> map) {
        Set<String> keyset;
        File file;
        ChecksumEntry entry;
        FileFilter filter;
        Queue<File> dirs;
        Queue<File> files;
        File dir;
        Checksum checksum;
        String algorithm;
        Digest result;

        keyset = map.keySet();
        //checksum = new CRC32();
        //checksum = new Adler32();
        //checksum = null;
        checksum = new BSD();
        algorithm = "SHA-512";
        result = null;
        for (String key : keyset) {
            file = new File(root, key);
            if (!file.isDirectory()) {
                throw new AssertionError(key);
            }
            entry = map.get(key);
            if (entry == null) {
                throw new NullPointerException();
            }
            if (!entry.isRecursive()) {
                throw new UnsupportedOperationException();
            }
            filter = (FileFilter) entry;
            file.listFiles(filter);
            dirs = entry.getDirs();
            while (!dirs.isEmpty()) {
                // fill 'files' queue
                dir = dirs.remove();
                // check isRecursive()
                dir.listFiles(filter);
                // retrieve 'files' queue
                files = entry.getFiles();
                while (!files.isEmpty()) {
                    // retrieve a file
                    file = files.remove();
                    // digest a file
                    if (checksum == null && algorithm != null) {
                        result = digest(file, algorithm);
                    } else {
                        result = digest(file, checksum);
                    }
                }
            }
        }

        if (checksum == null) {
            System.out.format("%s=\"%s\"%n",
                    algorithm == null ? Digest.DEFAULT_ALGORITHM : algorithm,
                    result.digest().toLowerCase());
        } else {
            // test output
            long value = checksum.getValue();
            //value &= 0xffff;
            System.out.format("%s=\"%x\"%n",
                    checksum.getClass().getSimpleName(), value);
        }
    }

    public static void main(String[] args) {
        String path;
        ChecksumManifest cm;

        if (args.length < 1) {
            return;
        }
        path = args[0];
        cm = new ChecksumManifest();
        cm.load(path);
    }
}
