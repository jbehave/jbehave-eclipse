package org.jbehave.eclipse.preferences;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.jbehave.eclipse.Dialogs;
import org.jbehave.eclipse.swt.SWTResourceManager;

import ch.qos.logback.classic.Level;

public class LoggerEntriesComposite extends Composite {

    private LoggerPreferences prefs;
    private TableViewer filterViewer;
    private Button removeButton;
    private Text loggerNameText;
    private ComboViewer loggerLevelCombo;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public LoggerEntriesComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        
        filterViewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        Table table = filterViewer.getTable();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData.heightHint = 75;
        table.setLayoutData(gridData);
        
        filterViewer.setContentProvider(new LoggerEntryContentProvider());
        filterViewer.setLabelProvider(new LoggerEntryLabelProvider());
        filterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                displaySelectedElement();
            }
        });
        dressUpTable(table);
        
        removeButton = new Button(this, SWT.NONE);
        removeButton.setText("Remove");
        removeButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                removeSelectedElements();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
                removeSelectedElements();
            }
        });
        
        Group grpFilter = new Group(this, SWT.NONE);
        grpFilter.setText("Logger");
        grpFilter.setLayout(new GridLayout(3, false));
        grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        Label lblFilterPattern = new Label(grpFilter, SWT.NONE);
        lblFilterPattern.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
        lblFilterPattern.setText("Logger name");
        
        Composite composite_1 = new Composite(grpFilter, SWT.NONE);
        GridLayout gl_composite_1 = new GridLayout(1, false);
        gl_composite_1.marginHeight = 0;
        gl_composite_1.verticalSpacing = 0;
        composite_1.setLayout(gl_composite_1);
        composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
        
        Label lblFilterPatternExample = new Label(composite_1, SWT.NONE);
        lblFilterPatternExample.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.ITALIC));
        lblFilterPatternExample.setAlignment(SWT.RIGHT);
        lblFilterPatternExample.setText("comma separated glob expressions");
        
        Label lblEgorgeclipseapple = new Label(composite_1, SWT.NONE);
        lblEgorgeclipseapple.setText("e.g. \"org.eclipse.*, *.apple.*\"");
        lblEgorgeclipseapple.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.ITALIC));
        lblEgorgeclipseapple.setAlignment(SWT.RIGHT);
        
        loggerNameText = new Text(grpFilter, SWT.BORDER);
        GridData gd_loggerNameText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_loggerNameText.widthHint = 90;
        loggerNameText.setLayoutData(gd_loggerNameText);
        
        loggerLevelCombo = new ComboViewer(grpFilter, SWT.NONE|SWT.READ_ONLY);
        loggerLevelCombo.setContentProvider(new ArrayContentProvider());
        loggerLevelCombo.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Level)element).levelStr;
            }
        });
        loggerLevelCombo.setInput(new Object[] {
                Level.ERROR, Level.WARN, Level.INFO,
                Level.DEBUG, Level.TRACE, Level.OFF
        });
        loggerLevelCombo.setSelection(new StructuredSelection(Level.INFO));
        Combo combo = loggerLevelCombo.getCombo();
        GridData gd_combo = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_combo.widthHint = 10;
        combo.setLayoutData(gd_combo);
        
        Button btnAdd = new Button(grpFilter, SWT.NONE);
        btnAdd.setText("Add");
        btnAdd.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addLoggerEntry();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                addLoggerEntry();
            }
        });
        new Label(grpFilter, SWT.NONE);
        new Label(grpFilter, SWT.NONE);
    }

    protected void dressUpTable(Table table) {
        TableColumn loggerNameCol = new TableColumn( table, SWT.LEFT );
        loggerNameCol.setText("Logger name");
        loggerNameCol.setResizable(true);
        loggerNameCol.setWidth(150);
        
        TableColumn loggerLevelCol = new TableColumn( table, SWT.LEFT );
        loggerLevelCol.setText("Level");
        loggerLevelCol.setResizable(false);
        loggerLevelCol.setWidth(50);
        
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
    }
    
    protected void displaySelectedElement() {
        IStructuredSelection selection = (IStructuredSelection) filterViewer.getSelection();
        if(!selection.isEmpty())
            displayEntry ((LoggerEntry)selection.getFirstElement());
    }

    private void displayEntry(LoggerEntry entry) {
        loggerLevelCombo.setSelection(new StructuredSelection(entry.getLevel()));
        loggerNameText.setText(entry.getLoggerName());
    }

    private void addLoggerEntry() {
        IStructuredSelection selection = (IStructuredSelection) loggerLevelCombo.getSelection();
        if(!selection.isEmpty()) {
            Level level = (Level)selection.getFirstElement();
            String loggerName = StringUtils.trimToEmpty(loggerNameText.getText());
            if(loggerName.isEmpty()) {
                Dialogs.warning("Missing logger name", "Empty or blank name specified");
                return;
            }
            
            prefs.addEntry(loggerName, level);
            filterViewer.refresh();
        }
    }

    protected void removeSelectedElements() {
        IStructuredSelection selection = (IStructuredSelection) filterViewer.getSelection();
        Object[] array = selection.toArray();
        for(Object o : array) {
            LoggerEntry entry = (LoggerEntry) o;
            prefs.removeEntry(entry);
        }
        filterViewer.remove(array);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
    
    public void setInput(LoggerPreferences prefs) {
        this.prefs = prefs;
        this.filterViewer.setInput(prefs);
    }

}
