package org.jbehave.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.jbehave.eclipse.wizards.GenerateStepsWizard;

public class GenerateStepsAction implements IObjectActionDelegate {

	private IWorkbench workbench;
	private IStructuredSelection selection;

	public void run(IAction action) {
		GenerateStepsWizard wizard = new GenerateStepsWizard();
		wizard.init(workbench, selection);
		WizardDialog dialog = new WizardDialog(workbench.getDisplay()
				.getActiveShell(), wizard);
		dialog.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		workbench = targetPart.getSite().getWorkbenchWindow().getWorkbench();
	}
}
