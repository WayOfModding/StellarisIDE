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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author donizyo
 */
public class Patterns {

    private static final String SP_INTEGER = "\\d*";
    private static final String SP_FLOAT = "\\d*(\\.\\d*)?";
    public static final Patterns PS_RANGE
            = compile("min", "=", SP_INTEGER, "max", "=", SP_INTEGER, "\\}");
    public static final Patterns PS_COLOR_HSV
            = compile("\\{", SP_FLOAT, SP_FLOAT, SP_FLOAT, "\\}");
    public static final Patterns PS_COLOR_RGB
            = compile("\\{", SP_INTEGER, SP_INTEGER, SP_INTEGER, "\\}");

    private final Pattern[] patterns;

    private Patterns(Pattern[] ps) {
        patterns = ps;
    }

    public static Patterns compile(String... patterns) {
        int len;
        String str;
        Pattern[] ps;
        int i;

        len = patterns.length;
        ps = new Pattern[len];
        for (i = 0; i < len; i++) {
            str = patterns[i];
            ps[i] = Pattern.compile(str);
        }

        return new Patterns(ps);
    }

    public boolean matches(List<String> input) {
        return matches(input, null);
    }

    public boolean matches(List<String> input, List<String> output) {
        Pattern p;
        Matcher m;
        String s;
        int i;
        int len;

        if (input == null) {
            throw new NullPointerException();
        }
        len = input.size();
        if (len != patterns.length) {
            return false;
        }
        for (i = 0; i < len; i++) {
            s = input.get(i);
            if (s == null) {
                return false;
            }
            p = patterns[i];
            m = p.matcher(s);
            if (!m.matches()) {
                //System.out.format("Unmatch @[%d]:%s%n", i, s);
                return false;
            }
            m = p.matcher(s);
            if (output != null) {
                m.find();
                output.add(m.group());
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb;
        boolean first;

        sb = new StringBuilder();
        first = true;
        for (Pattern p : patterns) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(p.pattern());
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        String s = "{, 142, 188, 241, }";
        String[] ss = s.split(", ");
        List<String> l = Arrays.asList(ss);
        System.out.format("Matches: %b%n", PS_COLOR_RGB.matches(l));
    }

}
