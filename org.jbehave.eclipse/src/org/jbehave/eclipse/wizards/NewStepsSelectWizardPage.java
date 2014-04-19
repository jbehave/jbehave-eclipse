package org.jbehave.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.parser.StoryElement;

public class NewStepsSelectWizardPage extends WizardPage {

	private IStructuredSelection selection;
	private List<String> selectedSteps = new ArrayList<String>();

	public NewStepsSelectWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.GenerateStepsPageName);
		setTitle(WizardsMessages.GenerateStepsPageTitle);
		setDescription(WizardsMessages.GenerateStepsPageDescription);
		this.selection = selection;
	}

	public List<String> getSelectedSteps() {		
		return selectedSteps;
	}

	@Override
	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(1, false));

		Tree tree = new Tree(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		tree.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String step : steps()) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(step);
			item.setChecked(false);
		}
		tree.setSize(100, 100);
		tree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String step = ((TreeItem)e.item).getText();
				selectedSteps.add(step);
			}			
		});

		// set the composite as the control for this page
		setControl(composite);
	}

	private List<String> steps() {
		List<String> steps = new ArrayList<String>();
		if (this.selection.getFirstElement() instanceof IFile) {
			IFile file = (IFile) this.selection.getFirstElement();
			JBehaveProject project = JBehaveProjectRegistry.get()
					.getOrCreateProject(file.getProject());
			IDocument document = new Document(contentOf(file));
			StoryDocumentUtils util = new StoryDocumentUtils(
					project.getLocalizedStepSupport());
			for (StoryElement element : util.getStoryElements(document)) {
				if (element.isStep()) {
					steps.add(element.getPreferredKeyword() + " "
							+ element.stepWithoutKeywordAndTrailingNewlines());
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
}