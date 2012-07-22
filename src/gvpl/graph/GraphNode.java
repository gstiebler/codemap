package gvpl.graph;

import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
	private static int _counter = 1;
	private int _id;
	public String _name;
	public NodeType _type;
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependent_nodes = new ArrayList<GraphNode>();

	public GraphNode(String name, NodeType type) {
		_id = _counter++;
		_name = name;
		_type = type;
	}
	
	public GraphNode(GraphNode other){
		_id = _counter++;
		_name = other._name;
		_type = other._type;
	}
	
	public int getId(){
		return _id;
	}
	
	public void addDependentNode(GraphNode dependentNode) {
		_dependent_nodes.add(dependentNode);
	}
	
	public Iterable<GraphNode> getDependentNodes() {
		return _dependent_nodes;
	}
	
}
