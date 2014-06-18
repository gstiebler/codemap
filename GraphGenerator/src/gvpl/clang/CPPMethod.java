package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

public class CPPMethod extends CPPFunction implements org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod {

	static Logger logger = LogManager.getLogger(CPPMethod.class.getName());

	public IASTName className;
	BindingInfo _bi;
	
	public CPPMethod(BindingInfo bi, String name, Cursor cursor, CPPASTFunctionDeclaration parent) {
		super(bi, name, cursor, parent);
		_bi = CPPASTTranslationUnit.parseBindingInfo(cursor.getLine());
		CPPASTTranslationUnit.addBinding(_bi, this);
		className = CPPASTTranslationUnit.lastClassName;
	}

	@Override
	public ICPPClassType getClassOwner() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public int getVisibility() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public boolean isDestructor() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isImplicit() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVirtual() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}


}
