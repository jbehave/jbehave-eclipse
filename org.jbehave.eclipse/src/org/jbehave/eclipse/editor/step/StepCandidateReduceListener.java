package org.jbehave.eclipse.editor.step;

import org.eclipse.jdt.core.IMethod;
import org.jbehave.core.steps.StepType;

/**
 * A listener for the MethodToStepCandidateReducer
 */
public interface StepCandidateReduceListener {
    /**
     * Called for any method/pattern combination that can act as a step
     * candidate.
     * 
     * @param method
     *            the method candidate
     * @param stepType extracted step type the method is for
     * @param stepPattern applying pattern
     * @param priority priority value, may be null
     */
    void add(IMethod method, StepType stepType, String stepPattern,
	    Integer priority);
}
