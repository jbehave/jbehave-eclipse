package org.jbehave.eclipse.wizards;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.parser.StoryElement;

public class SelectStepsToGenerateWizardPage extends WizardPage {

	private IStructuredSelection selection;

	public SelectStepsToGenerateWizardPage(IStructuredSelection selection) {
		super(WizardsMessages.NewStoryWizardPage_0);
		setTitle(WizardsMessages.NewStoryWizardPage_1);
		setDescription(WizardsMessages.NewStoryWizardPage_2);
		this.selection = selection;
	}

	@Override
	public void createControl(Composite parent) {
		// create the composite to hold the widgets
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		topLevel.setLayout(new FillLayout());
		// topLevel.setLayout(new GridLayout());
		// topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
		// | GridData.HORIZONTAL_ALIGN_FILL));
		topLevel.setFont(parent.getFont());
		// create the desired layout for this wizard page
		Tree tree = new Tree(topLevel, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		String[] missingSteps = getMissngSteps();
		int size = missingSteps.length;
		for (int i = 0; i < size; i++) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText(missingSteps[i]);
			item.setChecked(true);
		}
		tree.setSize(100, 100);
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String string = event.detail == SWT.CHECK ? "Checked"
						: "Selected";
				System.out.println(event.item + " " + string);
			}
		});
		Group ownerInfo = new Group(parent, SWT.NONE);
		ownerInfo.setText("Owner Info");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		ownerInfo.setLayout(gridLayout);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		gridData.horizontalSpan = 2;
		ownerInfo.setLayoutData(gridData);
		
		
		// Similar for Destination ...
		Button newClassButton = new Button(ownerInfo, SWT.RADIO);
		newClassButton.setText("Create steps in a new class");
		newClassButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		Button existingClassButton = new Button(ownerInfo, SWT.RADIO);
		existingClassButton.setText("Create steps in a existing class");
		existingClassButton.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));

		// set the composite as the control for this page
		setControl(topLevel);
	}

	private String[] getMissngSteps() {
		ArrayList<String> missingSteps = new ArrayList();
		if (this.selection.getFirstElement() instanceof IFile) {
			IFile file = (IFile) this.selection.getFirstElement();
			String content = "";
			try {
				content = IOUtils.toString(file.getContents()).replace("\r\n",
						"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			JBehaveProject project = JBehaveProjectRegistry.get()
					.getOrCreateProject(file.getProject());
			IDocument document = new Document(content);

			StoryDocumentUtils util = new StoryDocumentUtils(
					project.getLocalizedStepSupport());
			List<StoryElement> elements = util.getStoryElements(document);

			Iterator<StoryElement> it = elements.iterator();
			while (it.hasNext()) {
				StoryElement element = it.next();
				if (element.isStep()) {
					missingSteps.add(element.getPreferredKeyword() + " " +element.stepWithoutKeywordAndTrailingNewlines());
				}
			}

		}

		return missingSteps.toArray(new String[missingSteps.size()]);
	}
}