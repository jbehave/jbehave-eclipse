package org.jbehave.eclipse.cache;

import org.jbehave.eclipse.editor.step.StepCandidate;

/**
 * This listener is informed about loaded caches
 */
public interface StepCandidateCacheListener {
    void cacheLoaded(MethodCache<StepCandidate> cache);
}
