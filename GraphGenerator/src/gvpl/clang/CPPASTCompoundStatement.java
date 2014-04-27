package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompoundStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTCompoundStatement {

	public List<IASTStatement> _statements = new ArrayList<IASTStatement>();
	
	public CPPASTCompoundStatement(Cursor cursor) {
		super(cursor.nextLine());
		while(!cursor.theEnd()) {
			String stmtLine = cursor.getLine();
			String stmtType = CPPASTTranslationUnit.getType(stmtLine);
			if(stmtType.equals("DeclStmt"))
				_statements.add(new ASTDeclarationStatement(cursor.getSubCursor()));
			else if (stmtType.equals("BinaryOperator")) {
				_statements.add(new CPPASTExpressionStatement(cursor.getSubCursor()));
			} else
				_statements.add(new CPPASTStatement(cursor.getSubCursor()));
		}
	}
	
	@Override
	public IASTStatement[] getStatements() {
		IASTStatement[] result = new IASTStatement[_statements.size()];
		return _statements.toArray(result);
	}

	@Override
	public void addStatement(IASTStatement arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		return null;
	}


}
