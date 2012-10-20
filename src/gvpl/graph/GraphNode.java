package gvpl.graph;

import gvpl.common.GeneralOutputter;
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
		
		GeneralOutputter.debug("new graphnode " + name + " (" + _id + ")");
	}

	public GraphNode(Var parentVar, NodeType type, int startingLine) {
		_id = _counter++;
		_parentVar = parentVar;
		_name = parentVar.getName();
		_type = type;
		_startingLine = startingLine;
		
		GeneralOutputter.debug("new graphnode var " + parentVar.getName() + " (" + _id + ")");
	}

	public GraphNode(GraphNode other) {
		_id = _counter++;
		_name = other._name;
		_type = other._type;
		_startingLine = other._startingLine;
		_parentVar = other._parentVar;
		
		GeneralOutputter.debug("new graphnode copy " + _name + " (" + _id + ")");
		GeneralOutputter.debug("    from " + other._name + " (" + other._id + ")");
	}

	public int getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public void addDependentNode(GraphNode dependentNode, int startingLine) {
		if (_dependentNodes.contains(dependentNode))
		{
			GeneralOutputter.warning("Already dependent!!");
			return;
		}

		if (dependentNode == null)
			GeneralOutputter.fatalError("Inserting null depending node");

		_dependentNodes.add(dependentNode);
		dependentNode._sourceNodes.add(this);
	}
	
	public void merge(GraphNode node, int startingLine) {
		for(GraphNode dependentNode : node._dependentNodes) {
			addDependentNode(dependentNode, startingLine);
			dependentNode._sourceNodes.remove(node);
			dependentNode._sourceNodes.add(this);
		}
		
		for(GraphNode sourceNode : node._sourceNodes) {
			sourceNode._dependentNodes.remove(node);
			sourceNode.addDependentNode(this, startingLine);
		}
		
		node._parentVar.updateNodes(node, this);
	}

	public Iterable<GraphNode> getDependentNodes() {
		return _dependentNodes;
	}

	public Iterable<GraphNode> getSourceNodes() {
		return _sourceNodes;
	}

	public int getNumDependentNodes() {
		return _dependentNodes.size();
	}

	public int getNumSourceNodes() {
		return _sourceNodes.size();
	}

	public void updateDependents(GraphNode oldNode, GraphNode newNode) {
		int oldNodeIndex = _dependentNodes.indexOf(oldNode);
		if (oldNodeIndex != -1)
			_dependentNodes.set(oldNodeIndex, newNode);
	}

	public static void resetCounter() {
		_counter = 1;
	}

	public int getStartingLine() {
		return _startingLine;
	}

}
