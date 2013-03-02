package org.jbehave.eclipse.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import fj.Effect;

@RunWith(MockitoJUnitRunner.class)
public class StepCandidateCacheLoaderTest {

    /**
     * A checkpoint is a thread notifier that holds its signaled state for later
     * checks. It starts in the non-signaled state and can only be once in this
     * state.
     */
    private static class Checkpoint {
	private final Object signal = new Object();

	private final AtomicBoolean signalSet = new AtomicBoolean(false);

	public void setSignal() {
	    synchronized (this.signal) {
		this.signalSet.set(true);
		this.signal.notifyAll();
	    }
	}

	public void waitForSignal() {
	    try {
		waitForSignalUnsafe();
	    } catch (final InterruptedException e) {
		Assert.fail("Interrupted Exception");
	    }
	}

	private void waitForSignalUnsafe() throws InterruptedException {
	    synchronized (this.signal) {
		if (!this.signalSet.get()) {
		    this.signal.wait();
		}
	    }
	}
    }

    /**
     * This executor either runs tasks in a dedicated thread or directly.
     * Default is to run tasks directly.
     * 
     * Tasks sent to the dedicated thread are blocked until signaled to run.
     */
    private static class RiggedExecutor implements Executor {
	private final AtomicBoolean directExecution = new AtomicBoolean(true);

	private final Checkpoint completeThreadExecution = new Checkpoint();

	private final Checkpoint threadExecutionStopped = new Checkpoint();

	private final Executor executor = Executors.newSingleThreadExecutor();

	/** {@inheritDoc} */
	@Override
	public void execute(Runnable command) {
	    if (directExecution.get()) {
		command.run();
	    } else {
		runInExecutor(command);
	    }
	}

	public void setDirectExecution(boolean value) {
	    directExecution.set(value);
	}

	public void signalBlockedThread() {
	    completeThreadExecution.setSignal();
	    threadExecutionStopped.waitForSignal();
	}

	private void runInExecutor(final Runnable command) {
	    executor.execute(new Runnable() {

		@Override
		public void run() {
		    completeThreadExecution.waitForSignal();
		    try {
			command.run();
		    } finally {
			threadExecutionStopped.setSignal();
		    }
		}
	    });
	}

    }

    private static final Executor DIRECT_EXECUTOR = new Executor() {

	@Override
	public void execute(Runnable command) {
	    command.run();
	}
    };

    private Executor scanningExecutor;

    private RiggedExecutor completionExecutor;

    private StepCandidateCacheLoader loader;

    private StepCandidateCacheListener listener;

    private List<MethodCache<StepCandidate>> notifiedCaches;

    @Mock
    private IJavaProject javaProject;

    private Effect<JavaScanner<?>> scanInitializer;

    private List<IPackageFragmentRoot> rootPackageFragments;

    @Test
    public void testListenerCallbackCalled_WhenSimpleCase() {
	MethodCache<StepCandidate> cache = createCache();

	givenAnInitializedLoader();

	whenRequestingLoad(cache);

	thenCacheShouldHaveBeenNotified(cache);
    }

    @Test
    public void testLoaderDoesNotUseScanningExecutorForCompletionWait() {
	MethodCache<StepCandidate> cache = createCache();

	givenAMockedScanningExecutor();
	givenAnInitializedLoader();

	whenRequestingLoad(cache);

	thenScanningExecutorShouldNotHaveBeenUsed();
    }

    @Test
    public void testSecondReloadWaitsUntilFirstIsNotified_WhenRequestedDuringProcess() {
	MethodCache<StepCandidate> cacheA = createCache();
	MethodCache<StepCandidate> cacheB = createCache();

	givenAnInitializedLoader();
	givenTheNextCacheReloadWouldBlock();
	givenReloadWasRequested(cacheA);
	givenTheNextCacheReloadWouldRunInstantly();
	givenReloadWasRequested(cacheB);

	whenTheBlockedCacheReloadCompletes();

	thenCachesShouldHaveBeenNotifiedInOrder(cacheA, cacheB);
    }

    @Test
    public void testSecondReloadIsSkipped_WhenThreeRequestedWhileFirstIsBlocking() {
	MethodCache<StepCandidate> cacheA = createCache();
	MethodCache<StepCandidate> cacheB = createCache();
	MethodCache<StepCandidate> cacheC = createCache();

	givenAnInitializedLoader();
	givenTheNextCacheReloadWouldBlock();
	givenReloadWasRequested(cacheA);
	givenTheNextCacheReloadWouldRunInstantly();
	givenReloadWasRequested(cacheB);
	givenReloadWasRequested(cacheC);

	whenTheBlockedCacheReloadCompletes();

	thenCachesShouldHaveBeenNotifiedInOrder(cacheA, cacheC);
    }

    @Test
    public void testAnotherReloadIsPossible_WhenFirstOneCompleted() {
	MethodCache<StepCandidate> cacheA = createCache();
	MethodCache<StepCandidate> cacheB = createCache();

	givenAnInitializedLoader();
	givenReloadWasRequested(cacheA);

	whenRequestingLoad(cacheB);

	thenCacheShouldHaveBeenNotified(cacheB);
    }

    @Before
    public void setUp() throws Exception {
	this.scanningExecutor = DIRECT_EXECUTOR;
	this.completionExecutor = new RiggedExecutor();
	this.notifiedCaches = new ArrayList<MethodCache<StepCandidate>>();

	this.scanInitializer = new Effect<JavaScanner<?>>() {
	    @Override
	    public void e(JavaScanner<?> scanner) {

	    }
	};

	this.listener = new StepCandidateCacheListener() {

	    @Override
	    public void cacheLoaded(MethodCache<StepCandidate> cache) {
		notifiedCaches.add(cache);
	    }
	};

	this.rootPackageFragments = new ArrayList<IPackageFragmentRoot>();
    }

    private MethodCache<StepCandidate> createCache() {
	return new MethodCache<StepCandidate>(null);
    }

    public void givenAMockedScanningExecutor() {
	this.scanningExecutor = Mockito.mock(Executor.class);
    }

    public void givenTheNextCacheReloadWouldRunInstantly() {
	this.completionExecutor.setDirectExecution(true);
    }

    public void givenTheNextCacheReloadWouldBlock() {
	// The tests are rigged through the completion executor; Trying
	// this via a blocked scanningExecutor (as the real-life would) would
	// require way too much mocking and control. For the sake of the tests,
	// it is enough to directly block the completion executor.
	this.completionExecutor.setDirectExecution(false);
    }

    public void whenTheBlockedCacheReloadCompletes() {
	this.completionExecutor.signalBlockedThread();
    }

    public void givenAnInitializedLoader() {
	try {
	    Mockito.when(this.javaProject.getAllPackageFragmentRoots())
		    .thenReturn(
			    this.rootPackageFragments
				    .toArray(new IPackageFragmentRoot[0]));
	} catch (JavaModelException e) {
	}

	this.loader = new StepCandidateCacheLoader(this.listener,
		this.scanningExecutor, this.completionExecutor);
    }

    public void givenReloadWasRequested(MethodCache<StepCandidate> cache) {
	this.loader
		.requestReload(cache, this.javaProject, this.scanInitializer);
    }

    public void whenRequestingLoad(MethodCache<StepCandidate> cache) {
	this.loader
		.requestReload(cache, this.javaProject, this.scanInitializer);
    }

    public void thenCacheShouldHaveBeenNotified(MethodCache<StepCandidate> cache) {
	Assert.assertTrue(this.notifiedCaches.contains(cache));
    }

    public void thenScanningExecutorShouldNotHaveBeenUsed() {
	Mockito.verify(this.scanningExecutor, Mockito.never()).execute(
		Mockito.any(Runnable.class));
    }

    @SuppressWarnings("unchecked")
    public void thenCachesShouldHaveBeenNotifiedInOrder(
	    MethodCache<StepCandidate> expectedA,
	    MethodCache<StepCandidate> expectedB) {
	Assert.assertEquals(Arrays.asList(expectedA, expectedB),
		this.notifiedCaches);
    }
}
