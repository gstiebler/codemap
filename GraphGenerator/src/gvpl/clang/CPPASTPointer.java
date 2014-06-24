package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;

public class CPPASTPointer extends ASTNode implements IASTPointer {

	public CPPASTPointer(String line, IASTNode parent) {
		super(line, parent);
	}

	@Override
	public boolean isConst() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isVolatile() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setConst(boolean arg0) {}

	@Override
	public void setVolatile(boolean arg0) {}

}
