package org.jbehave.eclipse;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class Dialogs {

	public static void information(String title, String message) {
		MessageDialog.openInformation(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message);
	}

	public static void warning(String title, String message) {
		MessageDialog.openWarning(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), title, message);
	}
	
}
