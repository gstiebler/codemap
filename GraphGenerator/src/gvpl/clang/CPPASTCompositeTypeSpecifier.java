package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompositeTypeSpecifier extends ASTDeclSpecifier implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier {

	public CPPASTCompositeTypeSpecifier(Cursor cursor) {
		super(cursor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addMemberDeclaration(IASTDeclaration arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public int getKey() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IASTDeclaration[] getMembers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTName getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKey(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(IASTName arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isExplicit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFriend() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVirtual() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setExplicit(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFriend(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVirtual(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBaseSpecifier(ICPPASTBaseSpecifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ICPPASTBaseSpecifier[] getBaseSpecifiers() {
		// TODO Auto-generated method stub
		return null;
	}

}
