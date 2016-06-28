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

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author donizyo
 */
public abstract class Patterns {

    public static final Patterns PS_RANGE = new Patterns() {
        public boolean matches(List<String> input, List<String> output) {
            int size;
            Iterator<String> itr;
            String str;

            size = input.size();
            if (size != 7) {
                throw new IllegalArgumentException(Integer.toString(size));
            }
            itr = input.iterator();

            str = itr.next();
            if (!"min".equals(str)) {
                return false;
            }

            str = itr.next();
            if (!"=".equals(str)) {
                return false;
            }

            str = itr.next();
            Integer.parseInt(str);
            if (output != null) {
                output.add(str);
            }

            str = itr.next();
            if (!"max".equals(str)) {
                return false;
            }

            str = itr.next();
            if (!"=".equals(str)) {
                return false;
            }

            str = itr.next();
            Integer.parseInt(str);
            if (output != null) {
                output.add(str);
            }

            str = itr.next();
            return "}".equals(str);
        }
    };

    public static final Patterns PS_COLOR_HSV = new Patterns() {
        public boolean matches(List<String> input, List<String> output) {
            int size;
            Iterator<String> itr;
            String str;
            int counter;
            float[] value;

            size = input.size();
            if (size != 6) {
                throw new IllegalArgumentException(Integer.toString(size));
            }
            itr = input.iterator();
            str = itr.next();
            if (!"{".equals(str)) {
                throw new TokenException();
            }
            counter = 0;
            value = new float[4];
            while (itr.hasNext()) {
                str = itr.next();
                if ("}".equals(str)) {
                    break;
                }
                try {
                    value[counter++] = Float.parseFloat(str);
                    if (output != null) {
                        output.add(str);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new IllegalStateException("Too many entries", ex);
                }
            }
            if (counter < 3) {
                throw new IllegalStateException("Too few entries");
            }
            return true;
        }
    };

    public static final Patterns PS_COLOR_RGB = new Patterns() {
        public boolean matches(List<String> input, List<String> output) {
            int size;
            Iterator<String> itr;
            String str;
            int counter;
            int[] value;

            size = input.size();
            if (size != 6) {
                throw new IllegalArgumentException(Integer.toString(size));
            }
            itr = input.iterator();
            str = itr.next();
            if (!"{".equals(str)) {
                throw new TokenException();
            }
            counter = 0;
            value = new int[4];
            while (itr.hasNext()) {
                str = itr.next();
                if ("}".equals(str)) {
                    break;
                }
                try {
                    value[counter++] = Integer.parseInt(str);
                    if (output != null) {
                        output.add(str);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new IllegalStateException("Too many entries", ex);
                }
            }
            if (counter < 3) {
                throw new IllegalStateException("Too few entries");
            }
            return true;
        }
    };

    private Patterns() {
    }

    public boolean matches(List<String> input) {
        return matches(input, null);
    }

    public abstract boolean matches(List<String> input, List<String> output);

}
