package gvpl.clang;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTUnaryExpression extends ASTNode implements IASTUnaryExpression{

	IASTExpression _operand;
	int _operator;
	Map<String, Integer> _opMap = new HashMap<String, Integer>();
	
	public CPPASTUnaryExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		ClangLine strings = CPPASTTranslationUnit.lineToMap(cursor.getLine());
		
		_opMap.put("&", IASTUnaryExpression.op_amper);
		_opMap.put("*", IASTUnaryExpression.op_star);
		_opMap.put("!", IASTUnaryExpression.op_not);
		// TODO diferentiate prefixed and postfiexed
		_opMap.put("++", IASTUnaryExpression.op_postFixIncr);
		
		String operatorStr = strings.get("unaryOpcode");

		if(!_opMap.containsKey(operatorStr)) {
			logger.error("Operator {} not found");
			return;
		}
		_operator = _opMap.get(operatorStr);
		
		cursor.nextLine();
		_operand = ASTExpression.loadExpression(cursor, this);
	}

	@Override
	public IASTExpression getOperand() {
		return _operand;
	}

	@Override
	public int getOperator() {
		return _operator;
	}

	@Override
	public IType getExpressionType() {
		logger.error("Not implemented");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOperand(IASTExpression arg0) {}

	@Override
	public void setOperator(int arg0) {}

}
