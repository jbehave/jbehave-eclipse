package org.jbehave.eclipse.cache;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

public class JavaVisitorAdapter<T> implements JavaVisitor<T> {

    public boolean visit(IPackageFragmentRoot packageFragmentRoot, T arg) {
        return true;
    }

    public T argumentFor(IPackageFragmentRoot packageFragmentRoot, T arg) {
        return arg;
    }

    public boolean visit(IPackageFragment packageFragment, T arg) {
        return true;
    }

    public T argumentFor(IPackageFragment packageFragment, T arg) {
        return arg;
    }

    public boolean traverseCompilationUnit(IPackageFragment packageFragment, T arg) {
        return true;
    }

    public boolean traverseClassFile(IPackageFragment packageFragment, T arg) {
        return true;
    }

    public boolean visit(ICompilationUnit compilationUnit, T arg) {
        return true;
    }

    public T argumentFor(ICompilationUnit compilationUnit, T arg) {
        return arg;
    }

    public boolean visit(IClassFile classFile, T arg) {
        return true;
    }

    public T argumentFor(IClassFile classFile, T arg) {
        return arg;
    }

    public boolean visit(IType type, T arg) {
        return true;
    }

    public T argumentFor(IType classFile, T arg) {
        return arg;
    }

    public boolean visit(IMethod method, T arg) {
        return true;
    }

    public boolean visit(IJavaElement element, T arg) {
        return false;
    }
    
}
