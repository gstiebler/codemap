package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;

public class CPPASTSimpleDeclSpecifier extends ASTDeclSpecifier implements ICPPASTSimpleDeclSpecifier{

	public CPPASTSimpleDeclSpecifier(Cursor cursor) {
		super(cursor);
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isLong() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isShort() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSigned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUnsigned() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setLong(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setShort(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSigned(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setType(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUnsigned(boolean arg0) {
		// TODO Auto-generated method stub
		
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

	

}
