package gvpl.clang;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.runtime.CoreException;

public class CPPClassType implements org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType {

	BindingInfo _bindingInfo;
	
	public CPPClassType(String line) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		CPPASTTranslationUnit.addBinding(_bindingInfo.bindingId, this);
	}
	
	@Override
	public IScope getCompositeScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getKey() throws DOMException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSameType(IType arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IField findField(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPBase[] getBases() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPConstructor[] getConstructors() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPField[] getDeclaredFields() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IField[] getFields() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBinding[] getFriends() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPMethod[] getMethods() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICPPClassType[] getNestedClasses() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object clone() {
		return null;
	}

}
