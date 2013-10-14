package gvpl.common;

import gvpl.cdt.AstInterpreterCDT;
import gvpl.cdt.function.MemberFunc;
import gvpl.common.ifclasses.IfScope;
import gvpl.common.ifclasses.IfScope.eIfScopeKind;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.ExecTreeLogger;

public class PossiblePointedVar implements IVar, IClassVar, java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2844409348993680741L;

	static Logger logger = LogManager.getLogger(Graph.class.getName());

	public PossiblePointedVar _varTrue = null;
	public PossiblePointedVar _varFalse = null;
	public GraphNode _conditionNode = null;
	/** If it's a final variable, i.e. there isn't a if condition associated with this PossiblePointedVar, 
	 * then _finalVar holds the actual variable pointed by this PossiblePointedVar */
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

	/**
	 * Makes the tree of ifs for the possible pointed var
	 * @param finalVar
	 */
	void setVar(IVar finalVar) {
		ExecTreeLogger.log("Var: " + finalVar.getName());

		if(finalVar instanceof PointerVar)
			finalVar = ((PointerVar) finalVar).getPointedVar();
		
		LinkedList<IfScope> ifList = new LinkedList<IfScope>();
		List<BaseScope> scopes = ScopeManager.getScopeList();
		
		// fill the ifs between the current scope and the scope of the variable
		for(int i = scopes.size() - 1; i >= 0; --i) {
			BaseScope scope = scopes.get(i);
			if(scope.hasVarInScope(_ownerVar))
				break;
			
			if(scope instanceof IfScope)
				ifList.addFirst((IfScope) scope);
		}
		
		if(ifList.size() == 0) {
			_finalVar = finalVar;
			_conditionNode = null;
			return;
		}
		
		PossiblePointedVar currPPV = this;
		for(IfScope ifScope : ifList) {
			if(ifScope.getConditionNode() == currPPV._conditionNode) {
				if(ifScope.getKind() == eIfScopeKind.E_THEN) {
					PossiblePointedVar oldPPV = new PossiblePointedVar(currPPV._ownerVar);
					oldPPV._finalVar = finalVar;
					currPPV._varTrue = oldPPV;
					currPPV = currPPV._varTrue;
				} else {
					PossiblePointedVar oldPPV = new PossiblePointedVar(currPPV._ownerVar);
					oldPPV._finalVar = finalVar;
					currPPV._varFalse = oldPPV;
					currPPV = currPPV._varFalse;
				}
				continue;
			}

			PossiblePointedVar ppvOld = new PossiblePointedVar(currPPV);
			currPPV._conditionNode = ifScope.getConditionNode();
			currPPV._finalVar = null;
			PossiblePointedVar nextPPV = new PossiblePointedVar(currPPV._ownerVar);
			nextPPV._finalVar = finalVar;
			if(ifScope.getKind() == eIfScopeKind.E_THEN) {
				currPPV._varTrue = nextPPV;
				currPPV._varFalse = ppvOld;
			} else {
				currPPV._varTrue = ppvOld;
				currPPV._varFalse = nextPPV;
			}
			currPPV = nextPPV;
		}
	}

	void setPossibleVars(GraphNode conditionNode, PossiblePointedVar varTrue,
			PossiblePointedVar varFalse) {
		ExecTreeLogger.log("Var: " + getName());
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
		ExecTreeLogger.log("Var: " + getName());
		updateNodeRecursive(this, _ownerVar.getGraph(), node);
	}
	
	public static PossiblePointedVar filterPPVInsideIfScopes(PossiblePointedVar possiblePointedVar) {
		while(true) {
			eIfScopeKind ifKind = IfScope.getScopeKind(possiblePointedVar._conditionNode);
			if(ifKind == null)
				break;
			else if (ifKind == eIfScopeKind.E_THEN)
				possiblePointedVar = possiblePointedVar._varTrue;
			else if (ifKind == eIfScopeKind.E_ELSE)
				possiblePointedVar = possiblePointedVar._varFalse;
		}
		
		return possiblePointedVar;
	}

	public static void updateNodeRecursive(PossiblePointedVar possiblePointedVar, Graph graph,
			GraphNode node) {
		possiblePointedVar = filterPPVInsideIfScopes(possiblePointedVar);
		
		if (possiblePointedVar._conditionNode != null) {
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varTrue);
			updateNodeInternal(possiblePointedVar, graph, node, possiblePointedVar._varFalse);
		} else {
			try {
				possiblePointedVar._finalVar.receiveAssign(NodeType.E_VARIABLE, new Value(node), graph);
			} catch(Exception e) {
				logger.error("Final var shouldn't be null. {}. Msg: {}", possiblePointedVar.getName(), e.getMessage());
			}
		}
	}

	private static void updateNodeInternal(PossiblePointedVar possiblePointedVar, Graph graph,
			GraphNode node, PossiblePointedVar trueOrFalse) {
		ExecTreeLogger.log("Var: " + possiblePointedVar.getName());
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

	public String getName() {
		return _ownerVar.getName();
	}
	
	boolean nullPointer() {
		return _finalVar == null && _varFalse == null && _varTrue == null;
	}

	public static Value loadMemberFuncRefRecursive(PossiblePointedVar possiblePointedVar,
			MemberFunc memberFunc, List<FuncParameter> parameterValues, Graph graph,
			BaseScope astLoader) {
		if(possiblePointedVar.nullPointer()) {
			logger.error("not properly implemented");
			String nodeName = "NULL_POINTER_" + possiblePointedVar.getName();
			GraphNode problemGraphNode = graph.addGraphNode(nodeName, NodeType.E_INVALID_NODE_TYPE);
			return new Value(problemGraphNode);
		}
		
		if (possiblePointedVar._finalVar == null) {
			Value trueValue = loadMemberFuncRefRecursive(possiblePointedVar._varTrue,
					memberFunc, parameterValues, graph, astLoader);
			Value falseValue = loadMemberFuncRefRecursive(possiblePointedVar._varFalse,
					memberFunc, parameterValues, graph, astLoader);
			if(trueValue == null || falseValue == null) {
				logger.error("trueValue and falseValue must be valid");
				GraphNode problemGraphNode = graph.addGraphNode("PROBLEM_NODE", NodeType.E_INVALID_NODE_TYPE);
				return new Value(problemGraphNode);
			}
			GraphNode trueNode = trueValue.getNode();
			GraphNode falseNode = falseValue.getNode();

			GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION);

			trueNode.addDependentNode(ifOpNode);
			falseNode.addDependentNode(ifOpNode);
			possiblePointedVar._conditionNode.addDependentNode(ifOpNode);

			return new Value(ifOpNode);
		} else if (possiblePointedVar._finalVar instanceof ClassVar){
			ClassVar classVar = (ClassVar) possiblePointedVar._finalVar;
			MemberFunc eqFunc = classVar.getClassDecl().getEquivalentFunc(memberFunc);
			if(eqFunc == null) {
				logger.error("eqFunc must not be null");
				return new Value( graph.addGraphNode("PROBLEM_NODE", NodeType.E_INVALID_NODE_TYPE));
			}
			return eqFunc.addFuncRef(parameterValues, graph, classVar, astLoader);
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

	public void initializeVar(NodeType nodeType, Graph graph, AstInterpreterCDT astInterpreter) {
		logger.fatal("You're doing it wrong.");
	}

	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType,
			Graph graph, BaseScope astLoader, AstInterpreterCDT astInterpreter) {
		logger.fatal("You're doing it wrong.");
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, Value rhsValue, Graph graph) {
		ExecTreeLogger.log("Var: " + getName());
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

	public List<IVar> getInternalVars() {
		logger.fatal("You're doing it wrong.");
		return null;
	}

	public void setGraph(Graph graph) {
		logger.fatal("You're doing it wrong.");
	}

	public int getId() {
		return -1;
	}
}