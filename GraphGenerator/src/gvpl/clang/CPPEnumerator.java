package gvpl.clang;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.CoreException;

public class CPPEnumerator implements IBinding {

	static Logger logger = LogManager.getLogger(CPPEnumerator.class.getName());

	BindingInfo _bindingInfo;
	
	public CPPEnumerator(String line) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		CPPASTTranslationUnit.addBinding(_bindingInfo, this);
	}
	
	public String toString() {
		return _bindingInfo.name;
	}

	@Override
	public String getName() {
		return _bindingInfo.name;
	}
	
	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

}
