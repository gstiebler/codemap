package tests;

import gvpl.graph.GraphNode;
import gvpl.graphviz.FileDriver;
import gvpl.graphviz.FileDriver.PropertyPair;
import gvpl.graphviz.Visualizer;
import static org.junit.Assert.*;

public class GraphCompare {

	/**
	 * Returns true if the graphs are equal
	 * 
	 * @param gvplGraph
	 * @param gvGraph
	 * @return True if the graphs are equal
	 */
	static boolean isEqual(gvpl.graph.Graph gvplGraph, org.cesta.parsers.dot.DotTree.Graph gvGraph) {

		FileDriverTests fileDriver = new FileDriverTests();

		if (gvplGraph.getNumNodes() != gvGraph.getNodes().size())
			fail("Number of nodes are different");

		int numNodes = gvplGraph.getNumNodes();
		for (int i = 0; i < numNodes; i++) {
			GraphNode gvplNode = gvplGraph.getNode(i);
			String nodeInternalName = FileDriver.nodeInternalName(gvplNode.getId());
			org.cesta.parsers.dot.DotTree.Node gvNode = gvGraph.getNode(nodeInternalName);
			if (gvNode == null)
				fail("Node " + nodeInternalName + "does not exist");

			Visualizer.printNode(gvplNode, fileDriver);
			
			String gvNodeLabel = gvNode.getAttribute("label");
			if(gvNodeLabel == null)
				fail("Node " + nodeInternalName + " not found");
			gvNodeLabel = gvNodeLabel.replace("\"", "");
			if(!gvplNode.getName().equals(gvNodeLabel))
				fail("Node label " + gvplNode.getName() + " does not match.");

			for (PropertyPair propertyPair : fileDriver._properties) {
				String value = gvNode.getAttribute(propertyPair._key);
				if (!propertyPair._value.equals(value)) {
					fail("Different properties \"" + propertyPair._value + "\" and \"" + value
							+ "\" in node " + nodeInternalName);
				}
			}
		}

		fail("something");
		return true;
	}

}
