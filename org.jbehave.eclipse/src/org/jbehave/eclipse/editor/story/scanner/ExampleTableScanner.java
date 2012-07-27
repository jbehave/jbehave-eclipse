package org.jbehave.eclipse.editor.story.scanner;

import org.eclipse.jface.text.rules.IToken;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.story.StoryPartition;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.ContentWithIgnorableEmitter;
import org.jbehave.eclipse.parser.StoryElement;

public class ExampleTableScanner extends StoryTokenScanner {
    
    private IToken keywordToken;

    public ExampleTableScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setDefaultToken(newToken(TextStyle.EXAMPLE_TABLE_DEFAULT));
        keywordToken = newToken(TextStyle.EXAMPLE_TABLE_KEYWORD);
        exampleTableCellToken = newToken(TextStyle.EXAMPLE_TABLE_CELL);
        exampleTableSepToken  = newToken(TextStyle.EXAMPLE_TABLE_SEPARATOR);
    }
    
    @Override
    protected boolean isAccepted(StoryElement element) {
        Keyword keyword = element.getPreferredKeyword();
        if(StoryPartition.ExampleTable==StoryPartition.partitionOf(keyword)) {
            return true;
        }
        return false;
    }

    @Override
    protected void emit(StoryElement element) {
        String content = element.getContent();
        String kwString = getLocalizedStepSupport().examplesTable(false);
        int offset = element.getOffset();
        
        if(content.startsWith(kwString)) {
            emit(keywordToken, offset, kwString.length());
            offset += kwString.length();
            
            String rawAfterKeyword = content.substring(kwString.length());
            ContentWithIgnorableEmitter emitter = new ContentWithIgnorableEmitter(RegexUtils.COMMENT_PATTERN, rawAfterKeyword);
            String cleanedAfterKeyword = emitter.contentWithoutIgnorables();
            emitTable(emitter, getDefaultToken(), offset, cleanedAfterKeyword);
        }
        else {
            ContentWithIgnorableEmitter emitter = new ContentWithIgnorableEmitter(RegexUtils.COMMENT_PATTERN, content);
            String cleanedContent = emitter.contentWithoutIgnorables();
            emit(emitter, getDefaultToken(), offset, cleanedContent.length());
        }
    }


}
