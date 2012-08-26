package gvpl.graph;

import gvpl.cdt.AstLoader;
import gvpl.common.DirectVarDecl;
import gvpl.common.ErrorOutputter;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
	private static int _counter = 1;
	private int _id;
	public String _name;
	public NodeType _type;
	private DirectVarDecl _parentVar = null;
	private List<GraphNode> _sourceNodes = new ArrayList<GraphNode>();
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependent_nodes = new ArrayList<GraphNode>();
	private int _startingLine;

	public GraphNode(String name, NodeType type, int startingLine) {
		_id = _counter++;
		_name = name;
		_type = type;
		_startingLine = startingLine;
	}	
	
	public GraphNode(DirectVarDecl parentVar, NodeType type, int startingLine) { 
		_id = _counter++;
		_parentVar = parentVar;
		_name = parentVar.getName();
		_type = type;
		_startingLine = startingLine;
	}
	
	public GraphNode(GraphNode other){
		_id = _counter++;
		_name = other._name;
		_type = other._type;
		_startingLine = other._startingLine;
	}
	
	public int getId(){
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public void addDependentNode(GraphNode dependentNode, AstLoader astLoader, int startingLine) {
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
			astLoader.varWrite(dependentNode._parentVar, startingLine);
	}
	
	public Iterable<GraphNode> getDependentNodes() {
		return _dependent_nodes;
	}
	
	public Iterable<GraphNode> getSourceNodes() {
		return _sourceNodes;
	}
	
	public int getNumDependentNodes() {
		return _dependent_nodes.size();
	}
	
	public void updateDependents(GraphNode oldNode, GraphNode newNode) {
		int oldNodeIndex = _dependent_nodes.indexOf(oldNode);
		if(oldNodeIndex != -1)
			_dependent_nodes.set(oldNodeIndex, newNode);
	}
	
	public static void resetCounter() {
		_counter = 1;
	}
	
	public int getStartingLine() {
		return _startingLine;
	}
	
}
