package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ASTDeclarationStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement {

	IASTDeclaration _declaration = null;
	
	public ASTDeclarationStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_declaration = new ASTSimpleDeclaration(cursor, this);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return _declaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration arg0) { }

}
