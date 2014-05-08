package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.CoreException;

public class CPPFunction implements org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction {

	int _bindingId = -1;
	String _name;
	boolean _isStatic = false;
	
	public CPPFunction(int bindingId, String name, Cursor cursor) {
		_bindingId = bindingId;
		List<String> strings = CPPASTTranslationUnit.parseLine(cursor.getLine());
		if(strings.size() > 6 && strings.get(6).equals("static"))
			_isStatic = true;
		CPPASTTranslationUnit.addBinding(bindingId, this);
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
	public IScope getFunctionScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IParameter[] getParameters() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFunctionType getType() throws DOMException {
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
		return _isStatic;
	}

	@Override
	public boolean takesVarArgs() throws DOMException {
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
	public boolean isInline() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMutable() throws DOMException {
		// TODO Auto-generated method stub
		return false;
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

	public IASTNode getDefinition() {
		// TODO Auto-generated method stub
		return null;
	}

}
