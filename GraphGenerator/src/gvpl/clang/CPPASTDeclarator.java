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
	IASTPointerOperator[] _pointerOperators;
	
	public CPPASTDeclarator(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.getLine();
		String firstType = CPPASTTranslationUnit.getType(line);
		IBinding binding = null;
		if(firstType.equals("FieldDecl"))
			binding = new CPPField(cursor);
		else if(firstType.equals("VarDecl"))
			binding = new CPPVariable(line);
		else if(firstType.equals("ParmVarDecl"))
			binding = new CPPParameter(line);
		else
			logger.error("Type not expected: {}", firstType);
		
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
		CPPASTTranslationUnit.addBindingSynonymIfNecessary(parsedLine, binding);
		_name = CPPASTName.loadASTName(binding, line, this);
		_pointerOperators = loadPointerOps(line, this);
		cursor.nextLine();
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(!cursor.theEnd()) {
			if(type.equals("CXXConstructExpr")) {
				_initializer = new CPPASTConstructorInitializer(cursor.getSubCursor(), this);
				if(((CPPASTConstructorInitializer)_initializer).getExpression() == null)
					_initializer = null;
			} else  {
				_initializer = new CPPASTInitializerExpression(cursor.getSubCursor(), this);
				if(((CPPASTInitializerExpression)_initializer).getExpression() == null)
					_initializer = null;
			}
		}
	}
	
	public static IASTPointerOperator[] loadPointerOps(String line, IASTNode parent) {
		IASTPointerOperator[] result = null;
		ClangLine strings = CPPASTTranslationUnit.lineToMap(line);
		String currType = strings.get("type");
		currType = currType.split("[(]")[0];
		int count = currType.length() - currType.replace("*", "").length();
		if(count > 0) {
			result = new IASTPointerOperator [count];
			for(int i = 0; i < count; i++) {
				result[i] = new CPPASTPointer(line, parent);
			}
		}
		return result;
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
	public IASTPointerOperator[] getPointerOperators() {
		return _pointerOperators;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public void addPointerOperator(IASTPointerOperator arg0) {}

	@Override
	public IASTDeclarator getNestedDeclarator() {
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
