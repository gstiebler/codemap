package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTSimpleDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration {

	static Logger logger = LogManager.getLogger(CPPASTSimpleDeclaration.class.getName());
	
	public List<IASTDeclarator> _declarators = new ArrayList<IASTDeclarator>();
	IASTDeclSpecifier _declSpec;
	
	public CPPASTSimpleDeclaration(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String baseType = CPPASTTranslationUnit.getType(cursor.getLine());
		if(baseType.equals("CXXRecordDecl")) {
			_declSpec = new CPPASTCompositeTypeSpecifier(cursor.getSubCursor(), this);
		} else if (baseType.equals("EnumDecl")) {
			_declSpec = new CPPASTEnumerationSpecifier(cursor.getSubCursor(), parent);
		} else if(baseType.equals("FieldDecl") || baseType.equals("VarDecl")) {
			_declSpec = CPPASTBaseDeclSpecifier.loadDeclSpec(cursor, this);
			_declarators.add(new CPPASTDeclarator(cursor.getSubCursor(), this));
		} else if(baseType.equals("DeclStmt")) {
			cursor.nextLine();
			_declSpec = CPPASTBaseDeclSpecifier.loadDeclSpec(cursor, this);
			while(!cursor.theEnd()) {
				String line = cursor.getLine();
				String type = CPPASTTranslationUnit.getType(line);
				if(type.equals("VarDecl")) {
					_declarators.add(new CPPASTDeclarator(cursor.getSubCursor(), this));
				} else {
					logger.error("Error reading " + type);
				}
			}
		} else {
			logger.error("Not implemented  " + baseType);
			cursor.runToTheEnd();
		}
	}
	
	@Override
	public String toString() {
		return _declSpec.toString() + " - " + _declarators.toString();
	}
	
	public CPPASTSimpleDeclaration(String line, IASTNode parent, IASTDeclSpecifier declSpec, IASTDeclarator child) {
		super(line, parent);
		_declSpec = declSpec;
		if(child != null) {
			_declarators.add(child);
			if(child instanceof CPPASTFunctionDeclarator) {
				CPPASTFunctionDeclarator funcDecl = (CPPASTFunctionDeclarator) child;
				IASTNode fdParent = funcDecl._parent;
				if(fdParent instanceof CPPASTFunctionDefinition) {
					_declSpec = ((CPPASTFunctionDefinition)fdParent)._declSpec;
					return;
				}
			}
			logger.error("not expected");
		}
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return _declSpec;
	}

	@Override
	public IASTDeclarator[] getDeclarators() {
		IASTDeclarator[] result = new IASTDeclarator [_declarators.size()];
		return _declarators.toArray(result);
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) { }

	@Override
	public void addDeclarator(IASTDeclarator arg0) { }

}
