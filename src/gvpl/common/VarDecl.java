package gvpl.common;

import gvpl.graph.GraphNode;
import gvpl.graph.GraphBuilder.TypeId;

public abstract class VarDecl {
	protected TypeId _type;
	protected GraphNode _curr_graph_node;
	protected GraphNode _first_graph_node;
	
	protected boolean _written = false;
	protected boolean _read = false;

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
		_written = true;
	}
	
	public void setRead() {
		_read = true;
	}
	
	public GraphNode getFirstNode() {
		return _first_graph_node;
	}
	
	public GraphNode getCurrentNode() {
		return _curr_graph_node;
	}
	
	public boolean getWritten() {
		return _written;
	}
	
	public boolean getRead() {
		return _read;
	}
	
	abstract public String getName();
}
