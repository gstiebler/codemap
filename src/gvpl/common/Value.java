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
	
	public Value() {
		
	}
	
	public GraphNode getNode() {
		if(_var != null)
			return _var.getCurrentNode();
		
		return _node;
	}
	
	public IVar getVar() {
		return _var;
	}
	
	public boolean hasVar() {
		return _var != null;
	}
}
