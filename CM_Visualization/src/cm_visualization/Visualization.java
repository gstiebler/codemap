package cm_visualization;

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
		generateDOT(args[0], "");
	}
	
	public static void generateDOT(String graphFileName, String openNode) {
		Graph graph = Graph.loadFromFile(graphFileName);
		
		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		Visualizer visualizer = new Visualizer(fileDriver);

		int indexOfPoint = graphFileName.lastIndexOf('.');
		String outputDotFileName = graphFileName.substring(0, indexOfPoint) + ".dot";
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
