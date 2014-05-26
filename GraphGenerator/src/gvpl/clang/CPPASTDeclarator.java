package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTDeclarator extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTDeclarator {

	public CPPASTName _name = null;
	IASTInitializer _initializer;
	
	public CPPASTDeclarator(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.getLine();
		String firstType = CPPASTTranslationUnit.getType(line);
		IBinding binding = null;
		if(firstType.equals("FieldDecl"))
			binding = new CPPField(cursor);
		else if(firstType.equals("VarDecl") || firstType.equals("ParmVarDecl"))
			binding = new CPPVariable(line);
		else
			logger.error("Type not expected: {}", firstType);
		
		_name = CPPASTName.loadASTName(binding, line, this);
		String line2 = cursor.nextLine();
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(!cursor.theEnd()) {
			if(type.equals("CStyleCastExpr") || 
					type.equals("CXXFunctionalCastExpr") || 
					type.equals("CallExpr") || 
					type.equals("BinaryOperator") || 
					type.equals("CXXMemberCallExpr") || 
					type.equals("ImplicitCastExpr"))
				_initializer = new CPPASTInitializerExpression(cursor.getSubCursor(), this);
			else
				cursor.runToTheEnd();				
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
	public IASTInitializer getInitializer() {
		return _initializer;
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
