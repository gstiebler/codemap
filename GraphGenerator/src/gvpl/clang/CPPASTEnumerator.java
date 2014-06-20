package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTEnumerator extends ASTNode implements IASTEnumerator {

	IASTName _name;
	IASTExpression _value;
	
	public CPPASTEnumerator(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		IBinding binding = new CPPEnumerator(cursor.getLine());
		_name = CPPASTName.loadASTName(binding, cursor.getLine(), this);
		cursor.nextLine();
		if(!cursor.theEnd())
			_value = ASTExpression.loadExpression(cursor.getSubCursor(), this);
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public IASTExpression getValue() {
		return _value;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void setName(IASTName arg0) {}

	@Override
	public void setValue(IASTExpression arg0) {}

}
