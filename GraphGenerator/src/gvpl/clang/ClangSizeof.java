package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class ClangSizeof extends ASTNode implements IASTExpression {

	IASTExpression _expr;
	
	public ClangSizeof(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_expr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
	}
	
	@Override
	public String toString() {
		return "sizeof(" + _expr + ")";
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

}
