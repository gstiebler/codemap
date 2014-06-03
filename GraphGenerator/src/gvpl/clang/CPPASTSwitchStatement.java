package gvpl.clang;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;

public class CPPASTSwitchStatement extends ASTNode implements ICPPASTSwitchStatement {

	IASTExpression _controllerExpr;
	IASTStatement _body;
	
	public CPPASTSwitchStatement(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		cursor.nextLine();
		cursor.nextLine();
		_controllerExpr = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_body = CPPASTCompoundStatement.loadStatement(cursor.getSubCursor(), this);
	}

	@Override
	public IASTStatement getBody() {
		return _body;
	}

	@Override
	public IASTExpression getControllerExpression() {
		return _controllerExpr;
	}

	@Override
	public IASTDeclaration getControllerDeclaration() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
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
	public void setControllerExpression(IASTExpression arg0) {}

	@Override
	public void setControllerDeclaration(IASTDeclaration arg0) {}

}
