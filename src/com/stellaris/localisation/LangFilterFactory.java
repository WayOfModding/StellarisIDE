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
package com.stellaris.localisation;

/**
 *
 * @author donizyo
 */
public class LangFilterFactory {

    public static final LangFilter LANG_ENGLISH;
    private static final LangFilterFactory FACTORY;

    public LangFilter getFilter(String langName, String langId) {
        return new LangFilter(langName, langId);
    }

    public static LangFilterFactory getFactory() {
        return FACTORY;
    }

    static {
        FACTORY = new LangFilterFactory();
        LANG_ENGLISH = FACTORY.getFilter("English", "l_english");
    }
}
