package org.jbehave.eclipse.util;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A monitored executor is an {@link Executor} that keeps track of all issued
 * tasks and allows a thread to wait for their completion.
 * 
 * There is no check that would prevent calling awaitCompletion() from within a
 * scheduled task, which will lead to a dead-lock.
 */
public class MonitoredExecutor implements Executor {
    /** Counter for active tasks */
    private final AtomicInteger activeCount = new AtomicInteger(0);
    /** signal object for all tasks finished */
    private final Object finishedSignal = new Object();
    /** The actual executor for the tasks */
    private final Executor executor;

    /**
     * Constructor
     * 
     * @param executor
     *            the executor to use in the background
     */
    public MonitoredExecutor(Executor executor) {
	this.executor = executor;
    }

    /**
     * Waits for all scheduled tasks to complete. Must not be called from within
     * a scheduled task.
     * 
     * @throws InterruptedException
     *             when the waiting thread is interrupted
     */
    public void awaitCompletion() throws InterruptedException {
	synchronized (finishedSignal) {
	    if (activeCount.get() > 0) {
		finishedSignal.wait();
	    }
	}
    }

    /** {@inheritDoc} */
    @Override
    public void execute(final Runnable runnable) {
	activeCount.incrementAndGet();
	executor.execute(new Runnable() {

	    @Override
	    public void run() {
		try {
		    runnable.run();
		} finally {
		    onRunnableReturned();
		}
	    }
	});

    }

    private void onRunnableReturned() {
	synchronized (finishedSignal) {
	    if (activeCount.decrementAndGet() == 0) {
		finishedSignal.notifyAll();
	    }
	}
    }
}
