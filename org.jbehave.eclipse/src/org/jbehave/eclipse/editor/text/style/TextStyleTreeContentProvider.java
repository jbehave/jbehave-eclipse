package org.jbehave.eclipse.editor.text.style;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TextStyleTreeContentProvider implements ITreeContentProvider {

    public static Object[] EMPTY = new Object[0];
    
    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
    
    @Override
    public boolean hasChildren(Object element) {
        TextStyle ts = (TextStyle)element;
        List<TextStyle> children = ts.getChildren();
        return !(children.isEmpty());
    }
    
    @Override
    public Object[] getChildren(Object parentElement) {
        TextStyle ts = (TextStyle)parentElement;
        List<TextStyle> children = ts.getChildren();
        if(children.isEmpty())
            return EMPTY;
        return children.toArray();
    }
    
    @Override
    public Object[] getElements(Object inputElement) {
        if(inputElement instanceof Object[])
            return ((Object[])inputElement);//root case
        return getChildren(inputElement);
    }
    
    @Override
    public Object getParent(Object element) {
        return ((TextStyle)element).getParent();
    }
    
    @Override
    public void dispose() {
    }
}
