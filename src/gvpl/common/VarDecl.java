package gvpl.common;

import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public abstract class VarDecl {
	protected TypeId _type;
	protected GraphNode _curr_graph_node = null;
	protected GraphNode _first_graph_node = null;
	
	private Graph _gvpl_graph;

	public VarDecl(TypeId type, Graph graph) {
		_type = type;
		_gvpl_graph = graph;
	}
	
	public TypeId getType() {
		return _type;
	}
	
	public void updateNode(GraphNode node) {
		if (_curr_graph_node == null)
			_first_graph_node = node;
		
		_curr_graph_node = node;
	}
	
	public GraphNode getFirstNode() {
		return _first_graph_node;
	}
	
	public GraphNode getCurrentNode() {
		return _curr_graph_node;
	}
	
	public void initializeGraphNode(NodeType type) {
		updateNode(_gvpl_graph.add_graph_node(this, type));
	}
	
	abstract public String getName();
}
