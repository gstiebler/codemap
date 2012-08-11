package gvpl.common;

import gvpl.cdt.AstLoader;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphBuilder.TypeId;
import gvpl.graph.GraphNode;

public abstract class VarDecl {
	protected TypeId _type;
	protected GraphNode _currGraphNode = null;
	protected GraphNode _firstGraphNode = null;
	
	protected Graph _gvplGraph;

	public VarDecl(TypeId type, Graph graph) {
		_type = type;
		_gvplGraph = graph;
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
	
	public GraphNode getCurrentNode() {
		return _currGraphNode;
	}
	
	public void initializeGraphNode(NodeType type) {
		updateNode(_gvplGraph.addGraphNode(this, type));
	}
	
	/**
	 * Creates an assignment to this variable
	 * 
	 * @return New node from assignment, the left from assignment
	 */
	public GraphNode addAssign(NodeType lhs_type, GraphNode rhs_node,
			AstLoader astLoader) {
		GraphNode lhs_node = _gvplGraph.addGraphNode(this, lhs_type);
		rhs_node.addDependentNode(lhs_node, astLoader);
		updateNode(lhs_node);

		return lhs_node;
	}
	
	abstract public String getName();
}
