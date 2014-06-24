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
		
		if(isOperator(line))
			_names[1] = new CPPASTOperatorName(binding, line, parent);
		else
			_names[1] = this;
	}
	
	public static boolean isOperator(String line) {
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		// TODO improve check if the function is an operator
		String funcName = strings.getAndCheck("name");
		return funcName.contains("operator");
	}

	@Override
	public IASTName[] getNames() {
		return _names;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void addName(IASTName arg0) {
		
		logger.error("Not implemented");
	}

	@Override
	public IASTName getLastName() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public boolean isConversionOrOperator() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public boolean isFullyQualified() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setFullyQualified(boolean arg0) {}

}
