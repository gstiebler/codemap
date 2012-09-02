package gvpl.graph;

import gvpl.cdt.CppMaps;
import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.CppMaps.eUnOp;
import gvpl.cdt.CppMaps.eValueType;
import gvpl.common.Var;
import gvpl.graph.Graph.NodeType;

public class GraphBuilder {

	/** typedef */
	public class TypeId {
	}

	/** typedef */
	public class MemberId {
	}

	/** Stores all the graph */
	public Graph _gvplGraph = new Graph(-1);
	private CppMaps _cppMaps = null;

	public GraphBuilder(CppMaps cppMaps) {
		_cppMaps = cppMaps;
	}

	public GraphNode addDirectVal(eValueType type, String value, int startingLine) {
		return _gvplGraph.add_graph_node(value, NodeType.E_DIRECT_VALUE, startingLine);
	}

	GraphNode addUnOp(eUnOp op, GraphNode val_node, int startingLine) {
		GraphNode un_op_node = _gvplGraph.add_graph_node(_cppMaps._un_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		val_node.addDependentNode(un_op_node, startingLine);

		return un_op_node;
	}

	public GraphNode addNotOp(GraphNode val_node, int startingLine) {
		GraphNode notOpNode = _gvplGraph.add_graph_node("!", NodeType.E_OPERATION, startingLine);
		val_node.addDependentNode(notOpNode, startingLine);

		return notOpNode;
	}

	public GraphNode addBinOp(eBinOp op, GraphNode val1_node, GraphNode val2_node,
			int startingLine) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_cppMaps._bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		val1_node.addDependentNode(bin_op_node, startingLine);
		val2_node.addDependentNode(bin_op_node, startingLine);

		return bin_op_node;
	}

	public GraphNode addAssignBinOp(eAssignBinOp op, Var lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node, int startingLine) {
		GraphNode bin_op_node = _gvplGraph.add_graph_node(_cppMaps._assign_bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		lhs_node.addDependentNode(bin_op_node, startingLine);
		rhs_node.addDependentNode(bin_op_node, startingLine);

		return lhs_var_decl.receiveAssign(_gvplGraph, NodeType.E_VARIABLE, bin_op_node, startingLine);
	}

	public void addIf(Var var, GraphNode ifTrue, GraphNode ifFalse, GraphNode condition,
			int startingLine) {
		GraphNode ifOpNode = _gvplGraph.add_graph_node("If", NodeType.E_OPERATION, startingLine);

		ifTrue.addDependentNode(ifOpNode, startingLine);
		ifFalse.addDependentNode(ifOpNode, startingLine);
		condition.addDependentNode(ifOpNode, startingLine);

		var.receiveAssign(_gvplGraph, NodeType.E_VARIABLE, ifOpNode, startingLine);
	}

}
