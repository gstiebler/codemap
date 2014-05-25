package gvpl.clang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;


public class CPPASTBinaryExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTBinaryExpression {

	static Logger logger = LogManager.getLogger(CPPASTBinaryExpression.class.getName());
	
	String _operator;
	IASTExpression _operand1;
	IASTExpression _operand2;
	Map<String, Integer> _opMap = new HashMap<String, Integer>();
	
	public CPPASTBinaryExpression(Cursor cursor, IASTNode parent) {
		super(cursor.getLine(), parent);
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		
		if(parsedLine.get(0).equals("BinaryOperator"))
			_operator = parsedLine.get( parsedLine.size() - 1 );
		else
			_operator = parsedLine.get( 5 );
		
		_operand1 = ASTExpression.loadExpression(cursor, this);
		_operand2 = ASTExpression.loadExpression(cursor, this);
		
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

/*
		// Field descriptor #8 I
		public static final int op_divideAssign = 19;

		// Field descriptor #8 I
		public static final int op_moduloAssign = 20;

		// Field descriptor #8 I
		public static final int op_plusAssign = 21;

		// Field descriptor #8 I
		public static final int op_minusAssign = 22;

		// Field descriptor #8 I
		public static final int op_shiftLeftAssign = 23;

		// Field descriptor #8 I
		public static final int op_shiftRightAssign = 24;

		// Field descriptor #8 I
		public static final int op_binaryAndAssign = 25;

		// Field descriptor #8 I
		public static final int op_binaryXorAssign = 26;

		// Field descriptor #8 I
		public static final int op_binaryOrAssign = 27;

		// Field descriptor #8 I
		public static final int op_equals = 28;

		// Field descriptor #8 I
		public static final int op_notequals = 29;

		// Field descriptor #8 I
		public static final int op_last = 29;*/
		
		if(!_opMap.containsKey(_operator))
			logger.error("Operator {} not found");
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
		return _opMap.get(_operator);
	}

	@Override
	public String getRawSignature() {
		return _operator;
	}

	@Override
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
		return null;
	}

	@Override
	public void setOperand1(IASTExpression arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void setOperand2(IASTExpression arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}

	@Override
	public void setOperator(int arg0) {
		// TODO Auto-generated method stub
		logger.error("Not implemented");
	}
}
