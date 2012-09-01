package gvpl.graph;

import gvpl.common.ErrorOutputter;
import gvpl.common.Var;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

public class GraphNode {
	private static int _counter = 1;
	private int _id;
	public String _name;
	public NodeType _type;
	private Var _parentVar = null;
	private List<GraphNode> _sourceNodes = new ArrayList<GraphNode>();
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependentNodes = new ArrayList<GraphNode>();
	private int _startingLine;

	public GraphNode(String name, NodeType type, int startingLine) {
		_id = _counter++;
		_name = name;
		_type = type;
		_startingLine = startingLine;
	}	
	
	public GraphNode(Var parentVar, NodeType type, int startingLine) { 
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
	
	public void addDependentNode(GraphNode dependentNode, int startingLine) {
		if(_dependentNodes.contains(dependentNode))
			ErrorOutputter.fatalError("Already dependent!!");
		
		if(dependentNode == null)
			ErrorOutputter.fatalError("Inserting null depending node");
		
		_dependentNodes.add(dependentNode);
		dependentNode._sourceNodes.add(this);
	}
	
	public Iterable<GraphNode> getDependentNodes() {
		return _dependentNodes;
	}
	
	public Iterable<GraphNode> getSourceNodes() {
		return _sourceNodes;
	}
	
	public boolean hasSourceNodes() {
		boolean result =_sourceNodes.size() > 0;
		return result;
	}
	
	public boolean hasDependentNodes() {
		return _dependentNodes.size() > 0;
	}
	
	public int getNumDependentNodes() {
		return _dependentNodes.size();
	}
	
	public Var getParentVar() {
		return _parentVar;
	}
	
	public void updateDependents(GraphNode oldNode, GraphNode newNode) {
		int oldNodeIndex = _dependentNodes.indexOf(oldNode);
		if(oldNodeIndex != -1)
			_dependentNodes.set(oldNodeIndex, newNode);
	}
	
	public static void resetCounter() {
		_counter = 1;
	}
	
	public int getStartingLine() {
		return _startingLine;
	}
	
}
