package gvpl.graph;

import gvpl.common.Var;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {

	public enum NodeType {
		E_INVALID_NODE_TYPE,
		E_DIRECT_VALUE,
		E_VARIABLE,
		E_OPERATION,
		E_FOR_LOOP,
		E_DECLARED_PARAMETER,
		E_RETURN_VALUE,
		E_LOOP_HEADER
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
	
	public Iterable<GraphNode> getNodes() {
		return _graphNodes;
	}

	public Graph getCopy(Map<GraphNode, GraphNode> map, int startingLine) {
		
		class NodeChange {
			GraphNode _originalNode;
			GraphNode _newNode;
			NodeChange(GraphNode originalNode, GraphNode newNode) {
				_originalNode = originalNode;
				_newNode = newNode;
			}
		}
		
		Graph graph = new Graph(_label, startingLine);
		List<NodeChange> nodesList = new ArrayList<NodeChange>();
		
		// duplicate the nodes
		for (GraphNode node : _graphNodes) {
			GraphNode newNode = new GraphNode(node);
			map.put(node, newNode);
			graph._graphNodes.add(newNode);
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
				newNode.addDependentNode(map.get(dependentNode), oldNode.getStartingLine());
			}
		}
		
		return graph;
	}
	
	/**
	 * Adds one graph into another
	 * @param graph
	 * @param name
	 * @return The map between the nodes in the old graph and in the new
	 */
	public Map<GraphNode, GraphNode> addSubGraph(Graph graph, int startingLine){
		Map<GraphNode, GraphNode> map = new HashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map, startingLine);
		_subgraphs.add(graphCopy);
		return map;
	}
	
	public Iterable<Graph> getSubgraphs() {
		return _subgraphs;
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
	
	public static void getAccessedVars(Graph graph, Set<Var> writtenVars, Set<Var> readVars) {
		for(GraphNode graphNode : graph.getNodes()) {
			if(graphNode.hasSourceNodes()) {
				Var parentVar = graphNode.getParentVar();
				if(parentVar != null) {
					writtenVars.add(parentVar);
				}
			}
		
			if(graphNode.hasDependentNodes()) {
				Var parentVar = graphNode.getParentVar();
				if(parentVar != null)
					readVars.add(parentVar);
			}
		}
		
		for(Graph subGraph : graph.getSubgraphs()) {
			Set<Var> subGraphWrittenVars = new HashSet<Var>();
			Set<Var> subGraphReadVars = new HashSet<Var>();
			getAccessedVars(subGraph, subGraphWrittenVars, subGraphReadVars);
			
			writtenVars.addAll(subGraphWrittenVars);
			readVars.addAll(subGraphReadVars);
		}
	}
	
	public void append(Graph graph) {
		_graphNodes.addAll(graph._graphNodes);
		_subgraphs.addAll(graph._subgraphs);
	}
}
