package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.cdt.CppMaps;
import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.CppMaps.eUnOp;
import gvpl.cdt.CppMaps.eValueType;
import gvpl.common.GeneralOutputter;
import gvpl.common.IVar;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import debug.DebugOptions;

public class Graph {

	public enum NodeType {
		E_INVALID_NODE_TYPE, E_DIRECT_VALUE, E_VARIABLE, E_OPERATION, E_FOR_LOOP, E_DECLARED_PARAMETER, E_RETURN_VALUE, E_LOOP_HEADER
	}

	private String _label;
	private List<GraphNode> _graphNodes = new ArrayList<GraphNode>();
	public List<Graph> _subgraphs = new ArrayList<Graph>();
	private int _startingLine = -1;
	private static int _counter = 1000;
	private int _id;

	public Graph(String label, int startingLine) {
		_label = label;
		_startingLine = startingLine;
		_id = _counter++;
	}

	public Graph(int startingLine) {
		_startingLine = startingLine;
		_id = _counter++;
	}
	
	public int getId() {
		return _id;
	}

	public GraphNode addGraphNode(String name, NodeType type, int startingLine) {
		GraphNode graphNode = new GraphNode(name, type, startingLine);
		_graphNodes.add(graphNode);
		
		if(DebugOptions.outputNodeInfo())
			GeneralOutputter.debug("Add node " + graphNode.getName() +" (" + graphNode.getId() + ") graph " + _label + " (" + _id + ")");
		return graphNode;
	}

	public GraphNode addGraphNode(IVar parentVar, NodeType type, int startingLine) {
		GraphNode graphNode = new GraphNode(parentVar, type, startingLine);
		_graphNodes.add(graphNode);
		
		if(DebugOptions.outputNodeInfo())
			GeneralOutputter.debug("Add node " + graphNode.getName() +" (" + graphNode.getId() + ") graph " + _label + " (" + _id + ")");
		return graphNode;
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
				newNode.addDependentNode(mappedNode, onSL);
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
		Map<GraphNode, GraphNode> map = new LinkedHashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map, astLoader, startingLine);
		_subgraphs.add(graphCopy);
		if(DebugOptions.outputGraphInfo())
			GeneralOutputter.debug("Adding graph (" + graph._id + ") to (" + _id + ")");
		return map;
	}
	
	public void merge(Graph graph) {
		_graphNodes.addAll(graph._graphNodes);
		_subgraphs.addAll(graph._subgraphs);
		if(DebugOptions.outputGraphInfo())
			GeneralOutputter.debug("Merging graph (" + graph._id + ") to (" + _id + ")");
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
		return addGraphNode(value, NodeType.E_DIRECT_VALUE, startingLine);
	}

	GraphNode addUnOp(eUnOp op, GraphNode valNode, AstLoader astLoader, int startingLine) {
		GraphNode unOpNode = addGraphNode(CppMaps._un_op_strings.get(op), NodeType.E_OPERATION,
				startingLine);

		valNode.addDependentNode(unOpNode, startingLine);

		return unOpNode;
	}

	public GraphNode addNotOp(GraphNode val_node, AstLoader astLoader, int startingLine) {
		GraphNode notOpNode = addGraphNode("!", NodeType.E_OPERATION, startingLine);
		val_node.addDependentNode(notOpNode, startingLine);

		return notOpNode;
	}

	public GraphNode addBinOp(eBinOp op, GraphNode val1_node, GraphNode val2_node,
			AstLoader astLoader, int startingLine) {
		GraphNode binOpNode = addGraphNode(CppMaps._bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		val1_node.addDependentNode(binOpNode, startingLine);
		val2_node.addDependentNode(binOpNode, startingLine);

		return binOpNode;
	}

	public GraphNode addAssignBinOp(eAssignBinOp op, IVar lhs_varDecl, GraphNode lhsNode,
			GraphNode rhs_node, AstLoader astLoader, int startingLine) {
		GraphNode binOpNode = addGraphNode(CppMaps._assign_bin_op_strings.get(op),
				NodeType.E_OPERATION, startingLine);

		lhsNode.addDependentNode(binOpNode, startingLine);
		rhs_node.addDependentNode(binOpNode, startingLine);

		return lhs_varDecl
				.receiveAssign(NodeType.E_VARIABLE, binOpNode, startingLine);
	}
	
	public void mergeNodes(GraphNode primaryNode, GraphNode secondaryNode, int startingLine) {
		primaryNode.merge(secondaryNode, startingLine);
		GeneralOutputter.debug("Merging node (" + secondaryNode.getId() + ") to (" + primaryNode.getId() + ")");
		_graphNodes.remove(secondaryNode);
		if(!_graphNodes.contains(primaryNode))
			_graphNodes.add(primaryNode);
	}
	
	public void removeNode(GraphNode node) {
		_graphNodes.remove(node);
	}
	
	@Override
	public String toString() {
		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);
		
		StringWriter outString = new StringWriter();		
		fileDriver.print(this, outString, visualizer);
		
		return outString.toString();
	}
}
