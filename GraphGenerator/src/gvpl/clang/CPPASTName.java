package gvpl.clang;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTName extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTName{

	private IBinding _binding = null;
	
	public static CPPASTName loadASTName(IBinding binding, String line, IASTNode parent) {
		if(binding == null)
			logger.error("Binding should not be null");

		String type = CPPASTTranslationUnit.getType(line);
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		if(type.equals("DeclRefExpr")) { 
			type = strings.get("kindName");
		}
		
		if((type.equals("CXXMethod") || 
				type.equals("CXXMethodDecl") || 
				type.equals("CXXConstructorDecl")) &&
					binding != null) {
			if(CPPASTQualifiedName.isOperator(line) && !strings.containsKey("prev"))// if it's the operator declaration, the name is OperatorName
				return new CPPASTOperatorName(binding, line, parent);
			else // otherwise it will be QualifiedName with OperatorName inside
				return new CPPASTQualifiedName(binding, line, parent);
		} else
			return new CPPASTName(binding, line, parent);
	}
	
	public CPPASTName(IBinding binding, String line, IASTNode parent) {
		super(line, parent);
		_binding = binding;

		int bindingId = Binding.getBindingId(binding);
		CPPASTTranslationUnit.addBindingOwner(bindingId, this);	
	}
	
	@Override
	public String toString() {
		return _binding.toString();
	}
	
	@Override
	public IBinding getBinding() {
		return _binding;
	}

	@Override
	public IBinding resolveBinding() {
		return _binding;
	}

	@Override
	public boolean isDeclaration() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isDefinition() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isReference() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public char[] toCharArray() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ILinkage getLinkage() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setBinding(IBinding binding) {
		_binding = binding;
	}
}
