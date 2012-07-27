package org.jbehave.eclipse.preferences;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.editor.text.ColorManager;
import org.jbehave.eclipse.editor.text.style.StyleRangeConverter;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.editor.text.style.TextStyleLabelProvider;
import org.jbehave.eclipse.editor.text.style.TextStylePreferences;
import org.jbehave.eclipse.editor.text.style.TextStyleTreeBuilder;
import org.jbehave.eclipse.editor.text.style.TextStyleTreeContentProvider;
import org.jbehave.eclipse.swt.SWTResourceManager;
import org.jbehave.eclipse.util.New;

public class EditorColorPreferencePage extends PreferencePage implements org.eclipse.ui.IWorkbenchPreferencePage {

    private ColorManager colorManager;
    private StyleRangeConverter styleRangeConverter;
    //
    private Combo themeCombo;
    private TreeViewer keywordTree;
    private Button customForegroundChk;
    private ColorSelector customForegroundButton;
    private Button customBackgroundChk;
    private ColorSelector customBackgroundButton;
    private Button customItalicChk;
    private Button customBoldChk;
    private TextViewer previewStyledText;
    //
    private TextStyle current;
    private TextStyle rootStyle;
    //
    private Map<String, TextStyle> themesLoaded = New.hashMap();

    /**
     * Create the preference page.
     */
    public EditorColorPreferencePage() {
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        // Initialize the preference page
        setPreferenceStore(Activator.getDefault().getPreferenceStore());        
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        
        colorManager = new ColorManager();
        styleRangeConverter = new StyleRangeConverter(colorManager);
        
        SelectionListener styleChangedListener = new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                adjustButtonStatusAndColors();
                updatePreview();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        };
        
        IPropertyChangeListener styleChangedPropertyListener = new IPropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                ColorSelector selector = (ColorSelector)event.getSource();
                selector.getButton().setToolTipText(selector.getColorValue().toString());
                adjustButtonStatusAndColors();
                updatePreview();
            }
        };
        
        ResourceBundle bundle = PreferencesMessages.getBundle();
        
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(6, false));
        
        // ~~ row 1
        Label lblTheme = new Label(container, SWT.NONE);
        lblTheme.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTheme.setText("Theme");
        
        themeCombo = new Combo(container, SWT.NONE|SWT.READ_ONLY);
        themeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
        themeCombo.addSelectionListener(new SelectionListener() {
           @Override
            public void widgetDefaultSelected(SelectionEvent event) {
               widgetSelected(event);
            }
           @Override
            public void widgetSelected(SelectionEvent event) {
               int selectionIndex = themeCombo.getSelectionIndex();
               setCurrentTheme(themeCombo.getItem(selectionIndex));
            }
        });
        new Label(container, SWT.NONE);
        
        // ~~ row 2
        Label lblCurrentLine = new Label(container, SWT.NONE);
        lblCurrentLine.setText("Current line marker:");
        
        currentLineColor = new ColorSelector(container);
        currentLineColor.addListener(styleChangedPropertyListener);
        currentLineColor.getButton().setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        Label lblCurrentLineToolTip = new Label(container, SWT.NONE);
        lblCurrentLineToolTip.setText("(Change the caret position in the preview for feedback)");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        
        //
        keywordTree = new TreeViewer(container, SWT.SINGLE |SWT.BORDER);
        Tree tree = keywordTree.getTree();
        
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 5);
        gridData.widthHint = 80;
        gridData.heightHint = 30;
        tree.setLayoutData(gridData);
        keywordTree.setLabelProvider(new TextStyleLabelProvider(bundle, "text-style."));
        keywordTree.setContentProvider(new TextStyleTreeContentProvider());
        keywordTree.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if(selection instanceof IStructuredSelection) {
                    IStructuredSelection structured = (IStructuredSelection)selection;
                    Object firstElement = structured.getFirstElement();
                    setCurrentTextStyle((TextStyle)firstElement);
                }
            }
        });
        
        customForegroundChk = new Button(container, SWT.CHECK);
        customForegroundChk.setText("Foreground");
        customForegroundChk.addSelectionListener(styleChangedListener);
        
        customForegroundButton = new ColorSelector(container);
        customForegroundButton.addListener(styleChangedPropertyListener);
        
        customBackgroundChk = new Button(container, SWT.CHECK);
        customBackgroundChk.setText("Background");
        customBackgroundChk.addSelectionListener(styleChangedListener);
        
        customBackgroundButton = new ColorSelector(container);
        customBackgroundButton.addListener(styleChangedPropertyListener);
        
        customItalicChk = new Button(container, SWT.CHECK);
        customItalicChk.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.ITALIC));
        customItalicChk.setText("Italic");
        customItalicChk.addSelectionListener(styleChangedListener);
        new Label(container, SWT.NONE);
        
        customBoldChk = new Button(container, SWT.CHECK);
        customBoldChk.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
        customBoldChk.setText("Bold");
        customBoldChk.addSelectionListener(styleChangedListener);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        Label lblPreview = new Label(container, SWT.NONE);
        lblPreview.setText("Preview");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        
        previewStyledText = new TextViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY);
        GridData gridData2 = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1);
        gridData2.widthHint = 80;
        gridData2.heightHint = 140;
        previewStyledText.getControl().setLayoutData(gridData2);
        
        cursorLinePainter = new CursorLinePainter(previewStyledText);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        previewStyledText.addPainter(cursorLinePainter);
        
        initialize();
        
        return container;
    }
    
    @Override
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }
    
    private Color getLineBackground() {
        return colorManager.getColor(currentLineColor.getColorValue());
    }
    
    /**
     * Initial values.
     */
    protected void initialize() {
        previewStyledText.setDocument(new Document(createText()));
        
        IPreferenceStore store = getPreferenceStore();
        String inlinedThemes = store.getString(PreferenceConstants.THEMES);
        String selectedTheme = store.getString(PreferenceConstants.THEME);
        String[] themes = inlinedThemes.split(",");
        themeCombo.setItems(themes);
        themeCombo.select(ArrayUtils.indexOf(themes, selectedTheme));
        setCurrentTheme(selectedTheme);
    }
    
    /**
     * 
     */
    private void setCurrentTheme(String theme) {
        rootStyle = themesLoaded.get(theme);
        if(rootStyle==null) {
            rootStyle = new TextStyleTreeBuilder().createTree(theme);
            TextStylePreferences.load(rootStyle, getPreferenceStore());
            themesLoaded.put(theme, rootStyle);
        }
        
        keywordTree.setInput(new Object[] { rootStyle });
        currentLineColor.setColorValue(rootStyle.getCurrentLineHighlight());
        setCurrentTextStyle(rootStyle);
        updatePreview();
    }
    
    /**
     * 
     */
    private void setCurrentTextStyle(TextStyle textStyle) {
        if(textStyle==null) {
            return;
        }
        this.current = textStyle;
        
        customBackgroundChk.setSelection(current.hasBackground());
        customForegroundChk.setSelection(current.hasForeground());
        customBackgroundButton.setColorValue(current.getBackgroundOrDefault());
        customForegroundButton.setColorValue(current.getForegroundOrDefault());
        
        customBoldChk.setSelection(current.isBold());
        customItalicChk.setSelection(current.isItalic());
        
        adjustButtonStatusAndColors();
    }
    
    protected void adjustButtonStatusAndColors() {
        customForegroundButton.setEnabled(customForegroundChk.getSelection());
        if(customForegroundChk.getSelection()) {
            current.setForeground(customForegroundButton.getColorValue());
        }
        else {
            current.setForeground(null);
        }
        
        customBackgroundButton.setEnabled(customBackgroundChk.getSelection());
        if(customBackgroundChk.getSelection()) {
            current.setBackground(customBackgroundButton.getColorValue());
        }
        else {
            current.setBackground(null);
        }
        
        current.setBold(customBoldChk.getSelection());
        current.setItalic(customItalicChk.getSelection());
        
        rootStyle.setCurrentLineHighlight(currentLineColor.getColorValue());
    }
    
    protected void storeModifications() {
        IPreferenceStore store = getPreferenceStore();
        for(TextStyle rootStyle : themesLoaded.values()) {
            TextStylePreferences.store(rootStyle, store);
        }
        
        String theme = rootStyle.getPath();
        store.setValue(PreferenceConstants.THEME, theme);
        
        // fire an overall change, to have a unique property notification hook
        // instead of one per property
        store.firePropertyChangeEvent(PreferenceConstants.THEME_CHANGED, theme, theme);
    }

    @Override
    public boolean performOk() {
        storeModifications();
        return super.performOk();
    }
    
    private static boolean DumpCurrentStyleOnApply = false;
    private ColorSelector currentLineColor;
    private CursorLinePainter cursorLinePainter;
    
    @Override
    protected void performApply() {
        if(DumpCurrentStyleOnApply) {
            dumpCurrentStyle();
        }
        storeModifications();
        super.performApply();
    }

    @Override
    protected void performDefaults() {
        TextStylePreferences.loadFromDefault(rootStyle, getPreferenceStore());
        setCurrentTextStyle(rootStyle);
        updatePreview();
        super.performDefaults();
    }
    
    private void updatePreview() {
        cursorLinePainter.setHighlightColor(getLineBackground());
        previewStyledText.getTextWidget().setBackground(colorManager.getColor(rootStyle.getBackgroundOrDefault()));
        previewStyledText.getTextWidget().setForeground(colorManager.getColor(rootStyle.getForegroundOrDefault()));
        previewStyledText.getTextWidget().setStyleRanges(createStyleRanges());
    }
    
    /**
     * Iterate over all fragments and generate corresponding text for the preview.
     */
    private String createText() {
        StringBuilder builder = new StringBuilder();
        for(Fragment f : fragments()) {
            builder.append(f.content);
        }
        return builder.toString();
    }

    /**
     * Iterate over all fragments and generate corresponding styles for the preview.
     */
    private StyleRange[] createStyleRanges() {
        Map<String, TextStyle> map = rootStyle.createMap();
        
        List<StyleRange> ranges = New.arrayList();
        int offset = 0;
        for(Fragment f : fragments()) {
            TextStyle style = map.get(f.key);
            int length = f.content.length();
            
            StyleRange styleRange = styleRangeConverter.createStyleRange(style, offset, length);
            ranges.add(styleRange);
            offset += length;
        }
        
        return ranges.toArray(new StyleRange[ranges.size()]);
    }

    /**
     * The fragments used for the preview: content with its associated style.
     * @return
     */
    private List<Fragment> fragments() {
        return Arrays.asList(
                f(TextStyle.DEFAULT, "A story is a collection of scenarios\n\n"),//
                f(TextStyle.NARRATIVE_KEYWORD, "Narrative:\n"),//
                f(TextStyle.NARRATIVE_KEYWORD, "In order to "),//
                f(TextStyle.NARRATIVE_DEFAULT, "communicate effectively to the business some functionality\n"),//
                f(TextStyle.NARRATIVE_KEYWORD, "As a "),//
                f(TextStyle.NARRATIVE_DEFAULT, "development team\n"),//
                f(TextStyle.NARRATIVE_KEYWORD, "I want to "),//
                f(TextStyle.NARRATIVE_DEFAULT, "use Behaviour-Driven Development\n"),//
                f(TextStyle.DEFAULT, "\n"),//
                f(TextStyle.SCENARIO_KEYWORD, "Scenario: "),//
                f(TextStyle.SCENARIO_DEFAULT, " A scenario is a collection of executable steps of different type\n"),//
                f(TextStyle.DEFAULT, "\n"),//
                f(TextStyle.META_KEYWORD, "GivenStories: "),//
                f(TextStyle.META_DEFAULT, "path/to/precondition1.story,\n"),//
                f(TextStyle.META_DEFAULT, "              path/to/precondition1.story,\n"),//
                f(TextStyle.DEFAULT, "\n"),//
                f(TextStyle.STEP_KEYWORD, "Given "),//
                f(TextStyle.STEP_DEFAULT, "a new user with the following properties:\n"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_CELL,"firstname"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_CELL,"Sherlock"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|\n"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_CELL,"lastname"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_CELL,"Holmes"),//
                f(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR,"|\n"),//
                f(TextStyle.STEP_KEYWORD, "When "),//
                f(TextStyle.STEP_DEFAULT, "user clicks on "),//
                f(TextStyle.STEP_PARAMETER_VALUE, "login"),//
                f(TextStyle.STEP_DEFAULT, " button\n"),//
                f(TextStyle.STEP_KEYWORD, "And "),//
                f(TextStyle.STEP_DEFAULT, "user clicks on "),//
                f(TextStyle.STEP_PARAMETER, "$button_label"),//
                f(TextStyle.STEP_DEFAULT, " button\n"),//
                f(TextStyle.COMMENT, "!-- Look at this beautiful comment!\n"),//
                f(TextStyle.STEP_KEYWORD, "Then "),//
                f(TextStyle.STEP_DEFAULT, "login page must be displayed\n"),//
                f(TextStyle.DEFAULT, "\n"),//
                f(TextStyle.EXAMPLE_TABLE_KEYWORD, "ExampleTable:\n"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "user"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "login"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|\n"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "Sherlock Holmes"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "sherlock"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|\n"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "Arsene Lupin"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "arsene"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|\n"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "Fileas Fogg"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|"),//
                f(TextStyle.EXAMPLE_TABLE_CELL, "flogg"),//
                f(TextStyle.EXAMPLE_TABLE_SEPARATOR, "|\n")
        );
    }
    
    private static Fragment f(String key, String content) {
        return new Fragment(key, content);
    }
    
    private static class Fragment {
        public String key;
        public String content;
        public Fragment(String key, String content) {
            super();
            this.key = key;
            this.content = content;
        }
    }

    private void dumpCurrentStyle() {
        Map<String, TextStyle> map = rootStyle.createMap();
        for(Map.Entry<String, TextStyle> e : map.entrySet()) {
            StringBuilder builder = new StringBuilder("define(map, ");
            builder.append('"').append(e.getKey()).append('"').append(", ");
            TextStyle style = e.getValue();
            if(style.hasBackground()) {
                RGB rgb = style.getBackgroundOrDefault();
                builder.append("new RGB(").append(rgb.red).append(",").append(rgb.green).append(",").append(rgb.blue).append(")");
            }
            else
                builder.append("null");
            builder.append(", ");
            if(style.hasForeground()) {
                RGB rgb = style.getForegroundOrDefault();
                builder.append("new RGB(").append(rgb.red).append(",").append(rgb.green).append(",").append(rgb.blue).append(")");
            }
            else
                builder.append("null");
            builder.append(", ");
            builder.append(style.isItalic()).append(", ").append(style.isBold()).append(")");
            System.out.println(builder);
        }
    }
}
