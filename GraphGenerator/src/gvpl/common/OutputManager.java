package gvpl.common;

import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OutputManager implements java.io.Serializable {
	
	private static final long serialVersionUID = -2057095081149348325L;

	public class VarInfo implements java.io.Serializable {
		
		private static final long serialVersionUID = -2241742471918330974L;
		public int id;
		public String name;
		
		public VarInfo(int id, String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	public class NodesTree {
		public String text;
		public List<NodesTree> children = new ArrayList<NodesTree>();
		
		public NodesTree( String nodeName ) {
			text = nodeName;
		}
	}
	
	static OutputManager _instance;
	List<VarInfo> _vars = new ArrayList<VarInfo>();
	Map<Integer, Set<GraphNode>> _nodesOfVar = new TreeMap<Integer, Set<GraphNode>>();

	public static void setInstance() {
		_instance = new OutputManager();
	}
	
	public static OutputManager getInstance() {
		return _instance;
	}
	
	public void addVar(IVar var) {
		_vars.add(new VarInfo(var.getId(), var.getName()));
	}
	
	public void addGraphNode(GraphNode node) {
		int parentId = node.getParentVarId();
		Set<GraphNode> nodes = _nodesOfVar.get(new Integer(parentId));
		if(nodes == null) {
			nodes = new HashSet<GraphNode>();
			_nodesOfVar.put(new Integer(parentId), nodes);
		}
		nodes.add(node);
	}
	
	public void saveToFile(String filePath) {
		FileFuncs.saveToFile(this, filePath);
	}
	
	public static OutputManager loadFromFile(String filePath) {
		return (OutputManager) FileFuncs.loadFromFile(filePath);
	}
	
	public List<VarInfo> getVars() {
		return _vars;
	}
	
	public NodesTree buildNodesTree( String varName ) {
		VarInfo selected = null;
		for( VarInfo varInfo : _vars )
			if(varInfo.name.compareTo(varName) == 0)
				selected = varInfo;
		
		Set<GraphNode> graphNodes = _nodesOfVar.get(new Integer(selected.id));
		NodesTree result = new NodesTree("root");
		for(GraphNode graphNode : graphNodes)
			buildNodesTreeRecursive( graphNode, result );
		
		return result;
	}
	
	void buildNodesTreeRecursive( GraphNode graphNode, NodesTree nodesTree ) {
		for(GraphNode localGraphNode : graphNode.getSourceNodes()) {
			String text = graphNode.getName() + " - " + graphNode.getCodeLocation().getStartingLine();
			NodesTree localNodesTree = new NodesTree(text);
			nodesTree.children.add(localNodesTree);
			buildNodesTreeRecursive( localGraphNode, localNodesTree );
		}
	}
	
}
