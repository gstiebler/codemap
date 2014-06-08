package gvpl.clang;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.CoreException;

public class CPPFunction implements org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction {

	static Logger logger = LogManager.getLogger(CPPFunction.class.getName());

	int _bindingId = -1;
	String _name;
	boolean _isStatic = false;
	String _hex;
	
	public CPPFunction(BindingInfo bi, String name, Cursor cursor) {
		_bindingId = bi.bindingId;
		_isStatic = isStatic(cursor.getLine());
		CPPASTTranslationUnit.addBinding(bi, this);
		_name = name;

		_hex = "0x" + Integer.toHexString(_bindingId);
	}
	
	public static boolean isStatic(String line) {
		List<String> strings = CPPASTTranslationUnit.parseLine(line);
		return strings.size() > 6 && strings.get(6).equals("static");
	}
	
	public String toString() {
		return _name;
	}

	@Override
	public boolean isStatic() throws DOMException {
		return _isStatic;
	}

	public IASTNode getDefinition() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getFunctionScope() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IParameter[] getParameters() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IFunctionType getType() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isAuto() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isExtern() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isRegister() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean takesVarArgs() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isInline() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isMutable() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

}
