package gvpl.common;

import gvpl.common.FuncParameter.IndirectionType;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used to represent variables of primitive types
 */
public class Var implements IVar {
	
	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private static int _counter = 2000;
	
	protected String _name;

	protected TypeId _type;
	protected GraphNode _currGraphNode = null;
	protected GraphNode _firstGraphNode = null;
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

	public GraphNode getCurrentNode() {
		return _currGraphNode;
	}

	public void initializeVar(NodeType nodeType, Graph graph, AstLoader astLoader,
			AstInterpreter astInterpreter) {
		updateNode(_gvplGraph.addGraphNode(this, nodeType));
	}

	public void callConstructor(List<FuncParameter> parameter_values, NodeType nodeType, Graph graph,
			AstLoader astLoader, AstInterpreter astInterpreter) {
		if (parameter_values != null) {
			if (parameter_values.size() > 1)
				logger.fatal("Primitive type receiving more than 1 parameter in initialization");

			GraphNode parameterNode = parameter_values.get(0).getNode();
			receiveAssign(NodeType.E_DECLARED_PARAMETER, parameterNode);
		} else
			initializeVar(nodeType, graph, astLoader, astInterpreter);
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhsType, GraphNode rhsNode) {
		GraphNode lhsNode = _gvplGraph.addGraphNode(this, lhsType);
		rhsNode.addDependentNode(lhsNode);
		updateNode(lhsNode);

		return lhsNode;
	}

	public String getName() {
		return _name;
	}

	public IVar getVarInMem() {
		return this;
	}
	
	public void setOwner(IVar owner) {
		_owner = owner;
	}
	
	public List<IVar> getInternalVars() {
		List<IVar> internalVars = new ArrayList<>();
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