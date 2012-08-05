package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.common.ErrorOutputter;
import gvpl.common.VarDecl;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
	private static int _counter = 1;
	private int _id;
	public String _name;
	public NodeType _type;
	private VarDecl _parentVar = null;
	private List<GraphNode> _sourceNodes = new ArrayList<GraphNode>();
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependent_nodes = new ArrayList<GraphNode>();

	public GraphNode(String name, NodeType type) {
		_id = _counter++;
		_name = name;
		_type = type;
	}	
	
	public GraphNode(VarDecl parentVar, NodeType type) { 
		_id = _counter++;
		_parentVar = parentVar;
		_name = parentVar.getName();
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
	
	public String getName() {
		return _name;
	}
	
	public void addDependentNode(GraphNode dependentNode, AstLoader astLoader) {
		if(_dependent_nodes.contains(dependentNode))
			ErrorOutputter.fatalError("Already dependent!!");
		
		if(dependentNode == null)
			ErrorOutputter.fatalError("Inserting null depending node");
		
		_dependent_nodes.add(dependentNode);
		dependentNode._sourceNodes.add(this);
		
		if(astLoader == null)
			return;
		
		if(_parentVar != null)
			astLoader.varRead(_parentVar);
		
		if(dependentNode._parentVar != null)
			astLoader.varWrite(dependentNode._parentVar);
	}
	
	public Iterable<GraphNode> getDependentNodes() {
		return _dependent_nodes;
	}
	
	public Iterable<GraphNode> getSourceNodes() {
		return _sourceNodes;
	}
	
	public void updateDependents(GraphNode oldNode, GraphNode newNode) {
		int oldNodeIndex = _dependent_nodes.indexOf(oldNode);
		if(oldNodeIndex != -1)
			_dependent_nodes.set(oldNodeIndex, newNode);
	}
	
}
