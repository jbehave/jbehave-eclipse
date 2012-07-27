package org.jbehave.eclipse.editor.text;

import java.util.Map;
import java.util.Observable;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.util.New;

public class TextAttributeProvider extends Observable {
    
    private Map<String, TextAttribute> textAttributes = New.hashMap();
    
    private ColorManager colorManager;
    private Map<String, TextStyle> themeMap;
    
    public TextAttributeProvider(ColorManager colorManager) {
        super();
        this.colorManager = colorManager;
    }
    
    public synchronized TextAttribute get(String key) {
        if(themeMap==null)
            throw new IllegalStateException("Make sure to call 'changeTheme' first");
        
        TextAttribute textAttribute = textAttributes.get(key);
        if(textAttribute==null) {
            final TextStyle textStyle = themeMap.get(key);
            Color fcolor = null;
            if(!textStyle.isForegroundSameAsRoot()) {
                fcolor = colorManager.getColor(textStyle.getForegroundOrDefault());
            }
            
            Color bcolor = null;
            if(!textStyle.isBackgroundSameAsRoot()) {
                bcolor = colorManager.getColor(textStyle.getBackgroundOrDefault());
            }
            
            
            int style = SWT.NORMAL;
            if(textStyle.isBold())
                style |= SWT.BOLD;
            if(textStyle.isItalic())
                style |= SWT.ITALIC;
            textAttribute = new TextAttribute(fcolor, bcolor, style) {
                @Override
                public String toString() {
                    return "TextAttribute[" + textStyle + "]";
                }
            };
            textAttributes.put(key, textAttribute);
        }
        return textAttribute;
    }

    public synchronized void changeTheme(TextStyle theme) {
        this.themeMap = theme.createMap();
        this.textAttributes.clear();
        setChanged();
        notifyObservers(theme);
    }
}
