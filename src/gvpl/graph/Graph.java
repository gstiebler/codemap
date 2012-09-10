package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.cdt.CppMaps;
import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.CppMaps.eUnOp;
import gvpl.cdt.CppMaps.eValueType;
import gvpl.common.Var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

	public enum NodeType {
		E_INVALID_NODE_TYPE, E_DIRECT_VALUE, E_VARIABLE, E_OPERATION, E_FOR_LOOP, E_DECLARED_PARAMETER, E_RETURN_VALUE, E_LOOP_HEADER
	}

	private String _label;
	private List<GraphNode> _graphNodes = new ArrayList<GraphNode>();
	public List<Graph> _subgraphs = new ArrayList<Graph>();
	private int _startingLine = -1;

	public Graph(String label, int startingLine) {
		_label = label;
		_startingLine = startingLine;
	}

	public Graph(int startingLine) {
		_startingLine = startingLine;
	}

	public GraphNode add_graph_node(String name, NodeType type, int startingLine) {
		GraphNode graph_node = new GraphNode(name, type, startingLine);
		_graphNodes.add(graph_node);
		return graph_node;
	}

	public GraphNode addGraphNode(Var parentVar, NodeType type, int startingLine) {
		GraphNode graph_node = new GraphNode(parentVar, type, startingLine);
		_graphNodes.add(graph_node);
		return graph_node;
	}

	public int getNumNodes() {
		return _graphNodes.size();
	}

	public GraphNode getNode(int index) {
		return _graphNodes.get(index);
	}

	public Graph getCopy(Map<GraphNode, GraphNode> map, AstLoader astLoader, int startingLine) {

		class NodeChange {
			GraphNode _originalNode;
			GraphNode _newNode;

			NodeChange(GraphNode originalNode, GraphNode newNode) {
				_originalNode = originalNode;
				_newNode = newNode;
			}
		}

		int usedStartingLine = startingLine;
		if(_startingLine > 0)
			usedStartingLine = _startingLine;
		Graph graph = new Graph(_label, usedStartingLine);
		List<NodeChange> nodesList = new ArrayList<NodeChange>();

		// duplicate the nodes
		for (GraphNode node : _graphNodes) {
			GraphNode newNode = new GraphNode(node);
			map.put(node, newNode);
			graph._graphNodes.add(newNode);
			nodesList.add(new NodeChange(node, newNode));
		}

		for (Graph subgraph : _subgraphs) {
			Graph copy = subgraph.getCopy(map, astLoader, startingLine);
			graph._subgraphs.add(copy);
		}

		// add the dependent nodes
		for (NodeChange nodeChange : nodesList) {
			GraphNode oldNode = nodeChange._originalNode;
			GraphNode newNode = nodeChange._newNode;
			int onSL = oldNode.getStartingLine();
			for (GraphNode dependentNode : oldNode.getDependentNodes()) {
				GraphNode mappedNode = map.get(dependentNode);
				newNode.addDependentNode(mappedNode, astLoader, onSL);
			}
		}

		return graph;
	}

	/**
	 * Adds one graph into another
	 * 
	 * @param graph
	 * @param name
	 * @return The map between the nodes in the old graph and in the new
	 */
	public Map<GraphNode, GraphNode> addSubGraph(Graph graph, AstLoader astLoader, int startingLine) {
		Map<GraphNode, GraphNode> map = new HashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map, astLoader, startingLine);
		_subgraphs.add(graphCopy);
		return map;
	}
	
	public void merge(Graph graph) {
		_graphNodes.addAll(graph._graphNodes);
		_subgraphs.addAll(graph._subgraphs);
	}

	public String getName() {
		return _label;
	}

	public void setLabel(String label) {
		_label = label;
	}

	public int getStartingLine() {
		return _startingLine;
	}

	public GraphNode addDirectVal(eValueType type, String value, int startingLine) {
		return add_graph_node(value, NodeType.E_DIRECT_VALUE, startingLine);
	}

	GraphNode addUnOp(eUnOp op, GraphNode val_node, AstLoader astLoader, int startingLine) {
		GraphNode un_op_node = add_graph_node(CppMaps._un_op_strings.get(op), NodeType.E_OPERATION,
				startingLine);

		val_node.addDependentNode(un_op_node, astLoader, startingLine);

		return un_op_node;
	}

	public GraphNode addNotOp(GraphNode val_node, AstLoader astLoader, int startingLine) {
		GraphNode notOpNode = add_graph_node("!", NodeType.E_OPERATION, startingLine);
		val_node.addDependentNode(notOpNode, astLoader, startingLine);

		return notOpNode;
	}

	public GraphNode addBinOp(eBinOp op, GraphNode val1_node, GraphNode val2_node,
			AstLoader astLoader, int startingLine) {
		GraphNode bin_op_node = add_graph_node(CppMaps._bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		val1_node.addDependentNode(bin_op_node, astLoader, startingLine);
		val2_node.addDependentNode(bin_op_node, astLoader, startingLine);

		return bin_op_node;
	}

	public GraphNode addAssignBinOp(eAssignBinOp op, Var lhs_var_decl, GraphNode lhs_node,
			GraphNode rhs_node, AstLoader astLoader, int startingLine) {
		GraphNode bin_op_node = add_graph_node(CppMaps._assign_bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		lhs_node.addDependentNode(bin_op_node, astLoader, startingLine);
		rhs_node.addDependentNode(bin_op_node, astLoader, startingLine);

		return lhs_var_decl
				.receiveAssign(NodeType.E_VARIABLE, bin_op_node, astLoader, startingLine);
	}

	public void addIf(Var var, GraphNode ifTrue, GraphNode ifFalse, GraphNode condition,
			AstLoader astLoader, int startingLine) {
		GraphNode ifOpNode = add_graph_node("If", NodeType.E_OPERATION, startingLine);

		ifTrue.addDependentNode(ifOpNode, astLoader, startingLine);
		ifFalse.addDependentNode(ifOpNode, astLoader, startingLine);
		condition.addDependentNode(ifOpNode, astLoader, startingLine);

		var.receiveAssign(NodeType.E_VARIABLE, ifOpNode, null, startingLine);
	}
	
	public void mergeNodes(GraphNode primaryNode, GraphNode secondaryNode, int startingLine) {
		primaryNode.merge(secondaryNode, startingLine);
		_graphNodes.remove(secondaryNode);
	}
}
