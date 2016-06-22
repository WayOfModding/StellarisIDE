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
package com.stellaris.script;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.script.ScriptEngine;

/**
 *
 * @author donizyo
 * @param <T>
 */
public class ScriptList<T extends ScriptValue>
        extends ScriptValue implements List<T> {

    private final List<T> list;

    public ScriptList() {
        this(new LinkedList<>());
    }

    protected ScriptList(List<T> list) {
        super();
        this.list = list;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean add(T e) {
        return list.add(e);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        return list.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public void clear() {
        list.clear();
    }

    public T get(int index) {
        return list.get(index);
    }

    public T set(int index, T element) {
        return list.set(index, element);
    }

    public void add(int index, T element) {
        list.add(index, element);;
    }

    public T remove(int index) {
        return list.remove(index);
    }

    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    public List<T> subList(int fromIndex, int toIndex) {
        return list.subList(fromIndex, toIndex);
    }

    public void parse(String s) {
        throw new UnsupportedOperationException();
    }
}
