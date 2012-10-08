package gvpl.common;

import gvpl.graph.GraphNode;

public class FuncParameter {
	
	public enum IndirectionType {
		E_VARIABLE, E_POINTER, E_REFERENCE, E_INDIFERENT
	};
	
	private Var _varDecl = null;
	private GraphNode _node = null;
	private IndirectionType _indirectionType = null;
	int _type = -1;
	
	public FuncParameter(Var varDecl, IndirectionType type) {
		_varDecl = varDecl;
		_indirectionType = type;
	}
	
	public FuncParameter(GraphNode node, IndirectionType type) {
		_node = node;
		_indirectionType = type;
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
		return _indirectionType;
	}
	
	public void setType(int type) {
		_type = type;
	}
	
	//TODO improve
	public boolean isEquivalent(FuncParameter other) {
		if(_indirectionType != other._indirectionType)
			return false;
		
		if(_type != other._type)
			return false;
		
		return true;
	}
}
