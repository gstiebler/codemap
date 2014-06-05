package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTName extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTName{

	public IBinding _binding = null;
	
	public static CPPASTName loadASTName(IBinding binding, String line, IASTNode parent) {
		if(binding == null)
			logger.error("Binding should not be null");
		BindingInfo bi = CPPASTTranslationUnit.parseBindingInfo(line);

		List<String> strings = CPPASTTranslationUnit.parseLine(line);
		// TODO improve check if the function is an operator
		if(strings.size() >= 5) {
			List<Integer> ids = CPPASTTranslationUnit.getIds(line);
			int index = 4;
			if(ids.size() == 3)
				index = 8;
			
			if(strings.get(index).contains("operator"))
				return new CPPASTOperatorName(binding, line, parent);
		}
		
		if(bi.type.equals("CXXMethod")) {
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
	public void setBinding(IBinding arg0) {}
}
