package org.jbehave.eclipse.parser;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.StepSupport;
import org.jbehave.eclipse.util.CharTree;

public class StoryElement {

	private final LocalizedStepSupport localizedStepSupport;
	private final int offset;
	private final String content;
	private Keyword preferredKeyword;

	public StoryElement(LocalizedStepSupport localizedStepSupport, int offset,
			String content) {
		this.localizedStepSupport = localizedStepSupport;
		this.offset = offset;
		this.content = content;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return content.length();
	}

	public String getContentWithoutComment() {
		return RegexUtils.removeComment(content);
	}

	public String getContent() {
		return content;
	}

	public String stepWithoutKeyword() {
		return StepSupport.stepWithoutKeyword(localizedStepSupport,
				getContent());
	}

	/**
	 * @see #isStep()
	 */
	public String stepWithoutKeywordAndTrailingNewlines() {
		return StepSupport.stepWithoutKeywordAndTrailingNewlines(
				localizedStepSupport, getContent());
	}

	/**
	 * @see #extractKeyword(CharTree<Keyword>)
	 */
	public Keyword getPreferredKeyword() {
		return getPreferredKeyword(defaultTree());
	}

	public void setPreferredKeyword(Keyword keyword) {
		this.preferredKeyword = keyword;
	}

	public Keyword getPreferredKeyword(CharTree<Keyword> tree) {
		if (preferredKeyword == null) {
			preferredKeyword = extractKeyword(tree);
		}
		return preferredKeyword;
	}

	public Keyword extractKeyword() {
		return extractKeyword(defaultTree());
	}

	public Keyword extractKeyword(CharTree<Keyword> tree) {
		return tree.lookup(getContent());
	}

	public boolean startsWithKeyword() {
		return startsWithKeyword(defaultTree());
	}

	public boolean startsWithKeyword(CharTree<Keyword> tree) {
		return (getPreferredKeyword(tree) != null);
	}

	private CharTree<Keyword> defaultTree() {
		return localizedStepSupport.getKeywordTree();
	}

	/**
	 * Same as {@link #getOffset()}
	 * 
	 * @see #getOffset()
	 */
	public int getOffsetStart() {
		return getOffset();
	}

	public int getOffsetEnd() {
		return getOffset() + getLength();
	}

	public boolean intersects(int offset, int length) {
		int tmin = getOffset();
		int tmax = getOffsetEnd();
		int omin = offset;
		int omax = offset + length;
		return omin <= tmax && tmin <= omax;
	}

	public boolean isStep() {
		Keyword keyword = getPreferredKeyword();
		return keyword != null && keyword.isStep();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(ToStringStyle.SIMPLE_STYLE)
				.append("locale", localizedStepSupport.getLocale())
				.append("content", content.replace("\n", "\\n")).toString();
	}
}
