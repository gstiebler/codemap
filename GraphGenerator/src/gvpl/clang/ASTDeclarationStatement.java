package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

public class ASTDeclarationStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement {

	IASTDeclaration _declaration = null;
	
	public ASTDeclarationStatement(Cursor cursor) {
		super(cursor.getLine());
		_declaration = new ASTSimpleDeclaration(cursor);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return _declaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration arg0) {
		// TODO Auto-generated method stub
		
	}

}
