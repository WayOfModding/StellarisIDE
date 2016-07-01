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
            String token;
            String str;

            size = input.size();
            if (size != 7) {
                return false;
            }
            itr = input.iterator();

            token = itr.next();
            if (!"min".equals(token)) {
                return false;
            }

            token = itr.next();
            if (!"=".equals(token)) {
                return false;
            }

            token = itr.next();
            try {
                Integer.parseInt(token);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (output != null) {
                output.add(token);
            }

            token = itr.next();
            if (!"max".equals(token)) {
                return false;
            }

            token = itr.next();
            if (!"=".equals(token)) {
                return false;
            }

            token = itr.next();
            try {
                Integer.parseInt(token);
            } catch (NumberFormatException ex) {
                return false;
            }
            if (output != null) {
                output.add(token);
            }

            token = itr.next();
            return "}".equals(token);
        }
    };

    public static final Patterns PS_COLOR_HSV = new Patterns() {
        public boolean matches(List<String> input, List<String> output) {
            int size;
            Iterator<String> itr;
            String token;
            String str;
            int counter;
            float[] value;

            size = input.size();
            if (size != 6) {
                throw new TokenException(Integer.toString(size));
            }
            itr = input.iterator();
            token = itr.next();
            if (!"{".equals(token)) {
                throw new TokenException(token);
            }
            counter = 0;
            value = new float[4];
            while (itr.hasNext()) {
                token = itr.next();
                if ("}".equals(token)) {
                    break;
                }
                try {
                    value[counter++] = Float.parseFloat(token);
                    if (output != null) {
                        output.add(token);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new TokenException("Too many entries");
                }
            }
            if (counter < 3) {
                throw new TokenException("Too few entries");
            }
            return true;
        }
    };

    public static final Patterns PS_COLOR_RGB = new Patterns() {
        public boolean matches(List<String> input, List<String> output) {
            int size;
            Iterator<String> itr;
            String token;
            String str;
            int counter;
            int[] value;

            size = input.size();
            if (size != 6) {
                throw new TokenException(Integer.toString(size));
            }
            itr = input.iterator();
            token = itr.next();
            if (!"{".equals(token)) {
                throw new TokenException();
            }
            counter = 0;
            value = new int[4];
            while (itr.hasNext()) {
                token = itr.next();
                if ("}".equals(token)) {
                    break;
                }
                try {
                    value[counter++] = Integer.parseInt(token);
                    if (output != null) {
                        output.add(token);
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    throw new TokenException("Too many entries");
                }
            }
            if (counter < 3) {
                throw new TokenException("Too few entries");
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
