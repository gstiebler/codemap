package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

public class CPPASTParameterDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration {

	public CPPASTParameterDeclaration(String line) {
		super(line);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTDeclarator getDeclarator() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {}

	@Override
	public void setDeclarator(IASTDeclarator arg0) {}

}
