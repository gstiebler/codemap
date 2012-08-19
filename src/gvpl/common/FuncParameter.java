package gvpl.common;

import gvpl.graph.GraphNode;

public class FuncParameter {

	private VarDecl _varDecl = null;
	private GraphNode _node = null;
	
	public FuncParameter(VarDecl varDecl) {
		_varDecl = varDecl;
	}
	
	public FuncParameter(GraphNode node) {
		_node = node;
	}
	
	public GraphNode getNode(int startingLine) {
		if(_varDecl != null)
			return _varDecl.getCurrentNode(startingLine);
		
		return _node;
	}
	
	public VarDecl getVar() {
		return _varDecl;
	}
}
