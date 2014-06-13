package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;

public class CPPASTNamespaceDefinition extends ASTNode implements ICPPASTNamespaceDefinition{

	List<IASTDeclaration> _decls = new ArrayList<IASTDeclaration>();
	String _name;
	
	public CPPASTNamespaceDefinition(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		List<String> lines = CPPASTTranslationUnit.parseLine(line);
		_name = lines.get(4);
		CPPASTTranslationUnit.addNamespace(_name);
		while (!cursor.theEnd()) {
			IASTDeclaration decl = CPPASTTranslationUnit.loadDeclaration(cursor, this);
			if(decl != null)
				_decls.add(decl);
		}
		CPPASTTranslationUnit.removeNamespace();
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public int getRoleForName(IASTName arg0) {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return 0;
	}

	@Override
	public IASTDeclaration[] getDeclarations() {
		IASTDeclaration[] result = new IASTDeclaration[_decls.size()];
		return _decls.toArray(result);
	}

	@Override
	public IASTName getName() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public void addDeclaration(IASTDeclaration arg0) {}

	@Override
	public void setName(IASTName arg0) {}

}
