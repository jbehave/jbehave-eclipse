package org.jbehave.eclipse.wizards;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.misc.ResourceAndContainerGroup;
import org.jbehave.eclipse.Activator;

public class NewStoryWizardPage extends WizardNewFileCreationPage {

	public NewStoryWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.NewStoryWizardPage_0, selection);
		setTitle(WizardsMessages.NewStoryWizardPage_1);
		setDescription(WizardsMessages.NewStoryWizardPage_2);
		setFileExtension(WizardsMessages.NewStoryWizardPage_3);
		setFileName(WizardsMessages.NewStoryWizardPage_5);
	}

	@Override
	protected InputStream getInitialContents() {

		try {
			return Activator.getDefault().getBundle()
					.getEntry(WizardsMessages.NewStoryWizardPage_4).openStream();
		} catch (IOException e) {
			return null;
		}
	}
}