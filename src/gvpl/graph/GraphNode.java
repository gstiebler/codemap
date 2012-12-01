package gvpl.graph;

import gvpl.common.CodeLocation;
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
	private CodeLocation _codeLocation = DebugOptions.getCurrentCodeLocation();

	public GraphNode(String name, NodeType type) {
		_id = getNewId();
		_name = name;
		_type = type;
		
		logger.info("new graphnode {} ({})", name, _id);
	}

	public GraphNode(IVar parentVar, NodeType type) {
		_id = getNewId();
		_parentVar = parentVar;
		_name = parentVar.getName();
		_type = type;

		logger.info("new graphnode ({}) var {} ({})", _id, parentVar.getName(), parentVar.getId());
	}

	public GraphNode(GraphNode other) {
		_id = getNewId();
		_name = other._name;
		_type = other._type;
		_codeLocation = other._codeLocation;
		_parentVar = other._parentVar;

		logger.info("new graphnode copy " + _name + " (" + _id + ")");
		logger.info("    from " + other._name + " (" + other._id + ")");
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

	public void addDependentNode(GraphNode dependentNode) {
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
	
	public void merge(GraphNode node) {
		for(GraphNode dependentNode : node._dependentNodes) {
			addDependentNode(dependentNode);
			dependentNode._sourceNodes.remove(node);
			dependentNode._sourceNodes.add(this);
		}
		
		for(GraphNode sourceNode : node._sourceNodes) {
			sourceNode._dependentNodes.remove(node);
			sourceNode.addDependentNode(this);
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

	public CodeLocation getCodeLocation() {
		return _codeLocation;
	}
	
	public IVar getParentVar() {
		return _parentVar;
	}
	
	public static GraphNode newGarbageNode(Graph graph, String name) {
		return graph.addGraphNode(name + " (GARBAGE)", NodeType.E_GARBAGE);
	}
	
	@Override
	public String toString() {
		return _name + " (" + _id + ")";
	}

}
