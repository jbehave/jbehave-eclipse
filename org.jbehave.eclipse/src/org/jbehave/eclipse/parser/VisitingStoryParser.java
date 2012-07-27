package org.jbehave.eclipse.parser;

import java.util.List;

import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.util.CharIterator;
import org.jbehave.eclipse.util.CharIterators;
import org.jbehave.eclipse.util.CharTree;

public class VisitingStoryParser {

	private final LocalizedStepSupport localizedStepSupport;

	public VisitingStoryParser(LocalizedStepSupport localizedStepSupport) {
		this.localizedStepSupport = localizedStepSupport;
	}

	public List<StoryElement> parse(CharSequence content) {
		VisitingCollector collector = new VisitingCollector();
		parse(content, collector);
		return collector.getElements();
	}

	public void parse(CharSequence content, StoryVisitor visitor) {
		parse(CharIterators.createFrom(content), 0, visitor);
	}

	public void parse(CharIterator it, StoryVisitor visitor) {
		parse(it, 0, visitor);
	}

	public void parse(CharIterator it, int baseOffset, StoryVisitor visitor) {
		CharTree<Keyword> tree = localizedStepSupport.getKeywordTree();
		int offset = baseOffset;
		Line line = new Line();
		Block block = new Block();
		block.reset(offset);
		line.reset(offset);
		while (true) {
			int read = it.read();
			if (read == CharIterator.EOF) {
				break;
			}
			line.append((char) read);
			if (isNewlineCharacter(read)) {
				if (line.startsWithBreakingKeyword(tree, block)) {
					block.emitTo(visitor);
					block.reset(line.offset);
				}
				line.emitTo(block);
				// line is reset without any char, offset must be the next one
				line.reset(offset + 1);
			}
			offset++;
		}

		// remaining
		if (line.startsWithBreakingKeyword(tree, block)) {
			block.emitTo(visitor);
			block.reset(line.offset);
		}
		line.emitTo(block);
		block.emitTo(visitor);
	}

	private class Block {
		private StringBuilder buffer = new StringBuilder();
		private int offset;
		private Keyword keyword;

		public void reset(int offset) {
			this.offset = offset;
			this.buffer.setLength(0);
		}

		public void emitTo(StoryVisitor visitor) {
			if (buffer.length() > 0) {
				String content = buffer.toString();
				StoryElement element = new StoryElement(localizedStepSupport, offset,
						content);
				Keyword extractedKeyword = element.extractKeyword();
				if (extractedKeyword != null && extractedKeyword.isStep()) {
					if (extractedKeyword == Keyword.And) {
						element.setPreferredKeyword(keyword);
					} else {
						keyword = extractedKeyword;
					}
				} else {
					keyword = null;
				}
				visitor.visit(element);
			}
		}
	}

	private class Line {
		private StringBuilder buffer = new StringBuilder();
		private int offset;

		public void append(char c) {
			buffer.append(c);
		}

		public void reset(int offset) {
			this.offset = offset;
			this.buffer.setLength(0);
		}

		public boolean startsWithBreakingKeyword(CharTree<Keyword> tree,
				Block block) {
			Keyword keyword = tree.lookup(buffer);
			if (keyword == null) {
				return false;
			}
			switch (keyword) {
			case Ignorable: // comment should not be considered as breaking, but
							// must be ignored...
			case ExamplesTableHeaderSeparator:
			case ExamplesTableValueSeparator:
			case ExamplesTableIgnorableSeparator:
				return false;
			default:
				return true;
			}
		}

		public void emitTo(Block block) {
			block.buffer.append(buffer);
		}
	}

	private static boolean isNewlineCharacter(int read) {
		return read == '\r' || read == '\n';
	}
}
