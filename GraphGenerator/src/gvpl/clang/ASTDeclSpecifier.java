package gvpl.clang;

public class ASTDeclSpecifier extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier{

	public ASTDeclSpecifier(Cursor cursor) {
		super(cursor.getLine());
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getStorageClass() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isConst() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isInline() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setConst(boolean arg0) { }

	@Override
	public void setInline(boolean arg0) { }

	@Override
	public void setStorageClass(int arg0) { }

	@Override
	public void setVolatile(boolean arg0) { }

}
