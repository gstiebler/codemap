package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTExpressionStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTExpressionStatement {

	IASTExpression _expression;
	
	public CPPASTExpressionStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("BinaryOperator")) {
			_expression = new CPPASTBinaryExpression(cursor, this);
		}
	}

	@Override
	public IASTExpression getExpression() {
		return _expression;
	}

	@Override
	public void setExpression(IASTExpression arg0) {}

}
