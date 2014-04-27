package gvpl.clang;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;


public class CPPASTBinaryExpression extends ASTNode implements org.eclipse.cdt.core.dom.ast.IASTBinaryExpression {

	String _operator;
	IASTExpression _operand1;
	IASTExpression _operand2;
	Map<String, Integer> _opMap = new HashMap<String, Integer>();
	
	public CPPASTBinaryExpression(Cursor cursor) {
		super(cursor.getLine());
		String line = cursor.nextLine();
		List<String> parsedLine = CPPASTTranslationUnit.parseLine(line);
		_operator = parsedLine.get( parsedLine.size() - 1 );
		
		_operand1 = loadOperand(cursor);
		_operand2 = loadOperand(cursor);
		
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

		// Field descriptor #8 I
/*		public static final int op_multiplyAssign = 18;

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
	}

	IASTExpression loadOperand(Cursor cursor) {
		String line = cursor.getLine();
		String type = CPPASTTranslationUnit.getType(line);
		if(type.equals("DeclRefExpr")) {
			return new CPPASTIdExpression(cursor);
		} else if(type.equals("IntegerLiteral")) {
			return new CPPASTLiteralExpression(cursor);
		} else if(type.equals("BinaryOperator")) {
			return new CPPASTBinaryExpression(cursor);
		} else if(type.equals("ImplicitCastExpr")) {
			cursor.nextLine();
			return new CPPASTIdExpression(cursor);
		} else
			return null;
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
	public IType getExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOperand1(IASTExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOperand2(IASTExpression arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOperator(int arg0) {
		// TODO Auto-generated method stub
		
	}
}
