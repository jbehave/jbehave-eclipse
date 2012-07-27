package org.jbehave.eclipse.editor.step;

import static org.jbehave.eclipse.util.StringDecorator.decorate;
import static org.jbehave.eclipse.util.Strings.array;

import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.util.StringDecorator;
import org.jbehave.eclipse.util.Strings;

public class StepSupport {

	public static boolean isStartOfStepIgnoringCase(
			LocalizedStepSupport localizedStepSupport, String step) {
		return decorate(step).isStartOfOneOfIgnoringCase(//
				localizedStepSupport.given(true), //
				localizedStepSupport.when(true), //
				localizedStepSupport.then(true), //
				localizedStepSupport.and(true));
	}

	public static boolean isStepIgnoringCase(
			LocalizedStepSupport localizedStepSupport, String step) {
		return decorate(step).startsWithOneOfIgnoringCase(//
				localizedStepSupport.given(true), //
				localizedStepSupport.when(true), //
				localizedStepSupport.then(true), //
				localizedStepSupport.and(true));
	}

	public static boolean isStep(LocalizedStepSupport localizedStepSupport,
			String step) {
		return decorate(step).equalsToOneOf(//
				localizedStepSupport.given(true), //
				localizedStepSupport.when(true), //
				localizedStepSupport.then(true), //
				localizedStepSupport.and(true));
	}

	public static boolean isStepAnd(
			LocalizedStepSupport localizedStepSupport, String step) {
		return decorate(step).startsWithOneOfIgnoringCase(
				localizedStepSupport.and(true));
	}

	public static int stepKeywordIndex(
			LocalizedStepSupport localizedStepSupport, String step) {
		StringDecorator enhanced = decorate(step);
		for (String prefix : array(//
				localizedStepSupport.given(true), //
				localizedStepSupport.when(true), //
				localizedStepSupport.then(true), //
				localizedStepSupport.and(true))) {
			if (enhanced.startsWithIgnoringCase(prefix)) {
				return prefix.length();
			}
		}
		return 0;
	}

	/**
	 * Remove the step keyword from the given line. <strong>In case of multiline
	 * step</strong> you may prefer to use the
	 * {@link #extractStepSentenceAndRemoveTrailingNewlines(String)}
	 * alternative.
	 * 
	 * @param step
	 * @return A step without keyword
	 */
	public static String stepWithoutKeyword(
			LocalizedStepSupport localizedStepSupport, String step) {
		return step.substring(stepKeywordIndex(localizedStepSupport, step));
	}

	public static String stepWithoutKeywordAndTrailingNewlines(
			LocalizedStepSupport localizedStepSupport, String step) {
		return Strings.removeTrailingNewlines(stepWithoutKeyword(
				localizedStepSupport, step));
	}

	public static String stepType(LocalizedStepSupport localizedStepSupport,
			String step) {
		StringDecorator enhanced = decorate(step);
		if (enhanced.startsWithIgnoringCase(localizedStepSupport.when(true)))
			return StepType.WHEN.name();
		else if (enhanced.startsWithIgnoringCase(localizedStepSupport
				.given(true)))
			return StepType.GIVEN.name();
		else if (enhanced.startsWithIgnoringCase(localizedStepSupport
				.then(true)))
			return StepType.THEN.name();
		return null;
	}
}
