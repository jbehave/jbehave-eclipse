package org.jbehave.eclipse.editor.story.actions;

import java.util.Properties;
import java.util.ResourceBundle;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.jbehave.core.model.ExamplesTableProperties;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.editor.story.StoryEditor;

import fj.Effect;

/**
 * Takes the text selection and formats it as a table if a table separator is
 * found. It also replaces all tabs in the text with table separators.
 *
 * <p>TODO:  A more generic story formatting solution should be based on
 * {@link IContentFormatter} and {@link IFormattingStrategy}.
 * </p>
 */
public class FormatTableAction extends TextEditorAction {

	public FormatTableAction(final ResourceBundle bundle, final String prefix,
			final ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	public void run() {
		if (!(getTextEditor() instanceof StoryEditor)) {
			return;
		}
		final StoryEditor storyEditor = (StoryEditor) getTextEditor();
		storyEditor.applyChange(new Effect<ISourceViewer>() {
			@Override
			public void e(ISourceViewer viewer) {
				process(storyEditor, viewer);
			}
		});
	}

	private void process(StoryEditor storyEditor, ISourceViewer viewer) {
		ISelectionProvider selectionProvider = viewer.getSelectionProvider();
		ITextSelection selection = (ITextSelection) selectionProvider
				.getSelection();
		IDocument document = viewer.getDocument();
		try {
			String text = replaceTabs(storyEditor, selection.getText());
			if (text.trim().isEmpty()) {
				return;
			}
			if (!isTable(storyEditor, text)) {
				return;
			}
			int offset = document.getLineOffset(selection.getStartLine());
			document.replace(offset, text.length(), format(text));
		} catch (BadLocationException e) {
			Activator.logError("Failed to format table", e);
		}
	}

	private boolean isTable(StoryEditor storyEditor, String text) {
		return text.contains(tableValueSeparator(storyEditor));
	}

	private String format(String text) {
		return new TableTransformers().transform(TableTransformers.FORMATTING,
				text, new ExamplesTableProperties(new Properties()));
	}

	private String replaceTabs(StoryEditor storyEditor, String text) {
		Properties properties = new Properties();
		properties.setProperty("replacing", "\t");
		properties.setProperty("replacement", tableValueSeparator(storyEditor));
		return new TableTransformers().transform(TableTransformers.REPLACING,
				text, new ExamplesTableProperties(properties));
	}

	private String tableValueSeparator(StoryEditor storyEditor) {
		return storyEditor.getJBehaveProject().getLocalizedStepSupport()
				.getLocalizedKeywords().examplesTableValueSeparator();
	}

}
