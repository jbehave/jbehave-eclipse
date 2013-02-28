package org.jbehave.eclipse.editor.story.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.step.StepLocator;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.preferences.ProjectPreferences;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepParserTest {
    private Logger log = LoggerFactory.getLogger(StepParserTest.class);

    private String storyAsText;
    private TextAttribute defaultAttribute;
    private TextAttribute keywordAttribute;
    private TextAttribute parameterAttribute;
    private TextAttribute parameterValueAttribute;
    private TextAttribute exampleTableSeparator;
    private StepLocator locator;
    private TextAttributeProvider textAttributeProvider;
    private JBehaveProject jbehaveProject;
    private int offset;
    private TextAttribute exampleTableCell;
    private static LocalizedStepSupport localizedSupport = createLocalizedStepSupport();

    
    @Before
    public void setUp () throws IOException {
        defaultAttribute = mock(TextAttribute.class);
        keywordAttribute = mock(TextAttribute.class);
        parameterAttribute = mock(TextAttribute.class);
        parameterValueAttribute = mock(TextAttribute.class);
        exampleTableSeparator = mock(TextAttribute.class);
        exampleTableCell = mock(TextAttribute.class);
        
        when(defaultAttribute.toString()).thenReturn("mock-default");
        when(keywordAttribute.toString()).thenReturn("mock-keyword");
        when(parameterAttribute.toString()).thenReturn("mock-parameter");
        when(parameterValueAttribute.toString()).thenReturn("mock-parameter-value");
        when(exampleTableSeparator.toString()).thenReturn("mock-table-sep");
        when(exampleTableCell.toString()).thenReturn("mock-table-cell");
        
        textAttributeProvider = mock(TextAttributeProvider.class);
        when(textAttributeProvider.get(TextStyle.STEP_DEFAULT)).thenReturn(defaultAttribute);
        when(textAttributeProvider.get(TextStyle.STEP_KEYWORD)).thenReturn(keywordAttribute);
        when(textAttributeProvider.get(TextStyle.STEP_PARAMETER)).thenReturn(parameterAttribute);
        when(textAttributeProvider.get(TextStyle.STEP_PARAMETER_VALUE)).thenReturn(parameterValueAttribute);
        when(textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR)).thenReturn(exampleTableSeparator);
        when(textAttributeProvider.get(TextStyle.STEP_EXAMPLE_TABLE_CELL)).thenReturn(exampleTableCell);
        
        locator = mock(StepLocator.class);
        
        this.offset = 0;
        
        jbehaveProject = mock(JBehaveProject.class);
        when(jbehaveProject.getLocalizedStepSupport()).thenReturn(localizedSupport);
        when(jbehaveProject.getStepLocator()).thenReturn(locator);
        when(jbehaveProject.getProjectPreferences()).thenReturn(new ProjectPreferences());
    }

    private static LocalizedStepSupport createLocalizedStepSupport() {
        LocalizedStepSupport localizedSupport = new LocalizedStepSupport();
        localizedSupport.setStoryLocale(Locale.ENGLISH);
        return localizedSupport;
    }

    private static StepCandidate givenStep(String content) {
        return new StepCandidate(localizedSupport, "$", null, StepType.GIVEN, content, 0);
    }
    
    private static StepCandidate whenStep(String content) {
        return new StepCandidate(localizedSupport, "$", null, StepType.WHEN, content, 0);
    }
    
    private static StepCandidate thenStep(String content) {
        return new StepCandidate(localizedSupport, "$", null, StepType.THEN, content, 0);
    }
    
    @Test
    public void usecase_ex1() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx02.story"));
        
        when(locator.findFirstStep("a user named \"Bob\"")).thenReturn(givenStep("a user named \"$name\""));
        when(locator.findFirstStep("'Bob' clicks on the 'login' button")).thenReturn(whenStep("'$who' clicks on the '$button' button"));
        when(locator.findFirstStep("the 'password' field becomes 'red'")).thenReturn(thenStep("the '$field' field becomes '$color'"));
        
        IDocument document= new Document(storyAsText);
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, 0, document.getLength());

        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);

        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    @Test
    public void usecase_ex2_light() throws Exception {
        StepCandidate user = givenStep("a user named $username");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03-light.story")).replace("\r\n", "\n");
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex2() throws Exception {
        StepCandidate user = givenStep("a user named $username");
        StepCandidate credits = whenStep("user credits is $amount dollars");
        StepCandidate clicks = whenStep("user clicks on $button button");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03.story")).replace("\r\n", "\n");
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        when(locator.findFirstStep("user clicks on $button button")).thenReturn(clicks);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        when(locator.findFirstStep("a user named username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex3 () throws Exception {
        StepCandidate user = givenStep("a user named $username");
        StepCandidate credits = whenStep("user credits is $amount dollars");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx03.story")).replace("\r\n", "\n");
        when(locator.findFirstStep("a user named $username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        when(locator.findFirstStep("a user named username")).thenReturn(user);
        when(locator.findFirstStep("user credits is 5 dollars")).thenReturn(credits);
        
        IDocument document= new Document(storyAsText);
        
        offset = 1;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength()-offset);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        checkToken(scanner, document, parameterValueAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex04_exampleTable () throws Exception {
        final StepCandidate user = givenStep("a new account named 'networkAgent' with the following properties (properties not set will be completed) $exampleTable");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx04.story"));
        when(locator.findFirstStep(Mockito.anyString())).thenAnswer(new Answer<StepCandidate>() {
            @Override
            public StepCandidate answer(InvocationOnMock invocation) throws Throwable {
                String searched = (String) invocation.getArguments()[0];
                if(searched.startsWith("a new account named 'networkAgent' with the following properties"))
                    return user;
                else
                    return null;
            }
        });
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        
        checkToken(scanner, document, defaultAttribute); // NL

        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        
        checkToken(scanner, document, defaultAttribute); // NL

        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    public void usecase_ex05_exampleTable () throws Exception {
        final StepCandidate user = givenStep("a new account named 'networkAgent' with the following properties (properties not set will be completed) $exampleTable");
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/data/UseCaseEx06-exampletable.story"));
        when(locator.findFirstStep(Mockito.anyString())).thenAnswer(new Answer<StepCandidate>() {
            @Override
            public StepCandidate answer(InvocationOnMock invocation) throws Throwable {
                String searched = (String) invocation.getArguments()[0];
                if(searched.startsWith("a new account named 'networkAgent' with the following properties"))
                    return user;
                else
                    return null;
            }
        });
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);

        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        
        checkToken(scanner, document, defaultAttribute); // NL
        
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        checkToken(scanner, document, exampleTableCell);
        checkToken(scanner, document, exampleTableSeparator);
        
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    @Ignore
    public void usecase_ex4() throws Exception {
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        
        IDocument document= new Document(storyAsText);
        
        offset = 0;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, document.getLength());
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(document.getLength()));
    }
    
    @Test
    @Ignore
    public void usecase_ex5() throws Exception {
        StepCandidate seeHomePage = thenStep("agent see the application home page");
        when(locator.findFirstStep("agent see the application home page")).thenReturn(seeHomePage);
        
        storyAsText = IOUtils.toString(getClass().getResourceAsStream("/domain/i_can_login_using_parameters_table.story"));
        IDocument document= new Document(storyAsText);
        offset = 477;
        
        StepScanner scanner= new StepScanner(jbehaveProject, textAttributeProvider);
        scanner.setRange(document, offset, 179);
        
        checkToken(scanner, document, keywordAttribute);
        checkToken(scanner, document, defaultAttribute);
        
        consumeRemaining(document, scanner);
        
        assertThat(offset, equalTo(477+179));
    }

    private void consumeRemaining(IDocument document, StepScanner scanner) throws BadLocationException {
        IToken token = scanner.nextToken();
        while(!token.isEOF()) {
            offset += scanner.getTokenLength();
            dumpState(scanner, document);
            token = scanner.nextToken();
        }
    }
    
    private void checkToken(StepScanner scanner, IDocument document, Object object) throws BadLocationException {
        log.debug(object + " > ");
        IToken token = scanner.nextToken();
        dumpState(scanner, document);

        assertThat(token.getData(), equalTo(object));
        assertThat(offset, equalTo(scanner.getTokenOffset()));
        offset += scanner.getTokenLength();
    }
    
    private void dumpState(StepScanner scanner, IDocument doc) throws BadLocationException {
        int tokenOffset = scanner.getTokenOffset();
        int tokenLength = scanner.getTokenLength();
        log.debug(tokenOffset + " ~> " + tokenLength + " >>" + doc.get(tokenOffset, tokenLength) + "<<");
    }
    
    
}
