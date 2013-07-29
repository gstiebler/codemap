package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.ExecTreeLogger;

/**
 * This class is used to represent variables of primitive types
 */
public class Var implements IVar {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private static int _counter = 2000;
	
	protected String _name;

	protected TypeId _type;
	protected GraphNode _currGraphNode = null;
	protected IVar _owner = null;
	private int _id = -1;

	protected Graph _gvplGraph;

	public Var(Graph graph, String name, TypeId type) {
		_type = type;
		_gvplGraph = graph;
		_name = name;
		_id = _counter++;

		logger.debug("New var ({}) {} - Graph {} ({})", _id, _name, graph.getName(), graph.getId());
	}
	
	protected Var(Var other) {
		_currGraphNode = other._currGraphNode;
		_gvplGraph = other._gvplGraph;
		_name = other._name;
		_owner = other._owner;
		_type = other._type;
		_id = _counter++;
	}

	public TypeId getType() {
		return _type;
	}

	public void updateNode(GraphNode node) {
		ExecTreeLogger.log("Var: " + getName() + " node: " + node.getName());

		_currGraphNode = node;
		
		BaseScope currScope = ScopeManager.getCurrentScope();
		currScope.varWritten(this);
	}
	
	public void updateNodes(GraphNode oldNode, GraphNode newNode) {
		ExecTreeLogger.log("Var: " + getName());
		if(_currGraphNode == oldNode)
			_currGraphNode = newNode;
	}

	public GraphNode getCurrentNode() {
		if(_currGraphNode == null)
			updateNode(GraphNode.newGarbageNode(_gvplGraph, _name));
		
		return _currGraphNode;
	}

	public void initializeVar(NodeType nodeType, Graph graph, AstInterpreter astInterpreter) {
		ExecTreeLogger.log("Var: " + getName());
		updateNode(_gvplGraph.addGraphNode(this, nodeType));
	}

	public void callConstructor(List<FuncParameter> parameterValues, NodeType nodeType, Graph graph,
			BaseScope astLoader, AstInterpreter astInterpreter) {
		ExecTreeLogger.log("Var: " + getName());
		if (parameterValues != null) {
			if (parameterValues.size() > 1)
				logger.fatal("Primitive type receiving more than 1 parameter in initialization");

			Value parameterValue = parameterValues.get(0).getValue();
			receiveAssign(NodeType.E_DECLARED_PARAMETER, parameterValue, _gvplGraph);
		} else
			initializeVar(nodeType, graph, astInterpreter);
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

	public String getName() {
		return _name;
	}

	public IVar getVarInMem() {
		ExecTreeLogger.log("Var: " + getName());
		return this;
	}
	
	public void setOwner(IVar owner) {
		ExecTreeLogger.log("Var: " + getName());
		_owner = owner;
	}
	
	public List<IVar> getInternalVars() {
		ExecTreeLogger.log("Var: " + getName());
		List<IVar> internalVars = new ArrayList<>();
		internalVars.add(this);
		return internalVars;
	}
	
	public VarInfo getVarInfo() {
		return new VarInfo(_type, IndirectionType.E_VARIABLE);
	}
	
	public void setGraph(Graph graph) {
		_gvplGraph = graph;
	}
	
	public Graph getGraph() {
		return _gvplGraph;
	}
	
	public int getId() {
		return _id;
	}
	
	@Override
	public String toString() {
		return _name + " (" + _id + ")";
	}
}