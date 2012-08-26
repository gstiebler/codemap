package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;

/**
 * Structure that holds variable declaration parameters
 */
public class DirectVarDecl {
	
	protected String _name;
	
	
	protected TypeId _type;
	protected GraphNode _currGraphNode = null;
	protected GraphNode _firstGraphNode = null;
	
	protected Graph _gvplGraph;

	public DirectVarDecl(Graph graph, String name, TypeId type) {
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
	
	public void initializeGraphNode(NodeType type, int startingLine) {
		updateNode(_gvplGraph.addGraphNode(this, type, startingLine));
	}
	
	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode receiveAssign(NodeType lhs_type, GraphNode rhs_node,
			AstLoader astLoader, int startingLine) {
		GraphNode lhs_node = _gvplGraph.addGraphNode(this, lhs_type, startingLine);
		rhs_node.addDependentNode(lhs_node, astLoader, startingLine);
		updateNode(lhs_node);

		return lhs_node;
	}

	public String getName() {
		return _name;
	}
}