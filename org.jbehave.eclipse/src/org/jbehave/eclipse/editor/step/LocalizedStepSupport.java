package org.jbehave.eclipse.editor.step;

import java.util.Locale;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.util.CharTree;

public class LocalizedStepSupport {

	private CharTree<Keyword> keywordTree;

	private Locale storyLocale;

	private LocalizedKeywords localizedKeywords;

	public void setStoryLocale(Locale storyLocale) {
		this.storyLocale = storyLocale;
		localizedKeywords = null;
		keywordTree = null;
	}

	public Locale getLocale() {
		return storyLocale;
	}

	public CharTree<Keyword> getKeywordTree() {
		if (keywordTree == null) {
			keywordTree = createKeywordTree();
		}
		return keywordTree;
	}

	public LocalizedKeywords getLocalizedKeywords() {
		if (localizedKeywords == null) {
			localizedKeywords = new LocalizedKeywords(storyLocale);
		}
		return localizedKeywords;
	}

	protected CharTree<Keyword> createKeywordTree() {
		LocalizedKeywords keywords = getLocalizedKeywords();
		CharTree<Keyword> tree = new CharTree<Keyword>('/', null);
		for (Keyword keyword : Keyword.values()) {
			String asString = keyword.asString(keywords);
			tree.push(asString, keyword);
		}
		return tree;
	}

	public String given(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().given(), withTrailingSpace);
	}

	public String and(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().and(), withTrailingSpace);
	}

	public String asA(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().asA(), withTrailingSpace);
	}

	public String examplesTable(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().examplesTable(),
				withTrailingSpace);
	}

	public String givenStories(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().givenStories(),
				withTrailingSpace);
	}

	public String ignorable(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().ignorable(), withTrailingSpace);
	}

	public String inOrderTo(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().inOrderTo(), withTrailingSpace);
	}

	public String iWantTo(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().iWantTo(), withTrailingSpace);
	}

	public String meta(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().meta(), withTrailingSpace);
	}

	public String narrative(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().narrative(), withTrailingSpace);
	}

	public String scenario(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().scenario(), withTrailingSpace);
	}

	public String then(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().then(), withTrailingSpace);
	}

	public String when(boolean withTrailingSpace) {
		return plusSpace(getLocalizedKeywords().when(), withTrailingSpace);
	}

	private static String plusSpace(String aString, boolean withTrailingSpace) {
		return withTrailingSpace ? aString + " " : aString;
	}

}
