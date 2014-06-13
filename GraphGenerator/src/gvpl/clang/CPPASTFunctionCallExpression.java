package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTFunctionCallExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression{

	static Logger logger = LogManager.getLogger(CPPASTFunctionCallExpression.class.getName());
	
	IASTExpression _functionNameExpression;
	IASTExpressionList _parameterExpression;
	
	public CPPASTFunctionCallExpression(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		_functionNameExpression = ASTExpression.loadExpression(cursor, this);
		_parameterExpression = new CPPASTExpressionList(cursor, this);
	}

	@Override
	public IASTExpression getFunctionNameExpression() {
		return _functionNameExpression;
	}

	@Override
	public IASTExpression getParameterExpression() {
		return _parameterExpression;
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
