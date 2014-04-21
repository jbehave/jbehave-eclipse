package org.jbehave.eclipse.editor.story.validator;

import static fj.data.List.iterableList;
import static org.jbehave.eclipse.util.Objects.o;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IMarker;
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

    public void validate(Runnable afterApplyCallback) {
        analyze(new StoryDocumentUtils(project.getLocalizedStepSupport()).getStoryElements(document), afterApplyCallback);
    }

	protected void analyze(final List<StoryElement> storyElements, final Runnable afterApplyCallback) {
        final fj.data.List<Part> parts = iterableList(storyElements).map(new F<StoryElement,Part>() {
            @Override
            public Part f(StoryElement storyElement) {
                return new Part(storyElement);
            }
        });
        
        Activator.logInfo(AbstractStoryValidator.class.getSimpleName()+": Analyzing parts " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(checkStepsAsRunnable(parts));

        try {
            Activator.logInfo(AbstractStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(AbstractStoryValidator.class.getSimpleName()+": Error while analyzing parts: " + parts, e);
        }

    }
    
   
    protected Runnable checkStepsAsRunnable(final fj.data.List<Part> parts)  {
        return new Runnable() {
            public void run() {
                try {
                    checkSteps(parts);
                } catch (Throwable e) {
                    Activator.logError(AbstractStoryValidator.class.getSimpleName()+": Error while checking steps for parts: " + parts, e);
                }
            }
        };
    }

    protected abstract void checkSteps(final fj.data.List<Part> parts) throws JavaModelException;
    
    class Part {
        private final ConcurrentLinkedQueue<MarkData> marks = New.concurrentLinkedQueue();
        private final ConcurrentLinkedQueue<StepCandidate> candidates = New.concurrentLinkedQueue();
        private final StoryElement storyElement;

        protected Part(StoryElement storyElement) {
            super();
            this.storyElement = storyElement;
            computeExtractStepSentenceAndRemoveTrailingNewlines();
        }
        
        protected ConcurrentLinkedQueue<MarkData> getMarks(){
        	return marks;
        }
        
        protected StoryElement getStoryElement(){
        	return storyElement;
        }
        
        protected Keyword partType() {
            return storyElement.getPreferredKeyword();
        }

        public void evaluateCandidate(StepCandidate candidate) {
            String pattern = extractStepSentenceAndRemoveTrailingNewlines();
            Keyword type = partType();
            log.debug("Candidate evaluated against part {}", storyElement);
            boolean patternMatch = candidate.matches(pattern);
            boolean typeMatch = type.isSameAs(candidate.stepType);
            if (patternMatch && typeMatch) {
                addCandidate(candidate);
                log.debug("<{} {}> accepts <{} {}>", o(type, pattern, //
                        candidate.stepType, candidate.stepPattern));
            }
            else {
                log.debug("<{} {}> rejects <{} {}>", o(type, pattern, //
                        candidate.stepType, candidate.stepPattern));
            }
        }
        
        public ConcurrentLinkedQueue<StepCandidate> getCandidates() {
            return candidates;
        }

        private void addCandidate(StepCandidate candidate) {
            candidates.add(candidate);
        }

        private String extractStepSentenceAndRemoveTrailingNewlines;
        public String extractStepSentenceAndRemoveTrailingNewlines() {
            return extractStepSentenceAndRemoveTrailingNewlines;
        }
        
        private void computeExtractStepSentenceAndRemoveTrailingNewlines() {
            String stepSentence = storyElement.stepWithoutKeyword();
            // remove any comment that can still be within the step
            String cleaned = RegexUtils.removeComment(stepSentence);
            extractStepSentenceAndRemoveTrailingNewlines = Strings.removeTrailingNewlines(cleaned);
        }

        public MarkData addErrorMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_ERROR);
        }
        
        public MarkData addInfoMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_INFO);
        }

        public MarkData addWarningMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_WARNING);
        }

        public MarkData addMark(Marks.Code code, String message, int severity) {
            MarkData markData = new MarkData()//
                    .severity(severity)//
                    .message(message)//
                    .offsetStart(storyElement.getOffsetStart())//
                    .offsetEnd(storyElement.getOffsetEnd());
            Marks.putCode(markData, code);
            marks.add(markData);
            return markData;
        }

        public String text() {
            return storyElement.getContent();
        }

        public String textWithoutTrailingNewlines() {
            return Strings.removeTrailingNewlines(text());
        }

        @Override
        public String toString() {
            return "Part [offset=" + storyElement.getOffset() + ", length=" + storyElement.getLength() + ", keyword=" + storyElement.getPreferredKeyword() + ", marks="
                    + marks + ", text=" + textWithoutTrailingNewlines() + "]";
        }


    }

}
