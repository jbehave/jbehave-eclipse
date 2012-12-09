package org.jbehave.eclipse.wizards;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.jbehave.eclipse.Activator;

public class NewStoryWizardPage extends WizardNewFileCreationPage {

	public NewStoryWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.NewStoryWizardPageName, selection);
		setTitle(WizardsMessages.NewStoryWizardPageTitle);
		setDescription(WizardsMessages.NewStoryWizardPageDescription);
		setFileExtension(WizardsMessages.NewStoryWizardPageFileExtension);
		setFileName(WizardsMessages.NewStoryWizardPageFileName);
	}

	@Override
	protected InputStream getInitialContents() {
		try {
			return Activator.getDefault().getBundle()
					.getEntry(WizardsMessages.NewStoryWizardPageInitialContents).openStream();
		} catch (IOException e) {
			return null;
		}
	}

}