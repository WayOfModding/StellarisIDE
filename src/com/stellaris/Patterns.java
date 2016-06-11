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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author donizyo
 */
public class Patterns {

    public static final Patterns PS_RANGE
            = compile("min", "=", "\\d*", "max", "=", "\\d*", "\\}");

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

    public boolean matches(String[] input) {
        Pattern p;
        Matcher m;
        String s;
        int i;
        int len;

        len = input.length;
        if (len != patterns.length) {
            return false;
        }
        for (i = 0; i < len; i++) {
            p = patterns[i];
            s = input[i];
            m = p.matcher(s);
            if (!m.matches()) {
                //System.out.format("Unmatch @[%d]:%s%n", i, s);
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String s = "min = 0 max = 3 }";
        String[] ss = s.split(" ");
        System.out.format("Matches: %b%n", PS_RANGE.matches(ss));
    }

}
