package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;

public class CPPASTDeclaration extends ASTNode implements IASTDeclaration {

	public CPPASTDeclaration(String line) {
		super(line);
	}

}
