package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class ASTSimpleDeclaration implements org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration {

	public List<IASTDeclarator> _declarators = new ArrayList<IASTDeclarator>();
	
	public ASTSimpleDeclaration(Cursor cursor) {
		while(!cursor.theEnd()) {
			cursor.nextLine();
		}
	}
	
	@Override
	public boolean accept(ASTVisitor arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean contains(IASTNode arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getContainingFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTNode getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ASTNodeProperty getPropertyInParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRawSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTTranslationUnit getTranslationUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParent(IASTNode arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPropertyInParent(ASTNodeProperty arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDeclarator(IASTDeclarator arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		return new CPPASTSimpleDeclSpecifier();
	}

	@Override
	public IASTDeclarator[] getDeclarators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {
		// TODO Auto-generated method stub
		
	}

}
