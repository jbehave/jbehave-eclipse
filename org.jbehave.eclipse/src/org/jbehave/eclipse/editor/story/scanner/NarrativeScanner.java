package org.jbehave.eclipse.editor.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.parser.StoryElement;

public class NarrativeScanner extends StoryTokenScanner {
    
    private IToken keywordToken;

    public NarrativeScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setDefaultToken(newToken(TextStyle.NARRATIVE_DEFAULT));
        keywordToken = newToken(TextStyle.NARRATIVE_KEYWORD);
    }
    
    @Override
    protected boolean isAccepted(StoryElement element) {
        Keyword keyword = element.getPreferredKeyword();
        if(keyword.isNarrative()) {
            return true;
        }
        return false;
    }

    @Override
    protected void emit(StoryElement element) {
        LocalizedStepSupport localizedStepSupport = getLocalizedStepSupport();
        if(handleKeyword(element, localizedStepSupport.narrative(false)) //
                || handleKeyword(element, localizedStepSupport.asA(false)) //
                || handleKeyword(element, localizedStepSupport.inOrderTo(false)) //
                || handleKeyword(element, localizedStepSupport.iWantTo(false))) {
            // done!
        }
        else {
            emitCommentAware(getDefaultToken(), element.getOffset(), element.getContent());
        }
    }
    
    private boolean handleKeyword(StoryElement element, String keyword) {
        String content = element.getContent();
        int offset = element.getOffset();
        
        if(content.startsWith(keyword)) {
            emit(keywordToken, offset, keyword.length());
            offset += keyword.length();
            emitCommentAware(getDefaultToken(), offset, content.substring(keyword.length()));
            return true;
        }
        return false;
    }
}
