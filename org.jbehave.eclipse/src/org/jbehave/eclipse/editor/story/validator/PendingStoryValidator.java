package org.jbehave.eclipse.editor.story.validator;

import static fj.data.List.iterableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.step.StepLocator;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.MonitoredExecutor;
import org.jbehave.eclipse.util.Strings;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.F;

public class PendingStoryValidator extends AbstractStoryValidator {
    
    private static Logger log = LoggerFactory.getLogger(PendingStoryValidator.class);

    private List<StoryElement> pending = new ArrayList<StoryElement>();

    public PendingStoryValidator(JBehaveProject project, IDocument document) {
    	super(project, document);
    }

    public void validate(Runnable afterApplyCallback) {
        analyze(new StoryDocumentUtils(project.getLocalizedStepSupport()).getStoryElements(document), afterApplyCallback);
    }

    public List<StoryElement> getPending() {
		return pending;
	}

    protected void analyze(final List<StoryElement> storyElements, final Runnable afterApplyCallback) {
        final fj.data.List<Part> parts = iterableList(storyElements).map(new F<StoryElement,Part>() {
            @Override
            public Part f(StoryElement storyElement) {
                return new Part(storyElement);
            }
        });
        
        Activator.logInfo(PendingStoryValidator.class.getSimpleName()+": Analyzing parts " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(checkStepsAsRunnable(parts));

        try {
            Activator.logInfo(PendingStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(PendingStoryValidator.class.getSimpleName()+": Error while analyzing parts: " + parts, e);
        }

    }
    
   
    protected void checkSteps(final fj.data.List<Part> parts) throws JavaModelException {
        final fj.data.List<Part> steps = parts.filter(new F<Part,Boolean>() {
            public Boolean f(Part part) {
                return Keyword.isStep(part.partType());
            };
        });
        
        log.debug("Checking steps");
        
        StepLocator locator = project.getStepLocator();
        locator.traverseSteps(new Visitor<StepCandidate, Object>() {
            @Override
            public void visit(StepCandidate candidate) {
                log.debug("Evaluating candidate: <{}>", candidate);
                for (Part part : steps) {
                    part.evaluateCandidate(candidate);
                }
            }
        });
        
        log.debug("All candidates have been evaluated");

        for (Part part : steps) {
            String pattern = part.extractStepSentenceAndRemoveTrailingNewlines();
            ConcurrentLinkedQueue<StepCandidate> candidates = part.getCandidates();
            int count = candidates.size();
            log.debug("#" + count + "result(s) found for >>" + Strings.escapeNL(pattern) + "<<");
            if (count == 0){
            	pending.add(part.getStoryElement());
            }
        }
    }
    
}
