package org.jbehave.eclipse.cache;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

public interface JavaVisitor<T> {
    
    /**
     * @param packageFragmentRoot
     * @param arg initial argument provided when recursion is initiated
     * @return <code>true</code> if the scanner should traverse the element.
     * @see JavaScanner#traversePackageFragmentRoots(Object)
     */
    boolean visit(IPackageFragmentRoot packageFragmentRoot, T arg);
    
    /**
     * @return the argument that will be passed to corresponding {@link #visit(IPackageFragment,T)}.
     */
    T argumentFor(IPackageFragmentRoot packageFragmentRoot, T arg);
    
    /**
     * @return <code>true</code> if the scanner should traverse the element.
     * @see #traverseClassFile(IPackageFragment)
     * @see #traverseCompilationUnit(IPackageFragment)
     */
    boolean visit(IPackageFragment packageFragment, T arg);
    
    /**
     * @return the argument that will be passed to corresponding {@link #visit(ICompilationUnit,T)}
     * and {@link #visit(IClassFile, Object)}.
     */
    T argumentFor(IPackageFragment packageFragment, T arg);
    
    /**
     * Indicates if the scanner should traverse the packageFragment sub elements.
     * 
     * @return <code>true</code> if the scanner should traverse the elements's compilation units.
     */
    boolean traverseCompilationUnit(IPackageFragment packageFragment, T arg);
    
    /**
     * Indicates if the scanner should traverse the packageFragment sub elements.
     * 
     * @return <code>true</code> if the scanner should traverse the elements's class files.
     */
    boolean traverseClassFile(IPackageFragment packageFragment, T arg);

    /**
     * @return <code>true</code> if the scanner should traverse the element.
     */
    boolean visit(ICompilationUnit compilationUnit, T arg);
    
    /**
     * @return the argument that will be passed to corresponding {@link #visit(IType,T)}.
     */
    T argumentFor(ICompilationUnit compilationUnit, T arg);
    
    /**
     * @return <code>true</code> if the scanner should traverse the element.
     */
    boolean visit(IClassFile classFile, T arg);
    
    /**
     * @return the argument that will be passed to corresponding {@link #visit(IType,T)}.
     */
    T argumentFor(IClassFile classFile, T arg);

    /**
     * @return <code>true</code> if the scanner should traverse the element.
     */
    boolean visit(IType type, T arg);
    
    /**
     * @return the argument that will be passed to corresponding {@link #visit(IMethod,T)}.
     */
    T argumentFor(IType classFile, T arg);

    /**
     * @return <code>true</code> if the scanner should traverse the element.
     */
    boolean visit(IMethod method, T arg);
    
    /**
     * @return <code>true</code> if the scanner should traverse the element.
     */
    boolean visit(IJavaElement element, T arg);
}