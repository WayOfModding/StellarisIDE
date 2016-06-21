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
import java.io.StringReader;
import javax.script.*;

/**
 *
 * @author donizyo
 */
public class SimpleScriptEngine implements ScriptEngine {

    private ScriptContext context;

    public SimpleScriptEngine() {
        context = new SimpleScriptContext();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(new StringReader(script), context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        Bindings bindings;

        bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        return eval(reader, bindings);
    }

    @Override
    public Object eval(String script) throws ScriptException {
        Bindings bindings;

        bindings = getBindings(ScriptContext.ENGINE_SCOPE);
        return eval(script, bindings);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        Bindings bindings;

        bindings = getBindings(ScriptContext.ENGINE_SCOPE);
        return eval(reader, bindings);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        return eval(new StringReader(script), bindings);
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void put(String key, Object value) {
        Bindings bindings;

        bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put(key, value);
    }

    @Override
    public Object get(String key) {
        Bindings bindings;
        Object value;

        bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
        value = bindings.get(key);
        if (value != null) {
            return value;
        }
        bindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        value = bindings.get(key);
        return value;
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        context.setBindings(bindings, scope);
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(ScriptContext context) {
        this.context = context;
    }

    @Override
    public ScriptEngineFactory getFactory() {
        throw new UnsupportedOperationException();
    }

}
