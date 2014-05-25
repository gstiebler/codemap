package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTFunctionCallExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression {

	static Logger logger = LogManager.getLogger(ASTExpression.class.getName());
	
	IASTExpression _funcNameExpr;
	IASTExpression _paramExpr;
	
	public CPPASTFunctionCallExpression(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		_funcNameExpr = ASTExpression.loadExpression(cursor.getSubCursor(), parent);
		// TODO prepare for multiple parameters
		while(!cursor.theEnd()) {
			_paramExpr = ASTExpression.loadExpression(cursor.getSubCursor(), parent);
		}
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getFunctionNameExpression() {
		return _funcNameExpr;
	}

	@Override
	public IASTExpression getParameterExpression() {
		return _paramExpr;
	}

	@Override
	public void setFunctionNameExpression(IASTExpression arg0) {}

	@Override
	public void setParameterExpression(IASTExpression arg0) {}

}
