package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTNamedTypeSpecifier extends CPPASTBaseDeclSpecifier implements org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier {

	IASTName _name;
	
	public CPPASTNamedTypeSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor, parent);
		String line = cursor.getLine();
		String simpleType = CPPASTTranslationUnit.getSimplifiedUserType(line);
		String firstType = CPPASTTranslationUnit.getType(line);
		IBinding binding = null;
		if(firstType.equals("CXXNewExpr")) {
			cursor.nextLine();
			ClangLine strings = CPPASTTranslationUnit.lineToMap(cursor.getLine());
			String userType = CPPASTTranslationUnit.getUserType(strings, 1);
			binding = CPPASTTranslationUnit.getConstructorBinding(simpleType, userType);
		} else {
			binding = CPPASTTranslationUnit.getBinding(simpleType);
		}
		
		if(binding == null) {
			BindingInfo bi = CPPASTTranslationUnit.parseBindingInfo(line);
			binding = CPPASTTranslationUnit.getBinding(bi.type);
		}
		
		if(binding == null) {
			String[] strings = simpleType.split("[<]");
			// if it's a template
			if(strings.length == 2) {
				binding = new CPPClassInstance(strings[0], line, this);
			}
		}
		
		_name = CPPASTName.loadASTName(binding, line, parent);
	}
	

	public CPPASTNamedTypeSpecifier(Cursor cursor, IBinding binding, IASTNode parent) {
		super(cursor, parent);
		_name = CPPASTName.loadASTName(binding, cursor.getLine(), parent);
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public IASTName getName() {
		return _name;
	}

	@Override
	public void setName(IASTName arg0) {}

}
