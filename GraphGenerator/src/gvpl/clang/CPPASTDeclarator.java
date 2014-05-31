package gvpl.clang;

import java.util.List;

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
		
		_name = CPPASTName.loadASTName(binding, line, this);
		
		{
			List<String> strings = CPPASTTranslationUnit.parseLine(line);
			String currType = strings.get(strings.size() - 1);
			int count = currType.length() - currType.replace("*", "").length();
			if(count > 0) {
				_pointerOperators = new IASTPointerOperator [count];
				for(int i = 0; i < count; i++) {
					_pointerOperators[i] = new CPPASTPointer(cursor, this);
				}
			}
		}
		
		cursor.nextLine();
		
		String type = CPPASTTranslationUnit.getType(cursor.getLine());
		if(!cursor.theEnd()) {
			if(type.equals("CStyleCastExpr") || 
					type.equals("CXXFunctionalCastExpr") || 
					type.equals("CallExpr") || 
					type.equals("BinaryOperator") || 
					type.equals("CXXMemberCallExpr") || 
					type.equals("ImplicitCastExpr") || 
					type.equals("IntegerLiteral") || 
					type.equals("ConditionalOperator"))
				_initializer = new CPPASTInitializerExpression(cursor.getSubCursor(), this);
			else if (type.equals("CXXConstructExpr"))
				cursor.runToTheEnd();				
			else {
				logger.error("Not implemented {}", type);
				cursor.runToTheEnd();				
			}
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
