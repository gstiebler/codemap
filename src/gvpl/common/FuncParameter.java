package gvpl.common;

import gvpl.graph.GraphNode;

public class FuncParameter {
	
	public enum eParameterType {
		E_VARIABLE, E_POINTER, E_REFERENCE
	};
	
	private DirectVarDecl _varDecl = null;
	private GraphNode _node = null;
	eParameterType _type = null;
	
	public FuncParameter(DirectVarDecl varDecl, eParameterType type) {
		_varDecl = varDecl;
		_type = type;
	}
	
	public FuncParameter(GraphNode node, eParameterType type) {
		_node = node;
		_type = type;
	}
	
	public GraphNode getNode(int startingLine) {
		if(_varDecl != null)
			return _varDecl.getCurrentNode(startingLine);
		
		return _node;
	}
	
	public DirectVarDecl getVar() {
		return _varDecl;
	}
	
	public eParameterType getType() {
		return _type;
	}
}
