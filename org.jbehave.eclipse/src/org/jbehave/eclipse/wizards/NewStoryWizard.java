package org.jbehave.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NewStoryWizard extends Wizard implements INewWizard {
	private static Logger log = LoggerFactory.getLogger(NewStoryWizard.class);

	private NewStoryWizardPage page;
	private IStructuredSelection selection;
	private IWorkbench workbench;

	public NewStoryWizard() {
		setWindowTitle(WizardsMessages.NewStoryWizardTitle);
	}

	public void addPages() {
		this.page = new NewStoryWizardPage(selection);
		addPage(page);
	}

	private void openEditor(final IFile file) {
		if (file != null) {
			getShell().getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						log.debug(e.getMessage(), e);
					}
				}
			});
		}
	}

	public boolean performFinish() {
		boolean performed = false;
		IFile file = page.createNewFile();
		if (file != null) {
			// open the file in editor
			openEditor(file);

			// everything is fine
			performed = true;
		}
		return performed;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}
}