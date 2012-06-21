package gvpl.graph;


import java.util.ArrayList;
import java.util.List;

public class Graph {

	public enum NodeType {
		E_INVALID_NODE_TYPE,
		E_DIRECT_VALUE,
		E_VARIABLE,
		E_OPERATION,
		E_FOR_LOOP
	}

	public List<GraphNode> _graph_nodes = new ArrayList<GraphNode>();

	public GraphNode add_graph_node(String name, NodeType type) {
		GraphNode graph_node = new GraphNode(name, type);
		_graph_nodes.add(graph_node);
		return graph_node;
	}
}
