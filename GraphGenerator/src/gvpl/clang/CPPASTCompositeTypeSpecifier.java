package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompositeTypeSpecifier extends ASTDeclSpecifier implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclaration.class.getName());

	public CPPASTCompositeTypeSpecifier(Cursor cursor) {
		super(cursor);
		while(!cursor.theEnd()) {
			cursor.nextLine();
		}
	}

	@Override
	public void addMemberDeclaration(IASTDeclaration arg0) { }

	@Override
	public int getKey() {
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public IASTDeclaration[] getMembers() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTName getName() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setKey(int arg0) { }

	@Override
	public void setName(IASTName arg0) { }

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isExplicit() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isFriend() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVirtual() {
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setExplicit(boolean arg0) { }

	@Override
	public void setFriend(boolean arg0) { }

	@Override
	public void setVirtual(boolean arg0) { }

	@Override
	public void addBaseSpecifier(ICPPASTBaseSpecifier arg0) { }

	@Override
	public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
		// TODO Auto-generated method stub
		return null;
	}

}
