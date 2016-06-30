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
package com.stellaris.test;

import java.io.PrintStream;

/**
 *
 * @author donizyo
 */
public class Debug {

    public static PrintStream out = System.out;
    public static PrintStream err = System.err;
    public static boolean DEBUG = false;
    public static boolean DEBUG_REFRESH = false;
    public static boolean DEBUG_FILL = false;
    public static boolean DEBUG_NEXT = false;
    public static boolean DEBUG_CACHE = false;
    public static boolean DEBUG_DISCARD = false;
    public static boolean ACCEPT_COMMENT = false;
    public static boolean DEBUG_FIELD = false;

}
