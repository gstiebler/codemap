package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;

public class CPPField extends CPPVariable implements ICPPField {
	
	static Logger logger = LogManager.getLogger(CPPField.class.getName());
	
	BindingInfo _bi;
	
	public CPPField(Cursor cursor) {
		super(cursor.getLine());
		_bi = CPPASTTranslationUnit.parseBindingInfo(cursor.getLine());
		CPPASTTranslationUnit.addBinding(_bi, this);
	}

	@Override
	public String toString() {
		return _bi.name;
	}

	@Override
	public ICompositeType getCompositeTypeOwner() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
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
	public boolean isMutable() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}
	

}
