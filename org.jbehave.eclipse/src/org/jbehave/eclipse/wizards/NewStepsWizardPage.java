package org.jbehave.eclipse.wizards;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.jbehave.eclipse.wizards.NewStepsSelectWizardPage.NewStep;

public class NewStepsWizardPage extends WizardNewFileCreationPage {

	private List<NewStep> steps;

	public NewStepsWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.NewStoryWizardPageName, selection);
		setTitle(WizardsMessages.NewStepsWizardPageTitle);
		setDescription(WizardsMessages.NewStepsWizardPageDescription);
		setFileExtension(WizardsMessages.NewStepsWizardPageFileExtension);
		setFileName(WizardsMessages.NewStepsWizardPageFileName);
	}

	public void useSteps(List<NewStep> selectedSteps) {
		this.steps = selectedSteps;
	}

	@Override
	protected InputStream getInitialContents() {
		return IOUtils.toInputStream(contents());
	}

	private String contents() {
		StringBuilder builder = new StringBuilder();
		String packageName = packageName();
		if (!packageName.isEmpty()) {
			builder.append("package " + packageName + ";\n");
		}
		builder.append("import org.jbehave.core.annotations.*;\n");
		builder.append("public class " + className() + "{\n");
		for (NewStep step : steps) {
			builder.append(step.generateMethod());
		}
		builder.append("}");
		return builder.toString();
	}

	private String className() {
		return FilenameUtils.removeExtension(this.getFileName());
	}

	private String packageName() {
		if (this.getContainerFullPath() != null) {
			String path = this.getContainerFullPath().toPortableString();
			if (path.contains("java")) {
				String relative = StringUtils.substringAfter(path, "java");
				if (relative.startsWith("/")) {
					relative = StringUtils.removeStart(relative, "/");
				}
				return StringUtils.replace(relative, "/", ".");
			}
		}
		return "";
	}

}