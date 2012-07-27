package org.jbehave.eclipse.editor.story;

import java.util.List;
import java.util.Locale;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.parser.VisitingStoryParser;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.parser.StoryVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryDocument extends Document {

	private Logger logger = LoggerFactory.getLogger(StoryDocument.class);

	private volatile List<StoryElement> elements;
	private JBehaveProject jbehaveProject;
	private Locale lastLocale;

	public StoryDocument() {
	}

	public void setJBehaveProject(JBehaveProject project) {
		this.jbehaveProject = project;
	}

	public JBehaveProject getJBehaveProject() {
		return jbehaveProject;
	}

	protected void fireDocumentChanged(DocumentEvent event) {
		invalidateStoryElements();
		// ... continue processing
		super.fireDocumentChanged(event);
	}

	public void traverseStory(StoryVisitor visitor) {
		for (StoryElement part : storyElements()) {
			visitor.visit(part);
			if (visitor.isDone())
				return;
		}
	}

	private synchronized void invalidateStoryElements() {
		elements = null;
	}

	private synchronized List<StoryElement> storyElements() {
		logger.debug(
				"Retrieving story elements from document (previous locale {} current locale {})",
				lastLocale, jbehaveProject.getLocale());

		if (lastLocale == null
				|| !lastLocale.equals(jbehaveProject.getLocale())) {
			invalidateStoryElements();
			lastLocale = jbehaveProject.getLocale();
		}
		if (elements == null) {
			elements = new VisitingStoryParser(jbehaveProject.getLocalizedStepSupport())
					.parse(get());
		}
		return elements;
	}

}
