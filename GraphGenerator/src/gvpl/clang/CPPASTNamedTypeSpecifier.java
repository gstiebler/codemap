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
		
		List<String> strings = CPPASTTranslationUnit.parseLine(line);
		// may have *
		String completeType = strings.get(strings.size() - 1);
		String simpleType = CPPASTTranslationUnit.simplifyType(completeType);
		IBinding binding = CPPASTTranslationUnit.getBinding(simpleType);
		
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
