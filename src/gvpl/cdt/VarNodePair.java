package gvpl.cdt;

import gvpl.common.Var;
import gvpl.graph.GraphNode;

class VarNodePair {
	public Var _var;
	public GraphNode _graphNode;
	public VarNodePair(Var var, GraphNode graphNode) {
		_var = var;
		_graphNode = graphNode;
	}
}