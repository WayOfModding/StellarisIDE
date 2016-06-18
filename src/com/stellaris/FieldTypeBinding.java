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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author donizyo
 */
public class FieldTypeBinding {

    private static final String DEFAULT_FILE = "fieldtype.ini";
    private static final Comparator<Type> DEFAULT_COMPARATOR = new TypeComparator();
    private final Map<Field, SortedSet<Type>> map;

    public FieldTypeBinding() {
        map = new HashMap<>();
    }

    public SortedSet<Type> getAll(Field field) {
        if (field == null) {
            return null;
        }
        return map.get(field);
    }

    public Type get(Field field) {
        SortedSet<Type> set;

        if (field == null) {
            return null;
        }
        set = getAll(field);
        if (set == null) {
            return null;
        }
        return set.first();
    }

    public void putAll(FieldTypeBinding ftb) {
        Map<Field, SortedSet<Type>> _map;
        Set<Field> keyset;
        SortedSet<Type> v0, v1;

        _map = ftb.map;
        keyset = _map.keySet();
        for (Field field : keyset) {
            v0 = map.get(field);
            v1 = _map.get(field);
            if (v0 == null) {
                map.put(field, v1);
            } else {
                v0.addAll(v1);
            }
        }
    }

    public boolean put(Field field, Type type) {
        SortedSet<Type> set;

        if (field == null) {
            return false;
        }
        if (type == null) {
            return false;
        }
        set = map.get(field);
        if (set == null) {
            set = new TreeSet<>(DEFAULT_COMPARATOR);
            map.put(field, set);
        }
        return set.add(type);
    }

    /**
     * Clean up the binding map, remove succeeding elements in the sorted set
     */
    public void clean() {
        Set<Field> keyset;
        Comparator<Type> comp;
        SortedSet<Type> set;
        Type first;

        keyset = map.keySet();
        comp = DEFAULT_COMPARATOR;
        for (Field field : keyset) {
            set = map.get(field);
            first = set.first();
            set = new TreeSet<>(comp);
            set.add(first);
            map.put(field, set);
        }
    }

    private Properties toProperties() {
        Properties prop;
        Set<Field> keyset;
        SortedSet<Type> set;
        String strKey, strVal;

        prop = new Properties();
        keyset = map.keySet();
        for (Field field : keyset) {
            try {
                set = map.get(field);
                strKey = field.toString();
                strVal = set.toString();
                prop.put(strKey, strVal);
            } catch (NullPointerException | NoSuchElementException ex) {
                throw new AssertionError(ex);
            }
        }
        return prop;
    }

    private Field findField(String str) {
        Set<Field> keyset;
        String strField;

        if (str == null) {
            return null;
        }
        keyset = map.keySet();
        for (Field field : keyset) {
            strField = field.toString();
            if (strField.equals(str)) {
                return field;
            }
        }
        return null;
    }

    private Field createField(String str) {
        String strParent;
        String strName;
        int index;
        int len;
        Field parent;
        Field field;

        index = str.lastIndexOf(Field.SEPERATOR);
        len = str.length();
        if (index == -1) {
            strName = str;
            parent = null;
        } else {
            strParent = str.substring(0, index);
            strName = str.substring(index + 1, len);
            parent = getField(strParent);
        }
        field = new Field(parent, strName);
        return field;
    }

    private Field getField(String str) {
        Field field;

        field = findField(str);
        if (field == null) {
            field = createField(str);
            map.put(field, null);
        }
        return field;
    }

    private void fromProperties(Properties prop) {
        Set<String> keyset;
        SortedSet<Type> set;
        Object objVal;
        String strVal;
        int lenVal;
        String[] types;
        Type val;
        Field field;

        keyset = (Set) prop.keySet();
        for (String strKey : keyset) {
            try {
                objVal = prop.get(strKey);
                strVal = (String) objVal;
                lenVal = strVal.length();
                strVal = strVal.substring(1, lenVal - 1);
                types = strVal.split(", ");
                set = new TreeSet<>(DEFAULT_COMPARATOR);
                for (String type : types) {
                    val = Type.getType(type);
                    set.add(val);
                }
                field = getField(strKey);
                map.put(field, set);
            } catch (NullPointerException | NoSuchElementException ex) {
                throw new AssertionError(ex);
            }
        }
    }

    private static File getDefaultFile() {
        String userdir;
        File file;

        userdir = System.getProperty("user.dir");
        file = new File(userdir, DEFAULT_FILE);
        return file;
    }

    private Set<String> getFieldNameSet() {
        Set<Field> keyset;
        Set<String> set;
        String name;

        keyset = map.keySet();
        set = new HashSet<>();
        for (Field field : keyset) {
            name = field.getName();
            set.add(name);
        }

        return set;
    }

    public static void store(FieldTypeBinding ftb) {
        File file;
        Properties prop;
        Set<String> nameSet;
        int nameCount;
        String comment;

        file = getDefaultFile();
        prop = ftb.toProperties();
        nameSet = ftb.getFieldNameSet();
        nameCount = nameSet.size();
        comment = String.format("Field Type Binding%nName-count: %d%n", nameCount);
        try (FileOutputStream out = new FileOutputStream(file);) {
            prop.store(out, comment);
        } catch (IOException ex) {
            Logger.getLogger(FieldTypeBinding.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static FieldTypeBinding load() {
        File file;
        Properties prop;
        FieldTypeBinding res;

        file = getDefaultFile();
        prop = new Properties();
        try (FileInputStream in = new FileInputStream(file);) {
            prop.load(in);
            res = new FieldTypeBinding();
            res.fromProperties(prop);
        } catch (IOException ex) {
            Logger.getLogger(FieldTypeBinding.class.getName()).log(Level.SEVERE, null, ex);
            res = null;
        }

        return res;
    }

    private static class TypeComparator implements Comparator<Type> {

        @Override
        public int compare(Type o1, Type o2) {
            return o2.ordinal() - o1.ordinal();
        }
    }
}
