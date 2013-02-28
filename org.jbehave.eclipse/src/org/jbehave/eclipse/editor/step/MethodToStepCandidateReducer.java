package org.jbehave.eclipse.editor.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.steps.PatternVariantBuilder;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.cache.container.Containers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes IMethod instances, analyzes their annotations for JBehave
 * related bindings and, if matching, creates StepCandidates to extend the
 * collecting container.
 */
public class MethodToStepCandidateReducer {
    private static final Logger log = LoggerFactory
	    .getLogger(MethodToStepCandidateReducer.class);

    private static final List<String> HANDLED_ANNOTATION_NAMES;

    static {
	HANDLED_ANNOTATION_NAMES = Arrays.asList(Given.class.getSimpleName(),
		When.class.getSimpleName(), Then.class.getSimpleName(),
		Alias.class.getSimpleName(), Aliases.class.getSimpleName());
    }

    /**
     * Constructor
     */
    public MethodToStepCandidateReducer() {

    }

    /**
     * This method checks the method and reports a step candidate if it
     * represents one.
     * 
     * @param method
     *            the method to analyze
     * @param listener
     *            for collecting results
     * @throws JavaModelException
     *             at problems extracting model information
     */
    public void reduce(final IMethod method,
	    final StepCandidateReduceListener listener)
	    throws JavaModelException {
	StepType stepType = null;
	for (final IAnnotation annotation : method.getAnnotations()) {
	    final String fullQualifiedName = getFullQualifiedName(annotation);
	    IMemberValuePair[] annotationAttributes = annotation
		    .getMemberValuePairs();
	    Integer priority = Integer.valueOf(0);
	    boolean basicStep = false;

	    List<String> patterns = new ArrayList<String>();
	    if (Given.class.getName().equals(fullQualifiedName)) {
		stepType = StepType.GIVEN;
		basicStep = true;
	    } else if (When.class.getName().equals(fullQualifiedName)) {
		stepType = StepType.WHEN;
		basicStep = true;
	    } else if (Then.class.getName().equals(fullQualifiedName)) {
		stepType = StepType.THEN;
		basicStep = true;
	    }

	    if (basicStep) {
		String stepPattern = JBehaveProject.getValue(
			annotationAttributes, "value");
		priority = JBehaveProject.getValue(annotationAttributes,
			"priority");
		PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
		for (String variant : b.allVariants()) {
		    patterns.add(variant);
		}
	    } else if (Aliases.class.getName().equals(fullQualifiedName)) {
		Object aliases = JBehaveProject.getValue(annotationAttributes,
			"values");
		if (aliases instanceof Object[]) {
		    for (Object o : (Object[]) aliases) {
			if (o instanceof String) {
			    PatternVariantBuilder b = new PatternVariantBuilder(
				    (String) o);
			    for (String variant : b.allVariants()) {
				patterns.add(variant);
			    }
			}
		    }
		    if (!patterns.isEmpty() && stepType == null)
			stepType = StepType.GIVEN;
		}
	    } else if (Alias.class.getName().equals(fullQualifiedName)) {
		String stepPattern = JBehaveProject.getValue(
			annotationAttributes, "value");
		PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
		for (String variant : b.allVariants()) {
		    patterns.add(variant);
		}

		if (!patterns.isEmpty() && stepType == null)
		    stepType = StepType.GIVEN;
	    }

	    if (!patterns.isEmpty()) {
		log.debug("Analysing method: " + Containers.pathOf(method)
			+ " found: " + patterns);
		for (String stepPattern : patterns) {
		    if (stepPattern == null) {
			continue;
		    }
		    listener.add(method, stepType, stepPattern, priority);
		}
	    }
	}
    }

    private String getFullQualifiedName(IAnnotation annotation) {
	String elementName = annotation.getElementName();
	StringBuilder fullQualifiedName = new StringBuilder();
	boolean isQualified = elementName.indexOf('.') >= 0;

	if (!isQualified
		&& (HANDLED_ANNOTATION_NAMES.indexOf(elementName) >= 0)) {
	    // TODO check import declaration matches org.jbehave...
	    fullQualifiedName.append("org.jbehave.core.annotations.");
	}
	fullQualifiedName.append(elementName);

	return fullQualifiedName.toString();
    }
}
