package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTIdExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTIdExpression {

	public CPPASTName _name = null;
		
	public CPPASTIdExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		List<Integer> ids = CPPASTTranslationUnit.getIds(line);
		int bindingId = ids.get(ids.size() - 1);
		IBinding binding = CPPASTTranslationUnit.getBinding(bindingId);
		_name = CPPASTName.loadASTName(binding, line, this);
	}
	
	public String toString() {
		return _name.toString();
	}

	@Override
	public String getRawSignature() {
		return _name.toString();
	}

	@Override
	public IASTName getName() {
		return _name;
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
	public void setName(IASTName arg0) {}
	
}
