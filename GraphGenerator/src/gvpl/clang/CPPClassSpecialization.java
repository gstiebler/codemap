package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.core.runtime.CoreException;

public class CPPClassSpecialization implements ICPPSpecialization {

	static Logger logger = LogManager.getLogger(CPPClassSpecialization.class.getName());

	BindingInfo _bindingInfo;	
	
	public CPPClassSpecialization(String line, String specializationType) {
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		//_name = _bindingInfo.name;
		String typeName = _bindingInfo.name;
		String compositeTypeName = CPPASTTranslationUnit.getCurrentNamespace() + typeName + "<" + specializationType + ">";
		CPPASTTranslationUnit.addBinding(compositeTypeName, this);
		CPPASTTranslationUnit.addBinding(_bindingInfo, this);
	}
	
	@Override
	public String toString() {
		return _bindingInfo.name;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		
		logger.error("not implemented");
		return false;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public String getName() {
		
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public Object getAdapter(Class arg0) {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public ObjectMap getArgumentMap() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public IBinding getSpecializedBinding() {
		
		logger.error("not implemented");
		return null;
	}

}
