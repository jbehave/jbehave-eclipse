package org.jbehave.eclipse.editor.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.parser.StoryElement;

public class ScenarioScanner extends StoryTokenScanner {

	private IToken keywordToken;

	public ScenarioScanner(JBehaveProject jbehaveProject,
			TextAttributeProvider textAttributeProvider) {
		super(jbehaveProject, textAttributeProvider);
		initialize();
	}

	@Override
	protected void initialize() {
		super.initialize();
		setDefaultToken(newToken(TextStyle.SCENARIO_DEFAULT));
		keywordToken = newToken(TextStyle.SCENARIO_KEYWORD);
	}

	@Override
	protected boolean isAccepted(StoryElement element) {
		Keyword keyword = element.getPreferredKeyword();
		if (keyword == Keyword.Scenario || keyword.isComment()) {
			return true;
		}
		return false;
	}

	@Override
	protected void emit(StoryElement element) {
		String content = element.getContent();
		String kwString = getLocalizedStepSupport().scenario(false);
		int offset = element.getOffset();

		if (content.startsWith(kwString)) {
			emit(keywordToken, offset, kwString.length());
			offset += kwString.length();
			emitCommentAware(getDefaultToken(), offset,
					content.substring(kwString.length()));
		} else {
			emitCommentAware(getDefaultToken(), offset, content);
		}
	}
}
