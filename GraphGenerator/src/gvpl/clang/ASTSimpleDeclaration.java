package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

public class ASTSimpleDeclaration extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclaration.class.getName());
	
	public List<IASTDeclarator> _declarators = new ArrayList<IASTDeclarator>();
	IASTDeclSpecifier _declSpec;
	
	public ASTSimpleDeclaration(Cursor cursor) {
		super(cursor.getLine());
		String baseType = CPPASTTranslationUnit.getType(cursor.nextLine());
		if(baseType.equals("CXXRecordDecl")) {
			_declSpec = new CPPASTCompositeTypeSpecifier(cursor);
		} else {
			_declSpec = new CPPASTSimpleDeclSpecifier(cursor);
			String line = cursor.nextLine();
			while(!cursor.theEnd()) {
				String type = CPPASTTranslationUnit.getType(line);
				if(type.equals("VarDecl")) {
					_declarators.add(new CPPASTDeclarator(cursor));
				} else {
					logger.error("Error reading " + type);
				}
			}
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
