package org.jbehave.eclipse.editor.text;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public class ProjectAwareFastPartitioner extends CustomFastPartitioner {
	IProject project;

	public ProjectAwareFastPartitioner(IPartitionTokenScanner scanner,
			String[] legalContentTypes, IProject project) {
		super(scanner, legalContentTypes);
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}

}
