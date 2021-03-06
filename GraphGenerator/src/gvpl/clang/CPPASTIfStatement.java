package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;

public class CPPASTIfStatement extends ASTNode implements ICPPASTIfStatement{

	IASTExpression _conditionExpr;
	IASTStatement _thenClause;
	IASTStatement _elseClause;
	
	public CPPASTIfStatement(Cursor cursor, IASTNode parent) {
		super(cursor.nextLine(), parent);
		cursor.nextLine();
		_conditionExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_thenClause = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
		_elseClause = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
		if(!cursor.theEnd())
			cursor.runToTheEnd();
	}

	@Override
	public IASTExpression getConditionExpression() {
		return _conditionExpr;
	}

	@Override
	public IASTStatement getElseClause() {
		return _elseClause;
	}

	@Override
	public IASTStatement getThenClause() {
		return _thenClause;
	}

	@Override
	public IASTDeclaration getConditionDeclaration() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public IScope getScope() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setConditionExpression(IASTExpression arg0) {}

	@Override
	public void setElseClause(IASTStatement arg0) {}

	@Override
	public void setThenClause(IASTStatement arg0) {}

	@Override
	public void setConditionDeclaration(IASTDeclaration arg0) {}

}
