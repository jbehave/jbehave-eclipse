package org.jbehave.eclipse.editor.step;


import fj.F;

final class FilterByPriority extends F<StepCandidate, Boolean> {
    private final int maxPrio;

    FilterByPriority(int maxPrio) {
        this.maxPrio = maxPrio;
    }

    @Override
    public Boolean f(StepCandidate pStep) {
        return maxPrio == pStep.priority;
    }
}