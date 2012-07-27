package org.jbehave.eclipse.editor.text.style;

import static org.jbehave.eclipse.editor.text.style.TextStyle.*;

import java.util.Map;

import org.eclipse.swt.graphics.RGB;

public class TextStyleTheme {
    public static TextStyle createDarkTheme () {
        TextStyle root = new TextStyleTreeBuilder().createTree("dark");
        root.setCurrentLineHighlight(new RGB(70,70,70));
        Map<String,TextStyle> map = root.createMap(); 
        define(map, DEFAULT, new RGB(0,0,0),new RGB(255,255,255),false,false);
        define(map, ERROR, new RGB(0,0,0),new RGB(255,0,0),true,false);
        define(map, NARRATIVE_DEFAULT, null,new RGB(238,159,97),true,false);
        define(map, NARRATIVE_KEYWORD, null,null,false,true);
        define(map, COMMENT, null,new RGB(210,255,210),false,false);
        define(map, META_DEFAULT, null,new RGB(232,178,255),false,false);
        define(map, META_KEYWORD, null,null,false,true);
        define(map, SCENARIO_DEFAULT, null,new RGB(255,237,117),true,false);
        define(map, SCENARIO_KEYWORD, null,null,false,true);
        define(map, STEP_DEFAULT, null,new RGB(223,225,225),false,false);
        define(map, STEP_KEYWORD, null,new RGB(118,197,255),false,true);
        define(map, STEP_PARAMETER, null,new RGB(192,230,249),true,true);
        define(map, STEP_PARAMETER_VALUE, null,new RGB(209,235,253),true,false);
        define(map, STEP_EXAMPLE_TABLE_SEPARATOR, null,new RGB(255,169,249),false,false);
        define(map, STEP_EXAMPLE_TABLE_CELL, null,new RGB(190,248,255),true,false);
        define(map, EXAMPLE_TABLE_DEFAULT, null,new RGB(223,225,225),false,false);
        define(map, EXAMPLE_TABLE_KEYWORD, null,new RGB(118,197,255),false,true);
        define(map, EXAMPLE_TABLE_SEPARATOR, null,new RGB(255,169,249),false,false);
        define(map, EXAMPLE_TABLE_CELL, null,new RGB(190,248,255),true,false);
        return root;
    }
    
    public static TextStyle createLightTheme() {
        TextStyle root = new TextStyleTreeBuilder().createTree("light");
        root.setCurrentLineHighlight(new RGB(247,225,203));
        Map<String,TextStyle> map = root.createMap(); 
        define(map, DEFAULT, new RGB(255,255,255),new RGB(0,0,0),false,false);
        define(map, ERROR, new RGB(255,255,255), new RGB(255,0,0),true,false);
        define(map, NARRATIVE_DEFAULT, null,new RGB(183,57,20),true,false);
        define(map, NARRATIVE_KEYWORD, null,null,false,true);
        define(map, COMMENT, null,new RGB(62,165,0),false,false);
        define(map, META_DEFAULT, null,new RGB(156,5,203),false,false);
        define(map, META_KEYWORD, null,null,false,true);
        define(map, SCENARIO_DEFAULT, null,new RGB(203,95,0),true,false);
        define(map, SCENARIO_KEYWORD, null,null,false,true);
        define(map, STEP_DEFAULT, null,new RGB(81, 37, 16),false,false);
        define(map, STEP_KEYWORD, null,new RGB(81, 37, 16),false,true);
        define(map, STEP_PARAMETER, null,new RGB(183,57,20),true,true);
        define(map, STEP_PARAMETER_VALUE, null,new RGB(183,57,20),true,false);
        define(map, STEP_EXAMPLE_TABLE_SEPARATOR, null,new RGB(205,131,55),false,false);
        define(map, STEP_EXAMPLE_TABLE_CELL, null,new RGB(183,57,20),true,false);
        define(map, EXAMPLE_TABLE_DEFAULT, null,null,false,false);
        define(map, EXAMPLE_TABLE_KEYWORD, null,new RGB(144,144,144),false,true);
        define(map, EXAMPLE_TABLE_SEPARATOR, null,new RGB(205,131,55),false,false);
        define(map, EXAMPLE_TABLE_CELL, null,new RGB(183,57,20),true,false);
        return root;
    }

    public static void define(Map<String, TextStyle> map, String key, RGB background, RGB foreground, boolean italic, boolean bold) {
        TextStyle style = map.get(key);
        style.setBackground(background);
        style.setForeground(foreground);
        style.setItalic(italic);
        style.setBold(bold);
    }
}
