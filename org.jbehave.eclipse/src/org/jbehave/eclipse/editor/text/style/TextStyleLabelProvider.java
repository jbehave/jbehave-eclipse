package org.jbehave.eclipse.editor.text.style;

import java.util.ResourceBundle;

import org.eclipse.jface.viewers.LabelProvider;

public class TextStyleLabelProvider extends LabelProvider {
    
    private ResourceBundle bundle;
    private String prefix;
    
    public TextStyleLabelProvider(ResourceBundle bundle, String prefix) {
        this.bundle = bundle;
        this.prefix = prefix;
    }

    @Override
    public String getText(Object element) {
        TextStyle ts = (TextStyle)element;
        return bundle.getString(prefix+ts.getKey()+".label");
    }
    
}
