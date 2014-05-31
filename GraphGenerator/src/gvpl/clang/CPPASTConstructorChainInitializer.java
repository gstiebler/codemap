package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;

public class CPPASTConstructorChainInitializer extends ASTNode implements ICPPASTConstructorChainInitializer{

	IASTExpression _initValue;
	IASTName _memberInitId;
	
	public CPPASTConstructorChainInitializer(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		List<Integer> ids = CPPASTTranslationUnit.getIds(line);
		IBinding binding = CPPASTTranslationUnit.getBinding(ids.get(0));
		_memberInitId = CPPASTName.loadASTName(binding, line, this);
		_initValue = ASTExpression.loadExpression(cursor.getSubCursor(), this);
	}

	@Override
	public IASTExpression getInitializerValue() {
		return _initValue;
	}

	@Override
	public IASTName getMemberInitializerId() {
		return _memberInitId;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void setInitializerValue(IASTExpression arg0) {}

	@Override
	public void setMemberInitializerId(IASTName arg0) {}

}
