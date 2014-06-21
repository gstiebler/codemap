package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTFunctionDefinition extends CPPASTDeclaration implements org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition {

	static Logger logger = LogManager.getLogger(CPPASTFunctionDefinition.class.getName());
	
	public IBinding _binding = null;
	public String _funcName = null;
	public CPPASTFunctionDeclarator _declarator = null;
	public IASTStatement _body = null;
	public IASTDeclSpecifier _declSpec;
	
	public CPPASTFunctionDefinition(Cursor cursor, boolean isMethod, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.getLine();
		String firstType = CPPASTTranslationUnit.getType(line);
		if(firstType.equals("CXXConstructorDecl") || firstType.equals("CXXDestructorDecl"))
			_declSpec = new CPPASTSimpleDeclSpecifier(cursor, parent);
		else
			_declSpec = CPPASTBaseDeclSpecifier.loadDeclSpec(cursor.getSubCursor(), this);
		BindingInfo bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		
		_funcName = strings.get("name");

		if(firstType.equals("CXXConstructorDecl")) {
			_binding = new CPPConstructor(bindingInfo, _funcName, cursor.getSubCursor(), this);
		} else if(isMethod)
			_binding = new CPPMethod(bindingInfo, _funcName, cursor.getSubCursor(), this);
		else
			_binding = new CPPFunction(bindingInfo, _funcName, cursor.getSubCursor(), this);
			
		{
			int bindingId = Binding.getBindingId(_binding);
			CPPASTTranslationUnit.addBindingOwner(bindingId, this);
		}	
	
		_declarator = new CPPASTFunctionDeclarator(_binding, this, cursor);
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(type.equals("CXXCtorInitializer")) {
			cursor.getSubCursor().runToTheEnd();
			type = CPPASTTranslationUnit.getType(cursor.getLine());
			logger.error("Error reading CXXCtorInitializer");
			cursor.runToTheEnd();
		}
		
		if(cursor.theEnd())
			return;
		
		if(type.equals("CompoundStmt")) {
			_body = new CPPASTCompoundStatement(cursor.getSubCursor(), this);
		} else {
			logger.error("Error reading {}, line {}", type, cursor.getPos());
			cursor.runToTheEnd();
		}
	}
	
	@Override
	public String toString() {
		return _funcName;
	}
	
	@Override
	public IASTStatement getBody() {
		return _body;
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return _declSpec;
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
	public void setBody(IASTStatement arg0) {}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {}

	@Override
	public void setDeclarator(IASTFunctionDeclarator arg0) {}

}