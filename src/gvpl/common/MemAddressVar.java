package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public class MemAddressVar extends Var {

	private Var _pointedVar = null;
	GraphNode _lastPointedVarNode = null;
	
	public MemAddressVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}

	public void setPointedVarDecl(Var pointedVarDecl) {
		_pointedVar = pointedVarDecl;
	}
	
	@Override
	public void updateNode(GraphNode node) {
		_pointedVar.updateNode(node);
	}
	
	@Override
	public GraphNode getFirstNode() {
		return _pointedVar.getFirstNode();
	}
	
	@Override
	public GraphNode getCurrentNode(int startingLine) {
		GraphNode currentPointedVarNode = _pointedVar.getCurrentNode(startingLine);
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
		return _pointedVar.receiveAssign(lhs_type, newNode, astLoader, startLocation);
	}
	
	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader, AstInterpreter astInterpreter, 
			int startingLine) {
		//_pointedVar = new Var(_gvplGraph, _name + "_pointed", _type);
		_pointedVar = AstLoader.instanceVarDecl(IndirectionType.E_VARIABLE, _name + "_pointed", _type, graph, astLoader, astInterpreter);
		_pointedVar.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}
	
	public Var getPointedVar() {
		return _pointedVar;
	}

}
