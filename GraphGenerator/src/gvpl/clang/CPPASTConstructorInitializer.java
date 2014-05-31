package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;

public class CPPASTConstructorInitializer extends CPPASTInitializerExpression implements ICPPASTConstructorInitializer {

	public CPPASTConstructorInitializer(Cursor cursor, IASTNode parent) {
		super(cursor, parent);
	}
}
