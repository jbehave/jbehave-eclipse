package org.jbehave.eclipse.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.jbehave.eclipse.Activator;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewStoryWizardPage extends WizardNewFileCreationPage {
	private Text containerText;

	private Text fileText;

	private ISelection selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewStoryWizardPage(IStructuredSelection selection) {
		super(Messages.NewStoryWizardPage_0, selection);
		setTitle(Messages.NewStoryWizardPage_1);
		setDescription(Messages.NewStoryWizardPage_2);
		setFileExtension(Messages.NewStoryWizardPage_3);
	}

	@Override
	protected InputStream getInitialContents() {
		
			try {
				return Activator.getDefault().getBundle().getEntry(Messages.NewStoryWizardPage_4).openStream();
			} catch (IOException e) {
				return null;
			}
	}
}