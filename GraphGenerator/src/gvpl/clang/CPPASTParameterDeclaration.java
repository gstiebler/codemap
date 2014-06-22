package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTParameterDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration{

	IASTDeclSpecifier _declSpec;
	IASTDeclarator _declarator;
	
	public CPPASTParameterDeclaration(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		String userType = CPPASTTranslationUnit.getUserType(parsedLine);
		if(userType.contains("(")) { // is function pointer
			IBinding cppTypedef = CPPASTTranslationUnit.getBinding(userType);
			_declSpec = new CPPASTNamedTypeSpecifier(cursor, cppTypedef, parent);
		} else {
			_declSpec = CPPASTBaseDeclSpecifier.loadDeclSpec(cursor.getSubCursor(), this);
		}
		_declarator = new CPPASTDeclarator(cursor.getSubCursor(), this);
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
