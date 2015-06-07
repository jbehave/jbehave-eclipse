package org.jbehave.eclipse.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.editor.story.validator.PendingStoryValidator;
import org.jbehave.eclipse.parser.StoryElement;

public class NewStepsSelectWizardPage extends WizardPage {

	private IStructuredSelection selection;
	private Set<NewStep> selectedSteps = new HashSet<NewStep>();

	public NewStepsSelectWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.GenerateStepsPageName);
		setTitle(WizardsMessages.GenerateStepsPageTitle);
		setDescription(WizardsMessages.GenerateStepsPageDescription);
		this.selection = selection;
	}

	public List<NewStep> getSelectedSteps() {
		return new ArrayList<NewStep>(selectedSteps);
	}

	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Tree tree = new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (NewStep step : steps()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setData(step);
			item.setText(step.asString());
			item.setChecked(false);
		}
		tree.setSize(100, 100);
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem item = (TreeItem) e.item;
				NewStep step = (NewStep) item.getData();
				if ( item.getChecked() ) {
					selectedSteps.add(step);
				} else {
					selectedSteps.remove(step);
				}
			}
		});
		// set the composite as the control for this page
		setControl(composite);
	}

	private List<NewStep> steps() {
		List<NewStep> steps = new ArrayList<NewStep>();
		if (this.selection.getFirstElement() instanceof IFile) {
			IFile file = (IFile) this.selection.getFirstElement();
			JBehaveProject project = JBehaveProjectRegistry.get()
					.getOrCreateProject(file.getProject());
			IDocument document = new Document(contentOf(file));
			PendingStoryValidator validator = new PendingStoryValidator(project, document);
			validator.validate();
			for (StoryElement element : validator.getPending()) {
				if (element.isStep()) {
					steps.add(new NewStep(element));
				}
			}			
		}
		return steps;
	}

	private String contentOf(IFile file) {
		try {
			return IOUtils.toString(file.getContents()).replace("\r\n", "\n");
		} catch (Exception e) {
			throw new RuntimeException("Failed to get content of " + file, e);
		}
	}

	public static class NewStep {
		private StoryElement element;

		public NewStep(StoryElement element) {
			this.element = element;
		}

		public String asString() {
			return element.getPreferredKeyword() + " "
					+ pattern();
		}

		public String generateMethod() {
			StringBuilder builder = new StringBuilder();
			builder.append("\t@" + element.getPreferredKeyword() + "(\"" 
					+ pattern() + "\")\n");
			builder.append("\t@Pending\n");
			builder.append("\tpublic void " + methodName() + "(){\n");
			builder.append("\t\t //TODO \n");
			builder.append("\t}\n");
			return builder.toString();
		}

		private String pattern() {
			return element.stepWithoutKeyword().replace("\n", "");
		}

		private String methodName() {
			String name = WordUtils.capitalize(asString());
			char filteredName[] = new char[name.length()];
			int index = 0;
			for (int i = 0; i < name.length(); i++) {
				char ch = name.charAt(i);
				if (Character.isJavaIdentifierPart(ch) && ch != '$'
						&& ch != 127) {
					filteredName[index++] = ch;
				}
			}
			return StringUtils.uncapitalize(new String(filteredName, 0, index));
		}

	}
}