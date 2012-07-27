package org.jbehave.eclipse.cache.container;

import static org.jbehave.eclipse.cache.container.Containers.modificationStampOf;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Container<E> {
    private Logger log = LoggerFactory.getLogger(Container.class);

    private long timestamp = Containers.UNITIALIZED_STAMP;
    private volatile int lastBuildTick;
    protected final String containerName;

    public Container(String containerName) {
        this.containerName = containerName;
    }

    int getLastBuildTick() {
        return lastBuildTick;
    }

    void setLastBuildTick(int traverseTick) {
        while (this.lastBuildTick < traverseTick)
            this.lastBuildTick = traverseTick;
    }

    abstract void recursivelyRemoveBuildOlderThan(int buildTick, IProgressMonitor monitor);

    void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    boolean isTimestampDifferent(long timestamp) {
        return this.timestamp != timestamp;
    }

    public abstract void traverse(Visitor<E, ?> visitor);

    public abstract void clear();

    public abstract void add(E element);

    public abstract Container<E> specializeFor(IPackageFragmentRoot pkgFragmentRoot);

    public abstract Container<E> specializeFor(IPackageFragment pkgFragment);

    public abstract Container<E> specializeFor(ICompilationUnit cunit);

    public boolean prepareForTraversal(IJavaElement element, int buildTick) {
        setLastBuildTick(buildTick);
        long timestamp = modificationStampOf(element);
        log.debug("Preparing for traversal [" + element.getElementName() + "] in [" + Containers.pathOf(element) + "] ts: " + timestamp);
        if (isTimestampDifferent(timestamp)) {
            // clear and rescan
            setTimestamp(timestamp);
            clear();
            log.debug("Change detected on [" + element.getElementName() + "] in [" + Containers.pathOf(element) + "] traversing!");
            return true;
        }
        boolean traverse = shouldContinueTraversingEvenIfNotChanged(element);
        log.debug("No change detected on [" + element.getElementName() + "] in [" + Containers.pathOf(element) + "] traverse anyway? " + traverse);
        return traverse;
    }

    protected boolean shouldContinueTraversingEvenIfNotChanged(IJavaElement element) {
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            return false;
        }
        // modification stamp does not reflect a modification deeper in hierarchy
        // so one cannot rely on it for step modification in source file
        // unless it an archive such as a jar or a source file...
        // in other words it is possible for a step class to be modified, and the
        // package not be stamped.
        if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
            try {
                int kind = ((IPackageFragmentRoot) element).getKind();
                if (kind == IPackageFragmentRoot.K_BINARY)
                    // archive would have changed if content changed...
                    return false;
            } catch (JavaModelException e) {
                log.warn("Failed to retrieve kind of " + element.getElementName() + "] in [" + Containers.pathOf(element), e);
            }
        }
        return true;
    }

    public void resetForBuild(IJavaElement element, int buildTick) {
        setLastBuildTick(buildTick);
        long timestamp = modificationStampOf(element);
        setTimestamp(timestamp);
        clear();
    }
}