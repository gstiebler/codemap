package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

public class CPPASTSimpleDeclSpecifier extends CPPASTBaseDeclSpecifier implements ICPPASTSimpleDeclSpecifier{
	
	static Logger logger = LogManager.getLogger(CPPASTSimpleDeclSpecifier.class.getName());
	
	String _name = "";

	public CPPASTSimpleDeclSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor, parent);
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		_name = parsedLine.getAndCheck("name");
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public boolean isLong() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isShort() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isSigned() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isUnsigned() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isExplicit() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isFriend() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVirtual() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setLong(boolean arg0) {}

	@Override
	public void setShort(boolean arg0) {}

	@Override
	public void setSigned(boolean arg0) {}

	@Override
	public void setType(int arg0) {}

	@Override
	public void setUnsigned(boolean arg0) {}

	@Override
	public void setExplicit(boolean arg0) {}

	@Override
	public void setFriend(boolean arg0) {}

	@Override
	public void setVirtual(boolean arg0) {}

	

}
