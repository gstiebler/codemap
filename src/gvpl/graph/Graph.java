package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.common.VarDecl;

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
		E_RETURN_VALUE,
		E_LOOP_HEADER
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

	public GraphNode add_graph_node(VarDecl parentVar, NodeType type) {
		GraphNode graph_node = new GraphNode(parentVar, type);
		_graph_nodes.add(graph_node);
		return graph_node;
	}

	public int getNumNodes() {
		return _graph_nodes.size();
	}

	public GraphNode getNode(int index) {
		return _graph_nodes.get(index);
	}

	public Graph getCopy(Map<GraphNode, GraphNode> map, AstLoader astLoader) {
		
		class NodeChange {
			GraphNode _originalNode;
			GraphNode _newNode;
			NodeChange(GraphNode originalNode, GraphNode newNode) {
				_originalNode = originalNode;
				_newNode = newNode;
			}
		}
		
		Graph graph = new Graph(_name);
		List<NodeChange> nodesList = new ArrayList<NodeChange>();
		
		// duplicate the nodes
		for (GraphNode node : _graph_nodes) {
			GraphNode newNode = new GraphNode(node);
			map.put(node, newNode);
			graph._graph_nodes.add(newNode);
			nodesList.add(new NodeChange(node, newNode));
		}

		for (Graph subgraph : _subgraphs) {
			Graph copy = subgraph.getCopy(map, astLoader); 
			graph._subgraphs.add(copy);
		}

		// add the dependent nodes
		for (NodeChange nodeChange : nodesList) {
			GraphNode oldNode = nodeChange._originalNode;
			GraphNode newNode = nodeChange._newNode;
			for (GraphNode dependentNode : oldNode.getDependentNodes()) {
				newNode.addDependentNode(map.get(dependentNode), astLoader);
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
	public Map<GraphNode, GraphNode> addSubGraph(Graph graph, AstLoader astLoader){
		Map<GraphNode, GraphNode> map = new HashMap<GraphNode, GraphNode>();
		Graph graphCopy = graph.getCopy(map, astLoader);
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
