package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

public class ASTDeclSpecifier extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier{

	int _storageClass = -1;
	
	public ASTDeclSpecifier(Cursor cursor) {
		super(cursor.getLine());
		if(CPPMethod.isStatic(cursor.getLine()))
			_storageClass = IASTDeclSpecifier.sc_static;
	}

	@Override
	public int getStorageClass() {
		return _storageClass;
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
