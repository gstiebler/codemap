package gvpl.common;

import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.TypeId;

/**
 * Structure that holds variable declaration parameters
 */
public class DirectVarDecl extends VarDecl {
	
	protected String _name;

	public DirectVarDecl(GraphBuilder graphBuilder, String name, TypeId type) {
		super(type, graphBuilder._gvplGraph);
		_name = name;
	}

	public String getName() {
		return _name;
	}
}