package org.jbehave.eclipse.editor.story;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.resources.IMarker;
import org.jbehave.eclipse.editor.JDTUtils;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.text.MarkData;

public class Marks {
    public static final String ERROR_CODE = "errorCode";
    public static final String STEPS_HTML = "stepsHtml";
    public static final String MESSAGE = "message";

    public enum Code {
        Unknown(-1),
        MultipleMatchingSteps(1),
        MultipleMatchingSteps_PrioritySelection(12),
        NoMatchingStep(2),
        InvalidNarrativePosition(3),
        InvalidNarrativeSequence_multipleNarrative(4),
        InvalidNarrativeSequence_multipleInOrderTo(5),    
        InvalidNarrativeSequence_multipleAsA(6),
        InvalidNarrativeSequence_multipleIWantTo(7),
        InvalidNarrativeSequence_missingNarrative(8),
        InvalidNarrativeSequence_missingInOrderTo(9),
        InvalidNarrativeSequence_missingAsA(10),
        InvalidNarrativeSequence_missingIWantTo(11);
        
        private final int legacyCode;
        private Code(int legacyCode) {
            this.legacyCode = legacyCode;
        }
        public static Code lookup(int intCode, Code fallback) {
            for(Code c : values()) {
                if(c.legacyCode == intCode)
                    return c;
            }
            return fallback;
        }
    }

    
    public static MarkData putCode(MarkData markData, Code errorCode) {
        return markData.attribute(ERROR_CODE, errorCode.legacyCode);
    }
    
    public static Code getCode(IMarker marker) {
        return Code.lookup(marker.getAttribute(ERROR_CODE, -1), Code.Unknown);
    }

    public static MarkData putStepsAsHtml(MarkData mark, Iterable<StepCandidate> candidates) {
        StringBuilder builder = new StringBuilder();
        builder.append("<ul>");
        for(StepCandidate pStep : candidates) {
            String qualifiedName = JDTUtils.formatQualifiedName(pStep.method);
            builder
                .append("<li>")
                .append("<b>")
                .append(StringEscapeUtils.escapeHtml(pStep.stepPattern))
                .append("</b>")
                .append(" (<code>")
                .append("<a href=\"").append(qualifiedName).append("\">")
                .append(qualifiedName)
                .append("</a>")
                .append("</code>)")
                .append("</li>");
        }
        builder.append("</ul>");
        return mark.attribute(STEPS_HTML, builder.toString());
    }

}
