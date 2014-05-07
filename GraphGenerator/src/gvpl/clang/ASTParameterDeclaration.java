package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

public class ASTParameterDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration{

	IASTDeclSpecifier _declSpec;
	IASTDeclarator _declarator;
	
	public ASTParameterDeclaration(Cursor cursor) {
		super(cursor.getLine());
		
		_declSpec = new ASTDeclSpecifier(cursor);
		_declarator = new CPPASTDeclarator(cursor.getSubCursor());
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return _declSpec;
	}

	@Override
	public IASTDeclarator getDeclarator() {
		return _declarator;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {}

	@Override
	public void setDeclarator(IASTDeclarator arg0) {}


}
