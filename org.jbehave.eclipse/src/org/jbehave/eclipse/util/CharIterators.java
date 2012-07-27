package org.jbehave.eclipse.util;


public class CharIterators {
    
    public static CharIterator createFrom(final CharSequence text) {
        return new CharIterator() {
            private int pos = 0;
            
            @Override
            public int read() {
                if(pos<text.length())
                    return text.charAt(pos++);
                return -1;
            }
        };
    }
}
