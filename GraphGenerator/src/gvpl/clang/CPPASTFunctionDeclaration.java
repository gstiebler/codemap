package gvpl.clang;

import java.util.List;

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
		List<String> strings = CPPASTTranslationUnit.parseLine(line);
		
		int nameIndex = strings.size() - 2;
		if(strings.get(strings.size() - 1).equals("static"))
			nameIndex = strings.size() - 3;
		_funcName = strings.get(nameIndex);

		if(isMethod)
			_binding = new CPPMethod(bindingInfo, _funcName, cursor.getSubCursor());
		else
			_binding = new CPPFunction(bindingInfo, _funcName, cursor.getSubCursor());
			
		_declarator = new CPPASTFunctionDeclarator(_binding, new CPPASTFunctionDefinition(cursor, this), cursor);
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(type.equals("CompoundStmt")) {
			_body = new CPPASTCompoundStatement(cursor.getSubCursor(), this);
		} else {
			logger.error("Error reading " + type);
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
