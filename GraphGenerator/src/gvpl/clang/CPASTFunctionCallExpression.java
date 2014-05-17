package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPASTFunctionCallExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression {

	static Logger logger = LogManager.getLogger(ASTExpression.class.getName());
	
	public CPASTFunctionCallExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.runToTheEnd();
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getFunctionNameExpression() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getParameterExpression() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setFunctionNameExpression(IASTExpression arg0) {}

	@Override
	public void setParameterExpression(IASTExpression arg0) {}

}
