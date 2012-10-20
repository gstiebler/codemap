package gvpl.common;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.cdt.MemberFunc;
import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to represent variables of primitive types
 */
public class Var {
	
	protected String _name;

	protected TypeId _type;
	protected GraphNode _currGraphNode = null;
	protected GraphNode _firstGraphNode = null;
	protected Var _owner = null;

	protected Graph _gvplGraph;

	public Var(Graph graph, String name, TypeId type) {
		_type = type;
		_gvplGraph = graph;
		_name = name;
	}

	public TypeId getType() {
		return _type;
	}

	public void updateNode(GraphNode node) {
		if (_currGraphNode == null)
			_firstGraphNode = node;

		_currGraphNode = node;
	}
	
	public void updateNodes(GraphNode oldNode, GraphNode newNode) {
		if(_firstGraphNode == oldNode)
			_firstGraphNode = newNode;
		if(_currGraphNode == oldNode)
			_currGraphNode = newNode;
	}

	public GraphNode getFirstNode() {
		return _firstGraphNode;
	}

	public GraphNode getCurrentNode(int startingLine) {
		return _currGraphNode;
	}

	public void initializeGraphNode(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter, int startingLine) {
		updateNode(_gvplGraph.addGraphNode(this, nodeType, startingLine));
	}

	public void constructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter, int startingLine) {
		if (parameter_values != null) {
			if (parameter_values.size() > 1)
				GeneralOutputter
						.fatalError("Primitive type receiving more than 1 parameter in initialization");

			GraphNode parameterNode = parameter_values.get(0).getNode(startingLine);
			receiveAssign(NodeType.E_DECLARED_PARAMETER, parameterNode, startingLine);
		} else
			initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode, int startingLine) {
		GraphNode lhs_node = _gvplGraph.addGraphNode(this, lhsType, startingLine);
		rhsNode.addDependentNode(lhs_node, startingLine);
		updateNode(lhs_node);

		return lhs_node;
	}

	public String getName() {
		return _name;
	}

	public Var getVarInMem() {
		return this;
	}
	
	public void setOwner(Var owner) {
		_owner = owner;
	}
	
	public List<Var> getOwnersStack() {
		List<Var> stack = new ArrayList<Var>();
		
		Var currVar = this;
		while (currVar != null) {
			stack.add(currVar);
			currVar = currVar._owner;
		}
		return stack;
	}
	
	public List<Var> getInternalVars() {
		List<Var> internalVars = new ArrayList<>();
		internalVars.add(this);
		return internalVars;
	}
	
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_VARIABLE);
	}
	
	public boolean onceRead() {
		return _firstGraphNode.getNumDependentNodes() > 0;
	}
	
	public boolean onceWritten() {
		return _currGraphNode.getNumSourceNodes() > 0;
	}
	
	public void setGraph(Graph graph) {
		_gvplGraph = graph;
	}
	
	public GraphNode loadMemberFuncRef(MemberFunc memberFunc, List<FuncParameter> parameterValues,
			Graph graph, AstLoader astLoader, int startingLine) {
		GeneralOutputter.fatalError("should not be here");
		return null;
	}
}