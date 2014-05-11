package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTFunctionDeclaration extends CPPASTDeclaration implements org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDeclaration.class.getName());
	
	public IBinding _binding = null;
	public String _funcName = null;
	public CPPASTFunctionDeclarator _declarator = null;
	public IASTStatement _body = null;
	
	public CPPASTFunctionDeclaration(Cursor cursor, boolean isMethod, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.getLine();
		BindingInfo bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		_funcName = bindingInfo.name;
		if(isMethod)
			_binding = new CPPMethod(bindingInfo.bindingId, _funcName, cursor.getSubCursor());
		else
			_binding = new CPPFunction(bindingInfo.bindingId, _funcName, cursor.getSubCursor());
		_declarator = new CPPASTFunctionDeclarator(_binding, new ASTFunctionDefinition(cursor, this), cursor);
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(type.equals("CompoundStmt")) {
			_body = new CPPASTCompoundStatement(cursor.getSubCursor(), this);
		} else {
			logger.error("Error reading " + type);
		}
	}
	
	@Override
	public IASTStatement getBody() {
		return _body;
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTFunctionDeclarator getDeclarator() {
		return _declarator;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setBody(IASTStatement arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void setDeclarator(IASTFunctionDeclarator arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

}
