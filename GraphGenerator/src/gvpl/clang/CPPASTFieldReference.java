package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTFieldReference extends ASTNode implements IASTFieldReference {

	IASTName _fieldName;
	
	public CPPASTFieldReference(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_fieldName = CPPASTName.loadASTName(binding, cursor.getLine(), this);
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IASTName getFieldName() {
		return _fieldName;
	}

	@Override
	public IASTExpression getFieldOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPointerDereference() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFieldName(IASTName arg0) {}

	@Override
	public void setFieldOwner(IASTExpression arg0) {}

	@Override
	public void setIsPointerDereference(boolean arg0) {}

}
