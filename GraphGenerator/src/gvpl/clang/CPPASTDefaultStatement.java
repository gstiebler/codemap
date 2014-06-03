package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class CPPASTDefaultStatement extends ASTNode implements IASTDefaultStatement {

	IASTStatement _clangStmt;
	
	public CPPASTDefaultStatement(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		_clangStmt = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
	}

}
