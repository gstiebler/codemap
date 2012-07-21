package gvpl.cdt;

import gvpl.graph.GraphBuilder.eAssignBinOp;
import gvpl.graph.GraphBuilder.eBinOp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;

public class CppMaps {
	
	private Map<Integer, eBinOp> _bin_op_types = new HashMap<Integer, eBinOp>();
	private Map<Integer, eAssignBinOp> _assign_bin_op_types = new HashMap<Integer, eAssignBinOp>();
	
	public CppMaps() {
		_bin_op_types.put(IASTBinaryExpression.op_plus, eBinOp.E_ADD_OP);
		_bin_op_types.put(IASTBinaryExpression.op_minus, eBinOp.E_SUB_OP);
		_bin_op_types.put(IASTBinaryExpression.op_multiply, eBinOp.E_MULT_OP);
		_bin_op_types.put(IASTBinaryExpression.op_divide, eBinOp.E_DIV_OP);
		_bin_op_types.put(IASTBinaryExpression.op_lessThan, eBinOp.E_LESS_THAN_OP);
		_bin_op_types.put(IASTBinaryExpression.op_greaterThan, eBinOp.E_GREATER_THAN_OP);

		_assign_bin_op_types.put(IASTBinaryExpression.op_assign, eAssignBinOp.E_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_plusAssign, eAssignBinOp.E_PLUS_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_minusAssign, eAssignBinOp.E_SUB_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_multiplyAssign, eAssignBinOp.E_MULT_ASSIGN_OP);
		_assign_bin_op_types.put(IASTBinaryExpression.op_divideAssign, eAssignBinOp.E_DIV_ASSIGN_OP);
	}
	
	public eBinOp getBinOpType(Integer op) {
		return _bin_op_types.get(op);
	}
	
	public eAssignBinOp getAssignBinOpTypes(Integer op) {
		return _assign_bin_op_types.get(op);
	}
}
