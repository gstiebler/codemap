package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTBreakStatement extends ASTNode implements IASTBreakStatement {

	public CPPASTBreakStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.runToTheEnd();
	}

}
