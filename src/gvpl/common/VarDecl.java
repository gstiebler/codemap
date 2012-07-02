package gvpl.common;

import gvpl.graph.GraphNode;
import gvpl.graph.GraphBuilder.TypeId;

public abstract class VarDecl {
	protected TypeId _type;
	private GraphNode _curr_graph_node;
	private GraphNode _first_graph_node;

	public VarDecl(TypeId type) {
		_type = type;
		_curr_graph_node = null;
		_first_graph_node = null;
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
	
	abstract public String getName();
}
