package gvpl.common;

import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

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
		
		if(_node != null)
			return _node;
		
		return new GraphNode("PROBLEM_NODE", NodeType.E_INVALID_NODE_TYPE);
	}
	
	public IVar getVar() {
		return _var;
	}
	
	public boolean hasVar() {
		return _var != null;
	}
}
