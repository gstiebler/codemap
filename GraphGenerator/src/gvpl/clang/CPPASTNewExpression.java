package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;

public class CPPASTNewExpression extends ASTNode implements ICPPASTNewExpression {

	IASTTypeId _typeId;
	IASTExpression _newInit;
	
	public CPPASTNewExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_typeId = new CPPASTTypeId(cursor, this);
		cursor.nextLine();
		if(!cursor.theEnd()) {
			_newInit = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		}
	}

	@Override
	public IASTTypeId getTypeId() {
		return _typeId;
	}

	@Override
	public IASTExpression getNewInitializer() {
		return _newInit;
	}

	@Override
	public IType getExpressionType() {
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
