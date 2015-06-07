package org.jbehave.eclipse.editor.story.outline;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class QuickOutlineTreeContentProvider implements ITreeContentProvider {

    private final Object[] NO_CHILDREN = new Object[] {};

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public Object[] getChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<OutlineModel> models = (List<OutlineModel>)parent;
            return models.toArray();
        }
        else if(parent instanceof OutlineModel) {
            OutlineModel model = (OutlineModel)parent;
            if(model.hasChildren())
                return model.getChildren().toArray();
        }
        return NO_CHILDREN;
    }

    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    public Object getParent(Object child) {
        return null;
    }

    public boolean hasChildren(Object parent) {
        if(parent instanceof List) {
            @SuppressWarnings("unchecked")
            List<OutlineModel> models = (List<OutlineModel>)parent;
            return !models.isEmpty();
        }
        else if(parent instanceof OutlineModel) {
            OutlineModel model = (OutlineModel)parent;
            return model.hasChildren();
        }
        return false;
    }

    
}
