package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.runtime.CoreException;

public class CPPClassType implements org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType {

	static Logger logger = LogManager.getLogger(CPPClassType.class.getName());

	BindingInfo _bindingInfo;
	String _name;
	public CPPASTCompositeTypeSpecifier _parent;
	
	public CPPClassType(String line, CPPASTCompositeTypeSpecifier parent) {
		_parent = parent;
		_bindingInfo = CPPASTTranslationUnit.parseBindingInfo(line);
		_name = _bindingInfo.name;
		String typeName = _bindingInfo.name;
		String compositeTypeName = CPPASTTranslationUnit.getCurrentNamespace() + typeName;
		CPPASTTranslationUnit.addBinding(compositeTypeName, this);
		CPPASTTranslationUnit.addBinding(_bindingInfo, this);
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
	@Override
	public Object clone() {
		return null;
	}
	
	@Override
	public IScope getCompositeScope() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public int getKey() throws DOMException {
		
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public ILinkage getLinkage() throws CoreException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public String getName() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public Object getAdapter(Class arg0) {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isSameType(IType arg0) {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public String[] getQualifiedName() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public char[][] getQualifiedNameCharArray() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isGloballyQualified() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public IField findField(String arg0) throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPBase[] getBases() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPConstructor[] getConstructors() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPField[] getDeclaredFields() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IField[] getFields() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IBinding[] getFriends() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPMethod[] getMethods() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public ICPPClassType[] getNestedClasses() throws DOMException {
		
		logger.error("Not implemented");
		return null;
	}

}
