package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;

public class CPPASTTemplateSpecialization extends ASTNode implements ICPPASTTemplateSpecialization {

	IASTDeclaration _declaration;
	
	public CPPASTTemplateSpecialization(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine(); // header
		ClangLine templateArgsLine = CPPASTTranslationUnit.lineToMap(cursor.nextLine());
		String specializationType = templateArgsLine.get("type");
		String line = cursor.getLine();
		IBinding binding = new CPPClassSpecialization(line, specializationType);
		CPPASTTemplateId name = new CPPASTTemplateId(binding, line, this);
		CPPASTCompositeTypeSpecifier compositeTypeSpec = new CPPASTCompositeTypeSpecifier(cursor, this, name);
		_declaration = new CPPASTSimpleDeclaration(cursor.getLine(), this, compositeTypeSpec);
	}

	@Override
	public IASTDeclaration getDeclaration() {
		return _declaration;
	}

	@Override
	public void setDeclaration(IASTDeclaration arg0) {}

}
