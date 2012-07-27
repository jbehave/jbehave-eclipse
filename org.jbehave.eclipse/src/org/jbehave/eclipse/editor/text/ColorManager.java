package org.jbehave.eclipse.editor.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

	protected Map<RGB,Color> fColorTable = new HashMap<RGB,Color>(10);

	public void dispose() {
	    releaseAll();
	}
	
	public Color getColor(RGB rgb) {
	    if(rgb==null)
	        return null;
	    
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

    public void releaseAll() {
        Iterator<Color> e = fColorTable.values().iterator();
        while (e.hasNext()) {
             e.next().dispose();
        }
        fColorTable.clear();
    }
}
