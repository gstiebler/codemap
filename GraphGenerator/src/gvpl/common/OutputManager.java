package gvpl.common;

import gvpl.graph.GraphNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
	List<String> _srcFiles = null;
	Map<String, List<String>> _loadedSrcFiles = new HashMap<String, List<String>>();

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
		loadSrcFiles();
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
	
	private String getTreeItemText( GraphNode graphNode ) {
		CodeLocation codeLocation = graphNode.getCodeLocation();
		String text = _loadedSrcFiles.get(codeLocation._fileName).get(codeLocation.getStartingLine() - 1);
		text = text + " - " + codeLocation.getStartingLine();
		return text.trim();
	}
	
	void buildNodesTreeRecursive( GraphNode graphNode, NodesTree nodesTree ) {
		String text = getTreeItemText(graphNode);
		NodesTree localNodesTree = new NodesTree(text);
		nodesTree.children.add(localNodesTree);
		for(GraphNode localGraphNode : graphNode.getSourceNodes()) {
			buildNodesTreeRecursive( localGraphNode, localNodesTree );
		}
	}

	public void setSrcFiles(List<String> srcFiles) {
		_srcFiles = srcFiles;
		for( int i = 0; i < _srcFiles.size(); ++i)
			_srcFiles.set(i, _srcFiles.get(i).replace('\\', '/'));
	}
	
	public void loadSrcFiles() {
		for(String fileName : _srcFiles) {
			_loadedSrcFiles.put(fileName, file2List(fileName));
		}
	}
	
	private List<String> file2List(String fileName) {
		//Create a buffred reader so that you can read in the file
	    BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(new File(fileName)));
			List<String> result = new ArrayList<String>();
		    String line;
			while((line = reader.readLine())!= null)
			{
				result.add(line);
			}
		    
		    reader.close();

		    return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
