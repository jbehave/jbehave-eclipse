package org.jbehave.eclipse.wizards;

import java.util.List;

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

public class GenerateStepsWizard extends Wizard implements INewWizard {
	private static Logger log = LoggerFactory
			.getLogger(GenerateStepsWizard.class);

	private NewStepsSelectWizardPage selectPage;
	private NewStepsWizardPage createPage;
	private IStructuredSelection selection;
	private IWorkbench workbench;

	public GenerateStepsWizard() {
		super();
		setWindowTitle(WizardsMessages.GenerateStepsPageTitle);
	}

	public void addPages() {
		selectPage = new NewStepsSelectWizardPage(selection);
		addPage(selectPage);
		createPage = new NewStepsWizardPage(selection);
		addPage(createPage);
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
		boolean performedOK = false;
		List<String> selectedSteps = selectPage.getSelectedSteps();
		if (!selectedSteps.isEmpty()) {
			createPage.useSteps(selectedSteps);
			IFile file = createPage.createNewFile();
			if (file != null) {
				// open the file in editor
				openEditor(file);
			}
			// everything is fine
			performedOK = true;
		}
		return performedOK;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		this.workbench = workbench;
	}
}