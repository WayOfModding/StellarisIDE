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

import javax.script.ScriptEngine;

/**
 *
 * @author donizyo
 */
public class ScriptRGBColor extends ScriptColor {

    private int r, g, b;

    public ScriptRGBColor(ScriptEngine engine) {
        super(engine);
    }

    public ScriptRGBColor(ScriptEngine engine, int r, int g, int b) {
        this(engine);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public String getColorSpace() {
        return COLOR_SPACE_RGB;
    }

    public String toString() {
        StringBuilder sb;
        String colorSpace;

        sb = new StringBuilder();
        colorSpace = getColorSpace();
        sb.append(colorSpace);
        sb.append(" { ");
        sb.append(r);
        sb.append(' ');
        sb.append(g);
        sb.append(' ');
        sb.append(b);
        sb.append(" }");
        return sb.toString();
    }
}