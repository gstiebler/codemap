package gvpl.common;

import java.util.ArrayList;
import java.util.List;

import gvpl.cdt.AstInterpreter;
import gvpl.cdt.AstLoader;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

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
				ErrorOutputter
						.fatalError("Primitive type receiving more than 1 parameter in initialization");

			GraphNode parameterNode = parameter_values.get(0).getNode(startingLine);
			receiveAssign(NodeType.E_DECLARED_PARAMETER, parameterNode, astLoader, startingLine);
		} else
			initializeGraphNode(nodeType, graph, astLoader, astInterpreter, startingLine);
	}

	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhs_type, GraphNode rhs_node, AstLoader astLoader,
			int startingLine) {
		GraphNode lhs_node = _gvplGraph.addGraphNode(this, lhs_type, startingLine);
		rhs_node.addDependentNode(lhs_node, astLoader, startingLine);
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
}