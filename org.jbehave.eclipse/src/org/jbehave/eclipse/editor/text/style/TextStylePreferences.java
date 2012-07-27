package org.jbehave.eclipse.editor.text.style;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.jbehave.eclipse.preferences.PreferenceConstants;

public class TextStylePreferences {
    
    private static final String CURRENT_LINE_COLOR = "-current_line_color";
    private static final String BACKGROUND = "-background";
    private static final String HAS_BACKGROUND = "-has_background";
    private static final String FOREGROUND = "-foreground";
    private static final String HAS_FOREGROUND = "-has_foreground";
    private static final String BOLD = "-bold";
    private static final String ITALIC = "-italic";

    public static void load(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            
            if(store.contains(path+CURRENT_LINE_COLOR))
                style.setCurrentLineHighlight(PreferenceConverter.getColor(store, path+CURRENT_LINE_COLOR));
            
            style.setItalic(store.getBoolean(path+ITALIC));
            style.setBold(store.getBoolean(path+BOLD));
            
            boolean hasForeground = store.getBoolean(path+HAS_FOREGROUND);
            if(hasForeground)
                style.setForeground(PreferenceConverter.getColor(store, path+FOREGROUND));
            else
                style.setForeground(null);
            
            boolean hasBackground = store.getBoolean(path+HAS_BACKGROUND);
            if(hasBackground)
                style.setBackground(PreferenceConverter.getColor(store, path+BACKGROUND));
            else
                style.setBackground(null);
        }
    }
    
    public static void loadFromDefault(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            
            if(store.contains(path+CURRENT_LINE_COLOR))
                style.setCurrentLineHighlight(PreferenceConverter.getDefaultColor(store, path+CURRENT_LINE_COLOR));
            
            style.setItalic(store.getDefaultBoolean(path+ITALIC));
            style.setBold(store.getDefaultBoolean(path+BOLD));
            
            boolean hasForeground = store.getDefaultBoolean(path+HAS_FOREGROUND);
            if(hasForeground)
                style.setForeground(PreferenceConverter.getDefaultColor(store, path+FOREGROUND));
            else
                style.setForeground(null);
            
            boolean hasBackground = store.getDefaultBoolean(path+HAS_BACKGROUND);
            if(hasBackground)
                style.setBackground(PreferenceConverter.getDefaultColor(store, path+BACKGROUND));
            else
                style.setBackground(null);
        }
    }

    public static void storeAsDefault(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            
            RGB currentLineHighlight = style.getCurrentLineHighlight();
            if(currentLineHighlight!=null)
                PreferenceConverter.setDefault(store, path+CURRENT_LINE_COLOR, currentLineHighlight);
            
            store.setDefault(path+ITALIC, style.isItalic());
            store.setDefault(path+BOLD, style.isBold());
            
            store.setDefault(path+HAS_FOREGROUND, style.hasForeground());
            if(style.hasForeground())
                PreferenceConverter.setDefault(store, path+FOREGROUND, style.getForegroundOrDefault());
            
            store.setDefault(path+HAS_BACKGROUND, style.hasBackground());
            if(style.hasBackground())
                PreferenceConverter.setDefault(store, path+BACKGROUND, style.getBackgroundOrDefault());
        }
    }
    
    public static void store(TextStyle rootStyle, IPreferenceStore store) {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(TextStyle style : map.values()) {
            String path = style.getPath();
            
            RGB currentLineHighlight = style.getCurrentLineHighlight();
            if(currentLineHighlight!=null)
                PreferenceConverter.setValue(store, path+CURRENT_LINE_COLOR, currentLineHighlight);
            
            store.setValue(path+ITALIC, style.isItalic());
            store.setValue(path+BOLD, style.isBold());

            store.setValue(path+HAS_FOREGROUND, style.hasForeground());
            if(style.hasForeground())
                PreferenceConverter.setValue(store, path+FOREGROUND, style.getForegroundOrDefault());
            
            store.setValue(path+HAS_BACKGROUND, style.hasBackground());
            if(style.hasBackground())
                PreferenceConverter.setValue(store, path+BACKGROUND, style.getBackgroundOrDefault());
        }
    }

    public static TextStyle getTheme(IPreferenceStore preferenceStore) {
        String theme = preferenceStore.getString(PreferenceConstants.THEME);
        TextStyle style = new TextStyleTreeBuilder().createTree(theme);
        load(style, preferenceStore);
        return style;
    }

}
