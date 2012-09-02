package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public class MemAddressVar extends Var {

	protected Var _pointedVar = null;
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
			currentPointedVarNode.addDependentNode(_currGraphNode, startingLine);
		}
			
		return _currGraphNode;
	}
	
	@Override
	public GraphNode receiveAssign(Graph graph, NodeType lhs_type, GraphNode rhs_node,
			int startLocation) {
		GraphNode newNode = super.receiveAssign(graph, lhs_type, rhs_node, startLocation);
		return _pointedVar.receiveAssign(graph, lhs_type, newNode, startLocation);
	}
	
	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader, AstInterpreter astInterpreter, 
			int startingLine) {
		_pointedVar = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type, graph, astLoader, astInterpreter);
		_pointedVar.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}
	
	public Var getPointedVar() {
		return _pointedVar;
	}
	
	@Override
	public Var getVarInMem() {
		return _pointedVar;
	}

}
