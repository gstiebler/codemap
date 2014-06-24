package gvpl.clang;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTExpressionList extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTExpressionList {

	static Logger logger = LogManager.getLogger(CPPASTExpressionList.class.getName());
	
	List<IASTExpression> _expressions = new ArrayList<IASTExpression>();
	
	public CPPASTExpressionList(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		while(!cursor.theEnd()) {
			_expressions.add(ASTExpression.loadExpression(cursor, this));
			//cursor.nextLine();
		}
	}

	@Override
	public IASTExpression[] getExpressions() {
		IASTExpression[] result = new IASTExpression[_expressions.size()];
		return _expressions.toArray(result);
	}

	@Override
	public IType getExpressionType() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void addExpression(IASTExpression arg0) { }

}
