package org.jbehave.eclipse.util;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class Visitor<T,R> {
    private boolean isDone;
    private ConcurrentLinkedQueue<R> elements = New.concurrentLinkedQueue();
    
    public abstract void visit(T value);
    
    public boolean isDone () {
        return isDone;
    }
    
    public void done () {
        this.isDone = true;
    }
    
    public void add(R found) {
        this.elements.add(found);
    }
    
    public R getFirst() {
        if(elements.isEmpty())
            return null;
        return elements.peek();
    }
    
    public ConcurrentLinkedQueue<R> getElementsFound() {
        return elements;
    }
}