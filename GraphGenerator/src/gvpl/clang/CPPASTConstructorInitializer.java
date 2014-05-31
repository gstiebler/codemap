package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;

public class CPPASTConstructorInitializer extends ASTNode implements ICPPASTConstructorInitializer, IASTInitializer {

	IASTExpression _initExpression;

	public CPPASTConstructorInitializer(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_initExpression = new CPPASTExpressionList(cursor, this);
	}

	@Override
	public IASTExpression getExpression() {
		return _initExpression;
	}

	@Override
	public void setExpression(IASTExpression arg0) { }
}
