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

    public void validate() {
        validate(new StoryDocumentUtils(project.getLocalizedStepSupport()).getStoryElements(document));
    }

    public List<StoryElement> getPending() {
		return pending;
	}

    protected void validate(final List<StoryElement> storyElements) {
        final fj.data.List<ValidatingPart> parts = iterableList(storyElements).map(new F<StoryElement,ValidatingPart>() {
            @Override
            public ValidatingPart f(StoryElement storyElement) {
                return new ValidatingPart(storyElement);
            }
        });
        
        Activator.logInfo(PendingStoryValidator.class.getSimpleName()+": Analyzing parts " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(validateStepsAsRunnable(parts));

        try {
            Activator.logInfo(PendingStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(PendingStoryValidator.class.getSimpleName()+": Error while analyzing parts: " + parts, e);
        }

    }
    
   
    protected void validateSteps(final fj.data.List<ValidatingPart> parts) throws JavaModelException {
        final fj.data.List<ValidatingPart> steps = parts.filter(new F<ValidatingPart,Boolean>() {
            public Boolean f(ValidatingPart part) {
                return Keyword.isStep(part.getKeyword());
            };
        });
        
        log.debug("Validating steps");
        
        StepLocator locator = project.getStepLocator();
        locator.traverseSteps(new Visitor<StepCandidate, Object>() {
            @Override
            public void visit(StepCandidate candidate) {
                log.debug("Evaluating step candidate: <{}>", candidate);
                for (ValidatingPart step : steps) {
                    step.evaluateCandidate(candidate);
                }
            }
        });
        
        log.debug("All step candidates have been evaluated");

        for (ValidatingPart step : steps) {
            ConcurrentLinkedQueue<StepCandidate> candidates = step.getCandidates();
            int count = candidates.size();
			if (count == 0) {
	            log.debug("No step candidate found for >>" + Strings.escapeNL(step.text()) + "<<");
				pending.add(step.getStoryElement());
			}
        }
    }
    
}
