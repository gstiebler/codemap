package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public class CPPASTCaseStatement extends ASTNode implements IASTCaseStatement {

	IASTExpression _expr;
	public IASTStatement _clangStmt;
	
	public CPPASTCaseStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_expr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		cursor.nextLine();
		_clangStmt = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
	}

	@Override
	public IASTExpression getExpression() {
		return _expr;
	}

	@Override
	public void setExpression(IASTExpression arg0) {}

}
