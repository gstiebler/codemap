package gvpl.clang;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.CoreException;

public class CPPEnumeration implements IBinding {

	String _name;
	BindingInfo _bindingInfo;
	
	public CPPEnumeration(String line) {
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		_name = parsedLine.get("name");
		CPPASTTranslationUnit.addBinding(_name, this);
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

}
