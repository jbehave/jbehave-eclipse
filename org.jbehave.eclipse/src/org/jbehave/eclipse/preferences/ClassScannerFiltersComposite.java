package org.jbehave.eclipse.preferences;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.jbehave.eclipse.Dialogs;
import org.jbehave.eclipse.preferences.ClassScannerFilterEntry.ApplyOn;
import org.jbehave.eclipse.swt.SWTResourceManager;

public class ClassScannerFiltersComposite extends Composite {
    private Text filterText;
    private Button radioPackage;
    private Button radioClass;
    private Button radioPkgRoot;
    private TableViewer filterViewer;
    private Button removeButton;
    private Button addInclude;
    private Button addExclude;
    //
    private ClassScannerPreferences prefs;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public ClassScannerFiltersComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        
        filterViewer = new TableViewer(this, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
        Table table = filterViewer.getTable();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gridData.heightHint = 75;
        table.setLayoutData(gridData);
        
        filterViewer.setContentProvider(new ClassScannerFilterContentProvider());
        filterViewer.setLabelProvider(new ClassScannerFilterLabelProvider());
        filterViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                displaySelectedElement();
            }
        });
        
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
        grpFilter.setText("Filter");
        grpFilter.setLayout(new GridLayout(2, false));
        grpFilter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        
        radioPkgRoot = new Button(grpFilter, SWT.RADIO);
        radioPkgRoot.setBounds(0, 0, 91, 18);
        radioPkgRoot.setText("Package Root");
        new Label(grpFilter, SWT.NONE);
        
        radioPackage = new Button(grpFilter, SWT.RADIO);
        radioPackage.setBounds(0, 0, 91, 18);
        radioPackage.setText("Package");
        radioPackage.setSelection(true);
        new Label(grpFilter, SWT.NONE);
        
        radioClass = new Button(grpFilter, SWT.RADIO);
        radioClass.setBounds(0, 0, 91, 18);
        radioClass.setText("Class");
        new Label(grpFilter, SWT.NONE);
        
        Label lblFilterPattern = new Label(grpFilter, SWT.NONE);
        lblFilterPattern.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false, 1, 1));
        lblFilterPattern.setText("Filter pattern");
        
        Composite composite_1 = new Composite(grpFilter, SWT.NONE);
        GridLayout gl_composite_1 = new GridLayout(1, false);
        gl_composite_1.marginHeight = 0;
        gl_composite_1.verticalSpacing = 0;
        composite_1.setLayout(gl_composite_1);
        composite_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        
        Label lblFilterPatternExample = new Label(composite_1, SWT.NONE);
        lblFilterPatternExample.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.ITALIC));
        lblFilterPatternExample.setAlignment(SWT.RIGHT);
        lblFilterPatternExample.setText("comma separated glob expressions");
        
        Label lblEgorgeclipseapple = new Label(composite_1, SWT.NONE);
        lblEgorgeclipseapple.setText("e.g. \"org.eclipse.*, *.apple.*\"");
        lblEgorgeclipseapple.setFont(SWTResourceManager.getFont("Lucida Grande", 10, SWT.ITALIC));
        lblEgorgeclipseapple.setAlignment(SWT.RIGHT);
        
        filterText = new Text(grpFilter, SWT.BORDER);
        filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        Composite composite = new Composite(grpFilter, SWT.NONE);
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.marginHeight = 0;
        gl_composite.verticalSpacing = 0;
        composite.setLayout(gl_composite);
        
        addInclude = new Button(composite, SWT.NONE);
        addInclude.setText("Include");
        addInclude.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addFilter(false);
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                addFilter(false);
            }
        });
        
        addExclude = new Button(composite, SWT.NONE);
        addExclude.setText("Exclude");
        addExclude.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                addFilter(true);
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                addFilter(true);
            }
        });
        new Label(grpFilter, SWT.NONE);
    }
    
    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    protected void addFilter(boolean exclude) {
        ApplyOn applyOn;
        if(radioPkgRoot.getSelection())
            applyOn = ApplyOn.PackageRoot;
        else if(radioPackage.getSelection())
            applyOn = ApplyOn.Package;
        else if(radioClass.getSelection())
            applyOn = ApplyOn.Class;
        else {
            Dialogs.warning("Missing filter settings", "Select where the filter applied");
            return;
        }
        
        String patterns = StringUtils.trimToEmpty(filterText.getText());
        if(patterns.isEmpty()) {
            Dialogs.warning("Missing filter settings", "Empty or blank patterns specified");
            return;
        }
        
        ClassScannerFilterEntry entry = prefs.addEntry(patterns, applyOn, exclude);
        if(entry!=null)
            filterViewer.add(entry);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
    }

    protected void removeSelectedElements() {
        IStructuredSelection selection = (IStructuredSelection) filterViewer.getSelection();
        Object[] array = selection.toArray();
        for(Object o : array) {
            ClassScannerFilterEntry entry = (ClassScannerFilterEntry) o;
            prefs.removeEntry(entry);
        }
        filterViewer.remove(array);
    }
    
    protected void displaySelectedElement() {
        IStructuredSelection selection = (IStructuredSelection) filterViewer.getSelection();
        if(!selection.isEmpty())
            displayEntry ((ClassScannerFilterEntry)selection.getFirstElement());
    }

    private void displayEntry(ClassScannerFilterEntry entry) {
        switch(entry.getApplyOn()) {
            case Class: radioClass.setSelection(true); break;
            case Package: radioPackage.setSelection(true); break;
            case PackageRoot: radioPkgRoot.setSelection(true); break;
        }
        filterText.setText(entry.getPatterns());
    }

    public void setInput(ClassScannerPreferences prefs) {
        this.prefs = prefs;
        this.filterViewer.setInput(prefs);
    }

}
