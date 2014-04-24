package org.jbehave.eclipse.editor.step;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.StepType;

/**
 * A StepCandidate is associated to a JDT IMethod and IAnnotation that can be
 * matched to a textual step. It plays an analogous role to the JBehave Core
 * StepCandidate.
 */
public class StepCandidate {
	private final LocalizedStepSupport localizedSupport;
	private final String parameterPrefix;
	public final IMethod method;
	public final StepType stepType;
	public final String stepPattern;
	public final Integer priority;
	private ParametrizedStep parametrizedStep;
	private StepPatternParser stepParser;
	private StepMatcher matcher;

	public StepCandidate(LocalizedStepSupport localizedSupport,
			String parameterPrefix, IMethod method,
			StepType stepType, String stepPattern, Integer priority) {
		this.localizedSupport = localizedSupport;
		this.parameterPrefix = parameterPrefix;
		this.method = method;
		this.stepType = stepType;
		this.stepPattern = stepPattern;
		this.stepParser = new RegexPrefixCapturingPatternParser(parameterPrefix);
		this.priority = (priority == null) ? Integer.valueOf(0) : priority
				.intValue();
	}

	public float weightOf(String input) {
		return getParametrizedStep().weightOf(input);
	}

	public ParametrizedStep getParametrizedStep() {
		if (parametrizedStep == null) {
			parametrizedStep = new ParametrizedStep(stepPattern,
					parameterPrefix);
		}
		return parametrizedStep;
	}

	public boolean hasParameters() {
		return getParametrizedStep().getParameterCount() > 0;
	}

	public boolean isTypeEqualTo(String searchedType) {
		return StringUtils.equalsIgnoreCase(searchedType, stepType.name());
	}

	public String fullStep() {
		return typeWord() + " " + stepPattern;
	}

	public String typeWord() {
		switch (stepType) {
		case WHEN:
			return localizedSupport.when(false);
		case THEN:
			return localizedSupport.then(false);
		case GIVEN:
		default:
			return localizedSupport.given(false);
		}
	}

	public boolean matches(String stepWithoutKeyword) {
		return matcher().matches(stepWithoutKeyword);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[").append(stepType).append("]").append(stepPattern)
				.append(", ");
		if (method == null) {
			builder.append("n/a");
		} else {
			IType classFile = method.getDeclaringType();
			if (classFile != null)
				builder.append(classFile.getElementName());
			else
				builder.append("<type-unknown>");
			builder.append('#').append(method.getElementName());

			if (priority != 0) {
				builder.append(", priority ").append(priority);
			}
		}
		return builder.toString();
	}

	private StepMatcher matcher() {
		if (matcher == null) {
			matcher = stepParser.parseStep(stepType, stepPattern);
		}
		return matcher;
	}

}