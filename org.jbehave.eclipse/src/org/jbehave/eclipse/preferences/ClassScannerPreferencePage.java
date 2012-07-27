package org.jbehave.eclipse.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jbehave.eclipse.Activator;
import org.osgi.service.prefs.BackingStoreException;

public class ClassScannerPreferencePage extends PropertyPage implements org.eclipse.ui.IWorkbenchPreferencePage {

    private IProject project;
    private Button enableProjectSpecific;
    private ClassScannerPreferences prefs;
    private ClassScannerFiltersComposite scannerFilterComposite;
    private ControlEnableState blockEnableState;

    /**
     * Create the property page.
     */
    public ClassScannerPreferencePage() {
        setTitle("Configure the Java scanner");
        setDescription("Configure which packages or classes should be scanned to lookup for steps");
    }

    /**
     * Create contents of the property page.
     * 
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(2, false));

        if (isProjectPreferencePage()) {
            enableProjectSpecific = new Button(container, SWT.CHECK);
            enableProjectSpecific.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
            enableProjectSpecific.setText("Enable project specific settings");
            enableProjectSpecific.addSelectionListener(new SelectionListener() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    adjustProjectSpecificState();
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent event) {
                    adjustProjectSpecificState();
                }
            });
        }

        scannerFilterComposite = new ClassScannerFiltersComposite(container, SWT.NONE);
        scannerFilterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        reload();
        updatePageWithPrefs();
        
        return container;
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void setElement(final IAdaptable element) {
        project = (IProject) element.getAdapter(IResource.class);
        super.setElement(element);
    }

    private boolean isProjectPreferencePage() {
        return project != null;
    }
    
    @Override
    protected void contributeButtons(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        Button defaultsButton = new Button(parent, SWT.PUSH);
        defaultsButton.setText("Reload");
        Dialog.applyDialogFont(defaultsButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        Point minButtonSize = defaultsButton.computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minButtonSize.x);
        defaultsButton.setLayoutData(data);
        defaultsButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                performReload();
            }
        });
        
        super.contributeButtons(parent);
    }

    @Override
    public boolean performOk() {
        try {
            updatePrefsWithPage();
            if (isProjectPreferencePage()
                    && !enableProjectSpecific.getSelection()) {
                prefs.removeAllSpecificSettings();
            } else {
                prefs.store();
            }
        } catch (final BackingStoreException e) {
            Activator.logError("Failed to store ClassScanner preferences", e);
        }
        return super.performOk();
    }


    @Override
    protected void performDefaults() {
        try {
            prefs.removeAllSpecificSettings();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to remove specific settings", e);
        }
        reload();
        updatePageWithPrefs();
        super.performDefaults();
    }
    
    protected void performReload () {
        reload();
        updatePageWithPrefs();
        super.performDefaults();
    }

    private void reload() {
        if (project == null) {
            prefs = new ClassScannerPreferences();
        } else {
            prefs = new ClassScannerPreferences(project);
        }
        try {
            prefs.load();
        } catch (BackingStoreException e) {
            Activator.logError("Failed to load preferences", e);
        }
    }
    
    private void updatePrefsWithPage() {
        if (isProjectPreferencePage()) {
            boolean isProjectSpecific = enableProjectSpecific.getSelection();
            prefs.setUseProjectSettings(isProjectSpecific);
        }
    }

    private void updatePageWithPrefs() {
        scannerFilterComposite.setInput(prefs);
        if (isProjectPreferencePage()) {
            enableProjectSpecific.setSelection(prefs.isUseProjectSettings());
            adjustProjectSpecificState();
        }
    }

    private void adjustProjectSpecificState() {
        boolean useProjectSpecificSettings = enableProjectSpecific.getSelection();
        if (useProjectSpecificSettings) {
            if (blockEnableState != null) {
                blockEnableState.restore();
                blockEnableState = null;
            }
        } else {
            if (blockEnableState == null) {
                blockEnableState = ControlEnableState.disable(scannerFilterComposite);
            }
        }
    }

}
