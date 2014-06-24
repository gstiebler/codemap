package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

public class CPPParameter extends CPPVariable implements ICPPParameter {

	public CPPParameter(String line) {
		super(line);
	}

	@Override
	public boolean isMutable() throws DOMException {
		
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
	public boolean hasDefaultValue() {
		
		logger.error("Not implemented");
		return false;
	}

}
