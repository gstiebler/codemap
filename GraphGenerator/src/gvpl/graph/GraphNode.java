package gvpl.graph;

import gvpl.common.CodeLocation;
import gvpl.common.OutputManager;
import gvpl.graph.Graph.NodeType;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import debug.DebugOptions;
import debug.ExecTreeLogger;

public class GraphNode implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 272441964510158948L;

	static Logger logger = LogManager.getLogger(Graph.class.getName());
	
	private static int _nodeCounter = 3000; 
	private int _id;
	public String _name;
	public NodeType _type;
	private int _parentVarId = -1;
	private List<GraphNode> _sourceNodes = new ArrayList<GraphNode>();
	/** Lista de nohs das quais este noh depende */
	private List<GraphNode> _dependentNodes = new ArrayList<GraphNode>();
	private int _startingLine = DebugOptions.getStartingLine();
	private Graph _graph = null;
	CodeLocation _codeLoc;

	public GraphNode(String name, NodeType type) {
		_id = getNewId();
		_name = name;
		_type = type;
		_codeLoc = DebugOptions.getCurrCodeLocation();
		OutputManager.getInstance().addGraphNode(this);
		
		logger.info("new graphnode {} ({})", name, _id);
	}

	public GraphNode(String name, NodeType type, int parentVarId) {
		_id = getNewId();
		_parentVarId = parentVarId;
		_name = name;
		_type = type;
		_codeLoc = DebugOptions.getCurrCodeLocation();
		OutputManager.getInstance().addGraphNode(this);
		
		logger.info("new graphnode ({}) var {} ({})", _id, _name, _parentVarId);
	}

	public GraphNode(GraphNode other) {
		_id = getNewId();
		_name = other._name;
		_type = other._type;
		_startingLine = other._startingLine;
		_parentVarId = other._parentVarId;
		_codeLoc = other._codeLoc;

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
	
	public CodeLocation getCodeLocation() {
		return _codeLoc;
	}

	/**
	 * Creates an arrow from "this" to dependentNode
	 * @param dependentNode
	 */
	public void addDependentNode(GraphNode dependentNode) {
		ExecTreeLogger.log("\"" + _name + "\" arrow_right \"" + dependentNode._name + "\"");
		if (_dependentNodes.contains(dependentNode))
		{
			ExecTreeLogger.log("Already dependent!!");
			logger.warn("Already dependent!! {}, {}", this, dependentNode);
			return;
		}

		_dependentNodes.add(dependentNode);
		dependentNode._sourceNodes.add(this);
	}

	public Iterable<GraphNode> getDependentNodes() {
		return _dependentNodes;
	}
	
	public boolean isDependentNode(GraphNode node) {
		return _dependentNodes.contains(node);
	}

	public Iterable<GraphNode> getSourceNodes() {
		return _sourceNodes;
	}

	public GraphNode getSourceNode(int index) {
		return _sourceNodes.get(index);
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
	
	public int getParentVarId() {
		return _parentVarId;
	}
	
	public static GraphNode newGarbageNode(Graph graph, String name) {
		return graph.addGraphNode(name + " (GARBAGE)", NodeType.E_GARBAGE);
	}
	
	@Override
	public String toString() {
		return _name + " (" + _id + ")";
	}

	public Graph getGraph() {
		return _graph;
	}

	public void setGraph(Graph graph) {
		_graph = graph;
	}

}
