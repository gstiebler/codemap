package gvpl.clang;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;

public class CPPConstructor  extends CPPMethod implements ICPPConstructor {

	public CPPConstructor(BindingInfo bi, String name, Cursor cursor) {
		super(bi, name, cursor);
		List<String> strings = CPPASTTranslationUnit.parseLine(cursor.getLine());
		CPPASTTranslationUnit.addConstructorBinding(this, strings.get(4), strings.get(5));
	}

	@Override
	public boolean isExplicit() throws DOMException {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return false;
	}

}
