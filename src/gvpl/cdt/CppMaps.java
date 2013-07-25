package gvpl.cdt;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public abstract class CppMaps {

	public enum eUnOp {
		E_INVALID_UN_OP, E_PLUS_PLUS_OP
	};

	public enum eBinOp {
		E_INVALID_BIN_OP, E_ADD_OP, E_SUB_OP, E_MULT_OP, E_DIV_OP, E_LESS_THAN_OP, E_GREATER_THAN_OP, E_LESS_EQUAL_OP, E_GREATER_EQUAL_OP, E_SHIFT_RIGHT, E_SHIFT_LEFT
	}

	public enum eAssignBinOp {
		E_INVALID_A_BIN_OP, E_ASSIGN_OP, E_PLUS_ASSIGN_OP, E_SUB_ASSIGN_OP, E_DIV_ASSIGN_OP, E_MULT_ASSIGN_OP
	}

	public enum eValueType {
		E_INVALID_TYPE, E_INT, E_FLOAT, E_DOUBLE, E_STRING, E_BOOL
	}

	static private Map<Integer, eBinOp> _binOpTypes = new LinkedHashMap<Integer, eBinOp>();
	static private Map<Integer, eAssignBinOp> _assignBinOpTypes = new LinkedHashMap<Integer, eAssignBinOp>();

	static public Map<eBinOp, String> _binOpStrings = new EnumMap<eBinOp, String>(eBinOp.class);
	static public Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	static public Map<eAssignBinOp, String> _assignBinOpStrings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);
	static public Map<String, Integer> _opAssignStrToId = new LinkedHashMap<String, Integer>();

	public CppMaps(int nada) {
	}

	public static void initialize() {
		_binOpTypes.put(IASTBinaryExpression.op_plus, eBinOp.E_ADD_OP);
		_binOpTypes.put(IASTBinaryExpression.op_minus, eBinOp.E_SUB_OP);
		_binOpTypes.put(IASTBinaryExpression.op_multiply, eBinOp.E_MULT_OP);
		_binOpTypes.put(IASTBinaryExpression.op_divide, eBinOp.E_DIV_OP);
		_binOpTypes.put(IASTBinaryExpression.op_lessThan, eBinOp.E_LESS_THAN_OP);
		_binOpTypes.put(IASTBinaryExpression.op_greaterThan, eBinOp.E_GREATER_THAN_OP);
		_binOpTypes.put(IASTBinaryExpression.op_lessEqual, eBinOp.E_LESS_EQUAL_OP);
		_binOpTypes.put(IASTBinaryExpression.op_greaterEqual, eBinOp.E_GREATER_EQUAL_OP);
		_binOpTypes.put(IASTBinaryExpression.op_shiftLeft, eBinOp.E_SHIFT_LEFT);
		_binOpTypes.put(IASTBinaryExpression.op_shiftRight, eBinOp.E_SHIFT_RIGHT);

		_assignBinOpTypes.put(IASTBinaryExpression.op_assign, eAssignBinOp.E_ASSIGN_OP);
		_assignBinOpTypes.put(IASTBinaryExpression.op_plusAssign, eAssignBinOp.E_PLUS_ASSIGN_OP);
		_assignBinOpTypes.put(IASTBinaryExpression.op_minusAssign, eAssignBinOp.E_SUB_ASSIGN_OP);
		_assignBinOpTypes.put(IASTBinaryExpression.op_multiplyAssign,
				eAssignBinOp.E_MULT_ASSIGN_OP);
		_assignBinOpTypes
				.put(IASTBinaryExpression.op_divideAssign, eAssignBinOp.E_DIV_ASSIGN_OP);

		_binOpStrings.put(eBinOp.E_ADD_OP, "+");
		_binOpStrings.put(eBinOp.E_SUB_OP, "-");
		_binOpStrings.put(eBinOp.E_MULT_OP, "*");
		_binOpStrings.put(eBinOp.E_DIV_OP, "/");
		_binOpStrings.put(eBinOp.E_LESS_THAN_OP, "<");
		_binOpStrings.put(eBinOp.E_GREATER_THAN_OP, ">");
		_binOpStrings.put(eBinOp.E_LESS_EQUAL_OP, "<=");
		_binOpStrings.put(eBinOp.E_GREATER_EQUAL_OP, ">=");
		_binOpStrings.put(eBinOp.E_SHIFT_LEFT, "<<");
		_binOpStrings.put(eBinOp.E_SHIFT_RIGHT, ">>");

		_un_op_strings.put(eUnOp.E_PLUS_PLUS_OP, "++");

		_assignBinOpStrings.put(eAssignBinOp.E_PLUS_ASSIGN_OP, "+");
		_assignBinOpStrings.put(eAssignBinOp.E_SUB_ASSIGN_OP, "-");
		_assignBinOpStrings.put(eAssignBinOp.E_DIV_ASSIGN_OP, "/");
		_assignBinOpStrings.put(eAssignBinOp.E_MULT_ASSIGN_OP, "*");

		_opAssignStrToId.put("*", IASTBinaryExpression.op_multiplyAssign);
		_opAssignStrToId.put("/", IASTBinaryExpression.op_divideAssign);
		_opAssignStrToId.put("%", IASTBinaryExpression.op_moduloAssign);
		_opAssignStrToId.put("+", IASTBinaryExpression.op_plusAssign);
		_opAssignStrToId.put("-", IASTBinaryExpression.op_minusAssign);
		_opAssignStrToId.put("<<", IASTBinaryExpression.op_shiftLeftAssign);
		_opAssignStrToId.put(">>", IASTBinaryExpression.op_shiftRightAssign);
	}

	public static eBinOp getBinOpType(Integer op) {
		return _binOpTypes.get(op);
	}

	public static eAssignBinOp getAssignBinOpTypes(Integer op) {
		return _assignBinOpTypes.get(op);
	}
}
