package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.cdt.CppMaps;
import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.CppMaps.eUnOp;
import gvpl.cdt.CppMaps.eValueType;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;

public class GraphBuilder {

	/** typedef */
	public class TypeId {
	}

	/** typedef */
	public class MemberId {
	}

	/** Stores all the graph */
	public Graph _gvplGraph = new Graph();
	private CppMaps _cppMaps = null;

	public GraphBuilder(CppMaps cppMaps) {
		_cppMaps = cppMaps;
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
		GraphNode un_op_node = _gvplGraph.add_graph_node(_cppMaps._un_op_strings.get(op),
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
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_cppMaps._bin_op_strings.get(op),
				NodeType.E_OPERATION);

		val1_node.addDependentNode(bin_op_node, astLoader);
		val2_node.addDependentNode(bin_op_node, astLoader);

		return bin_op_node;
	}

	public GraphNode add_assign_bin_op(eAssignBinOp op, VarDecl lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node, AstLoader astLoader) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_cppMaps._assign_bin_op_strings.get(op),
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
