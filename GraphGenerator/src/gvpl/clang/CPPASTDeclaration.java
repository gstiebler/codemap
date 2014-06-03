package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTDeclaration extends ASTNode implements IASTDeclaration {

	public CPPASTDeclaration(String line, IASTNode parent) {
		super(line, parent);
	}

}
