package org.jbehave.eclipse.util;

public interface RandomStream {
    public static final int EOF = -1;
    
    int read ();
    int peek (int relative_offset); 
    void unread();
    int getPosition ();
    void setPosition(int new_position);
}
