package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

public class CPPASTTypeId extends ASTNode implements IASTTypeId{

	IASTDeclSpecifier _declSpec;
	
	public CPPASTTypeId(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		_declSpec = CPPASTBaseDeclSpecifier.loadDeclSpec(cursor.getSubCursor(), this);
	}

	@Override
	public IASTDeclSpecifier getDeclSpecifier() {
		return _declSpec;
	}

	@Override
	public IASTDeclarator getAbstractDeclarator() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setAbstractDeclarator(IASTDeclarator arg0) {}

	@Override
	public void setDeclSpecifier(IASTDeclSpecifier arg0) {}

}
