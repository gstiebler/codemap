package gvpl.common;

import gvpl.graph.GraphNode;

public class Value {
	IVar _var = null;
	GraphNode _node = null;
	
	public Value(IVar var) {
		_var = var;
	}
	
	public Value(GraphNode node) {
		_node = node;
	}
	
	public GraphNode getNode() {
		if(_node != null)
			return _node;
		
		return _var.getCurrentNode();
	}
	
	public IVar getVar() {
		return _var;
	}
	
	public boolean hasVar() {
		return _var != null;
	}
}
