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
			IASTStatement stmt = loadStatement(cursor.getSubCursor(), this);
			if(stmt != null)
				_statements.add(stmt);
			if (stmt instanceof CPPASTCaseStatement) {
				_statements.add(((CPPASTCaseStatement)stmt)._clangStmt);
			} else if (stmt instanceof CPPASTDefaultStatement) {
				_statements.add(((CPPASTDefaultStatement)stmt)._clangStmt);
			}
		}
	}
	
	public static IASTStatement loadStatement(Cursor cursor, ASTNode parent) {
		String stmtLine = cursor.getLine();
		String stmtType = CPPASTTranslationUnit.getType(stmtLine);
		if(stmtType.equals("DeclStmt"))
			return new ASTDeclarationStatement(cursor.getSubCursor(), parent);
		else if (stmtType.equals("BinaryOperator") || 
				stmtType.equals("CompoundAssignOperator") || 
				stmtType.equals("CXXDeleteExpr")) {
			return new CPPASTExpressionStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("ReturnStmt")) {
			return new CPPASTReturnStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("CXXMemberCallExpr") || 
				stmtType.equals("CallExpr") || 
				stmtType.equals("CXXOperatorCallExpr")) {
			return new CPPASTExpressionStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("IfStmt")) {
			return new CPPASTIfStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("ForStmt")) {
			return new CPPASTForStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("CompoundStmt")) {
			return new CPPASTCompoundStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("SwitchStmt")) {
			return new CPPASTSwitchStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("CaseStmt")) {
			return new CPPASTCaseStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("DefaultStmt")) {
			return new CPPASTDefaultStatement(cursor.getSubCursor(), parent);
		} else if (stmtType.equals("<<<NULL>>>")) {
			return null;
		} else if (stmtType.equals("BreakStmt")) {
			return new CPPASTBreakStatement(cursor.getSubCursor(), parent);
		} else {
			logger.error("Error reading " + stmtType);
			cursor.runToTheEnd();
			return null;
		}
	}
	
	@Override
	public IASTStatement[] getStatements() {
		IASTStatement[] result = new IASTStatement[_statements.size()];
		return _statements.toArray(result);
	}

	@Override
	public void addStatement(IASTStatement arg0) {
		
		logger.error("Not implemented");
	}

	@Override
	public IScope getScope() {
		
		logger.error("Not implemented");
		return null;
	}


}
