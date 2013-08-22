package gvpl.common;

import gvpl.graph.GraphNode;

public class Value {
	IVar _var = null;
	GraphNode _node = null;
	String _name = null;
	
	public Value(IVar var) {
		_var = var;
	}
	
	public Value(GraphNode node) {
		_node = node;
	}
	
	public Value(String name) {
		_name = name;
	}
	
	public String getName() {
		if( _name != null)
			return _name;
		else if (_node != null)
			return _node.getName();
		else
			return _var.getName();
	}
	
	public GraphNode getNode() {
		if(_var != null)
			return _var.getCurrentNode();
		
		if(_node != null)
			return _node;
		
		return null;
	}
	
	public IVar getVar() {
		return _var;
	}
	
	public void setVar(IVar var) {
		_node = null;
		_var = var;
	}
	
	public boolean hasVar() {
		return _var != null;
	}
}
