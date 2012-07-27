package org.jbehave.eclipse.cache.container;


import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class Containers {
    
    static final long NULL_STAMP = IResource.NULL_STAMP;
    static final long UNITIALIZED_STAMP = NULL_STAMP-1;
    
    public interface Factory<E> {
        Container<E> create(String name);
    }

    public static <E> Factory<E> hierarchicalFactory() {
        return new Factory<E>() {
            @Override
            public Container<E> create(String name) {
                return new HierarchicalContainer<E>(name);
            };
        };
    }
    
    public static <E> Factory<E> flatFactory() {
        return new Factory<E>() {
            @Override
            public Container<E> create(String name) {
                return new FlatContainer<E>(name);
            };
        };
    }

    public static long modificationStampOf(IJavaElement elem) {
        IResource resource = elem.getResource();
        if (resource != null) {
            long ts = resource.getModificationStamp();
            if (ts != NULL_STAMP) {
                return ts;
            }
        }
    
        // rely on underlying file
        IPath path = elem.getPath();
        if (path != null)
            return path.toFile().lastModified();
    
        return NULL_STAMP;
    }

    public static boolean shouldTraverse(IJavaElement elem, boolean timestampChanged) {
        try {
            if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
                if (((IPackageFragmentRoot) elem).getKind() == IPackageFragmentRoot.K_SOURCE) {
                    // modification stamp does not reflect a modification deeper in hierarchy
                    // so one cannot rely on it for step modification in source file
                    // unless it an archive such as a jar...
                    return true;
                }
            } else if (elem.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                // modification stamp does not reflect a modification deeper in hierarchy
                return true;
            }
    
            return timestampChanged;
        } catch (JavaModelException e) {
            return true;
        }
    }

    public static String pathOf(IJavaElement elem) {
        // rely on underlying file
        IPath path = elem.getPath();
        if (path != null) {
            return path.toString();
        } else
            return elem.getElementName();
    }

    public static String keyOf(IJavaElement elem) {
        // the key is reversed because it is usually the end that most differs:
        // beginning is usually the library or package  path, so it is shared 
        // among almost all keys
        return StringUtils.reverse(pathOf(elem));
    }
    
    public static final IProgressMonitor wrapMonitorForRecursive(IProgressMonitor monitor) {
        if(monitor instanceof WrappedProgressMonitor)
            return monitor;
        return new WrappedProgressMonitor(monitor);
    }
    
    private static final class WrappedProgressMonitor implements IProgressMonitor {
        private final IProgressMonitor delegate;
        public WrappedProgressMonitor(IProgressMonitor delegate) {
            this.delegate = delegate;
        }
        @Override
        public void beginTask(String name, int totalWork) {
            delegate.subTask(name);
        }
        @Override
        public void done() {
        }
        @Override
        public void internalWorked(double work) {
        }
        @Override
        public boolean isCanceled() {
            return delegate.isCanceled();
        }
        @Override
        public void setCanceled(boolean value) {
            delegate.setCanceled(value);
        }
        @Override
        public void setTaskName(String name) {
            delegate.subTask(name);
        }
        @Override
        public void subTask(String name) {
            delegate.subTask(name);            
        }
        @Override
        public void worked(int work) {
        }
        
    }
}
