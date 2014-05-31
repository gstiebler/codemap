package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;

public class CPPASTPointer extends ASTNode implements IASTPointer {

	public CPPASTPointer(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
	}

	@Override
	public boolean isConst() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVolatile() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setConst(boolean arg0) {}

	@Override
	public void setVolatile(boolean arg0) {}

}
