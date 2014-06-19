package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTNamedTypeSpecifier extends CPPASTBaseDeclSpecifier implements org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier {

	IASTName _name;
	
	public CPPASTNamedTypeSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor, parent);
		String line = cursor.getLine();
		
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
		// may have *
		String completeType = parsedLine.get("type");
		String simpleType = CPPASTTranslationUnit.simplifyType(completeType);
		String firstType = CPPASTTranslationUnit.getType(line);
		IBinding binding = null;
		if(firstType.equals("CXXNewExpr")) {
			cursor.nextLine();
			List<String> stringsConstr = CPPASTTranslationUnit.parseLine(cursor.getLine());
			binding = CPPASTTranslationUnit.getConstructorBinding(simpleType, stringsConstr.get(4));
		} else {
			binding = CPPASTTranslationUnit.getBinding(simpleType);
		}
		
		if(binding == null) {
			BindingInfo bi = CPPASTTranslationUnit.parseBindingInfo(line);
			binding = CPPASTTranslationUnit.getBinding(bi.type);
		}
		
		_name = CPPASTName.loadASTName(binding, line, parent);
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
