package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;

public class CPPASTQualifiedName extends CPPASTName implements org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName {

	IASTName[] _names = new IASTName[2];
	
	public CPPASTQualifiedName(IBinding binding, String line) {
		super(binding, line);
		_names[0] = ((CPPMethod) binding).className;
		_names[1] = this;
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addName(IASTName arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IASTName getLastName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IASTName[] getNames() {
		return _names;
	}

	@Override
	public boolean isConversionOrOperator() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFullyQualified() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setFullyQualified(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
