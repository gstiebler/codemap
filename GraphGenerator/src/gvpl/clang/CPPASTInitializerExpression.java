package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;

public class CPPASTInitializerExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTInitializerExpression {

	IASTExpression _initExpression;
	
	public CPPASTInitializerExpression(Cursor cursor) {
		super(cursor.getLine());
		while(!cursor.theEnd()) {
			_initExpression = ASTExpression.loadExpression(cursor);
		}
	}

	@Override
	public IASTExpression getExpression() {
		return _initExpression;
	}

	@Override
	public void setExpression(IASTExpression arg0) { }

}
