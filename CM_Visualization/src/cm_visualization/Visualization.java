package cm_visualization;

import gvpl.graph.Graph;
import gvpl.graphviz.FileDriver;

import java.io.FileWriter;
import java.io.IOException;

public class Visualization {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String openNode = "";
		if(args.length > 1)
			openNode = args[1];
		generateDOT(args[0], openNode);
	}
	
	public static void generateDOT(String graphFileName, String openNode) {
		Graph graph = Graph.loadFromFile(graphFileName);

		int indexOfPoint = graphFileName.lastIndexOf('.');
		String baseFileName = graphFileName.substring(0, indexOfPoint);
		
		String visibleNodesListFileName = baseFileName + "_visible_nodes.xml";
		FileDriver fileDriver = new gvpl.graphviz.FileDriver();
		VisualizerFilter visualizer = new VisualizerFilter(fileDriver, visibleNodesListFileName);
		
		if(openNode.compareTo("") != 0) {
			String strId = openNode.split("_")[1];
			int id = Integer.parseInt(strId);
			visualizer.addGraphNode(graph, id);
		}

		String outputDotFileName = baseFileName + ".dot";
		FileWriter outFile = null;
		try {
			outFile = new FileWriter(outputDotFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		fileDriver.print(graph, outFile, visualizer);
		
		visualizer.saveVisibleNodesToFile(visibleNodesListFileName);
	}

}
