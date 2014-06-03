package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;

public class CPPASTOperatorName extends CPPASTName implements ICPPASTOperatorName {

	public CPPASTOperatorName(IBinding binding, String line, IASTNode parent) {
		super(binding, line, parent);
	}

}
