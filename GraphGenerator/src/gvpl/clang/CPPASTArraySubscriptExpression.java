package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTArraySubscriptExpression extends ASTNode implements IASTArraySubscriptExpression {

	IASTExpression _arrayExpr;
	IASTExpression _subscriptExpr;
	
	public CPPASTArraySubscriptExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_arrayExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_subscriptExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
	}

	@Override
	public IASTExpression getArrayExpression() {
		return _arrayExpr;
	}

	@Override
	public IASTExpression getSubscriptExpression() {
		return _subscriptExpr;
	}

	@Override
	public IType getExpressionType() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setArrayExpression(IASTExpression arg0) {}

	@Override
	public void setSubscriptExpression(IASTExpression arg0) {}

}
