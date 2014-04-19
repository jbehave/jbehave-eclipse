package org.jbehave.eclipse.wizards;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class NewStepsWizardPage extends WizardNewFileCreationPage {

	private List<String> steps;

	public NewStepsWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.NewStoryWizardPageName, selection);
		setTitle(WizardsMessages.NewStepsWizardPageTitle);
		setDescription(WizardsMessages.NewStepsWizardPageDescription);
		setFileExtension(WizardsMessages.NewStepsWizardPageFileExtension);
		setFileName(WizardsMessages.NewStepsWizardPageFileName);
}
	
	public void useSteps(List<String> steps){
		this.steps = steps;		
	}

	@Override
	protected InputStream getInitialContents() {
		return IOUtils.toInputStream(contents());
	}
	
	//TODO Use a template & configure package
	private String contents() {		
		StringBuilder builder = new StringBuilder();
		builder.append("import org.jbehave.core.annotations.Pending;\n");
		builder.append("public class "+WizardsMessages.NewStepsWizardPageFileName+"{\n");
		for ( String step : steps ){
			builder.append("\t@Pending\n");
			builder.append("\tpublic void "+methodName(step)+"(){\n");
			builder.append("\t\t //TODO \n");
			builder.append("\t}\n");
		}
		builder.append("}");
		return builder.toString();
	}

	private String methodName(String step) {
		StringBuilder builder = new StringBuilder();
		for (String word : step.split(" ")) {
			builder.append(StringUtils.capitalize(word));
		}
		return StringUtils.uncapitalize(builder.toString());
	}
	
}