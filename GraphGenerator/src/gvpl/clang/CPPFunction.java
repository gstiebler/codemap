package gvpl.clang;

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
	CPPASTFunctionDefinition _parent;
	
	public CPPFunction(BindingInfo bi, String name, Cursor cursor, CPPASTFunctionDefinition parent) {
		_parent = parent;
		_bindingId = bi.bindingId;
		_isStatic = isStatic(cursor.getLine());
		CPPASTTranslationUnit.addBinding(bi, this);
		_name = name;

		_hex = "0x" + Integer.toHexString(_bindingId);
	}
	
	public static boolean isStatic(String line) {
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		if(!strings.containsKey("storageClass"))
			return false;
		String storageClass = strings.get("storageClass");
		return storageClass.equals("static");
	}
	
	public String toString() {
		return _name;
	}

	@Override
	public boolean isStatic() throws DOMException {
		return _isStatic;
	}

	public IASTNode getDefinition() {
		IASTNode result = null;
		if(_parent._body != null)
			result = _parent._declarator;
		return result;
//		
//		logger.error("Not implemented");
//		return null;
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getFunctionScope() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IParameter[] getParameters() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IFunctionType getType() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isAuto() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isExtern() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isRegister() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean takesVarArgs() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isInline() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isMutable() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getName() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

}
