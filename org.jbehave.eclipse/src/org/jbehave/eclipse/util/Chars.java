package org.jbehave.eclipse.util;

public class Chars {

    public static boolean isBlank(int c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }
}
