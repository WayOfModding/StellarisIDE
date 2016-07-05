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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 *
 * @author donizyo
 */
public class WinUtils {

    public static <T extends Window> T mediateWindow(T window) {
        int x, y, w, h;
        Toolkit toolkit;
        Dimension dimension;
        int sw, sh;

        if (window == null) {
            throw new NullPointerException();
        }
        w = window.getWidth();
        h = window.getHeight();
        toolkit = Toolkit.getDefaultToolkit();
        dimension = toolkit.getScreenSize();
        sw = dimension.width;
        sh = dimension.height;
        x = (sw - w) / 2;
        y = (sh - h) / 2;
        window.setLocation(x, y);

        return window;
    }
}
