package gvpl.graphviz;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import gvpl.graph.Graph;

public class FileDriver implements IGraphOutput {

	PrintWriter out;

	static int subGraphCounter = 1;
	
	public class PropertyPair {
		public String _key;
		public String _value;
		
		public PropertyPair(String key, String value) {
			_key = key;
			_value = value;
		}
	}

	public void print(Graph graph, String filename, Visualizer visualizer) {
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out = new PrintWriter(outFile);
		out.println("digraph G\n{");
		out.println("rankdir=LR;");
		out.println("label = \"root\";");
		
		visualizer.print_graph(graph);
		
		out.println("}");
		out.close();
	}
	
	public void insertOperation(int node_id, String node_name) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("shape", "invtriangle"));
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#E0E0E0\""));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertValueNode(int node_id, String node_name) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#E9FFE9\""));
		insertNode(node_id, node_name, properties);
	}

	public void insertDeclaredParameter(int node_id, String node_name) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FFE9E9\""));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertReturnValue(int node_id, String node_name) {
		List<PropertyPair> properties = new ArrayList<PropertyPair>();
		properties.add(new PropertyPair("style", "filled"));
		properties.add(new PropertyPair("fillcolor", "\"#FFFFD0\""));
		insertNode(node_id, node_name, properties);
	}
	
	public void insertVariable(int node_id, String node_name) {
		insertNode(node_id, node_name, new ArrayList<PropertyPair>());
	}
	
	protected void insertNode(int node_id, String nodeLabel, List<PropertyPair> properties){
		String internalName = nodeInternalName(node_id);
		String propertiesString = "";
		for(PropertyPair propertyPair : properties) {
			propertiesString += ", " + propertyPair._key + "=" + propertyPair._value;
		}
		out.println("\t" + internalName + " [ label = \"" + nodeLabel + "\"" + propertiesString + " ]");
	}
	
    public String  insertSubGraphStart(String name, String parent) {
    	String clusterName = "cluster_" + subGraphCounter++;
    	out.println("subgraph " + clusterName + " {");
    	out.println("label = \"" + name + "\";");
    	out.println("parent = \"" + parent + "\";");
    	return clusterName;
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
}
