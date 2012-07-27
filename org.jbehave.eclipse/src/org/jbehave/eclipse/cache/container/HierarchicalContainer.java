package org.jbehave.eclipse.cache.container;

import static org.jbehave.eclipse.cache.container.Containers.wrapMonitorForRecursive;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.cache.container.Containers.Factory;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HierarchicalContainer<E> extends Container<E> {
    private Logger log = LoggerFactory.getLogger(HierarchicalContainer.class);

    private final ConcurrentMap<String, Container<E>> children = New.concurrentHashMap();

    public HierarchicalContainer(String containerName) {
        super(containerName);
    }

    @Override
    public void clear() {
        children.clear();
    }

    @Override
    public void add(E element) {
        throw new IllegalStateException();
    }

    @Override
    public void recursivelyRemoveBuildOlderThan(int buildTick, IProgressMonitor monitor) {
        ArrayList<Entry<String, Container<E>>> elems = New.arrayList(children.entrySet());
        monitor.beginTask("", elems.size());
        int removed = 0;
        int remaining = 0;
        for (Entry<String, Container<E>> e : elems) {
            Container<E> child = e.getValue();
            if (child.getLastBuildTick() < buildTick) {
                removed++;
                children.remove(e.getKey());
            } else {
                remaining++;
                child.recursivelyRemoveBuildOlderThan(buildTick, wrapMonitorForRecursive(monitor));
            }
            monitor.worked(1);
        }
        monitor.done();
        log.debug("Unsed #" + removed + " removed (#"+remaining+" remaining) in " + containerName);
    }

    @Override
    public void traverse(Visitor<E, ?> visitor) {
        for (Container<E> child : children.values()) {
            child.traverse(visitor);
            if (visitor.isDone())
                return;
        }
    }

    @Override
    public Container<E> specializeFor(IPackageFragmentRoot pkgFragmentRoot) {

        int kind;
        try {
            kind = pkgFragmentRoot.getKind();
        } catch (JavaModelException e) {
            log.error("Failed to retrieve kind of " + Containers.pathOf(pkgFragmentRoot), e);
            kind = IPackageFragmentRoot.K_BINARY;
        }

        Factory<E> factory;
        if (kind == IPackageFragmentRoot.K_SOURCE) {
            factory = Containers.hierarchicalFactory();
        } else {
            factory = Containers.flatFactory();
        }
        return specializeFor(pkgFragmentRoot, factory);
    }

    @Override
    public Container<E> specializeFor(IPackageFragment pkgFragment) {
        Factory<E> factory = Containers.hierarchicalFactory();
        return specializeFor(pkgFragment, factory);
    }

    @Override
    public Container<E> specializeFor(ICompilationUnit cunit) {
        Factory<E> factory = Containers.flatFactory();
        return specializeFor(cunit, factory);
    }

    protected Container<E> specializeFor(IJavaElement javaElement, Factory<E> factory) {
        String path = javaElement.getPath().toString();
        Container<E> container = children.get(path);
        if (container == null) {
            Container<E> newContainer = factory.create(path);
            container = children.putIfAbsent(path, newContainer);
            if (container == null)
                container = newContainer;
        }
        return container;
    }

}