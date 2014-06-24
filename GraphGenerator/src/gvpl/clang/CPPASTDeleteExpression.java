package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;

public class CPPASTDeleteExpression extends ASTNode implements ICPPASTDeleteExpression {

	IASTExpression _operand;
	
	public CPPASTDeleteExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_operand = ASTExpression.loadExpression(cursor.getSubCursor(), parent);
	}

	@Override
	public IType getExpressionType() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getOperand() {
		return _operand;
	}

	@Override
	public boolean isGlobal() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVectored() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setIsGlobal(boolean arg0) {}

	@Override
	public void setIsVectored(boolean arg0) {}

	@Override
	public void setOperand(IASTExpression arg0) {}

}
