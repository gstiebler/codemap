package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;

public class MemAddressVarDecl extends DirectVarDecl {

	private VarDecl _pointedVarDecl = null;
	GraphNode _lastPointedVarNode = null;
	
	public MemAddressVarDecl(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
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
	public GraphNode getCurrentNode(int startingLine) {
		GraphNode currentPointedVarNode = _pointedVarDecl.getCurrentNode(startingLine);
		if(currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE, startingLine);
			currentPointedVarNode.addDependentNode(_currGraphNode, null, startingLine);
		}
			
		return _currGraphNode;
	}
	
	@Override
	public GraphNode receiveAssign(NodeType lhs_type, GraphNode rhs_node,
			AstLoader astLoader, int startLocation) {
		GraphNode newNode = super.receiveAssign(lhs_type, rhs_node, astLoader, startLocation);
		return _pointedVarDecl.receiveAssign(lhs_type, newNode, astLoader, startLocation);
	}
	
	@Override
	public void initializeGraphNode(NodeType type, int startingLine) {
		_pointedVarDecl = new DirectVarDecl(_gvplGraph, _name + "_pointed", _type);
		_pointedVarDecl.initializeGraphNode(type, startingLine);
	}
	
	public VarDecl getPointedVarDecl() {
		return _pointedVarDecl;
	}

}
