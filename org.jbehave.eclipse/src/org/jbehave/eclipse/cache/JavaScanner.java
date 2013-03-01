package org.jbehave.eclipse.cache;

import java.util.concurrent.Executor;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.eclipse.util.FJ;
import org.jbehave.eclipse.util.StringMatcher;

import fj.F;

public class JavaScanner<T> {
    
    private final IJavaProject project;
    private final JavaVisitor<T> visitor;
    private final Executor executor;
    private F<String, Boolean> packageRootNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> packageNameFilter = FJ.alwaysTrue();
    private F<String, Boolean> classNameFilter = FJ.alwaysTrue();
    private byte[] filterHash;
    
    public JavaScanner(IJavaProject project, JavaVisitor<T> visitor, Executor executor) {
        super();
        this.project = project;
        this.visitor = visitor;
        this.executor = executor;
    }
    
    public void setPackageNameFilter(StringMatcher packageNameMatcher) {
        setPackageNameFilter(packageNameMatcher.compile());
    }
    
    public void setPackageNameFilter(F<String, Boolean> packageNameFilter) {
        if(packageNameFilter==null)
            packageNameFilter = FJ.alwaysTrue();
        this.packageNameFilter = packageNameFilter;
    }
    
    public void setClassNameFilter(StringMatcher classNameMatcher) {
        setClassNameFilter(classNameMatcher.compile());
    }
    
    public void setClassNameFilter(F<String, Boolean> classNameFilter) {
        if(classNameFilter==null)
            classNameFilter = FJ.alwaysTrue();
        this.classNameFilter = classNameFilter;
    }

    public void setPackageRootNameFilter(StringMatcher packageRootNameMatcher) {
        setPackageRootNameFilter(packageRootNameMatcher.compile());
    }

    public void setPackageRootNameFilter(F<String, Boolean> packageRootNameFilter) {
        if(packageRootNameFilter==null)
            packageRootNameFilter = FJ.alwaysTrue();
        this.packageRootNameFilter = packageRootNameFilter;
    }

    
    public void traversePackageFragmentRoots(final T argument) throws JavaModelException {
        for(IPackageFragmentRoot packageFragmentRoot : project.getAllPackageFragmentRoots()) {
            executor.execute(traverseAsRunnable(packageFragmentRoot, argument));
        }
    }

    private Runnable traverseAsRunnable(final IPackageFragmentRoot packageFragmentRoot, final T argument) {
        return new Runnable() {
            @Override
            public void run() {
                if(packageRootNameFilter.f(packageFragmentRoot.getElementName())
                    && visitor.visit(packageFragmentRoot, argument)) {
                    try {
			traverse(packageFragmentRoot, argument);
		    } catch (JavaModelException e) {
		    }
                }
            }
        };
    }
    
    protected void traverse(IPackageFragmentRoot packageFragmentRoot, T argument) throws JavaModelException {
        final T arg = visitor.argumentFor(packageFragmentRoot, argument);
        
        for(IJavaElement elem : packageFragmentRoot.getChildren()) {
            final IJavaElement jElem = elem;
            if(jElem.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                executor.execute(traverseAsRunnable((IPackageFragment)jElem, arg));
            }
        }
    }

    private Runnable traverseAsRunnable(final IPackageFragment packageFragment, final T arg) {
        return new Runnable() {
            @Override
            public void run() {
                if(packageNameFilter.f(packageFragment.getElementName())
                   && visitor.visit(packageFragment, arg)) {
                    try {
			traverse(packageFragment, arg);
		    } catch (JavaModelException e) {
		    }
                }
            }
        };
    }
    
    protected void traverse(IPackageFragment packageFragment, T argument) throws JavaModelException {
        boolean traverseCUnit = visitor.traverseCompilationUnit(packageFragment, argument);
        boolean traverseClassFile = visitor.traverseClassFile(packageFragment, argument);
        final T arg = visitor.argumentFor(packageFragment, argument);
        
        for(IJavaElement jElem : packageFragment.getChildren()) {
            switch(jElem.getElementType()) {
                case IJavaElement.COMPILATION_UNIT:
                    if(!traverseCUnit)
                        break;
                    
                    ICompilationUnit cunit = (ICompilationUnit)jElem;
                    executor.execute(traverseAsRunnable(cunit, arg));
                    break;
                case IJavaElement.CLASS_FILE:
                    if(!traverseClassFile)
                        break;
                    
                    IClassFile classFile = (IClassFile)jElem;
                    executor.execute(traverseAsRunnable(classFile, arg));
                    break;
                default:
                    visitor.visit(jElem, arg);
            }
        }
    }

    private Runnable traverseAsRunnable(final IClassFile classFile, final T arg) {
        return new Runnable() {
            @Override
            public void run(){
                if(classNameFilter.f(classFile.getElementName())
                   && visitor.visit(classFile, arg)) {
                    try {
			traverse(classFile, arg);
		    } catch (JavaModelException e) {
		    }
                }
            }
        };
    }

    private Runnable traverseAsRunnable(final ICompilationUnit cunit, final T arg) {
        return new Runnable() {
            @Override
            public void run() {
                if(classNameFilter.f(cunit.getElementName())
                   && visitor.visit(cunit, arg)) {
                    try {
			traverse(cunit, arg);
		    } catch (JavaModelException e) {
		    }
                }
            }
        };
    }
    
    protected void traverse(ICompilationUnit compilationUnit, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(compilationUnit, argument);
        
        for(IJavaElement jElem : compilationUnit.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type, arg)) {
                    traverseMethods(type, arg);
                }
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverse(IClassFile classFile, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(classFile, argument);
        
        for(IJavaElement jElem : classFile.getChildren()) {
            if(jElem.getElementType() == IJavaElement.TYPE) {
                IType type = (IType)jElem;
                if(visitor.visit(type, arg)) {
                    traverseMethods(type, arg);
                }
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    
    protected void traverseMethods(IType type, T argument) throws JavaModelException {
        T arg = visitor.argumentFor(type, argument);
        
        for(IJavaElement jElem : type.getChildren()) {
            if(jElem.getElementType() == IJavaElement.METHOD) {
                IMethod method = (IMethod)jElem;
                visitor.visit(method, arg);
            }
            else {
                visitor.visit(jElem, arg);
            }
        }
    }
    

    /**
     * Define the filter hash. It may be used to optimize scanning between two runs
     * if the filtering is not changed. It is the responsibility of the caller to
     * provides a suitable and valid hash according to the current filtering 
     * configuration.
     * 
     * @param filterHash
     */
    public void setFilterHash(byte[] filterHash) {
        this.filterHash = filterHash;
    }
    
    /**
     * Returns the filter hash. It may be used to optimize scanning between two runs
     * if the filtering is not changed. It is the responsibility of the caller to
     * provides a suitable and valid hash according to the current filtering 
     * configuration.
     * 
     * @return
     */
    public byte[] getFilterHash() {
        return filterHash;
    }
    
}
