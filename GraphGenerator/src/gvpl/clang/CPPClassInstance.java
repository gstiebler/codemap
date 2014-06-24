package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.core.runtime.CoreException;

public class CPPClassInstance implements ICPPInternalBinding {

	static Logger logger = LogManager.getLogger(CPPClassInstance.class.getName());
	
	IASTName _name = null;
	BindingInfo _bindingInfo;
	
	public CPPClassInstance(String templateClassName, String line, IASTNode parent) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		IBinding binding = CPPASTTranslationUnit.getBinding(templateClassName + "<>");
		_name = new CPPASTName(binding, line, parent);
	}

	@Override
	public IASTNode getDefinition() {
		return _name;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return false;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public String getName() {
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

	@Override
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public ICPPDelegate createDelegate(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public IASTNode[] getDeclarations() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public void addDeclaration(IASTNode arg0) {}

	@Override
	public void addDefinition(IASTNode arg0) {}

	@Override
	public void removeDeclaration(IASTNode arg0) {}

}
