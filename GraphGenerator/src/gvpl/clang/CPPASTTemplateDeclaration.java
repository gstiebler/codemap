package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;

public class CPPASTTemplateDeclaration extends ASTNode implements ICPPASTTemplateDeclaration{

	IASTDeclaration _declaration;
	
	public CPPASTTemplateDeclaration(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		cursor.nextLine(); //TemplateTypeParmDecl
		String line = cursor.getLine();
		IBinding binding = new CPPClassTemplate(line);
		CPPASTTemplateId name = new CPPASTTemplateId(binding, line, this);
		cursor.nextLine();
		CPPASTCompositeTypeSpecifier compositeTypeSpec = new CPPASTCompositeTypeSpecifier(cursor, this, name);
		_declaration = new CPPASTSimpleDeclaration(cursor.getLine(), this, compositeTypeSpec, null);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return _declaration;
	}

	@Override
	public ICPPTemplateScope getScope() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public ICPPASTTemplateParameter[] getTemplateParameters() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return null;
	}

	@Override
	public boolean isExported() {
		// TODO Auto-generated method stub
		logger.error("not implemented");
		return false;
	}

	@Override
	public void addTemplateParamter(ICPPASTTemplateParameter arg0) {}

	@Override
	public void setDeclaration(IASTDeclaration arg0) {}

	@Override
	public void setExported(boolean arg0) {}

}
