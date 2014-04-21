package org.jbehave.eclipse.editor.step;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.jbehave.eclipse.Dialogs;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.editor.EditorUtils;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.Ref;
import org.jbehave.eclipse.util.Strings;

public class StepJumper {

	private JBehaveProject jbehaveProject;

	public StepJumper(JBehaveProject jbehaveProject) {
		this.jbehaveProject = jbehaveProject;
	}

	public boolean jumpToSelectedMethod(final ITextViewer viewer)
			throws JavaModelException, PartInitException {

		Point point = viewer.getSelectedRange();

		final Ref<StoryElement> found = new StoryDocumentUtils(
				jbehaveProject.getLocalizedStepSupport())
				.findStoryElementAtOffset(viewer.getDocument(), point.x);
		if (found.isNull()) {
			return false;
		}

		final StoryElement element = found.get();
		if (!element.isStep()) {
			return false;
		}

		return jumpToMethod(viewer, element);
	}

	public boolean jumpToMethod(final ITextViewer viewer,
			final StoryElement element) throws JavaModelException,
			PartInitException {
		// find project
		IProject project = EditorUtils.findProject(viewer);
		if (project == null) {
			Dialogs.information("Step not found", "No project found.");
			return false;
		}

		// step can contain comment and trailing new lines...
		String stepWithoutKeywordCommentOrTrailingLines = RegexUtils
				.removeComment(element.stepWithoutKeywordAndTrailingNewlines());
		stepWithoutKeywordCommentOrTrailingLines = Strings
				.removeTrailingNewlines(stepWithoutKeywordCommentOrTrailingLines);

		JBehaveProject jbehaveProject = JBehaveProjectRegistry.get()
				.getOrCreateProject(project);
		IJavaElement method = jbehaveProject.getStepLocator().findMethod(
				stepWithoutKeywordCommentOrTrailingLines);
		// Open method in editor, if found
		if (method != null) {
			JavaUI.openInEditor(method);
			return true;
		} else {
			Dialogs.information("Step not found",
					"There is no step matching:\n" + element.getContent());
			return false;
		}
	}

	public boolean jumpToMethod(final String qualifiedName)
			throws PartInitException, JavaModelException {
		IJavaElement method = jbehaveProject.getStepLocator()
				.findMethodByQualifiedName(qualifiedName);
		// Open method in editor, if found
		if (method != null) {
			JavaUI.openInEditor(method);
			return true;
		} else {
			return false;
		}
	}
}
