package gvpl.common;

import gvpl.cdt.InToExtVar;
import gvpl.cdt.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

public class PossiblePointedVar implements IVar, IClassVar {
	public PossiblePointedVar _varTrue = null;
	public PossiblePointedVar _varFalse = null;
	public GraphNode _conditionNode = null;
	public IVar _finalVar = null;
	IVar _ownerVar = null;

	PossiblePointedVar(IVar ownerVar) {
		_ownerVar = ownerVar;
	}

	void setVar(IVar finalVar) {
		_finalVar = finalVar;
		_conditionNode = null;
	}

	void setPossibleVars(GraphNode conditionNode, PossiblePointedVar varTrue,
			PossiblePointedVar varFalse) {
		_conditionNode = conditionNode;
		_varTrue = varTrue;
		_varFalse = varFalse;
		_finalVar = null;
	}
	
	GraphNode getIfNode(Graph graph, int startingLine) {
		if(_conditionNode == null)
			return _finalVar.getCurrentNode(startingLine);
			
		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

		_varTrue.getIfNode(graph, startingLine).addDependentNode(ifOpNode, startingLine);
		_varFalse.getIfNode(graph, startingLine).addDependentNode(ifOpNode, startingLine);
		_conditionNode.addDependentNode(ifOpNode, startingLine);
		
		return ifOpNode;
	}
	
	
	public TypeId getType() {
		return _ownerVar.getType();
	}
	
	public void updateNode(GraphNode node) {
		updateNodeRecursive(this, _ownerVar.getGraph(), node);
	}

	public static void updateNodeRecursive(PossiblePointedVar possiblePointedVar, Graph graph, GraphNode node) {
		int startingLine = -4;
		if(possiblePointedVar._conditionNode != null) {
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varTrue);
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varFalse);
		} else {
			possiblePointedVar._finalVar.receiveAssign(NodeType.E_VARIABLE, node, startingLine);
		}
	}
	
	private static void updateNodeInternal(PossiblePointedVar possiblePointedVar, Graph graph,
			GraphNode node, PossiblePointedVar trueOrFalse) {
		int startingLine = -5;
		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

		node.addDependentNode(ifOpNode, startingLine);
		trueOrFalse._finalVar.getCurrentNode(startingLine).addDependentNode(
				ifOpNode, startingLine);
		possiblePointedVar._conditionNode.addDependentNode(ifOpNode, startingLine);

		updateNodeRecursive(trueOrFalse, graph, ifOpNode);
	}
	
	public IVar getMember(MemberId memberId) {
		return getMemberRecursive(this, memberId);
	}
	
	private static PossiblePointedVar getMemberRecursive(PossiblePointedVar ppv, MemberId memberId) {
		if (ppv._conditionNode != null) {
			PossiblePointedVar ppvTrue = getMemberRecursive(ppv._varTrue, memberId);
			PossiblePointedVar ppvFalse = getMemberRecursive(ppv._varFalse, memberId);
			PossiblePointedVar result = new PossiblePointedVar(ppv);
			result.setPossibleVars(ppv._conditionNode, ppvTrue, ppvFalse);
			return result;
		}
		
		PossiblePointedVar result = new PossiblePointedVar(ppv._finalVar);
		IClassVar finalClassVar =  (IClassVar) ppv._finalVar;
		result._finalVar = finalClassVar.getMember(memberId);
		return result;
	}

	public GraphNode getFirstNode() {
		return _finalVar.getFirstNode();
	}

	public String getName() {
		return _ownerVar.getName();
	}

	public static void updateInternalVarsRecursive(PossiblePointedVar possiblePointedVar, InToExtVar inToExtVar) {
		if(possiblePointedVar == null)
			return;
		
		updateInternalVarsRecursive(possiblePointedVar._varTrue, inToExtVar);
		updateInternalVarsRecursive(possiblePointedVar._varFalse, inToExtVar);

		IVar converted = inToExtVar.get(possiblePointedVar._finalVar);
		if (converted == null) {
			//It's used when a variable was allocated with the new operator inside a block
			possiblePointedVar._finalVar.setGraph(inToExtVar.getExtGraph());
			GeneralOutputter.debug("Var " + possiblePointedVar._finalVar.getName() + " ("
					+ possiblePointedVar._finalVar.getId() + ")" + " is now on graph "
					+ inToExtVar.getExtGraph().getName() + " (" + inToExtVar.getExtGraph().getId()
					+ ")");
		} else
			possiblePointedVar._finalVar = converted;
		
	}
	
	public static GraphNode loadMemberFuncRefRecursive(PossiblePointedVar possiblePointedVar,
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

	public GraphNode getCurrentNode(int startingLine) {
		if(_finalVar == null) {
			return getIfNode(_ownerVar.getGraph(), startingLine);
		}
		GraphNode currentPointedVarNode = _finalVar.getCurrentNode(startingLine);
		//if (currentPointedVarNode != _lastPointedVarNode) {
			GraphNode currGraphNode = _ownerVar.getGraph().addGraphNode(this, NodeType.E_VARIABLE, startingLine);
			currentPointedVarNode.addDependentNode(currGraphNode, startingLine);
		//}
		return currGraphNode;
	}

	public void initializeVar(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine) {
		GeneralOutputter.fatalError("You're doing it wrong.");
	}

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {
		GeneralOutputter.fatalError("You're doing it wrong.");
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode, int startingLine) {
		GraphNode lhsNode = _ownerVar.getGraph().addGraphNode(this, lhsType, startingLine);
		rhsNode.addDependentNode(lhsNode, startingLine);
		updateNode(lhsNode);

		return lhsNode;
	}
	
	public VarInfo getVarInfo() {
		return _ownerVar.getVarInfo();
	}
	
	public Graph getGraph() {
		return _ownerVar.getGraph();
	}
	
	public void updateNodes(GraphNode oldNode, GraphNode newNode) {
		GeneralOutputter.fatalError("You're doing it wrong.");
	}

	public IVar getVarInMem() {
		GeneralOutputter.fatalError("You're doing it wrong.");
		return null;
	}
	
	public void setOwner(IVar owner) {
		GeneralOutputter.fatalError("You're doing it wrong.");
	}
	
	public List<IVar> getInternalVars() {
		GeneralOutputter.fatalError("You're doing it wrong.");
		return null;
	}
	
	public boolean onceRead() {
		GeneralOutputter.fatalError("You're doing it wrong.");
		return false;
	}
	
	public boolean onceWritten() {
		GeneralOutputter.fatalError("You're doing it wrong.");
		return false;
	}
	
	public void setGraph(Graph graph) {
		GeneralOutputter.fatalError("You're doing it wrong.");
	}
	
	public int getId() {
		return -1;
	}
}