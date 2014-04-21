package org.jbehave.eclipse.editor.story.validator;

import static fj.data.List.iterableList;
import static org.jbehave.eclipse.util.Objects.o;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.story.Marks;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.editor.text.MarkData;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.MonitoredExecutor;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.F;

public abstract class AbstractStoryValidator {
    
    private static Logger log = LoggerFactory.getLogger(AbstractStoryValidator.class);

    protected IDocument document;
    protected JBehaveProject project;

    public AbstractStoryValidator(JBehaveProject project, IDocument document) {
        this.project = project;
        this.document = document;
    }

    public void validate() {
        validate(new StoryDocumentUtils(project.getLocalizedStepSupport()).getStoryElements(document));
    }

	protected void validate(final List<StoryElement> storyElements) {
        final fj.data.List<ValidatingPart> parts = iterableList(storyElements).map(new F<StoryElement,ValidatingPart>() {
            @Override
            public ValidatingPart f(StoryElement storyElement) {
                return new ValidatingPart(storyElement);
            }
        });
        
        Activator.logInfo(AbstractStoryValidator.class.getSimpleName()+": Validating: " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(validateStepsAsRunnable(parts));

        try {
            Activator.logInfo(AbstractStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(AbstractStoryValidator.class.getSimpleName()+": Error while validating: " + parts, e);
        }

    }    
   
    protected Runnable validateStepsAsRunnable(final fj.data.List<ValidatingPart> parts)  {
        return new Runnable() {
            public void run() {
                try {
                    validateSteps(parts);
                } catch (Throwable e) {
                    Activator.logError(AbstractStoryValidator.class.getSimpleName()+": Error while validating steps: " + parts, e);
                }
            }
        };
    }

    protected abstract void validateSteps(final fj.data.List<ValidatingPart> parts) throws JavaModelException;
    
    class ValidatingPart {
        private final ConcurrentLinkedQueue<MarkData> marks = New.concurrentLinkedQueue();
        private final ConcurrentLinkedQueue<StepCandidate> candidates = New.concurrentLinkedQueue();
        private final StoryElement storyElement;

        protected ValidatingPart(StoryElement storyElement) {
            this.storyElement = storyElement;
            extractStepWithoutKeywordAndTrailingComment();
        }
        
        protected ConcurrentLinkedQueue<MarkData> getMarks(){
        	return marks;
        }
        
        protected StoryElement getStoryElement(){
        	return storyElement;
        }
        
        protected Keyword getKeyword() {
            return storyElement.getPreferredKeyword();
        }

        public void evaluateCandidate(StepCandidate candidate) {
            Keyword keyword = getKeyword();
            String pattern = stepWithoutKeywordAndTrailingComment;
            log.debug("Evaluating step candidate against {}", storyElement);
            boolean typeMatch = keyword.isSameAs(candidate.stepType);
            boolean patternMatch = candidate.matches(pattern);
            if ( typeMatch && patternMatch ) {
                addCandidate(candidate);
                log.debug("<{} {}> accepts <{} {}>", o(keyword, pattern, 
                        candidate.stepType, candidate.stepPattern));
            } else {
                log.debug("<{} {}> rejects <{} {}>", o(keyword, pattern, 
                        candidate.stepType, candidate.stepPattern));
            }
        }
        
        public ConcurrentLinkedQueue<StepCandidate> getCandidates() {
            return candidates;
        }

        private void addCandidate(StepCandidate candidate) {
            candidates.add(candidate);
        }

        private String stepWithoutKeywordAndTrailingComment;        
        private void extractStepWithoutKeywordAndTrailingComment() {
            // remove any comment that can still be within the step
            String cleaned = RegexUtils.removeComment(storyElement.stepWithoutKeyword());
            stepWithoutKeywordAndTrailingComment = Strings.removeTrailingNewlines(cleaned);
        }

        public MarkData addMark(Marks.Code code, String message, int severity) {
            MarkData mark = new MarkData()
                    .severity(severity)
                    .message(message)
                    .offsetStart(storyElement.getOffsetStart())
                    .offsetEnd(storyElement.getOffsetEnd());
            Marks.putCode(mark, code);
            marks.add(mark);
            return mark;
        }

        public String text() {
            return storyElement.getContent();
        }

        public String textWithoutTrailingNewlines() {
            return Strings.removeTrailingNewlines(text());
        }

        @Override
        public String toString() {
            return "ValidatingPart [offset=" + storyElement.getOffset() + ", length=" + storyElement.getLength() + ", keyword=" + storyElement.getPreferredKeyword() + ", marks="
                    + marks + ", text=" + textWithoutTrailingNewlines() + "]";
        }


    }

}
