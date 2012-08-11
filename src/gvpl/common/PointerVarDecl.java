package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.GraphBuilder;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public class PointerVarDecl extends DirectVarDecl {

	private VarDecl _pointedVarDecl = null;
	GraphNode _lastPointedVarNode = null;
	
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
		GraphNode currentPointedVarNode = _pointedVarDecl.getCurrentNode();
		if(currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE);
			currentPointedVarNode.addDependentNode(_currGraphNode, null);
		}
			
		return _currGraphNode;
	}
	
	@Override
	public String getName() {
		return "*" + super.getName();
	}
	
	@Override
	public GraphNode addAssign(NodeType lhs_type, GraphNode rhs_node,
			AstLoader astLoader) {
		GraphNode newNode = super.addAssign(lhs_type, rhs_node, astLoader);
		return _pointedVarDecl.addAssign(lhs_type, newNode, astLoader);
	}
	
	public VarDecl getPointedVarDecl() {
		return _pointedVarDecl;
	}

}
