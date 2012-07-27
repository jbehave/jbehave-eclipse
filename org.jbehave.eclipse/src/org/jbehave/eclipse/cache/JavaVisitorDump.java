package org.jbehave.eclipse.cache;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;

public class JavaVisitorDump<T> extends JavaVisitorAdapter<T> {

	private final boolean dump;
    
    public JavaVisitorDump() {
    	this(true);
    }
    
    public JavaVisitorDump(boolean dump) {
    	this.dump = dump;
    }

    public boolean visit(IPackageFragmentRoot packageFragmentRoot, T arg) {
        if(dump) {
            System.out.println("JavaScanner.Visitor.visit(packageFragmentRoot...:" + packageFragmentRoot.getElementName() + ")");
        }
        return true;
    }
    
    public boolean visit(IPackageFragment packageFragment, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(packageFragment.......:" + packageFragment.getElementName() + ")");
        return true;
    }
    
    public boolean traverseCompilationUnit(IPackageFragment packageFragment, T arg) {
        return false;
    }
    
    public boolean traverseClassFile(IPackageFragment packageFragment, T arg) {
        return false;
    }

    public boolean visit(ICompilationUnit compilationUnit, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(compilationUnit.......:" + compilationUnit.getElementName() + ")");
        return true;
    }
    
    public boolean visit(IClassFile classFile, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(classFile.............:" + classFile.getElementName() + ")");
        return true;
    }
    
    public boolean visit(IType type, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(type..................:" + type.getElementName() + ")");
        return true;
    }
    
    public boolean visit(IMethod method, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(method................:" + method.getElementName() + ")");
        return true;
    }
    
    public boolean visit(IJavaElement element, T arg) {
        if(dump)
            System.out.println("JavaScanner.Visitor.visit(element...............:" + element.getElementName() + "): " + element.getClass());
        return true;
    }
}