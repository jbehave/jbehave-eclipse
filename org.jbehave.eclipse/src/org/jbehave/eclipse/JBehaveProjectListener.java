package org.jbehave.eclipse;

public interface JBehaveProjectListener {
    /**
     * Called when the list of known steps has changed. 
     */
    void stepsUpdated();

}
