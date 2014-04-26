package gvpl.clang;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.core.runtime.CoreException;

public class CPPVariable implements org.eclipse.cdt.core.dom.ast.IVariable {

	private String _name;
	
	public CPPVariable(String name) {
		_name = name;
	}
	
	public String toString() {
		return _name;
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
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
	public IType getType() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAuto() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isExtern() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRegister() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStatic() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

}
