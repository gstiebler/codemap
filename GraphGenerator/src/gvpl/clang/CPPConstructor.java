package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class CPPConstructor  extends CPPMethod implements ICPPConstructor {

	public CPPConstructor(BindingInfo bi, String name, Cursor cursor, CPPASTFunctionDefinition parent) {
		super(bi, name, cursor, parent);
		ClangLine strings = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		CPPASTTranslationUnit.addConstructorBinding(this, strings.get("name"), CPPASTTranslationUnit.getUserType(strings));
	}

	@Override
	public boolean isExplicit() throws DOMException {
		
		logger.error("Not implemented");
		return false;
	}

}
