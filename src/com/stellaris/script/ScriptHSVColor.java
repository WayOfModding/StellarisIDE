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

import com.stellaris.Type;
import javax.script.ScriptEngine;

/**
 *
 * @author donizyo
 */
public class ScriptHSVColor extends ScriptColor {

    private float h, s, v, a;

    public ScriptHSVColor() {
        super();
    }

    public ScriptHSVColor(float h, float s, float v) {
        this(h, s, v, 0.0f);
    }

    public ScriptHSVColor(float h, float s, float v, float a) {
        this();
        this.h = h;
        this.s = s;
        this.v = v;
        this.a = a;
    }

    public String getColorSpace() {
        return COLOR_SPACE_HSV;
    }

    protected Type getType() {
        return Type.COLOR;
    }

    public String toString() {
        StringBuilder sb;
        String colorSpace;

        sb = new StringBuilder();
        colorSpace = getColorSpace();
        sb.append(colorSpace);
        sb.append(" { ");
        sb.append(h);
        sb.append(' ');
        sb.append(s);
        sb.append(' ');
        sb.append(v);
        sb.append(" }");
        return sb.toString();
    }
}
