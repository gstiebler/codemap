package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IType;

public class ASTFunctionCallExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression{

	IASTExpression _functionNameExpression;
	IASTExpressionList _exprList;
	
	public ASTFunctionCallExpression(Cursor cursor) {
		super(cursor.nextLine());
		_functionNameExpression = ASTExpression.loadExpression(cursor);
		_exprList = new CPPASTExpressionList(cursor);
	}

	@Override
	public IASTExpression getFunctionNameExpression() {
		return _functionNameExpression;
	}

	@Override
	public IASTExpression getParameterExpression() {
		return _exprList;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFunctionNameExpression(IASTExpression arg0) { }

	@Override
	public void setParameterExpression(IASTExpression arg0) { }

}
