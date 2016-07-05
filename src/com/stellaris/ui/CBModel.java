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
package com.stellaris.ui;

import com.stellaris.mod.ModLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author donizyo
 */
public class CBModel
        extends javax.swing.AbstractListModel<String>
        implements javax.swing.ComboBoxModel<String> {

    private final List<String> listLocal, listRemote;
    private List<String> list;
    private Map<String, ModLoader> map;
    private String selectedItem;
    private final Lock lock;

    public CBModel(String pathHome) {
        LinkedList<ModLoader> local;
        LinkedList<ModLoader> remote;
        int len0, len1;
        String str;

        local = new LinkedList<>();
        remote = new LinkedList<>();
        ModLoader.getModLoaders(pathHome, local, remote);
        len0 = local.size();
        len1 = remote.size();
        list = listLocal = new ArrayList<>(len0);
        for (ModLoader loader : local) {
            str = loader.toString();
            listLocal.add(str);
            map.put(str, loader);
        }
        listRemote = new ArrayList<>(len0 + len1);
        listRemote.addAll(listLocal);
        for (ModLoader loader : remote) {
            str = loader.toString();
            listRemote.add(str);
            map.put(str, loader);
        }
        lock = new ReentrantLock();
    }

    public void setFilterMode(boolean val) {
        try {
            lock.lock();
            if (val) {
                list = listRemote;
            } else {
                list = listLocal;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setSelectedItem(Object anItem) {
        String selected;

        try {
            lock.lock();
            selected = selectedItem;
            if (selected != null && !selected.equals(anItem)
                    || selected == null && anItem != null
                    && anItem instanceof String) {
                selected = (String) anItem;
                selectedItem = selected;
                fireContentsChanged(selected, -1, -1);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object getSelectedItem() {
        try {
            lock.lock();
            return selectedItem;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getSize() {
        try {
            lock.lock();
            return list.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getElementAt(int index) {
        try {
            lock.lock();
            return list.get(index);
        } finally {
            lock.unlock();
        }
    }
}
