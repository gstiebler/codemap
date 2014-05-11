package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;

public class CPPASTDeclarator extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclarator {

	public CPPASTName _name = null;
	IASTInitializer _initializer;
	
	public CPPASTDeclarator(Cursor cursor) {
		super(cursor.getLine());
		String line = cursor.getLine();
		_name = CPPASTName.loadASTName(new CPPVariable(line), line);
		String line2 = cursor.nextLine();
		if(!cursor.theEnd())
			_initializer = new CPPASTInitializerExpression(cursor);
	}
	
	public String toString() {
		return _name.toString();
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void addPointerOperator(IASTPointerOperator arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public IASTInitializer getInitializer() {
		return _initializer;
	}

	@Override
	public IASTDeclarator getNestedDeclarator() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IASTPointerOperator[] getPointerOperators() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setInitializer(IASTInitializer arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		
	}

	@Override
	public void setName(IASTName arg0) { }

	@Override
	public void setNestedDeclarator(IASTDeclarator arg0) { }

}
