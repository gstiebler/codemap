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
	
	public CPPTypedef(String userType) {
		CPPASTTranslationUnit.addBinding(userType, this);
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public boolean isSameType(IType arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return false;
	}

	@Override
	public IType getType() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}
	
	@Override
	public Object clone() {
		logger.error("not implemented");
		return null;
	}

}
