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

	public void setPointedVar(IVar pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
		_hasReceivedVar = true;
	}

	public boolean getHasReceivedVar() {
		return _hasReceivedVar;
	}

	protected void initializePointedVar(IVar pointedVar) {
		_possiblePointedVar.setVar(pointedVar);
	}

	public IVar getPointedVar() {
		return _possiblePointedVar._finalVar;
	}

	@Override
	public void updateNode(GraphNode node) {
		updateNodeRecursive(_possiblePointedVar, node);
	}
	
	private void updateNodeRecursive(PossiblePointedVar possiblePointedVar, GraphNode node) {
		int startingLine = -4;
		if(possiblePointedVar._conditionNode != null) {
			{
				GraphNode ifOpNode = _gvplGraph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

				node.addDependentNode(ifOpNode, startingLine);
				possiblePointedVar._varTrue._finalVar.getCurrentNode(startingLine).addDependentNode(ifOpNode, startingLine);
				possiblePointedVar._conditionNode.addDependentNode(ifOpNode, startingLine);
				
				updateNodeRecursive(possiblePointedVar._varTrue, ifOpNode);
			}
			{
				GraphNode ifOpNode = _gvplGraph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

				possiblePointedVar._varFalse._finalVar.getCurrentNode(startingLine).addDependentNode(ifOpNode, startingLine);
				node.addDependentNode(ifOpNode, startingLine);
				possiblePointedVar._conditionNode.addDependentNode(ifOpNode, startingLine);
				
				updateNodeRecursive(possiblePointedVar._varFalse, ifOpNode);
			}
		} else {
			possiblePointedVar._finalVar.receiveAssign(null, node, startingLine);
		}
	}

	@Override
	public GraphNode getFirstNode() {
		return getPointedVar().getFirstNode();
	}
	
	@Override
	public GraphNode getCurrentNode(int startingLine) {
		IVar pointedVar = getPointedVar();
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
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode, int startLocation) {
		// Cria-se um novo nó para a variável "ponteiro"
		GraphNode newNode = super.receiveAssign(lhsType, rhsNode, startLocation);
		_onceWritten = true;

		return newNode;
	}

	@Override
	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine) {
		IVar var = AstLoader.instanceVar(IndirectionType.E_VARIABLE, _name + "_pointed", _type,
				graph, astLoader, astInterpreter);
		var.initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
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
	
	public MemAddressVar updateInternalVars(Map<IVar, IVar> inToExtVar) {
		updateInternalVarsRecursive(_possiblePointedVar, inToExtVar);
		
		return this;
	}
	
	private static void updateInternalVarsRecursive(PossiblePointedVar possiblePointedVar, Map<IVar, IVar> inToExtVar) {
		if(possiblePointedVar == null)
			return;
		
		updateInternalVarsRecursive(possiblePointedVar._varTrue, inToExtVar);
		updateInternalVarsRecursive(possiblePointedVar._varFalse, inToExtVar);
		
		IVar converted = inToExtVar.get(possiblePointedVar._finalVar);
		if(converted != null)
			possiblePointedVar._finalVar = converted; 
	}
	
	@Override
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader, int startingLine) {
		return loadMemberFuncRefRecursive(_possiblePointedVar, memberFunc, parameterValues, graph,
				astLoader, startingLine);
	}
	
	private static GraphNode loadMemberFuncRefRecursive(PossiblePointedVar possiblePointedVar,
			MemberFunc memberFunc, List<FuncParameter> parameterValues, Graph graph,
			AstLoader astLoader, int startingLine) {
		
		if (possiblePointedVar._finalVar == null) {
			GraphNode trueNode = loadMemberFuncRefRecursive(possiblePointedVar._varTrue,
					memberFunc, parameterValues, graph, astLoader, startingLine);
			GraphNode falseNode = loadMemberFuncRefRecursive(possiblePointedVar._varFalse,
					memberFunc, parameterValues, graph, astLoader, startingLine);
			
			
			GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

			trueNode.addDependentNode(ifOpNode, startingLine);
			falseNode.addDependentNode(ifOpNode, startingLine);
			possiblePointedVar._conditionNode.addDependentNode(ifOpNode, startingLine);
			
			return ifOpNode;
		} else {
			ClassVar classVar = (ClassVar) possiblePointedVar._finalVar;
			MemberFunc eqFunc = classVar.getClassDecl().getEquivalentFunc(memberFunc);
			return eqFunc.loadMemberFuncRef(classVar, parameterValues, graph, astLoader,
					startingLine);
		}
	}

}
