package org.jbehave.eclipse.editor.step;

import static fj.data.List.iterableList;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.JDTUtils;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Ord;

public class StepLocator {

	private static Logger log = LoggerFactory.getLogger(StepLocator.class);

	private JBehaveProject project;

	public StepLocator(JBehaveProject project) {
		this.project = project;
	}

	static boolean findCandidatesCheckStepType = true;

	/**
	 * When '$who' clicks on the '$button_id' button
	 * 
	 * When 'Bob' clicks on the 'login' button When 'Bob' clicks on the ... When
	 * 'Bo ...
	 * 
	 */
	public Iterable<WeightedStep> findCandidatesStartingWith(
			final String step) {
		log.debug("Finding candidates starting with <{}>", step);
		try {
			LocalizedStepSupport localizedStepSupport = project
					.getLocalizedStepSupport();
			final String searchedType = StepSupport.stepType(
					localizedStepSupport, step);
			final String stepWithoutKeyword = StepSupport.stepWithoutKeyword(
					localizedStepSupport, step);

			Visitor<StepCandidate, WeightedStep> findOne = new Visitor<StepCandidate, WeightedStep>() {
				@Override
				public void visit(StepCandidate candidate) {
					boolean sameType = candidate.isTypeEqualTo(searchedType);
					if (findCandidatesCheckStepType && !sameType) {
						return;
					}

					if (StringUtils.isBlank(stepWithoutKeyword) && sameType) {
						add(new WeightedStep(candidate, 0.1f));
						return;
					}

					float weight = candidate.weightOf(stepWithoutKeyword);
					if (weight > 0) {
						add(new WeightedStep(candidate, weight));
					} else {
						// Activator.logInfo(">> Step (" + weight +
						// ") rejected: " + candidate);
					}
				}
			};
			traverseSteps(findOne);
			ConcurrentLinkedQueue<WeightedStep> elements = findOne.getElementsFound();
			log.debug("Candidates starting with <{}> found: #{}", step,
					elements.size());
			return elements;
		} catch (JavaModelException e) {
			log.error("Failed to find candidates for step <" + step + ">",
					e);
			Activator.logError("Failed to find candidates for step <"
					+ step + ">", e);
		}
		return null;
	}

	/**
	 * Returns the first {@link StepCandidate} found that match the step,
	 * ordered by priority. Be careful that there can be several other
	 * {@link StepCandidate}s that fulfill the step too.
	 * 
	 * @param step
	 * @return
	 */
	public StepCandidate findFirstStep(final String step) {
		log.debug("Attempt to find the first step matching <{}>", step);

		try {
			Visitor<StepCandidate, StepCandidate> matchingStepVisitor = new Visitor<StepCandidate, StepCandidate>() {
				@Override
				public void visit(StepCandidate candidate) {
					boolean matches = candidate.matches(step);
					if (matches) {
						add(candidate);
					}
				}
			};
			traverseSteps(matchingStepVisitor);
			StepCandidate found = getFirstStepWithHighestPrio(matchingStepVisitor
					.getElementsFound());
			if (found == null) {
				log.debug("No candidate found matching <{}>", step);
				return null;
			} else {
				log.debug("First candidate matching <{}> found: <{}>", step,
						found.stepPattern);
				return found;
			}
		} catch (JavaModelException e) {
			log.error("Failed to find candidates for step <" + step + ">", e);
			Activator.logError("Failed to find candidates for step <" + step
					+ ">", e);
		}
		return null;
	}

	/**
	 * 
	 * @param findOne
	 * @return
	 */
	private StepCandidate getFirstStepWithHighestPrio(
			Iterable<StepCandidate> candidates) {
		fj.data.List<Integer> collectedPrios = iterableList(candidates).map(
				new TransformByPriority());
		if (collectedPrios.isEmpty()) {
			return null;
		}

		final int maxPrio = collectedPrios.maximum(Ord.intOrd);
		fj.data.List<StepCandidate> maxPrioSteps = iterableList(candidates)
				.filter(new FilterByPriority(maxPrio));

		return maxPrioSteps.head();
	}

	public IJavaElement findMethod(final String step) {
		log.debug("Attempt to find method for <{}>", step);
		StepCandidate pStep = findFirstStep(step);
		if (pStep != null) {
			log.debug("Method found for <{}>: <{}>", step, pStep.method);
			return pStep.method;
		} else {
			log.debug("No method found for <{}>", step);
			return null;
		}
	}

	public IJavaElement findMethodByQualifiedName(final String qualifiedName) {
		log.debug("Attempt to find method using its qualified name <{}>",
				qualifiedName);
		try {
			Visitor<StepCandidate, StepCandidate> findOne = new Visitor<StepCandidate, StepCandidate>() {
				@Override
				public void visit(StepCandidate candidate) {
					String qName = JDTUtils
							.formatQualifiedName(candidate.method);
					if (qName.equals(qualifiedName)) {
						add(candidate);
						done();
					}
				}
			};
			traverseSteps(findOne);
			StepCandidate first = findOne.getFirst();
			if (first == null) {
				log.debug("No method found using its qualified name <{}>",
						qualifiedName);
				return null;
			}
			log.debug("Found method using its qualified name <{}>, got: {}",
					qualifiedName, first.method);
			return first.method;
		} catch (JavaModelException e) {
			Activator.logError("Failed to find candidates for method <"
					+ qualifiedName + ">", e);
		}
		return null;
	}

	public void traverseSteps(Visitor<StepCandidate, ?> visitor)
			throws JavaModelException {
		log.debug("Traversing steps with: {}", visitor.getClass());
		project.traverseSteps(visitor);
	}

}
