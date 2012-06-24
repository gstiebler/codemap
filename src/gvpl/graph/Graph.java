package gvpl.graph;


import java.util.ArrayList;
import java.util.List;

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

	private List<GraphNode> _graph_nodes = new ArrayList<GraphNode>();

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
}
