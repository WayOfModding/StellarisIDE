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

import java.io.*;
import java.util.*;
import javax.script.*;

/**
 *
 * @author donizyo
 */
public class SimpleFactory implements ScriptEngineFactory {

    private static final SimpleFactory DEFAULT_FACTORY;

    private String strEngineName;
    private String strEngineVersion;
    private List<String> listExtensions;
    private String strLanguageName;
    private String strLanguageVersion;
    private List<String> listNames;

    private final Bindings bindings;

    public static SimpleFactory getEngineFactory() {
        return DEFAULT_FACTORY;
    }

    protected SimpleFactory() {
        bindings = new SimpleBindings();
    }

    private Properties init() {
        Properties properties;
        String strExtensions;
        int len;
        String[] split;

        properties = new Properties();
        try (InputStream input
                = SimpleFactory.class.getResourceAsStream("ParadoxScript.ini");) {
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        strEngineName = properties.getProperty("EngineName", "StellarisIDE");

        strEngineVersion = properties.getProperty("EngineVersion");

        strLanguageName = properties.getProperty("LanguageName", "Clausewitz");

        strLanguageVersion = properties.getProperty("LanguageVersion");

        strExtensions = properties.getProperty("Extensions");
        if (strExtensions == null) {
            throw new NullPointerException();
        }
        len = strExtensions.length();
        strExtensions = strExtensions.substring(1, len - 1);
        split = strExtensions.split(", ");
        listExtensions = Arrays.asList(split);

        return properties;
    }

    public Bindings getBindings() {
        return bindings;
    }

    @Override
    public String getEngineName() {
        return strEngineName;
    }

    @Override
    public String getEngineVersion() {
        return strEngineVersion;
    }

    @Override
    public List<String> getExtensions() {
        return listExtensions;
    }

    @Override
    public List<String> getMimeTypes() {
        return null;
    }

    @Override
    public List<String> getNames() {
        if (listNames == null) {
            listNames = new ArrayList<>(1);
            listNames.add(strLanguageName);
        }
        return listNames;
    }

    @Override
    public String getLanguageName() {
        return strLanguageName;
    }

    @Override
    public String getLanguageVersion() {
        return strLanguageVersion;
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.NAME:
                return strEngineName;
            case ScriptEngine.ENGINE_VERSION:
                return strEngineVersion;
            case ScriptEngine.LANGUAGE:
                return strLanguageName;
            case ScriptEngine.ENGINE:
                return strLanguageName;
            case ScriptEngine.LANGUAGE_VERSION:
                return strLanguageVersion;
        }
        return bindings.get(key);
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuilder sb;
        int len1, len2, len;

        len1 = obj.length();
        len2 = obj.length();
        len = len1 + len2 + 1;
        sb = new StringBuilder(len);
        sb.append(obj);
        sb.append('.');
        sb.append(m);
        return sb.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        // TODO maybe i can try to invoke event?
        return toDisplay;
    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder sb;
        String newline;

        sb = new StringBuilder();
        newline = System.getProperty("line.separator", "\n");
        for (String statement : statements) {
            sb.append(statement).append(newline);
        }
        return sb.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new SimpleEngine(bindings);
    }

    static {
        DEFAULT_FACTORY = new SimpleFactory();
        DEFAULT_FACTORY.init();
    }

    public static void main(String[] args) {
    }
}
