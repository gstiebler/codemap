package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class ASTSimpleDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclaration.class.getName());
	
	public List<IASTDeclarator> _declarators = new ArrayList<IASTDeclarator>();
	IASTDeclSpecifier _declSpec;
	
	public ASTSimpleDeclaration(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String baseType = CPPASTTranslationUnit.getType(cursor.nextLine());
		if(baseType.equals("CXXRecordDecl")) {
			_declSpec = new CPPASTCompositeTypeSpecifier(cursor, this);
		} else if(baseType.equals("FieldDecl")) {
			logger.error("Not implemented  " + baseType);
		} else if(baseType.equals("DeclStmt")) {
			_declSpec = new CPPASTSimpleDeclSpecifier(cursor, this);
			while(!cursor.theEnd()) {
				String line = cursor.getLine();
				String type = CPPASTTranslationUnit.getType(line);
				if(type.equals("VarDecl")) {
					_declarators.add(new CPPASTDeclarator(cursor, this));
				} else {
					logger.error("Error reading " + type);
				}
			}
		} else {
			logger.error("Not implemented  " + baseType);
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
