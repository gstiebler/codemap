package gvpl.common;

import gvpl.cdt.AstLoaderCDT;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

public class MemAddressVar extends Var {

	private PossiblePointedVar _possiblePointedVar = new PossiblePointedVar(this);
	private boolean _hasReceivedVar = false;
	GraphNode _lastPointedVarNode = null;
	boolean _onceRead = false;
	boolean _onceWritten = false;

	public MemAddressVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}

	public void setPointedVar(IVar pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
		_hasReceivedVar = true;
	}
	
	public IVar getPointedVar() {
		if(_possiblePointedVar._finalVar == null) {
			if(_possiblePointedVar._conditionNode == null)
				return null;
			else
				return _possiblePointedVar;
		} else
			return _possiblePointedVar._finalVar;
	}

	public boolean getHasReceivedVar() {
		return _hasReceivedVar;
	}

	protected void initializePointedVar(IVar pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
	}

	@Override
	public void updateNode(GraphNode node) {
		PossiblePointedVar.updateNodeRecursive(_possiblePointedVar, _gvplGraph, node);
	}

	@Override
	public GraphNode getFirstNode() {
		return _possiblePointedVar.getFirstNode();
	}
	
	@Override
	public GraphNode getCurrentNode() {
		if(_possiblePointedVar._finalVar == null) {
			return _possiblePointedVar.getIfNode(_gvplGraph);
		}
		GraphNode currentPointedVarNode = _possiblePointedVar._finalVar.getCurrentNode();
		if (currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE);
			currentPointedVarNode.addDependentNode(_currGraphNode);
		}
		_onceRead = true;
		return _currGraphNode;
	}

	@Override
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode, Graph graph) {
		// Create a new node to the "pointer" variable
		GraphNode newNode = super.receiveAssign(lhsType, rhsNode, graph);
		_onceWritten = true;

		return newNode;
	}

	@Override
	public void initializeVar(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter) {
		IVar var = astLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astLoader, astInterpreter);
		var.initializeVar(nodeType, graph, astLoader, astInterpreter);
		initializePointedVar(var);
	}

	@Override
	public IVar getVarInMem() {
		return getPointedVar();
	}

	public void setIf(GraphNode conditionNode, MemAddressVar varTrue, MemAddressVar varFalse) {
		_possiblePointedVar.setPossibleVars(conditionNode, varTrue._possiblePointedVar,
				varFalse._possiblePointedVar);
	}
	
	@Override
	public boolean onceRead() {
		return _onceRead;
	}
	
	@Override
	public boolean onceWritten() {
		return _onceWritten;
	}
	
	public MemAddressVar updateInternalVars(InToExtVar inToExtVar) {
		PossiblePointedVar.updateInternalVarsRecursive(_possiblePointedVar, inToExtVar);
		
		return this;
	}
	
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoaderCDT astLoader) {
		return PossiblePointedVar.loadMemberFuncRefRecursive(_possiblePointedVar, memberFunc,
				parameterValues, graph, astLoader);
	}
	
	public void delete() {
		_possiblePointedVar.delete();
	}

}
