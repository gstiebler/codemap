package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTNode;


public class CPPASTStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTStatement {

	public CPPASTStatement(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		while(!cursor.theEnd()) {
			cursor.nextLine();
		}
	}


}
