package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;

public class CPPASTReturnStatement extends ASTNode implements IASTReturnStatement {

	IASTExpression _value;
	
	public CPPASTReturnStatement(Cursor cursor, IASTNode parent) {
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
