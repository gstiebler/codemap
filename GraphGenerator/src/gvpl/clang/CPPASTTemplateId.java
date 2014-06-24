package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;

public class CPPASTTemplateId extends CPPASTName implements ICPPASTTemplateId {

	public CPPASTTemplateId(IBinding binding, String line, IASTNode parent) {
		super(binding, line, parent);
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		
		logger.error("not implemented");
		return 0;
	}

	@Override
	public IASTNode[] getTemplateArguments() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public IASTName getTemplateName() {
		
		logger.error("not implemented");
		return null;
	}

	@Override
	public void addTemplateArgument(IASTTypeId arg0) {}

	@Override
	public void addTemplateArgument(IASTExpression arg0) {}

	@Override
	public void setTemplateName(IASTName arg0) {}

}
