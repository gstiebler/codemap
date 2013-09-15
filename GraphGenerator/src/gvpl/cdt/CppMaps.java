package gvpl.cdt;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public abstract class CppMaps {

	static private Map<Integer, String> _binOpTypes = new LinkedHashMap<Integer, String>();
	static private Map<Integer, String> _assignBinOpTypes = new LinkedHashMap<Integer, String>();

	static public Map<String, Integer> _opAssignStrToId = new LinkedHashMap<String, Integer>();

	public CppMaps(int nada) {
	}

	public static void initialize() {
		_binOpTypes.put(IASTBinaryExpression.op_multiply, "*");
		_binOpTypes.put(IASTBinaryExpression.op_divide, "/");
		_binOpTypes.put(IASTBinaryExpression.op_modulo, "%");
		_binOpTypes.put(IASTBinaryExpression.op_plus, "+");
		_binOpTypes.put(IASTBinaryExpression.op_minus, "-");
		_binOpTypes.put(IASTBinaryExpression.op_shiftLeft, "<<");
		_binOpTypes.put(IASTBinaryExpression.op_shiftRight, ">>");
		_binOpTypes.put(IASTBinaryExpression.op_lessThan, "<");
		_binOpTypes.put(IASTBinaryExpression.op_greaterThan, ">");
		_binOpTypes.put(IASTBinaryExpression.op_lessEqual, "<=");
		_binOpTypes.put(IASTBinaryExpression.op_greaterEqual, ">=");
		_binOpTypes.put(IASTBinaryExpression.op_binaryAnd, "&");
		_binOpTypes.put(IASTBinaryExpression.op_binaryXor, "^");
		_binOpTypes.put(IASTBinaryExpression.op_binaryOr, "|");
		_binOpTypes.put(IASTBinaryExpression.op_logicalAnd, "&&");
		_binOpTypes.put(IASTBinaryExpression.op_logicalOr, "||");
		_binOpTypes.put(IASTBinaryExpression.op_equals, "==");
		_binOpTypes.put(IASTBinaryExpression.op_notequals, "!=");

		_assignBinOpTypes.put(IASTBinaryExpression.op_assign, "=");
		_assignBinOpTypes.put(IASTBinaryExpression.op_multiplyAssign, "*");
		_assignBinOpTypes.put(IASTBinaryExpression.op_divideAssign, "/");
		_assignBinOpTypes.put(IASTBinaryExpression.op_multiplyAssign, "*");
		_assignBinOpTypes.put(IASTBinaryExpression.op_plusAssign, "+");
		_assignBinOpTypes.put(IASTBinaryExpression.op_minusAssign, "-");
		_assignBinOpTypes.put(IASTBinaryExpression.op_shiftLeftAssign, "<<");
		_assignBinOpTypes.put(IASTBinaryExpression.op_shiftRightAssign, ">>");
		_assignBinOpTypes.put(IASTBinaryExpression.op_binaryAndAssign, "&");
		_assignBinOpTypes.put(IASTBinaryExpression.op_binaryXorAssign, "^");
		_assignBinOpTypes.put(IASTBinaryExpression.op_binaryOrAssign, "|");

		_opAssignStrToId.put("*", IASTBinaryExpression.op_multiplyAssign);
		_opAssignStrToId.put("/", IASTBinaryExpression.op_divideAssign);
		_opAssignStrToId.put("%", IASTBinaryExpression.op_moduloAssign);
		_opAssignStrToId.put("+", IASTBinaryExpression.op_plusAssign);
		_opAssignStrToId.put("-", IASTBinaryExpression.op_minusAssign);
		_opAssignStrToId.put("<<", IASTBinaryExpression.op_shiftLeftAssign);
		_opAssignStrToId.put(">>", IASTBinaryExpression.op_shiftRightAssign);
	}

	public static String getBinOpString(Integer op) {
		return _binOpTypes.get(op);
	}

	public static String getAssignBinOpString(Integer op) {
		return _assignBinOpTypes.get(op);
	}
}
