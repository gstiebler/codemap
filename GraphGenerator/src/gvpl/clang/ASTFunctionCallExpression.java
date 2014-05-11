package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class ASTFunctionCallExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression{

	static Logger logger = LogManager.getLogger(ASTFunctionCallExpression.class.getName());
	
	IASTExpression _functionNameExpression;
	IASTExpressionList _exprList;
	
	public ASTFunctionCallExpression(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		_functionNameExpression = ASTExpression.loadExpression(cursor, this);
		_exprList = new CPPASTExpressionList(cursor, this);
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
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setFunctionNameExpression(IASTExpression arg0) { }

	@Override
	public void setParameterExpression(IASTExpression arg0) { }

}
