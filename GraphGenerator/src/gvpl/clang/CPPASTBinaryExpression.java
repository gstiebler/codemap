package gvpl.clang;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;

public class CPPASTBinaryExpression extends ASTNode implements
		org.eclipse.cdt.core.dom.ast.IASTBinaryExpression {

	static Logger logger = LogManager.getLogger(CPPASTBinaryExpression.class
			.getName());

	int _operator;
	String _opStr;
	IASTExpression _operand1;
	IASTExpression _operand2;
	Map<String, Integer> _opMap = new HashMap<String, Integer>();

	public CPPASTBinaryExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		ClangLine parsedLine = CPPASTTranslationUnit.lineToMap(line);
		String type = parsedLine.get("mainType");
		
		if(type.equals("BinaryOperator"))
			_opStr = parsedLine.get("binOpcode");
		else if(type.equals("CXXOperatorCallExpr")) {
			cursor.nextLine();
			parsedLine = CPPASTTranslationUnit.lineToMap(cursor.nextLine());
			_opStr = parsedLine.get("refName");
			_opStr = _opStr.split("operator")[1];
		} else
			_opStr = parsedLine.get("binOpcode");

		_operand1 = ASTExpression.loadExpression(cursor.getSubCursor(), this);
		_operand2 = ASTExpression.loadExpression(cursor.getSubCursor(), this);

		_opMap.put("*", IASTBinaryExpression.op_multiply);
		_opMap.put("/", IASTBinaryExpression.op_divide);
		_opMap.put("%", IASTBinaryExpression.op_modulo);
		_opMap.put("+", IASTBinaryExpression.op_plus);
		_opMap.put("-", IASTBinaryExpression.op_minus);
		_opMap.put("<<", IASTBinaryExpression.op_shiftLeft);
		_opMap.put(">>", IASTBinaryExpression.op_shiftRight);
		_opMap.put("<", IASTBinaryExpression.op_lessThan);
		_opMap.put(">", IASTBinaryExpression.op_greaterThan);
		_opMap.put("<=", IASTBinaryExpression.op_lessEqual);
		_opMap.put(">=", IASTBinaryExpression.op_greaterEqual);
		_opMap.put("&", IASTBinaryExpression.op_binaryAnd);
		_opMap.put("^", IASTBinaryExpression.op_binaryXor);
		_opMap.put("|", IASTBinaryExpression.op_binaryOr);
		_opMap.put("&&", IASTBinaryExpression.op_logicalAnd);
		_opMap.put("||", IASTBinaryExpression.op_logicalOr);
		_opMap.put("=", IASTBinaryExpression.op_assign);
		_opMap.put("*=", IASTBinaryExpression.op_multiplyAssign);
		_opMap.put("+=", IASTBinaryExpression.op_plusAssign);
		_opMap.put("-=", IASTBinaryExpression.op_minusAssign);
		_opMap.put("/=", IASTBinaryExpression.op_divideAssign);

		/*
		 * 
		 * // Field descriptor #8 I public static final int op_moduloAssign =
		 * 20;
		 * 
		 * // Field descriptor #8 I public static final int op_shiftLeftAssign =
		 * 23;
		 * 
		 * // Field descriptor #8 I public static final int op_shiftRightAssign
		 * = 24;
		 * 
		 * // Field descriptor #8 I public static final int op_binaryAndAssign =
		 * 25;
		 * 
		 * // Field descriptor #8 I public static final int op_binaryXorAssign =
		 * 26;
		 * 
		 * // Field descriptor #8 I public static final int op_binaryOrAssign =
		 * 27;
		 * 
		 * // Field descriptor #8 I public static final int op_equals = 28;
		 * 
		 * // Field descriptor #8 I public static final int op_notequals = 29;
		 * 
		 * // Field descriptor #8 I public static final int op_last = 29;
		 */

		if (!_opMap.containsKey(_opStr))
			logger.error("Operator {} not found");

		_operator = _opMap.get(_opStr);
	}

	@Override
	public IASTExpression getOperand1() {
		return _operand1;
	}

	@Override
	public IASTExpression getOperand2() {
		return _operand2;
	}

	@Override
	public int getOperator() {
		return _operator;
	}

	@Override
	public String getRawSignature() {
		return _opStr;
	}

	@Override
	public IType getExpressionType() {
		
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setOperand1(IASTExpression arg0) {
		
		logger.error("Not implemented");
	}

	@Override
	public void setOperand2(IASTExpression arg0) {
		
		logger.error("Not implemented");
	}

	@Override
	public void setOperator(int arg0) {
		
		logger.error("Not implemented");
	}
}
