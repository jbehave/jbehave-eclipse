package org.jbehave.eclipse.editor.text.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.jbehave.eclipse.editor.text.ColorManager;

public class StyleRangeConverter {

    private final ColorManager colorManager;

    public StyleRangeConverter(ColorManager colorManager) {
        super();
        this.colorManager = colorManager;
    }
    public StyleRange createStyleRange(TextStyle style, int offset, int length) {
        
        Color foreground = null;
        if(!(style.isRoot() || style.isForegroundSameAsRoot()))
            foreground = colorManager.getColor(style.getForegroundOrDefault());
        
        Color background = null;
        if(!(style.isRoot() || style.isBackgroundSameAsRoot()))
            background = colorManager.getColor(style.getBackgroundOrDefault());
        
        int fontStyle = SWT.NORMAL;
        if(style.isBold())
            fontStyle |= SWT.BOLD;
        if(style.isItalic())
            fontStyle |= SWT.ITALIC;
        
        StyleRange styleRange = new StyleRange(offset, length, foreground, background, fontStyle);
        return styleRange;
    }
}
