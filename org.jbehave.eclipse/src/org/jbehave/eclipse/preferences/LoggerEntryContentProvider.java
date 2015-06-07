package org.jbehave.eclipse.preferences;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class LoggerEntryContentProvider implements IStructuredContentProvider {

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }
    
    public Object[] getElements(Object inputElement) {
        return ((LoggerPreferences)inputElement).getEntriesAsObjectArray();
    }
}
