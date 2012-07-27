package org.jbehave.eclipse.preferences;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ClassScannerFilterContentProvider implements IStructuredContentProvider {

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
        return ((ClassScannerPreferences)inputElement).getEntriesAsObjectArray();
    }
}
