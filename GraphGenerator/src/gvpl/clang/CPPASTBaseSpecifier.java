package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;

public class CPPASTBaseSpecifier extends ASTNode implements ICPPASTBaseSpecifier {

	IASTName _name;
	
	public CPPASTBaseSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String simpleType = CPPASTTranslationUnit.getSimplifiedUserType(cursor.getLine());
		IBinding binding = CPPASTTranslationUnit.getBinding(simpleType);
		_name = CPPASTName.loadASTName(binding, cursor.getLine(), this);
		cursor.nextLine();
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public int getVisibility() {
		
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public boolean isVirtual() {
		
		logger.error("Not implemented");
		return false;
	}

	@Override
	public void setName(IASTName arg0) {}

	@Override
	public void setVirtual(boolean arg0) {}

	@Override
	public void setVisibility(int arg0) {}

}
