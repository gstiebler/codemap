package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTQualifiedName extends CPPASTName implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName {
	
	static Logger logger = LogManager.getLogger(CPPASTQualifiedName.class.getName());

	IASTName[] _names = new IASTName[2];
	
	public CPPASTQualifiedName(IBinding binding, String line, IASTNode parent) {
		super(binding, line, parent);
		_names[0] = ((CPPMethod) binding).className;
		_names[1] = this;
		// TODO Auto-generated constructor stub
	}

	@Override
	public IASTName[] getNames() {
		return _names;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void addName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public IASTName getLastName() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isConversionOrOperator() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isFullyQualified() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setFullyQualified(boolean arg0) {}

}
