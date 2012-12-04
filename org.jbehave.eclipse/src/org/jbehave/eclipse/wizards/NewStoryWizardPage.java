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
		super("wizardPage", selection);
		setTitle("Story file");
		setDescription("This wizard creates a new story file.");
		setFileExtension("story");
	}

	@Override
	protected InputStream getInitialContents() {
		try {
			String text = "Narrative:"
					+ "\n"
					+ "In Order To communicate effectively to the business some functionality"
					+ "\n"
					+ "As A development team"
					+ "\n"
					+ "I Want To use Behaviour-Driven Development"
					+ "\n\n"
					+ "Scenario:  A scenario is a collection of executable steps of different type"
					+ "\n"
					+ "Given step represents a precondition to an event"
					+ "\n"
					+ "When step represents the occurrence of the event"
					+ "\n"
					+ "Then step represents the outcome of the event"
					+ "\n\n"
					+ "Scenario:  Another scenario exploring different combination of events"
					+ "\n" + "Given a precondition" + "\n"
					+ "When a negative event occurs" + "\n"
					+ "Then a the outcome should be captured" + "\n";
			return new ByteArrayInputStream(text.getBytes("UTF-8"));
			// return
			// Activator.getDefault().getBundle().getEntry("/resources/newFileContents.config").openStream();
		} catch (IOException e) {
			return null; // ignore and create empty comments
		}
	}
}