package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTFunctionDeclaration extends CPPASTDeclaration implements org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition {

	public Binding _binding = null;
	public String _funcName = null;
	public CPPASTFunctionDeclarator _declarator = null;
	
	public CPPASTFunctionDeclaration(List<String> lines, int pos) {
		String line = lines.get(pos);
		String postX = line.split("0x")[1];
		String bindText = postX.split(" ")[0];
		int bindingId = Integer.parseInt(bindText, 16);
		_binding = new Binding(bindingId);
		String postBico = postX.split(">")[1];
		_funcName = postBico.split(" ")[2];
		_declarator = new CPPASTFunctionDeclarator(_binding);
	}
	
	@Override
	public IASTStatement getBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTFunctionDeclarator getDeclarator() {
		return _declarator;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBody(IASTStatement arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDeclarator(IASTFunctionDeclarator arg0) {
		// TODO Auto-generated method stub
		
	}

}
