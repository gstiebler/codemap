package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTCompoundStatement extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTCompoundStatement {

	static Logger logger = LogManager.getLogger(CPPASTCompoundStatement.class.getName());
	
	public List<IASTStatement> _statements = new ArrayList<IASTStatement>();
	
	public CPPASTCompoundStatement(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		while(!cursor.theEnd()) {
			String stmtLine = cursor.getLine();
			String stmtType = CPPASTTranslationUnit.getType(stmtLine);
			if(stmtType.equals("DeclStmt"))
				_statements.add(new ASTDeclarationStatement(cursor.getSubCursor(), this));
			else if (stmtType.equals("BinaryOperator") || stmtType.equals("CompoundAssignOperator")) {
				_statements.add(new CPPASTExpressionStatement(cursor.getSubCursor(), this));
			} else if (stmtType.equals("ReturnStmt")) {
				_statements.add(new ASTReturnStatement(cursor.getSubCursor(), this));
			} else if (stmtType.equals("CXXMemberCallExpr")) {
				logger.error("Error reading " + stmtType);
				cursor.runToTheEnd();
			} else {
				logger.error("Error reading " + stmtType);
				cursor.runToTheEnd();
			}
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
		logger.error("Not implemented");
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}


}
