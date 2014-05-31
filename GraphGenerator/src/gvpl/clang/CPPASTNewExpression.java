package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

public class CPPASTNewExpression extends ASTNode implements ICPPASTNewExpression {

	public CPPASTNewExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.runToTheEnd();
		// TODO Auto-generated constructor stub
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getNewInitializer() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression getNewPlacement() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTExpression[] getNewTypeIdArrayExpressions() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTTypeId getTypeId() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isGlobal() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isNewTypeId() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void addNewTypeIdArrayExpression(IASTExpression arg0) {}
	
	@Override
	public void setIsGlobal(boolean arg0) {}

	@Override
	public void setIsNewTypeId(boolean arg0) {}

	@Override
	public void setNewInitializer(IASTExpression arg0) {}

	@Override
	public void setNewPlacement(IASTExpression arg0) {}

	@Override
	public void setTypeId(IASTTypeId arg0) {}

}
