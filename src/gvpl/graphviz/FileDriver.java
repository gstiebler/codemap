package gvpl.graphviz;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import gvpl.graph.Graph;

public class FileDriver extends Visualizer {

	PrintWriter out;
	
	public FileDriver(Graph graph, String filename){
		
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out = new PrintWriter(outFile);
		out.println("digraph G\n{");
		
		print_graph(graph);
		
		out.println("}");
		out.close();
	}

	void insertOperation(int node_id, String node_name) {
		String properties = ", shape=invtriangle, style=filled, fillcolor=\"#E0E0E0\"";
		insertNode(node_id, node_name, properties);
	}
	
	void insertValueNode(int node_id, String node_name) {
		String properties = ", style=filled, fillcolor=\"#E9FFE9\"";
		insertNode(node_id, node_name, properties);
	}
	
	void insertVariable(int node_id, String node_name) {
		insertNode(node_id, node_name, "");
	}
	
	void insertNode(int node_id, String node_name, String properties){
		out.println("\tnode_" + node_id + " [ label = \"" + node_name + "\"" + properties + " ]");
	}
	
	void insertDependency(int node_id, int dep_node_id){
		out.println("\tnode_" + node_id + " -> node_" + dep_node_id);
	}
}
