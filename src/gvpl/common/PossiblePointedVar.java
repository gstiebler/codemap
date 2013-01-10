package gvpl.common;

import gvpl.cdt.function.MemberFunc;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PossiblePointedVar implements IVar, IClassVar {

	static Logger logger = LogManager.getLogger(Graph.class.getName());

	public PossiblePointedVar _varTrue = null;
	public PossiblePointedVar _varFalse = null;
	public GraphNode _conditionNode = null;
	public IVar _finalVar = null;
	IVar _ownerVar = null;

	PossiblePointedVar(IVar ownerVar) {
		_ownerVar = ownerVar;
	}

	PossiblePointedVar(PossiblePointedVar other) {
		_varTrue = other._varTrue;
		_varFalse = other._varFalse;
		_conditionNode = other._conditionNode;
		_finalVar = other._finalVar;
		_ownerVar = other._ownerVar;
	}

	public void delete() {
		_varTrue = null;
		_varFalse = null;
		_conditionNode = null;
		_finalVar = null;
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

	GraphNode getIfNode(Graph graph) {
		if (_conditionNode == null){
			if(_finalVar == null)
				return GraphNode.newGarbageNode(graph, "INVALID_READ");
			return _finalVar.getCurrentNode();
		}

		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION);

		_varTrue.getIfNode(graph).addDependentNode(ifOpNode);
		_varFalse.getIfNode(graph).addDependentNode(ifOpNode);
		_conditionNode.addDependentNode(ifOpNode);

		return ifOpNode;
	}

	public TypeId getType() {
		return _ownerVar.getType();
	}

	public void updateNode(GraphNode node) {
		updateNodeRecursive(this, _ownerVar.getGraph(), node);
	}

	public static void updateNodeRecursive(PossiblePointedVar possiblePointedVar, Graph graph,
			GraphNode node) {
		if (possiblePointedVar._conditionNode != null) {
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varTrue);
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varFalse);
		} else {
			possiblePointedVar._finalVar.receiveAssign(NodeType.E_VARIABLE, new Value(node), graph);
		}
	}

	private static void updateNodeInternal(PossiblePointedVar possiblePointedVar, Graph graph,
			GraphNode node, PossiblePointedVar trueOrFalse) {
		GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION);

		node.addDependentNode(ifOpNode);
		trueOrFalse._finalVar.getCurrentNode().addDependentNode(ifOpNode);
		possiblePointedVar._conditionNode.addDependentNode(ifOpNode);

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
		if(ppv._finalVar.getName() == "NULL") {
			result._finalVar = ppv._finalVar;
		} else {
			IClassVar finalClassVar = (IClassVar) ppv._finalVar;
			result._finalVar = finalClassVar.getMember(memberId);
		}
		return result;
	}

	public GraphNode getFirstNode() {
		return _finalVar.getFirstNode();
	}

	public String getName() {
		return _ownerVar.getName();
	}

	public static void updateInternalVarsRecursive(PossiblePointedVar possiblePointedVar,
			InToExtVar inToExtVar) {
		if (possiblePointedVar == null)
			return;

		updateInternalVarsRecursive(possiblePointedVar._varTrue, inToExtVar);
		updateInternalVarsRecursive(possiblePointedVar._varFalse, inToExtVar);

		IVar converted = inToExtVar.get(possiblePointedVar._finalVar);
		if (converted == null) {
			// It's used when a variable was allocated with the new operator
			// inside a block
			possiblePointedVar._finalVar.setGraph(inToExtVar.getExtGraph());
			logger.debug("Var {} ({}) is now on graph {} ({})", inToExtVar.getExtGraph().getName(),
					inToExtVar.getExtGraph().getId(), possiblePointedVar._finalVar.getName(),
					possiblePointedVar._finalVar.getId());
		} else
			possiblePointedVar._finalVar = converted;

	}
	
	boolean nullPointer() {
		return _finalVar == null && _varFalse == null && _varTrue == null;
	}

	public static Value loadMemberFuncRefRecursive(PossiblePointedVar possiblePointedVar,
			MemberFunc memberFunc, List<FuncParameter> parameterValues, Graph graph,
			AstLoader astLoader) {
		if(possiblePointedVar.nullPointer()) {
			logger.error("not properly implemented");
			return new Value();
		}
		
		if (possiblePointedVar._finalVar == null) {
			GraphNode trueNode = loadMemberFuncRefRecursive(possiblePointedVar._varTrue,
					memberFunc, parameterValues, graph, astLoader).getNode();
			GraphNode falseNode = loadMemberFuncRefRecursive(possiblePointedVar._varFalse,
					memberFunc, parameterValues, graph, astLoader).getNode();

			GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION);

			trueNode.addDependentNode(ifOpNode);
			falseNode.addDependentNode(ifOpNode);
			possiblePointedVar._conditionNode.addDependentNode(ifOpNode);

			return new Value(ifOpNode);
		} else if (possiblePointedVar._finalVar instanceof ClassVar){
			ClassVar classVar = (ClassVar) possiblePointedVar._finalVar;
			MemberFunc eqFunc = classVar.getClassDecl().getEquivalentFunc(memberFunc);
			return eqFunc.addFuncRef(parameterValues, graph, classVar);
		} else {
			logger.error("possiblePointedVar._finalVar is {}", possiblePointedVar._finalVar.getClass());
		}
		return null;
	}

	public GraphNode getCurrentNode() {
		if (_finalVar == null) {
			return getIfNode(_ownerVar.getGraph());
		}
		GraphNode currentPointedVarNode = _finalVar.getCurrentNode();
		// if (currentPointedVarNode != _lastPointedVarNode) {
		GraphNode currGraphNode = _ownerVar.getGraph().addGraphNode(this, NodeType.E_VARIABLE);
		currentPointedVarNode.addDependentNode(currGraphNode);
		// }
		return currGraphNode;
	}

	public void initializeVar(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter) {
		logger.fatal("You're doing it wrong.");
	}

	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, AstLoader astLoader, AstInterpreter astInterpreter) {
		logger.fatal("You're doing it wrong.");
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, Value rhsValue, Graph graph) {
		GraphNode lhsNode = graph.addGraphNode(this, lhsType);
		rhsValue.getNode().addDependentNode(lhsNode);
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
		logger.fatal("You're doing it wrong.");
	}

	public IVar getVarInMem() {
		logger.fatal("You're doing it wrong.");
		return null;
	}

	public void setOwner(IVar owner) {
		logger.fatal("You're doing it wrong.");
	}

	public List<IVar> getInternalVars() {
		logger.fatal("You're doing it wrong.");
		return null;
	}

	public boolean onceRead() {
		logger.fatal("You're doing it wrong.");
		return false;
	}

	public boolean onceWritten() {
		logger.fatal("You're doing it wrong.");
		return false;
	}

	public void setGraph(Graph graph) {
		logger.fatal("You're doing it wrong.");
	}

	public int getId() {
		return -1;
	}
}