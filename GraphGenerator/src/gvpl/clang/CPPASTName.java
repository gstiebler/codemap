package gvpl.clang;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTName extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTName{

	public IBinding _binding = null;
	
	public static CPPASTName loadASTName(IBinding binding, String line) {
		BindingInfo bi = CPPASTTranslationUnit.parseBindingInfo(line);
		if(bi.type.equals("CXXMethod"))
			return new CPPASTQualifiedName(binding, line);
		else
			return new CPPASTName(binding, line);
	}
	
	public CPPASTName(IBinding binding, String line) {
		super(line);
		_binding = binding;
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
		return false;
	}

	@Override
	public boolean isDefinition() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReference() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public char[] toCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTCompletionContext getCompletionContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILinkage getLinkage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBinding(IBinding arg0) {
		// TODO Auto-generated method stub
		
	}
}
