package org.jbehave.eclipse.parser;

public abstract class StoryVisitor {
    
    private boolean isDone = false;

    public abstract void visit(StoryElement element);
    
    public boolean isDone() {
        return isDone;
    }

    public void done() {
        this.isDone = true;
    }
}
