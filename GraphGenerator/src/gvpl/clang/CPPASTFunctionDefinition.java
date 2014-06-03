package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTFunctionDefinition extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDefinition.class.getName());
	
	IASTDeclSpecifier _declSpec;
	
	public CPPASTFunctionDefinition(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_declSpec = new CPPASTSimpleDeclSpecifier(cursor, this);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return _declSpec;
	}

	@Override
	public IASTStatement getBody() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTFunctionDeclarator getDeclarator() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() {
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setBody(IASTStatement arg0) { }

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) { }

	@Override
	public void setDeclarator(IASTFunctionDeclarator arg0) { }

}