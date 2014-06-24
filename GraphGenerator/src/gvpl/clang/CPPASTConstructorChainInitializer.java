package gvpl.clang;

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
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		IBinding binding;
		if(!strings.containsKey("pointer")) {
			line = cursor.getLine();
			strings = CPPASTTranslationUnit.lineToMap(line);
			String userType = CPPASTTranslationUnit.getUserType(strings);
			String typeName = CPPASTTranslationUnit.simplifyType(userType);
			binding = CPPASTTranslationUnit.getConstructorBinding(typeName, CPPASTTranslationUnit.getUserType(strings, 1));
		} else {
			int bindId = CPPASTTranslationUnit.hexStrToInt(strings.get("pointer"));
			binding = CPPASTTranslationUnit.getBinding(bindId);
		}
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
		
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void setInitializerValue(IASTExpression arg0) {}

	@Override
	public void setMemberInitializerId(IASTName arg0) {}

}
