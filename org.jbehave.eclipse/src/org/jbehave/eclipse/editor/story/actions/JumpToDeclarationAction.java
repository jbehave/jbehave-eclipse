package org.jbehave.eclipse.editor.story.actions;

import java.util.ResourceBundle;

import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.jbehave.eclipse.editor.story.StoryEditor;

public class JumpToDeclarationAction extends TextEditorAction {

    public JumpToDeclarationAction(final ResourceBundle bundle, final String prefix,
            final ITextEditor editor) {
        super(bundle, prefix, editor);
    }

    @Override
    public void run() {
        if (getTextEditor() instanceof StoryEditor) {
            StoryEditor editor = (StoryEditor) getTextEditor();
            editor.jumpToMethod();
        }
    }
}
