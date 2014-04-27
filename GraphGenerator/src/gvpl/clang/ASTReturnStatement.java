package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;

public class ASTReturnStatement extends ASTNode implements IASTReturnStatement {

	IASTExpression _value;
	
	public ASTReturnStatement(Cursor cursor) {
		super(cursor.nextLine());
		_value = ASTExpression.loadExpression(cursor);
	}

	@Override
	public IASTExpression getReturnValue() {
		return _value;
	}

	@Override
	public void setReturnValue(IASTExpression arg0) {}

}
