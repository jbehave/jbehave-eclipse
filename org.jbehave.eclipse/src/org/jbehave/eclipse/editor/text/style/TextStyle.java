package org.jbehave.eclipse.editor.text.style;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.graphics.RGB;
import org.jbehave.eclipse.util.New;

public class TextStyle {

    public static final String COMMENT = "comment";
    public static final String META_DEFAULT = "meta_default";
    public static final String META_KEYWORD = "meta_keyword";
    public static final String EXAMPLE_TABLE_DEFAULT = "example_table_default";
    public static final String EXAMPLE_TABLE_KEYWORD = "example_table_keyword";
    public static final String EXAMPLE_TABLE_SEPARATOR = "example_table_separator";
    public static final String EXAMPLE_TABLE_CELL = "example_table_cell";
    public static final String SCENARIO_DEFAULT = "scenario_default";
    public static final String SCENARIO_KEYWORD = "scenario_keyword";
    public static final String STEP_DEFAULT = "step_default";
    public static final String STEP_KEYWORD = "step_keyword";
    public static final String STEP_PARAMETER = "step_parameter";
    public static final String STEP_PARAMETER_VALUE = "step_parameter_value";
    public static final String STEP_EXAMPLE_TABLE_SEPARATOR = "step_example_table_separator";
    public static final String STEP_EXAMPLE_TABLE_CELL = "step_example_table_cell";
    public static final String NARRATIVE_DEFAULT = "narrative_default";
    public static final String NARRATIVE_KEYWORD = "narrative_keyword";
    public static final String DEFAULT = "default";
    public static final String ERROR = "error";

    private final String key;
    private RGB foreground;
    private RGB background;
    private boolean italic;
    private boolean bold;
    //
    private RGB currentLineHighlight;

    private final TextStyle parent;
    private List<TextStyle> children = New.arrayList();

    public TextStyle(String key, TextStyle parent) {
        this.key = key;
        this.parent = parent;
    }

    public TextStyle getParent() {
        return parent;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        List<String> keys = New.arrayList();
        TextStyle ts = this;
        while (ts != null) {
            keys.add(ts.getKey());
            ts = ts.parent;
        }
        Collections.reverse(keys);
        return StringUtils.join(keys, ".");
    }

    public void setForeground(RGB foreground) {
        this.foreground = foreground;
    }

    public boolean hasForeground() {
        return foreground != null;
    }

    public RGB getForegroundOrDefault() {
        if (foreground == null) {
            if (parent != null)
                return parent.getForegroundOrDefault();
            else
                return new RGB(0, 0, 0);
        }
        return foreground;
    }

    public void setBackground(RGB background) {
        this.background = background;
    }

    public boolean hasBackground() {
        return background != null;
    }

    public RGB getBackgroundOrDefault() {
        if (background == null) {
            if (parent != null)
                return parent.getBackgroundOrDefault();
            else
                return new RGB(255, 255, 255);
        }
        return background;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public void setCurrentLineHighlight(RGB currentLineHighlight) {
        this.currentLineHighlight = currentLineHighlight;
    }
    
    public RGB getCurrentLineHighlight() {
        return currentLineHighlight;
    }
    
    public List<TextStyle> getChildren() {
        return children;
    }

    public TextStyle newChild(String key) {
        TextStyle child = new TextStyle(key, this);
        children.add(child);
        return child;
    }

    public Map<String, TextStyle> createMap() {
        Map<String, TextStyle> styles = New.hashMap();
        recursivelyFill(styles);
        return styles;
    }

    protected void recursivelyFill(Map<String, TextStyle> styles) {
        if (parent == null) {
            styles.put(DEFAULT, this);
        }
        else
            styles.put(key, this);
        for (TextStyle child : getChildren())
            child.recursivelyFill(styles);
    }

    @Override
    public String toString() {
        return "TextStyle [key=" + key + ", foreground=" + foreground + ", background=" + background + ", italic="
                + italic + ", bold=" + bold + "]";
    }

    private TextStyle root;

    private TextStyle getRoot() {
        if (root == null) {
            TextStyle ts = this;
            while (true) {
                if (ts.parent == null)
                    break;
                ts = ts.parent;
            }
            root = ts;
        }
        return root;
    }

    public boolean isForegroundSameAsRoot() {
        RGB tcolor = getForegroundOrDefault();
        RGB rcolor = getRoot().getForegroundOrDefault();
        return tcolor.equals(rcolor);
    }

    public boolean isBackgroundSameAsRoot() {
        RGB tcolor = getBackgroundOrDefault();
        RGB rcolor = getRoot().getBackgroundOrDefault();
        return tcolor.equals(rcolor);
    }

}
