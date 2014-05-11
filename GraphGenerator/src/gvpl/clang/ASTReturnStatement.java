package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;

public class ASTReturnStatement extends ASTNode implements IASTReturnStatement {

	IASTExpression _value;
	
	public ASTReturnStatement(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		_value = ASTExpression.loadExpression(cursor, this);
	}

	@Override
	public IASTExpression getReturnValue() {
		return _value;
	}

	@Override
	public void setReturnValue(IASTExpression arg0) {}

}
