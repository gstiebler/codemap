package gvpl.common;

import java.util.List;
import java.util.Map;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.cdt.MemberFunc;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

public class MemAddressVar extends Var {

	private PossiblePointedVar _possiblePointedVar = new PossiblePointedVar();
	private boolean _hasReceivedVar = false;
	GraphNode _lastPointedVarNode = null;
	boolean _onceRead = false;
	boolean _onceWritten = false;

	public MemAddressVar(Graph gvplGraph, String name, TypeId type) {
		super(gvplGraph, name, type);
		// TODO Auto-generated constructor stub
	}

	public void setPointedVar(Var pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
		_hasReceivedVar = true;
	}

	public boolean getHasReceivedVar() {
		return _hasReceivedVar;
	}

	protected void initializePointedVar(Var pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
	}

	public Var getPointedVar() {
		return _possiblePointedVar._finalVar;
	}

	@Override
	public void updateNode(GraphNode node) {
		getPointedVar().updateNode(node);
	}

	@Override
	public GraphNode getFirstNode() {
		return getPointedVar().getFirstNode();
	}
	
	@Override
	public GraphNode getCurrentNode(int startingLine) {
		Var pointedVar = getPointedVar();
		if(pointedVar == null) {
			return _possiblePointedVar.getIfNode(_gvplGraph, startingLine);
		}
		GraphNode currentPointedVarNode = pointedVar.getCurrentNode(startingLine);
		if (currentPointedVarNode != _lastPointedVarNode) {
			_currGraphNode = _gvplGraph.addGraphNode(this, NodeType.E_VARIABLE, startingLine);
			currentPointedVarNode.addDependentNode(_currGraphNode, startingLine);
		}
		_onceRead = true;
		return _currGraphNode;
	}

	/**
	 * Cria-se um novo nó para a
	 */
	@Override
	public GraphNode receiveAssign(NodeType lhs_type, GraphNode rhs_node, int startLocation) {
		// Cria-se um novo nó para a variável "ponteiro"
		GraphNode newNode = super.receiveAssign(lhs_type, rhs_node, startLocation);
		_onceWritten = true;
		// A variável apontada recebe o nó recém-criado
		return getPointedVar().receiveAssign(lhs_type, newNode, startLocation);
	}

	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine) {
		Var var = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astLoader, astInterpreter);
		initializePointedVar(var);
		var.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}

	@Override
	public Var getVarInMem() {
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
	
	public MemAddressVar updateInternalVars(Map<Var, Var> inToExtVar) {
		updateInternalVarsRecursive(_possiblePointedVar, inToExtVar);
		
		return this;
	}
	
	private void updateInternalVarsRecursive(PossiblePointedVar possiblePointedVar, Map<Var, Var> inToExtVar) {
		if(possiblePointedVar == null)
			return;
		
		updateInternalVarsRecursive(possiblePointedVar._varTrue, inToExtVar);
		updateInternalVarsRecursive(possiblePointedVar._varFalse, inToExtVar);
		
		Var converted = inToExtVar.get(possiblePointedVar._finalVar);
		if(converted != null)
			possiblePointedVar._finalVar = converted; 
	}
	
	@Override
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader, int startingLine) {
		Var var = getPointedVar();
		if(!(var instanceof ClassVar))
			ErrorOutputter.fatalError("You're doing it wrong.");
		
		return memberFunc.loadMemberFuncRef((ClassVar)var, parameterValues, graph, astLoader, startingLine);
	}

}
