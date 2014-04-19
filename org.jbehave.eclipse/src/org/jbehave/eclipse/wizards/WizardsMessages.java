package org.jbehave.eclipse.wizards;

import org.eclipse.osgi.util.NLS;

public class WizardsMessages extends NLS {
	
	private static final String BUNDLE_NAME = "org.jbehave.eclipse.wizards.wizardsMessages"; //$NON-NLS-1$
	public static String NewStepsWizardPageName; 
	public static String NewStepsWizardPageTitle; 
	public static String NewStepsWizardPageDescription; 
	public static String NewStepsWizardPageFileExtension; 
	public static String NewStepsWizardPageFileName; 
	public static String GenerateStepsPageTitle;
	public static String GenerateStepsPageDescription;
	public static String GenerateStepsPageName;
	public static String NewStoryWizardTitle;
	public static String NewStoryWizardPageName;
	public static String NewStoryWizardPageTitle;
	public static String NewStoryWizardPageDescription;
	public static String NewStoryWizardPageFileName;
	public static String NewStoryWizardPageFileExtension;
	public static String NewStoryWizardPageInitialContents;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, WizardsMessages.class);
	}

	private WizardsMessages() {
	}
	
}
