package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;

public class CPPASTExpressionStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTExpressionStatement {

	IASTExpression _expression;
	
	public CPPASTExpressionStatement(Cursor cursor) {
		super(cursor.getLine());
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("BinaryOperator")) {
			_expression = new CPPASTBinaryExpression(cursor);
		}
	}

	@Override
	public IASTExpression getExpression() {
		return _expression;
	}

	@Override
	public void setExpression(IASTExpression arg0) {
		// TODO Auto-generated method stub
		
	}

}
