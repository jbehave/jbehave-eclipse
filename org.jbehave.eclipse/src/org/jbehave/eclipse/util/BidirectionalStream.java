package org.jbehave.eclipse.util;


public interface BidirectionalStream {
    
    public int read();

    public void unread();
}