package gvpl.graph;

import gvpl.graph.GraphBuilder.DirectVarDecl;
import gvpl.graph.GraphBuilder.MemberId;

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

	/**
	 * Copy one graph into another
	 * 
	 * @param other
	 *            The other graph
	 * @return The map between the nodes in the old graph and in the new
	 */
	public Map<GraphNode, GraphNode> merge(Graph other) {
		Map<GraphNode, GraphNode> map = new HashMap<GraphNode, GraphNode>();
		Graph copy = other.getCopy(map);
		_graph_nodes.addAll(copy._graph_nodes);
		return map;
	}

	private Graph getCopy(Map<GraphNode, GraphNode> map) {
		Graph graph = new Graph();

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
			for (GraphNode dependentNode : oldNode._dependent_nodes) {
				newNode._dependent_nodes.add(map.get(dependentNode));
			}
		}

		return graph;
	}
}
