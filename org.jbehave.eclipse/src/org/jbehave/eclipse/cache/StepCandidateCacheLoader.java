package org.jbehave.eclipse.cache;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.util.MonitoredExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Effect;

/**
 * Responsible for loading caches and providing the most recent instance
 */
public class StepCandidateCacheLoader {
    private static final Logger log = LoggerFactory
	    .getLogger(StepCandidateCacheLoader.class);

    private final Executor scanningExecutor;

    private final Executor completionExecutor;

    private final StepCandidateCacheListener listener;

    private final AtomicBoolean loadInProgress = new AtomicBoolean(false);

    private Runnable pendingLoadRequest;

    /**
     * Constructor. The completionExecutor must work with a different thread
     * pool than the scanningExecutor. Otherwise a dead lock will happen.
     * 
     * @param listener
     *            the listener that will be informed of completion.
     * @param scanningExecutor
     *            the executor that will perform the scanning.
     * @param completionExecutor
     *            the executor that will wait for the scan completion and issue
     *            the callback.
     */
    public StepCandidateCacheLoader(final StepCandidateCacheListener listener,
	    final Executor scanningExecutor, final Executor completionExecutor) {
	this.listener = listener;
	this.scanningExecutor = scanningExecutor;
	this.completionExecutor = completionExecutor;
    }

    /**
     * Requests to load and fill the given cache asynchronously. This method can
     * be called any number of times, as long as a reload is in progress, only
     * the last request will be processed when the current one finished.
     * 
     * @param cache
     *            the cache to fill and return when done.
     * @param project
     *            the project from which to extract data.
     * @param scanInitializer
     *            the initializer used for the scanner.
     */
    public void requestReload(MethodCache<StepCandidate> cache,
	    IJavaProject project, Effect<JavaScanner<?>> scanInitializer) {
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
	    final MethodCache<StepCandidate> cache, final IJavaProject project,
	    final Effect<JavaScanner<?>> scanInitializer) {
	return new Runnable() {

	    @Override
	    public void run() {
		log.info("Rebuilding cache for project " + project.getElementName());
		final MonitoredExecutor rebuildProcess = new MonitoredExecutor(
			scanningExecutor);

		try {
		    cache.rebuild(project, scanInitializer, rebuildProcess);
		} catch (JavaModelException e) {
		    log.error("Error during startup of cache rebuild", e);
		}

		completionExecutor.execute(new Runnable() {

		    @Override
		    public void run() {
			try {
			    rebuildProcess.awaitCompletion();
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
