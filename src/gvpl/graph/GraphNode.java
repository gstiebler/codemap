package gvpl.graph;

import gvpl.common.IVar;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.DebugOptions;

public class GraphNode {
	
	Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private static int _nodeCounter = 3000; 
	private int _id;
	public String _name;
	public NodeType _type;
	private IVar _parentVar = null;
	private List<GraphNode> _sourceNodes = new ArrayList<GraphNode>();
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependentNodes = new ArrayList<GraphNode>();
	private int _startingLine;

	public GraphNode(String name, NodeType type, int startingLine) {
		_id = getNewId();
		_name = name;
		_type = type;
		_startingLine = startingLine;
		
		if(DebugOptions.outputNodeInfo())
			logger.info("new graphnode {} ({})", name, _id);
	}

	public GraphNode(IVar parentVar, NodeType type, int startingLine) {
		_id = getNewId();
		_parentVar = parentVar;
		_name = parentVar.getName();
		_type = type;
		_startingLine = startingLine;

		if(DebugOptions.outputNodeInfo())
			logger.info("new graphnode ({}) var {} ({})", _id, parentVar.getName(), parentVar.getId());
	}

	public GraphNode(GraphNode other) {
		_id = getNewId();
		_name = other._name;
		_type = other._type;
		_startingLine = other._startingLine;
		_parentVar = other._parentVar;

		if(DebugOptions.outputNodeInfo())
		{
			logger.info("new graphnode copy " + _name + " (" + _id + ")");
			logger.info("    from " + other._name + " (" + other._id + ")");
		}
	}
	
	private int getNewId() {
		return _nodeCounter++;
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
			logger.warn("Already dependent!!");
			return;
		}

		if (dependentNode == null)
			logger.fatal("Inserting null depending node");

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
	
	public boolean isDependentNode(GraphNode node) {
		return _dependentNodes.contains(node);
	}

	public List<GraphNode> getSourceNodes() {
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
		_nodeCounter = 1;
	}

	public int getStartingLine() {
		return _startingLine;
	}
	
	public IVar getParentVar() {
		return _parentVar;
	}
	
	@Override
	public String toString() {
		return _name + " (" + _id + ")";
	}

}
