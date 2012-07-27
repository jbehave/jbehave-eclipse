package org.jbehave.eclipse.preferences;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.swt.ResourceManager;

public class PluginPreferencePage extends PropertyPage implements IWorkbenchPreferencePage {

    /**
     * Create the preference page.
     */
    public PluginPreferencePage() {
    }

    /**
     * Create contents of the preference page.
     * @param parent
     */
    @Override
    public Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        ResourceBundle bundle = PreferencesMessages.getBundle();

        Composite container = new Composite(parent, SWT.NULL);
        container.setLayout(new GridLayout(1, false));
        
        Label lblLogo = new Label(container, SWT.NONE);
        lblLogo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
        lblLogo.setImage(ResourceManager.getPluginImage("org.jbehave.eclipse", "icons/jbehave-plugin-logo.png"));
        
        Label lblTitle = new Label(container, SWT.NONE);
        lblTitle.setText(bundle.getString("pluginPreferencePage.title"));
        
        Label lblVersion = new Label(container, SWT.NONE);
        String versionPattern = bundle.getString("pluginPreferencePage.version");
        lblVersion.setText(MessageFormat.format(versionPattern, Activator.getDefault().getVersion()));
        
        Link lblJbehaveSite = new Link(container, SWT.NONE);
        lblJbehaveSite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lblJbehaveSite.setText(bundle.getString("pluginPreferencePage.jbehaveLink"));
        
        return container;
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        // Initialize the preference page
    }

}
