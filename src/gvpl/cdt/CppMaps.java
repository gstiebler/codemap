package gvpl.cdt;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public abstract class CppMaps {
	
	public enum eUnOp {
		E_INVALID_UN_OP, E_PLUS_PLUS_OP
	};

	public enum eBinOp {
		E_INVALID_BIN_OP, E_ADD_OP, E_SUB_OP, E_MULT_OP, E_DIV_OP, E_LESS_THAN_OP, E_GREATER_THAN_OP, E_LESS_EQUAL_OP, E_GREATER_EQUAL_OP
	}

	public enum eAssignBinOp {
		E_INVALID_A_BIN_OP, E_ASSIGN_OP, E_PLUS_ASSIGN_OP, E_SUB_ASSIGN_OP, E_DIV_ASSIGN_OP, E_MULT_ASSIGN_OP
	}

	public enum eValueType {
		E_INVALID_TYPE, E_INT, E_FLOAT, E_DOUBLE, E_STRING, E_BOOL
	}
	
	static private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	static private Map<Integer, eAssignBinOp> _assign_bin_op_types = new HashMap<Integer, eAssignBinOp>();
	
	static public Map<eBinOp, String> _bin_op_strings = new EnumMap<eBinOp, String>(eBinOp.class);
	static public Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	static public Map<eAssignBinOp, String> _assign_bin_op_strings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);
	
	public CppMaps(int nada) {}
	
	public static void initialize() {
		_bin_op_types.put(IASTBinaryExpression.op_plus, eBinOp.E_ADD_OP);
		_bin_op_types.put(IASTBinaryExpression.op_minus, eBinOp.E_SUB_OP);
		_bin_op_types.put(IASTBinaryExpression.op_multiply, eBinOp.E_MULT_OP);
		_bin_op_types.put(IASTBinaryExpression.op_divide, eBinOp.E_DIV_OP);
		_bin_op_types.put(IASTBinaryExpression.op_lessThan, eBinOp.E_LESS_THAN_OP);
		_bin_op_types.put(IASTBinaryExpression.op_greaterThan, eBinOp.E_GREATER_THAN_OP);
		_bin_op_types.put(IASTBinaryExpression.op_lessEqual, eBinOp.E_LESS_EQUAL_OP);
		_bin_op_types.put(IASTBinaryExpression.op_greaterEqual, eBinOp.E_GREATER_EQUAL_OP);

		_assign_bin_op_types.put(IASTBinaryExpression.op_assign, eAssignBinOp.E_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_plusAssign, eAssignBinOp.E_PLUS_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_minusAssign, eAssignBinOp.E_SUB_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_multiplyAssign, eAssignBinOp.E_MULT_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_divideAssign, eAssignBinOp.E_DIV_ASSIGN_OP);
		
		_bin_op_strings.put(eBinOp.E_ADD_OP, "+");
		_bin_op_strings.put(eBinOp.E_SUB_OP, "-");
		_bin_op_strings.put(eBinOp.E_MULT_OP, "*");
		_bin_op_strings.put(eBinOp.E_DIV_OP, "/");
		_bin_op_strings.put(eBinOp.E_LESS_THAN_OP, "<");
		_bin_op_strings.put(eBinOp.E_GREATER_THAN_OP, ">");
		_bin_op_strings.put(eBinOp.E_LESS_EQUAL_OP, "<=");
		_bin_op_strings.put(eBinOp.E_GREATER_EQUAL_OP, ">=");

		_un_op_strings.put(eUnOp.E_PLUS_PLUS_OP, "++");

		_assign_bin_op_strings.put(eAssignBinOp.E_PLUS_ASSIGN_OP, "+");
		_assign_bin_op_strings.put(eAssignBinOp.E_SUB_ASSIGN_OP, "-");
		_assign_bin_op_strings.put(eAssignBinOp.E_DIV_ASSIGN_OP, "/");
		_assign_bin_op_strings.put(eAssignBinOp.E_MULT_ASSIGN_OP, "*");
	}
	
	public static eBinOp getBinOpType(Integer op) {
		return _bin_op_types.get(op);
	}
	
	public static eAssignBinOp getAssignBinOpTypes(Integer op) {
		return _assign_bin_op_types.get(op);
	}
}
