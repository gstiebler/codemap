package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTExpressionStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTExpressionStatement {

	static Logger logger = LogManager.getLogger(CPPASTExpressionStatement.class.getName());
	
	IASTExpression _expression;
	
	public CPPASTExpressionStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_expression = ASTExpression.loadExpression(cursor, this);
	}

	@Override
	public IASTExpression getExpression() {
		return _expression;
	}

	@Override
	public void setExpression(IASTExpression arg0) {}

}
