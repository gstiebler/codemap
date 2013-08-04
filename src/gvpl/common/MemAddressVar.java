package gvpl.common;

import gvpl.cdt.BaseScopeCDT;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

import debug.ExecTreeLogger;

public class MemAddressVar extends Var {

	private PossiblePointedVar _possiblePointedVar = new PossiblePointedVar(this);
	private boolean _hasReceivedVar = false;
	GraphNode _lastPointedVarNode = null;

	public MemAddressVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}
	
	public MemAddressVar(MemAddressVar other) {
		super(other);
		_possiblePointedVar = new PossiblePointedVar(other._possiblePointedVar);
		_hasReceivedVar = other._hasReceivedVar;
		_lastPointedVarNode = other._lastPointedVarNode;
	}

	public void setPointedVar(IVar pointedVar) {
		ExecTreeLogger.log("Var: " + pointedVar.getName());
		_possiblePointedVar.setVar(pointedVar);
		_hasReceivedVar = true;
	}
	
	public IVar getPointedVar() {
		ExecTreeLogger.log("Var: " + getName());
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
		ExecTreeLogger.log("Var: " + getName());
		PossiblePointedVar.updateNodeRecursive(_possiblePointedVar, _gvplGraph, node);
	}
	
	@Override
	public GraphNode getCurrentNode() {
		ExecTreeLogger.log("Var: " + getName());
		PossiblePointedVar possiblePointedVar = PossiblePointedVar.filterPPVInsideIfScopes(_possiblePointedVar);
		if(possiblePointedVar._finalVar == null) {
			return possiblePointedVar.getIfNode(_gvplGraph);
		}
		GraphNode currentPointedVarNode = possiblePointedVar._finalVar.getCurrentNode();
		if (currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE);
			currentPointedVarNode.addDependentNode(_currGraphNode);
		}
		return _currGraphNode;
	}

	@Override
	public GraphNode receiveAssign(NodeType lhsType, Value rhsValue, Graph graph) {
		ExecTreeLogger.log("Var: " + getName());
		// Create a new node to the "pointer" variable
		GraphNode newNode = super.receiveAssign(lhsType, rhsValue, graph);

		return newNode;
	}

	@Override
	public void initializeVar(NodeType nodeType, Graph graph, AstInterpreter astInterpreter) {
		ExecTreeLogger.log("Var: " + getName());
		IVar var = BaseScope.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astInterpreter);
		var.initializeVar(nodeType, graph, astInterpreter);
		initializePointedVar(var);
	}

	@Override
	public IVar getVarInMem() {
		return getPointedVar();
	}
	
	public Value loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, BaseScopeCDT astLoader) {
		return PossiblePointedVar.loadMemberFuncRefRecursive(_possiblePointedVar, memberFunc,
				parameterValues, graph, astLoader);
	}
	
	public void delete() {
		_possiblePointedVar.delete();
	}

}
