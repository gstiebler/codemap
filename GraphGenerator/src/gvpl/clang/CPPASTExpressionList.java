package gvpl.clang;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTExpressionList extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTExpressionList {

	static Logger logger = LogManager.getLogger(CPPASTExpressionList.class.getName());
	
	IASTExpression[] _expressions;
	
	public CPPASTExpressionList(Cursor cursor) {
		super(cursor.getLine());
		while(!cursor.theEnd()) {
			cursor.nextLine();
		}
		logger.error("not implemented");
	}

	@Override
	public IASTExpression[] getExpressions() {
		return _expressions;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addExpression(IASTExpression arg0) { }

}
