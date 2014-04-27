package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

public class ASTParameterDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration{

	public ASTParameterDeclaration(String line) {
		super(line);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTDeclarator getDeclarator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeclarator(IASTDeclarator arg0) {
		// TODO Auto-generated method stub
		
	}


}
