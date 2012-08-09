package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;

import java.util.EnumMap;
import java.util.Map;

public class GraphBuilder {

	enum eUnOp {
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

	/** typedef */
	public class TypeId {
	}

	/** typedef */
	public class MemberId {
	}

	/** typedef */
	public class MemberInstanceId {
	}

	private Map<eBinOp, String> _bin_op_strings = new EnumMap<eBinOp, String>(eBinOp.class);
	private Map<eUnOp, String> _un_op_strings = new EnumMap<eUnOp, String>(eUnOp.class);
	Map<eAssignBinOp, String> _assign_bin_op_strings = new EnumMap<eAssignBinOp, String>(
			eAssignBinOp.class);

	/** Stores all the graph */
	public Graph _gvplGraph = new Graph();

	/** Converts a ast node id to a VarDecl instance */
	// Map<var_id, VarDecl> _ast_variables;

	public GraphBuilder() {
		// _bin_op_strings[E_ASSIGN_OP] = "=";

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

	public GraphNode add_direct_val(eValueType type, String value) {
		return _gvplGraph.add_graph_node(value, NodeType.E_DIRECT_VALUE);
	}

	public void addAssignOp(VarDecl var_decl_lhs, GraphNode rhs_node, AstLoader astLoader) {
		addAssign(var_decl_lhs, NodeType.E_VARIABLE, rhs_node, astLoader);
	}

	/**
	 * Creates an assignment
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode addAssign(VarDecl lhs_var_decl, NodeType lhs_type, GraphNode rhs_node,
			AstLoader astLoader) {
		GraphNode lhs_node = _gvplGraph.add_graph_node(lhs_var_decl, lhs_type);
		rhs_node.addDependentNode(lhs_node, astLoader);
		lhs_var_decl.updateNode(lhs_node);

		return lhs_node;
	}

	GraphNode add_un_op(eUnOp op, GraphNode val_node, AstLoader astLoader) {
		GraphNode un_op_node = _gvplGraph.add_graph_node(_un_op_strings.get(op),
				NodeType.E_OPERATION);

		val_node.addDependentNode(un_op_node, astLoader);

		return un_op_node;
	}

	public GraphNode addNotOp(GraphNode val_node, AstLoader astLoader) {
		GraphNode notOpNode = _gvplGraph.add_graph_node("!", NodeType.E_OPERATION);
		val_node.addDependentNode(notOpNode, astLoader);

		return notOpNode;
	}

	public GraphNode addBinOp(eBinOp op, GraphNode val1_node, GraphNode val2_node,
			AstLoader astLoader) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		val1_node.addDependentNode(bin_op_node, astLoader);
		val2_node.addDependentNode(bin_op_node, astLoader);

		return bin_op_node;
	}

	public GraphNode add_assign_bin_op(eAssignBinOp op, VarDecl lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node, AstLoader astLoader) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_assign_bin_op_strings.get(op),
				NodeType.E_OPERATION);

		lhs_node.addDependentNode(bin_op_node, astLoader);
		rhs_node.addDependentNode(bin_op_node, astLoader);

		return addAssign(lhs_var_decl, NodeType.E_VARIABLE, bin_op_node, astLoader);
	}

	public GraphNode add_var_ref(VarDecl var_decl) {
		return var_decl.getCurrentNode();
	}

	public void addIf(VarDecl var, GraphNode ifTrue, GraphNode ifFalse, GraphNode condition,
			AstLoader astLoader) {
		GraphNode ifOpNode = _gvplGraph.add_graph_node("If", NodeType.E_OPERATION);

		ifTrue.addDependentNode(ifOpNode, astLoader);
		ifFalse.addDependentNode(ifOpNode, astLoader);
		condition.addDependentNode(ifOpNode, astLoader);

		addAssign(var, NodeType.E_VARIABLE, ifOpNode, null);
	}

}
