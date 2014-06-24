package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.core.runtime.CoreException;

public class CPPVariable implements ICPPVariable {

	static Logger logger = LogManager.getLogger(CPPVariable.class.getName());

	BindingInfo _bindingInfo;
	
	public CPPVariable(String line) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		CPPASTTranslationUnit.addBinding(_bindingInfo, this);
	}
	
	public String toString() {
		return _bindingInfo.name;
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		
		logger.error("Not implemented");
		return null;
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

	@Override
	public IType getType() throws DOMException {
		
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
	public boolean isStatic() throws DOMException {
		
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
	public boolean isMutable() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

}
