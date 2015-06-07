package org.jbehave.eclipse.preferences;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class LoggerEntryLabelProvider implements ITableLabelProvider {

    public void addListener(ILabelProviderListener listener) {
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    public String getColumnText(Object element, int columnIndex) {
        LoggerEntry entry = (LoggerEntry) element;
        switch (columnIndex) {
            case 0:
                return entry.getLoggerName();
            case 1:
                return entry.getLevel().levelStr;
            default:
                return "";
        }
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

}
