package gvpl.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {

	public enum NodeType {
		E_INVALID_NODE_TYPE,
		E_DIRECT_VALUE,
		E_VARIABLE,
		E_OPERATION,
		E_FOR_LOOP,
		E_DECLARED_PARAMETER,
		E_RETURN_VALUE
	}

	private String _name;

	private List<GraphNode> _graph_nodes = new ArrayList<GraphNode>();
	public List<Graph> _subgraphs = new ArrayList<Graph>();
	
	public Graph(String name) {
		_name = name;
	}	
	
	public Graph() { }

	public GraphNode add_graph_node(String name, NodeType type) {
		GraphNode graph_node = new GraphNode(name, type);
		_graph_nodes.add(graph_node);
		return graph_node;
	}

	public int getNumNodes() {
		return _graph_nodes.size();
	}

	public GraphNode getNode(int index) {
		return _graph_nodes.get(index);
	}

	public Graph getCopy(Map<GraphNode, GraphNode> map) {
		Graph graph = new Graph(_name);

		// duplicate the nodes
		for (GraphNode node : _graph_nodes) {
			GraphNode newNode = new GraphNode(node);
			map.put(node, newNode);
			graph._graph_nodes.add(newNode);
		}

		// add the dependent nodes
		for (Map.Entry<GraphNode, GraphNode> entry : map.entrySet()) {
			GraphNode oldNode = entry.getKey();
			GraphNode newNode = entry.getValue();
			for (GraphNode dependentNode : oldNode.getDependentNodes()) {
				newNode.addDependentNode(map.get(dependentNode));
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
	public Map<GraphNode, GraphNode> addSubGraph(Graph graph){
		Map<GraphNode, GraphNode> map = new HashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map);
		_subgraphs.add(graphCopy);
		return map;
	}
	
	public String getName() {
		return _name;
	}
	
	public void setName(String name) {
		_name = name;
	}
}
