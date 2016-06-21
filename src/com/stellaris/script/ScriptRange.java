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

import java.io.Reader;
import javax.script.ScriptEngine;

/**
 *
 * @author donizyo
 */
public class ScriptRange extends ScriptValue {

    private int minValue, maxValue;

    public ScriptRange(ScriptEngine engine) {
        super(engine);
    }

    public ScriptRange(ScriptEngine engine, int min, int max) {
        this(engine);
        minValue = min;
        maxValue = max;
    }

    public void set(int min, int max) {
        minValue = min;
        maxValue = max;
    }

    public int getMin() {
        return minValue;
    }

    public int getMax() {
        return maxValue;
    }

    public void parse(String s) {
        throw new UnsupportedOperationException();
    }
}
