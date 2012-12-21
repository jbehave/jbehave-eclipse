package org.jbehave.eclipse.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.actions.ModifyWorkingSetDelegate.NewWorkingSetAction;
import org.jbehave.eclipse.wizards.GenerateStepsWizard;

public class CreateGenerateStepsWizardAction implements IObjectActionDelegate {
	
    private IWorkbench mWorkbench;
	private IStructuredSelection selection;

	@Override
	public void run(IAction action) {
		GenerateStepsWizard wizard = new GenerateStepsWizard();
		wizard.init(mWorkbench, selection);
		WizardDialog dialog = new WizardDialog(mWorkbench.getDisplay().getActiveShell(), wizard);
		dialog.open();
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if(selection instanceof IStructuredSelection){
			this.selection = (IStructuredSelection) selection;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        mWorkbench = targetPart.getSite().getWorkbenchWindow().getWorkbench();
	}
}
