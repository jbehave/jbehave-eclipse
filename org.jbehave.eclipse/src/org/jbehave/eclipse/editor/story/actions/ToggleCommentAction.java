package org.jbehave.eclipse.editor.story.actions;

import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.story.StoryEditor;
import org.jbehave.eclipse.util.Strings;

import fj.Effect;

public class ToggleCommentAction extends TextEditorAction {

    public ToggleCommentAction(final ResourceBundle bundle, final String prefix, final ITextEditor editor) {
        super(bundle, prefix, editor);
    }

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
        ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
        IDocument document = viewer.getDocument();
        JBehaveProject jbehaveProject = storyEditor.getJBehaveProject();
        String ignorable = jbehaveProject.getLocalizedStepSupport().getLocalizedKeywords().ignorable();

        try {
            int startLine = selection.getStartLine();
            int endLine = selection.getEndLine();
            
            // if one line is not empty and not commented
            // then one comments all the lines! even those already commented
            int notCommentedCount = 0;
            for (int lineNb = startLine; lineNb <= endLine; lineNb++) {
                int lineOffset = document.getLineOffset(lineNb);
                int lineLength = document.getLineLength(lineNb);
                String text = document.get(lineOffset, lineLength);
                if (StringUtils.isNotBlank(text) && !text.startsWith(ignorable)){
                    notCommentedCount++;
                }
            }
            
            for (int lineNb = startLine; lineNb <= endLine; lineNb++) {
                int lineOffset = document.getLineOffset(lineNb);
                int lineLength = document.getLineLength(lineNb);
                String text = document.get(lineOffset, lineLength);

                if (notCommentedCount > 0) {
                    text = ignorable + " " + text;
                } else if (text.startsWith(ignorable)) {
                    text = text.substring(ignorable.length());
                    text = Strings.removeLeadingSpaces(text);
                }
                document.replace(lineOffset, lineLength, text);
            }

            int begOffset = document.getLineOffset(startLine);
            int endOffset = document.getLineOffset(endLine) + document.getLineLength(endLine);
            selectionProvider.setSelection(new TextSelection(begOffset, endOffset - begOffset - 1));
        } catch (BadLocationException e) {
            Activator.logError("Erf!", e);
        }
    }
}
