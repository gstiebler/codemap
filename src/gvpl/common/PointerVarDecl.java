package gvpl.common;

import gvpl.graph.GraphBuilder;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public class PointerVarDecl extends DirectVarDecl {

	private VarDecl _pointedVarDecl = null;
	
	public PointerVarDecl(GraphBuilder graphBuilder, String name, TypeId type) {
		super(graphBuilder, name, type);
	}
	
	public void setPointedVarDecl(VarDecl pointedVarDecl) {
		_pointedVarDecl = pointedVarDecl;
	}
	
	@Override
	public void updateNode(GraphNode node) {
		_pointedVarDecl.updateNode(node);
	}
	
	@Override
	public GraphNode getFirstNode() {
		return _pointedVarDecl.getFirstNode();
	}
	
	@Override
	public GraphNode getCurrentNode() {
		return _pointedVarDecl.getCurrentNode();
	}

}
