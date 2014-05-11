package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTInitializerExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTInitializerExpression {

	IASTExpression _initExpression;
	
	public CPPASTInitializerExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		while(!cursor.theEnd()) {
			_initExpression = ASTExpression.loadExpression(cursor, this);
		}
	}

	@Override
	public IASTExpression getExpression() {
		return _initExpression;
	}

	@Override
	public void setExpression(IASTExpression arg0) { }

}
