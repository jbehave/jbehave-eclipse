package org.jbehave.eclipse.cache;

import static org.jbehave.eclipse.util.Bytes.areDifferents;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.cache.container.Container;
import org.jbehave.eclipse.cache.container.HierarchicalContainer;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Effect;

public class MethodCache<E> extends
        JavaVisitorAdapter<MethodCache.Bucket<E>> {

	public interface Callback<T1, T2> {
	    void op(T1 value1, T2 value2);
	}

    private static AtomicInteger buildTickGenerator = new AtomicInteger();
    
    private Logger log = LoggerFactory.getLogger(MethodCache.class);

    private final HierarchicalContainer<E> content = new HierarchicalContainer<E>("<root>");
    private Callback<IMethod, Container<E>> callback;
    private byte[] cachedFilterHash;

    public MethodCache(Callback<IMethod, Container<E>> callback) {
        this.callback = callback;
    }

    public void rebuild(IJavaProject project, 
                        Effect<JavaScanner<?>> initializer,
                        Executor executor) throws JavaModelException {
        
        JavaScanner<Bucket<E>> javaScanner = new JavaScanner<Bucket<E>>(project, this, executor);
        initializer.e(javaScanner);
        
        int buildTick = buildTickGenerator.incrementAndGet();
        
        byte[] filterHash = javaScanner.getFilterHash();
        boolean filterChanged = hasFilterChanged(filterHash);
        Bucket<E> rootBucket = newRootBucket(buildTick, filterChanged);
        javaScanner.traversePackageFragmentRoots(rootBucket);
        removeUnsed(buildTick);
    }

    public void traverse(Visitor<E, ?> visitor) {
        content.traverse(visitor);
    }

    protected boolean hasFilterChanged(byte[] bytes) {
        if (bytes == null || cachedFilterHash == null || areDifferents(bytes, cachedFilterHash)) {
            cachedFilterHash = bytes;
            return true;
        }
        return false;
    }

    public Bucket<E> newRootBucket(int buildTick, boolean hasFilterHashChanged) {
        return new Bucket<E>(buildTick, hasFilterHashChanged, content);
    }

    protected void removeUnsed(final int buildTick) {
        log.debug("Removing unsed container (e.g. element deleted, moved or renamed). This should be done asynchronously.");
        Job job = new Job("Victor the method cleaner (build #" + buildTick + ")") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                content.recursivelyRemoveBuildOlderThan(buildTick, monitor);
                return Status.OK_STATUS;
            }
        };
        job.schedule(); // start as soon as possible
    }

    @Override
    public boolean visit(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(packageFragmentRoot);
        if(bucket.hasFilterChanged()) {
            container.resetForBuild(packageFragmentRoot, bucket.buildTick);
            return true;
        }
        else {
            return container.prepareForTraversal(packageFragmentRoot, bucket.buildTick);
        }
    }

    @Override
    public Bucket<E> argumentFor(IPackageFragmentRoot packageFragmentRoot, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(packageFragmentRoot);
        return bucket.withContainer(container);
    }
    
    @Override
    public boolean visit(IPackageFragment packageFragment, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(packageFragment);
        if(bucket.hasFilterChanged()) {
            container.resetForBuild(packageFragment, bucket.buildTick);
            return true;
        }
        else {
            return container.prepareForTraversal(packageFragment, bucket.buildTick);
        }
    }

    @Override
    public Bucket<E> argumentFor(IPackageFragment packageFragment, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(packageFragment);
        return bucket.withContainer(container);
    }

    @Override
    public boolean traverseClassFile(IPackageFragment packageFragment, Bucket<E> bucket) {
        return true;
    }

    @Override
    public boolean traverseCompilationUnit(IPackageFragment packageFragment, Bucket<E> bucket) {
        return true;
    }

    @Override
    public boolean visit(ICompilationUnit cunit, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(cunit);
        if(bucket.hasFilterChanged()) {
            container.resetForBuild(cunit, bucket.buildTick);
            return true;
        }
        else {
            return container.prepareForTraversal(cunit, bucket.buildTick);
        }
    }

    @Override
    public Bucket<E> argumentFor(ICompilationUnit cunit, Bucket<E> bucket) {
        Container<E> container = bucket.container.specializeFor(cunit);
        return bucket.withContainer(container);
    }
    
    @Override
    public boolean visit(IMethod method, Bucket<E> bucket) {
        Container<E> container = bucket.getContainer();
        callback.op(method, container);
        return false;
    }

    /**
     * Class is public in order to be invoked. But all methods are private for internal use.
     */
    public static final class Bucket<E> {
        private final Container<E> container;
        private final boolean filterChanged;
        private final int buildTick;

        private Bucket(int buildTick, boolean filterChanged) {
            this(buildTick, filterChanged, null);
        }

        private Bucket(int buildTick, boolean filterChanged, Container<E> container) {
            this.buildTick = buildTick;
            this.filterChanged = filterChanged;
            this.container = container;
        }

        public boolean hasFilterChanged() {
            return filterChanged;
        }

        private Container<E> getContainer() {
            return container;
        }

        private Bucket<E> withContainer(Container<E> container) {
            if (this.container == container)
                return this;
            return new Bucket<E>(buildTick, filterChanged, container);
        }
    }

}
