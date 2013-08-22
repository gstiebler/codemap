package gvpl.graphviz;

import gvpl.common.IVar;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import debug.DebugOptions;

public class FileDriver implements IGraphOutput {

	PrintWriter out;

	static int subGraphCounter = 1;
	static String startingLinesStr = "startinglines";
	
	public class PropertyPair {
		public String _key;
		public String _value;
		
		public PropertyPair(String key, String value) {
			_key = key;
			_value = value;
		}
	}

	public void print(Graph graph, Writer outFile, Visualizer visualizer) {

		out = new PrintWriter(outFile);
		out.println("digraph G\n{");
		out.println("rankdir=LR;");
		out.println("label = \"root\";");
		
		visualizer.print_graph(graph);
		
		out.println("}");
		out.close();
	}
	
	private String srcNodes(GraphNode node) {
		String srcNodes = "\"";
		for(GraphNode src : node.getSourceNodes()) {
			srcNodes += src.getId() + "/";
		}
		srcNodes += "\"";
		return srcNodes;
	}
	
	public void insertOperation(GraphNode node, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("shape", "invtriangle"));
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#E0E0E0\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		if (DebugOptions.printDotSrcNodes() && node.getNumSourceNodes() > 0)
			properties.add(new PropertyPair("srcNodes", srcNodes(node)));
		insertNode(node.getId(), node_name, properties);
	}
	
	public void insertValueNode(int nodeId, String nodeName, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#E9FFE9\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		nodeName = nodeName.replace("\"", "\\\"");
		insertNode(nodeId, nodeName, properties);
	}
	
	public void insertGarbageNode(int node_id, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FF8080\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		insertNode(node_id, node_name, properties);
	}

	public void insertDeclaredParameter(int node_id, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FFE9E9\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertReturnValue(int node_id, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FFFFD0\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertInvalidValue(int node_id, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FFA0A0\""));
		properties.add(new PropertyPair(startingLinesStr, String.valueOf(startingLine)));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertVariable(GraphNode node, String node_name, int startingLine) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("startingline", String.valueOf(startingLine)));
		if(DebugOptions.printDotSrcNodes() && node.getNumSourceNodes() > 0)
			properties.add(new PropertyPair("srcNodes", srcNodes(node)));
		
		if(DebugOptions.printDotSrcVarId()) {
			IVar parentVar = node.getParentVar();
			if(parentVar != null)
				properties.add(new PropertyPair("parentVarId", String.valueOf(parentVar.getId())));
		}
		
		insertNode(node.getId(), node_name, properties);
	}
	
	protected void insertNode(int nodeId, String nodeLabel, List<PropertyPair> properties){
		String internalName = nodeInternalName(nodeId);
		String propertiesString = "";
		for(PropertyPair propertyPair : properties) {
			propertiesString += ", " + propertyPair._key + "=" + propertyPair._value;
		}
		out.println("\t" + internalName + " [ label = \"" + nodeLabel + "\"" + propertiesString + " ]");
	}
	
    public String insertSubGraphStart(String name, String parent, List<Integer> linesHistory) {
    	String clusterName = genClusterName(subGraphCounter++);
    	out.println("subgraph " + clusterName + " {");
    	if(name != null)
    		out.println("label = \"" + name + "\";");
    	out.println("parent = \"" + parent + "\";");
    	String lines = startingLinesStr + " = \"";
    	for(Integer line : linesHistory)
    		lines += line.toString() + "_";
    	lines = lines.substring(0, lines.length() - 1);
    	lines += "\";";
    	
    	out.println(lines);
    	return clusterName;
    }
    
    public static String genClusterName(int counter) {
    	return "cluster_" + counter;
    }
    
	public void insertSubGraphEnd() {
    	out.println("}");
    }
	
	public void insertDependency(int node_id, int dep_node_id){
		String nodeInternalName = nodeInternalName(node_id);
		String depNodeInternalName = nodeInternalName(dep_node_id);
		out.println("\t" + nodeInternalName + " -> " + depNodeInternalName);
	}
	
	public static String nodeInternalName(int node_id) {
		return "node_" + String.format("%06d", node_id);
	}
	
	public static void resetCounter() {
		subGraphCounter = 1;
	}
}
