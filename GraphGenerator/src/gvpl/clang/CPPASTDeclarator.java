package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;

public class CPPASTDeclarator extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclarator {

	public CPPASTName _name = null;
	
	public CPPASTDeclarator(Cursor cursor) {
		super(cursor.getLine());
		String line = cursor.nextLine();
		_name = new CPPASTName(new CPPVariable(line), line);
		while(!cursor.theEnd()) {
			String line2 = cursor.nextLine();
			int x = 5;
		}
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
		return 0;
	}

	@Override
	public void addPointerOperator(IASTPointerOperator arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IASTInitializer getInitializer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTDeclarator getNestedDeclarator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTPointerOperator[] getPointerOperators() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInitializer(IASTInitializer arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setName(IASTName arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setNestedDeclarator(IASTDeclarator arg0) {
		// TODO Auto-generated method stub
		
	}

}
