package org.jbehave.eclipse.editor.step;

import org.jbehave.eclipse.editor.JDTUtils;
import org.jbehave.eclipse.util.HasHTMLComment;

public class WeightedStep implements Comparable<WeightedStep>, HasHTMLComment {
	public final StepCandidate stepCandidate;
	public final float weight;

	public WeightedStep(StepCandidate stepCandidate, float weight) {
		this.stepCandidate = stepCandidate;
		this.weight = weight;
	}

	@Override
	public int compareTo(WeightedStep o) {
		return (weight > o.weight) ? 1 : -1;
	}

	private String htmlComment;

	@Override
	public String getHTMLComment() {
		if (htmlComment == null) {
			try {
				htmlComment = JDTUtils.getJavadocOf(stepCandidate.method);
			} catch (Exception e) {
				htmlComment = "No documentation found";
			}
		}
		return htmlComment;
	}
}