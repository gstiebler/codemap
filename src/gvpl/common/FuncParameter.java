package gvpl.common;

import gvpl.cdt.function.*;

import gvpl.graph.GraphNode;

public class FuncParameter {
	
	public enum IndirectionType {
		E_VARIABLE, E_POINTER, E_REFERENCE, E_FUNCTION_POINTER, E_INDIFERENT
	};
	
	private IVar _varDecl = null;
	private GraphNode _node = null;
	private Function _functionPointer = null;
	private IndirectionType _indirectionType = null;
	int _type = -1;
	
	public FuncParameter(IVar varDecl, IndirectionType type) {
		_varDecl = varDecl;
		_indirectionType = type;
	}
	
	public FuncParameter(IndirectionType type) {
		_indirectionType = type;
	}
	
	public FuncParameter(GraphNode node, IndirectionType type) {
		_node = node;
		_indirectionType = type;
	}
	
	public FuncParameter(Function functionPointer) {
		_functionPointer = functionPointer;
		_indirectionType = IndirectionType.E_FUNCTION_POINTER;
	}

	public GraphNode getNode() {
		if(_varDecl != null)
			return _varDecl.getCurrentNode();
		
		return _node;
	}
	
	public IVar getVar() {
		return _varDecl;
	}
	
	public IndirectionType getType() {
		return _indirectionType;
	}
	
	public void setType(int type) {
		_type = type;
	}
	
	public Function getFunction() {
		return _functionPointer;
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
