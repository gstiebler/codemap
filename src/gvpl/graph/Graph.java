package gvpl.graph;

import gvpl.cdt.CppMaps;
import gvpl.cdt.CppMaps.eAssignBinOp;
import gvpl.cdt.CppMaps.eBinOp;
import gvpl.cdt.CppMaps.eUnOp;
import gvpl.common.BaseScope;
import gvpl.common.IVar;
import gvpl.common.Value;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class Graph {
	
	Logger logger = LogManager.getLogger(Graph.class.getName());

	public enum NodeType {
		E_INVALID_NODE_TYPE, 
		E_DIRECT_VALUE, 
		E_VARIABLE, 
		E_OPERATION, 
		E_FOR_LOOP, 
		E_DECLARED_PARAMETER, 
		E_RETURN_VALUE, 
		E_LOOP_HEADER,
		E_GARBAGE
	}

	private String _label;
	private List<GraphNode> _graphNodes = new ArrayList<GraphNode>();
	public List<Graph> _subgraphs = new ArrayList<Graph>();
	private int _startingLine = DebugOptions.getStartingLine();
	private List<Integer> _linesHistory = DebugOptions.lastVisitedLines();
	private static int _counter = 1000;
	private int _id;

	public Graph(String label) {
		_label = label;
		_id = _counter++;
		logger.debug("New graph, label: {}, sl: {}", _label, _startingLine);
	}

	public Graph() {
		logger.debug("New graph without label, sl: {}", _startingLine);
		_startingLine = -1;
		_id = _counter++;
	}
	
	public int getId() {
		return _id;
	}

	private void addGraphNode(GraphNode graphNode) {
		Graph parent = graphNode.getGraph();
		if(parent != null)
			parent.removeNode(graphNode);
		
		_graphNodes.add(graphNode);
		graphNode.setGraph(this);
	}
	
	public GraphNode addGraphNode(String name, NodeType type) {
		ExecTreeLogger.log("Graph: " + _label + ", node: " + name);
		GraphNode graphNode = new GraphNode(name, type);
		addGraphNode(graphNode);
		
		logger.info("Add node {} ({}) graph {} ({})", graphNode.getName(), graphNode.getId(), _label, _id);
		return graphNode;
	}

	public GraphNode addGraphNode(IVar parentVar, NodeType type) {
		ExecTreeLogger.log("Graph: " + _label + ", Parent var: " + parentVar);
		GraphNode graphNode = new GraphNode(parentVar, type);
		addGraphNode(graphNode);

		logger.info("Add node {} ({}) graph {} ({})", graphNode.getName(), graphNode.getId(), _label, _id);
		return graphNode;
	}

	public int getNumNodes() {
		return _graphNodes.size();
	}

	public GraphNode getNode(int index) {
		return _graphNodes.get(index);
	}
	
	public void addSubGraph(Graph graph) {
		_subgraphs.add(graph);
	}

	public Graph getCopy(Map<GraphNode, GraphNode> map, int startingLine) {

		logger.debug("Getting copy of the graph");
		
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
		Graph graph = new Graph(_label);
		graph._startingLine = usedStartingLine;
		List<NodeChange> nodesList = new ArrayList<NodeChange>();

		// duplicate the nodes
		for (GraphNode node : _graphNodes) {
			GraphNode newNode = new GraphNode(node);
			map.put(node, newNode);
			graph.addGraphNode(newNode);
			nodesList.add(new NodeChange(node, newNode));
		}

		for (Graph subgraph : _subgraphs) {
			Graph copy = subgraph.getCopy(map, startingLine);
			graph._subgraphs.add(copy);
		}

		// add the dependent nodes
		for (NodeChange nodeChange : nodesList) {
			GraphNode oldNode = nodeChange._originalNode;
			GraphNode newNode = nodeChange._newNode;
			for (GraphNode dependentNode : oldNode.getDependentNodes()) {
				GraphNode mappedNode = map.get(dependentNode);
				newNode.addDependentNode(mappedNode);
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
	public Map<GraphNode, GraphNode> addSubGraphCopy(Graph graph) {
		Map<GraphNode, GraphNode> map = new LinkedHashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map, DebugOptions.getStartingLine());
		_subgraphs.add(graphCopy);
		logger.info("Adding graph ({}) to ({})", graph._id, _id);
		return map;
	}
	
	public void merge(Graph graph) {
		List<GraphNode> childNodes = new ArrayList<GraphNode>(graph._graphNodes);
		for(GraphNode graphNode : childNodes)
			addGraphNode(graphNode);
		_subgraphs.addAll(graph._subgraphs);
		logger.info("Merging graph ({}) to ({})", graph._id, _id);
	}

	public String getName() {
		return _label;
	}

	public void setLabel(String label) {
		_label = label;
	}

//	public int getStartingLine() {
//		return _startingLine;
//	}

	public GraphNode addDirectVal(String value) {
		return addGraphNode(value, NodeType.E_DIRECT_VALUE);
	}

	GraphNode addUnOp(eUnOp op, GraphNode valNode, BaseScope astLoader) {
		GraphNode unOpNode = addGraphNode(CppMaps._un_op_strings.get(op), NodeType.E_OPERATION);

		valNode.addDependentNode(unOpNode);

		return unOpNode;
	}

	public GraphNode addNotOp(GraphNode val_node) {
		GraphNode notOpNode = addGraphNode("!", NodeType.E_OPERATION);
		val_node.addDependentNode(notOpNode);

		return notOpNode;
	}

	public GraphNode addBinOp(eBinOp op, GraphNode val1Node, GraphNode val2Node,
			BaseScope astLoader) {
		GraphNode binOpNode = addGraphNode(CppMaps._binOpStrings.get(op),
				NodeType.E_OPERATION);

		val1Node.addDependentNode(binOpNode);
		val2Node.addDependentNode(binOpNode);

		return binOpNode;
	}

	public GraphNode addAssignBinOp(eAssignBinOp op, IVar lhs_varDecl, GraphNode lhsNode,
			GraphNode rhsNode, BaseScope astLoader) {
		GraphNode binOpNode = addGraphNode(CppMaps._assignBinOpStrings.get(op),
				NodeType.E_OPERATION);

		lhsNode.addDependentNode(binOpNode);
		rhsNode.addDependentNode(binOpNode);

		return lhs_varDecl.receiveAssign(NodeType.E_VARIABLE, new Value(binOpNode), this);
	}
	
	public void mergeNodes(GraphNode primaryNode, GraphNode secondaryNode) {
		primaryNode.merge(secondaryNode);
		logger.info("Merging node ({}) to ({})", secondaryNode.getId(), primaryNode.getId());
		_graphNodes.remove(secondaryNode);
		if(!_graphNodes.contains(primaryNode))
			addGraphNode(primaryNode);
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
	
	public List<Integer> getLinesHistory() {
		return _linesHistory;
	}
}
