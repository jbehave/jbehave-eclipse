package org.jbehave.eclipse.preferences;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class LoggerEntryLabelProvider implements ITableLabelProvider {

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }

    @Override
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

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

}
