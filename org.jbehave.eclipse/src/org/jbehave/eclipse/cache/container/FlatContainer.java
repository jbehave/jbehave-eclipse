package org.jbehave.eclipse.cache.container;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlatContainer<E> extends Container<E> {
    
    private static Logger logger = LoggerFactory.getLogger(FlatContainer.class);
    
    private ConcurrentLinkedQueue<E> elements = new ConcurrentLinkedQueue<E>();

    public FlatContainer(String containerName) {
        super(containerName);
    }

    @Override
    public void clear() {
        elements.clear();
    }

    @Override
    public void add(E element) {
        elements.add(element);
    }

    @Override
    public void traverse(Visitor<E, ?> visitor) {
        for (E element : elements) {
            logger.debug("Traversing element: <{}>", element);
            visitor.visit(element);
            if (visitor.isDone())
                return;
        }
    }
    
    @Override
    public void recursivelyRemoveBuildOlderThan(int buildTick, IProgressMonitor monitor) {
    }

    @Override
    public Container<E> specializeFor(IPackageFragmentRoot pkgFragmentRoot) {
        return this;
    }

    @Override
    public Container<E> specializeFor(ICompilationUnit cunit) {
        return this;
    }

    @Override
    public Container<E> specializeFor(IPackageFragment pkgFragment) {
        return this;
    }
}