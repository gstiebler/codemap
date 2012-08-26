package gvpl.common;

import gvpl.graph.GraphNode;

public class FuncParameter {
	
	public enum IndirectionType {
		E_VARIABLE, E_POINTER, E_REFERENCE
	};
	
	private Var _varDecl = null;
	private GraphNode _node = null;
	IndirectionType _type = null;
	
	public FuncParameter(Var varDecl, IndirectionType type) {
		_varDecl = varDecl;
		_type = type;
	}
	
	public FuncParameter(GraphNode node, IndirectionType type) {
		_node = node;
		_type = type;
	}
	
	public GraphNode getNode(int startingLine) {
		if(_varDecl != null)
			return _varDecl.getCurrentNode(startingLine);
		
		return _node;
	}
	
	public Var getVar() {
		return _varDecl;
	}
	
	public IndirectionType getType() {
		return _type;
	}
}
