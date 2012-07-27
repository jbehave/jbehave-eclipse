package org.jbehave.eclipse.util;

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

public class LocaleUtils {

    /**
     * Creates locale from string representation as returned by Locale::toString();
     * 
     * @param code
     *            programmatic name with lang, country, variant, separated by underbars
     * @return Locale object (never returns <code>null</code>)
     */
    public static Locale createLocaleFromCode(String code, Locale defaultLocale) {
        if (code == null) {
            return defaultLocale;
        }

        String params[] = new String[3];
        StringTokenizer st = new StringTokenizer(code, "_");

        for (int i = 0; i < params.length; i++) {
            params[i] = (st.hasMoreTokens() ? st.nextToken() : StringUtils.EMPTY);
        }

        if (params[0].length() > 0) {
            return new Locale(params[0], params[1], params[2]);
        }

        return defaultLocale;
    }
}
