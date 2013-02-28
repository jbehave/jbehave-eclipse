package org.jbehave.eclipse.cache;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.util.ProcessGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Effect;

/**
 * Responsible for loading caches and providing the most recent instance
 */
public class StepCandidateCacheLoader {
    private static final Logger log = LoggerFactory
	    .getLogger(StepCandidateCacheLoader.class);

    private final StepCandidateCacheListener listener;

    private final AtomicBoolean loadInProgress = new AtomicBoolean(false);

    private Runnable pendingLoadRequest;

    public StepCandidateCacheLoader(StepCandidateCacheListener listener) {
	this.listener = listener;
    }

    public void requestReload(MethodCache<StepCandidate> cache,
	    IProject project, Effect<JavaScanner<?>> scanInitializer) {
	Runnable request = getReloadAsRunnable(cache, project, scanInitializer);

	synchronized (this.loadInProgress) {
	    if (this.loadInProgress.get()) {
		this.pendingLoadRequest = request;
	    } else {
		executeLoadRequest(request);
	    }
	}
    }

    private Runnable getReloadAsRunnable(
	    final MethodCache<StepCandidate> cache, final IProject project,
	    final Effect<JavaScanner<?>> scanInitializer) {
	return new Runnable() {

	    @Override
	    public void run() {
		log.info("Rebuilding cache for project " + project.getName());
		Activator processGroupFactory = Activator.getDefault();

		final ProcessGroup<Void> rebuildProcess = processGroupFactory
			.newProcessGroup();
		try {
		    cache.rebuild(project, scanInitializer, rebuildProcess);
		} catch (JavaModelException e) {
		    log.error("Error during startup of cache rebuild", e);
		}

		ProcessGroup<Void> completionProcess = processGroupFactory
			.newProcessGroup();

		completionProcess.spawn(new Runnable() {

		    @Override
		    public void run() {
			try {
			    rebuildProcess.awaitTermination();
			    onCacheLoaded(cache);
			} catch (InterruptedException e) {
			    Thread.interrupted();
			    log.warn("Cache rebuild interrupted");
			    onCacheLoadComplete();
			}
		    }
		});
	    }
	};
    }

    private void onCacheLoaded(MethodCache<StepCandidate> cache) {
	this.listener.cacheLoaded(cache);

	onCacheLoadComplete();
    }

    private void executeLoadRequest(Runnable request) {
	this.loadInProgress.set(true);
	request.run();
    }

    private void onCacheLoadComplete() {
	synchronized (this.loadInProgress) {
	    if (pendingLoadRequest != null) {
		Runnable request = this.pendingLoadRequest;

		this.pendingLoadRequest = null;
		executeLoadRequest(request);
	    } else {
		this.loadInProgress.set(false);
	    }
	}
    }
}
