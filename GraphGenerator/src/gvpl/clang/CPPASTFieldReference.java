package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTFieldReference extends ASTNode implements IASTFieldReference {

	static Logger logger = LogManager.getLogger(CPPASTFieldReference.class.getName());
	
	IASTName _fieldName;
	IASTExpression _fieldOwner;
	
	public CPPASTFieldReference(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		ClangLine strings = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		String bindingStr = strings.get("pointer", 1);
		int bindingId = CPPASTTranslationUnit.hexStrToInt(bindingStr);
		IBinding binding = CPPASTTranslationUnit.getBinding(bindingId);
		_fieldName = CPPASTName.loadASTName(binding, cursor.nextLine(), this);
		_fieldOwner = ASTExpression.loadExpression(cursor.getSubCursor(), this);
	}

	@Override
	public IASTName getFieldName() {
		return _fieldName;
	}

	@Override
	public IASTExpression getFieldOwner() {
		return _fieldOwner;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public boolean isPointerDereference() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setFieldName(IASTName arg0) {}

	@Override
	public void setFieldOwner(IASTExpression arg0) {}

	@Override
	public void setIsPointerDereference(boolean arg0) {}

}
