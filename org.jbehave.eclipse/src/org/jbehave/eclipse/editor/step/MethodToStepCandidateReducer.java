package org.jbehave.eclipse.editor.step;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.steps.PatternVariantBuilder;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.cache.container.Container;
import org.jbehave.eclipse.cache.container.Containers;
import org.jbehave.eclipse.util.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class takes IMethod instances, analyzes their annotations for JBehave
 * related bindings and, if matching, creates StepCandidates to extend the
 * collecting container.
 */
public class MethodToStepCandidateReducer {
    private static Logger log = LoggerFactory
	    .getLogger(MethodToStepCandidateReducer.class);

    private final String parameterPrefix;

    private final LocalizedStepSupport localizedStepSupport;

    /**
     * Constructor
     * 
     * @param parameterPrefix
     *            value passed on to created StepCandidates
     * @param localizedStepSupport
     *            value passed on to created StepCandidates
     */
    public MethodToStepCandidateReducer(final String parameterPrefix,
	    LocalizedStepSupport localizedStepSupport) {
	this.parameterPrefix = parameterPrefix;
	this.localizedStepSupport = localizedStepSupport;
    }

    /**
     * This method checks the method and reports a step candidate if it
     * represents one.
     * 
     * @param method
     *            the method to analyze
     * @param container
     *            the container to feed
     * @throws JavaModelException
     *             at problems extracting model information
     */
    public void reduce(final IMethod method, Container<StepCandidate> container)
	    throws JavaModelException {
	StepType stepType = null;
	for (IAnnotation annotation : method.getAnnotations()) {
	    String elementName = annotation.getElementName();
	    IMemberValuePair[] annotationAttributes = annotation
		    .getMemberValuePairs();
	    Integer priority = Integer.valueOf(0);

	    List<String> patterns = new ArrayList<String>();
	    if (StringDecorator.decorate(elementName).endsWithOneOf("Given",
		    "When", "Then")) {
		// TODO check import declaration matches org.jbehave...
		stepType = StepType.valueOf(elementName.toUpperCase());
		String stepPattern = JBehaveProject.getValue(
			annotationAttributes, "value");
		priority = JBehaveProject.getValue(annotationAttributes,
			"priority");
		PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
		for (String variant : b.allVariants()) {
		    patterns.add(variant);
		}
	    } else if (StringDecorator.decorate(elementName).endsWithOneOf(
		    "Aliases")) {
		// TODO check import declaration matches org.jbehave...
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
	    } else if (StringDecorator.decorate(elementName).endsWithOneOf(
		    "Alias")) {
		// TODO check import declaration matches org.jbehave...
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
		    container.add(new StepCandidate(//
			    this.localizedStepSupport,//
			    this.parameterPrefix,//
			    method, //
			    annotation, //
			    stepType, //
			    stepPattern, //
			    priority));
		}
	    }
	}
    }
}
