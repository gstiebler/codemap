package gvpl.common;

import gvpl.graph.Graph;
import gvpl.graph.GraphBuilder.TypeId;

/**
 * Structure that holds variable declaration parameters
 */
public class DirectVarDecl extends VarDecl {
	
	protected String _name;

	public DirectVarDecl(Graph gvplGraph, String name, TypeId type) {
		super(type, gvplGraph);
		_name = name;
	}

	public String getName() {
		return _name;
	}
}