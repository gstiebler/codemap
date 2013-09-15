package gvpl;

import java.io.FileWriter;
import java.io.IOException;

import gvpl.graph.Graph;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.Visualizer;

public class Visualization {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inputGraphFileName = args[0];
		Graph graph = Graph.loadFromFile(inputGraphFileName);
		
		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);

		int indexOfPoint = inputGraphFileName.lastIndexOf('.');
		String outputDotFileName = inputGraphFileName.substring(0, indexOfPoint) + ".dot";
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(outputDotFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		fileDriver.print(graph, outFile, visualizer);
	}

}
