package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.core.runtime.CoreException;

public class CPPTypedef implements ITypedef {

	static Logger logger = LogManager.getLogger(CPPTypedef.class.getName());
	
	BindingInfo _bindingInfo;
	
	public CPPTypedef(String line, String userType) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		CPPASTTranslationUnit.addBinding(userType, this);
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public String getName() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public boolean isSameType(IType arg0) {
		
		logger.error("not implemented");
		return false;
	}

	@Override
	public IType getType() throws DOMException {
		
		logger.error("not implemented");
		return null;
	}
	
	@Override
	public Object clone() {
		logger.error("not implemented");
		return null;
	}

}
