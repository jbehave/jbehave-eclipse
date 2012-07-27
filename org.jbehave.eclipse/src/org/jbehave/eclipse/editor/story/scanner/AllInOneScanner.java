package org.jbehave.eclipse.editor.story.scanner;

import java.util.Iterator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.parser.StoryElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllInOneScanner extends StoryTokenScanner {
    
    private Logger logger = LoggerFactory.getLogger(AllInOneScanner.class);
    
    public static boolean allInOne = true;
    
    private ExampleTableScanner exampleTableScanner;
    private MetaScanner metaScanner;
    private NarrativeScanner narrativeScanner;
    private ScenarioScanner scenarioScanner;
    private StepScanner stepScannerStyled;

    private Region realRange;

    private Token errorToken;

    public AllInOneScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        super(jbehaveProject, textAttributeProvider);
        initialize();
        exampleTableScanner = new ExampleTableScanner(jbehaveProject, textAttributeProvider);
        metaScanner = new MetaScanner(jbehaveProject, textAttributeProvider);
        narrativeScanner = new NarrativeScanner(jbehaveProject, textAttributeProvider);
        scenarioScanner = new ScenarioScanner(jbehaveProject, textAttributeProvider);
        stepScannerStyled = new StepScanner(jbehaveProject, textAttributeProvider);
    }
    
    @Override
    public void setRange(IDocument document, int offset, int length) {
        realRange = new Region(offset, length);
        super.setRange(document, 0, document.getLength());
    }
    
    @Override
    protected void evaluateFragments() {
        super.evaluateFragments();
        Iterator<Fragment> iterator = getFragments().iterator();
        while(iterator.hasNext()) {
            Fragment fragment = iterator.next();
            if(!fragment.intersects(realRange)) {
                iterator.remove();
            }
        }
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        TextAttribute textAttribute;
        textAttribute = textAttributeProvider.get(TextStyle.DEFAULT);
        setDefaultToken(new Token(textAttribute));
        textAttribute = textAttributeProvider.get(TextStyle.ERROR);
        errorToken = new Token(textAttribute);
    }
    
    protected Token getErrorToken() {
        return errorToken;
    }

    @Override
    protected boolean isAccepted(StoryElement element) {
        return true;
    }
    
    @Override
    protected void emit(StoryElement element) {
        Keyword keyword = element.getPreferredKeyword();
        if(keyword==null) {
			logger.debug("No keyword found for story element: {}", element);
            emitCommentAware(getErrorToken(), element.getOffset(), element.getContent());
            return;
        }
        switch(keyword) {
            case Given:
            case When:
            case Then:
            case And:
                emit(stepScannerStyled, element);
                break;
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableRow:
            case ExamplesTableValueSeparator:
                emit(exampleTableScanner, element);
                break;
            case Narrative:
            case AsA:
            case InOrderTo:
            case IWantTo:
                emit(narrativeScanner, element);
                break;
            case GivenStories:
            case Meta:
            case MetaProperty:
                emit(metaScanner, element);
                break;
            case Scenario:
                emit(scenarioScanner, element);
                break;
            case Ignorable:
            default:
                emitCommentAware(getDefaultToken(), element.getOffset(), element.getContent());
                break;
        }
    }

    private void emit(StoryTokenScanner scanner, StoryElement element) {
        scanner.setRange(document, 0, document.getLength());
        scanner.emit(element);
        addFragments(scanner.getFragments());
    }
}
