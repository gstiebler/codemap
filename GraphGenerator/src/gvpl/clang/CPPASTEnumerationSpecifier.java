package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CPPASTEnumerationSpecifier extends CPPASTBaseDeclSpecifier implements IASTEnumerationSpecifier{

	List<IASTEnumerator> _enumerators = new ArrayList<IASTEnumerator>();
	
	CPPASTEnumerationSpecifier(Cursor cursor, IASTNode parent) {
		super(cursor.getSubCursor(), parent);
		cursor.nextLine();
		while(!cursor.theEnd()) {
			_enumerators.add(new CPPASTEnumerator(cursor.getSubCursor(), this));
		}
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return 0;
	}

	@Override
	public IASTEnumerator[] getEnumerators() {
		IASTEnumerator[] result = new IASTEnumerator [_enumerators.size()];
		return _enumerators.toArray(result);
	}

	@Override
	public IASTName getName() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void addEnumerator(IASTEnumerator arg0) {}

	@Override
	public void setName(IASTName arg0) {}

}
