package gvpl.clang;


public class CPPASTStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTStatement {

	public CPPASTStatement(Cursor cursor) {
		super(cursor.nextLine());
		while(!cursor.theEnd()) {
			cursor.nextLine();
		}
	}


}
