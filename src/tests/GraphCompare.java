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
		int numNodes = gvplGraph.getNumNodes();
		int numGvNodes = gvGraph.getNodes().size();
		assertEquals("Number of nodes", numNodes, numGvNodes);

		for (int i = 0; i < numNodes; i++) {
			GraphNode gvplNode = gvplGraph.getNode(i);
			String nodeInternalName = FileDriver.nodeInternalName(gvplNode.getId());
			org.cesta.parsers.dot.DotTree.Node gvNode = gvGraph.getNode(nodeInternalName);	
			assertNotNull("Node: " + nodeInternalName, gvNode);

			Visualizer.printNode(gvplNode, fileDriver);
			String gvNodeLabel = gvNode.getAttribute("label");
			assertNotNull("Node: " + nodeInternalName, gvNodeLabel);
			
			gvNodeLabel = gvNodeLabel.replace("\"", "");
			assertEquals("Node label", gvplNode.getName(), gvNodeLabel);

			for (PropertyPair propertyPair : fileDriver._properties) {
				String value = gvNode.getAttribute(propertyPair._key);
				assertEquals("Property " + propertyPair._key, propertyPair._value, value);
			}
		}
		return true;
	}

}
