package org.jbehave.eclipse.editor.story.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IToken;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.junit.Before;
import org.junit.Test;

public class PartitionScannerTest {

    private String storyAsText;
    private LocalizedStepSupport localizedSupport;
    private JBehaveProject jbehaveProject;
    
    @Before
    public void setUp () throws IOException {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx01.story")).replace("\r\n", "\n");
        jbehaveProject = mock(JBehaveProject.class);
        localizedSupport = new LocalizedStepSupport();
        localizedSupport.setStoryLocale(Locale.ENGLISH);
    }

    @Test
    public void canScanADocument() throws Exception {
        when(jbehaveProject.getLocalizedStepSupport()).thenReturn(localizedSupport);
        IDocument document= new Document(storyAsText);
        
        StoryPartitionScanner scanner = new StoryPartitionScanner (jbehaveProject);
        scanner.setRange(document, 0, document.getLength());
        
        checkNextToken(scanner, document, "Narrative", 0, 172);
        checkNextToken(scanner, document, "Scenario", 172, 57);
        checkNextToken(scanner, document, "Step", 229, 208);
        
        assertThat(scanner.nextToken().isEOF(), is(true));
    }
    
    private void checkNextToken(IPartitionTokenScanner scanner, IDocument document, Object jk, int offset, int length) throws BadLocationException {
        IToken token = scanner.nextToken();
        assertThat(token.getData(), equalTo(jk));
        assertThat(scanner.getTokenOffset(), equalTo(offset));
        assertThat(scanner.getTokenLength(), equalTo(length));        
    }
    
}
