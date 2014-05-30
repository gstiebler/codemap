package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;

public class CPPASTForStatement extends ASTNode implements IASTForStatement{

	IASTStatement _body;
	IASTExpression _condExpr;
	IASTStatement _initStat;
	IASTExpression _iterExpr;
	
	public CPPASTForStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		_initStat = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
		// NULL
		cursor.nextLine();
		_condExpr = ASTExpression.loadExpression(cursor, this);
		_iterExpr = ASTExpression.loadExpression(cursor, this);
		_body = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
	}

	@Override
	public IASTStatement getBody() {
		return _body;
	}

	@Override
	public IASTExpression getConditionExpression() {
		return _condExpr;
	}

	@Override
	public IASTStatement getInitializerStatement() {
		return _initStat;
	}

	@Override
	public IASTExpression getIterationExpression() {
		return _iterExpr;
	}

	@Override
	public IScope getScope() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setBody(IASTStatement arg0) {}

	@Override
	public void setConditionExpression(IASTExpression arg0) {}

	@Override
	public void setInitializerStatement(IASTStatement arg0) {}

	@Override
	public void setIterationExpression(IASTExpression arg0) {}

}
