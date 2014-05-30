package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTConditionalExpression extends ASTNode implements IASTConditionalExpression {

	IASTExpression _conditionExpr;
	IASTExpression _positiveExpr;
	IASTExpression _negativeExpr;
	
	public CPPASTConditionalExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_conditionExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_positiveExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_negativeExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		if(!cursor.theEnd())
			cursor.runToTheEnd();
	}

	@Override
	public IASTExpression getLogicalConditionExpression() {
		return _conditionExpr;
	}

	@Override
	public IASTExpression getNegativeResultExpression() {
		return _negativeExpr;
	}

	@Override
	public IASTExpression getPositiveResultExpression() {
		return _positiveExpr;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setLogicalConditionExpression(IASTExpression arg0) {}

	@Override
	public void setNegativeResultExpression(IASTExpression arg0) {}

	@Override
	public void setPositiveResultExpression(IASTExpression arg0) {}

}
