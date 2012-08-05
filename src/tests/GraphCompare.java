package tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cesta.parsers.dot.DotTree.NodePair;

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
		Map<String, Set<String>> gvEdges = getEdges(gvGraph);
		analyseSubGraph(gvplGraph, gvGraph, gvEdges);
		
		return true;
	}
	
	static void analyseSubGraph(gvpl.graph.Graph gvplGraph, org.cesta.parsers.dot.DotTree.Graph gvGraph, Map<String, Set<String>> gvEdges) {
		Map<String, org.cesta.parsers.dot.DotTree.Graph> subGraphs = subGraphsByLabel(gvGraph);
		assertEquals("Number of subgraphs of " + gvplGraph.getName(), gvplGraph._subgraphs.size(), subGraphs.size());
		for(gvpl.graph.Graph gvplSubGraph : gvplGraph._subgraphs) {
			org.cesta.parsers.dot.DotTree.Graph gvSubGraph = subGraphs.get(gvplSubGraph.getName());
			analyseSubGraph(gvplSubGraph, gvSubGraph, gvEdges);
		}
		
		FileDriverTests fileDriver = new FileDriverTests();
		int numNodes = gvplGraph.getNumNodes();
		int numGvNodes = gvGraph.getNodes().size();
		//assertEquals("Number of nodes", numNodes, numGvNodes);
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

			int numNodeEdges = gvplNode.getNumDependentNodes();
			Set<String> gvNodeEdges = gvEdges.get(nodeInternalName);
			assertEquals("Number of edges of node " + nodeInternalName, numNodeEdges,
					gvNodeEdges.size());
			for (GraphNode depNode : gvplNode.getDependentNodes()) {
				String depNodeInternalName = FileDriver.nodeInternalName(depNode.getId());
				String msg = "Edge not found. Node " + nodeInternalName + " dep node "
						+ depNodeInternalName;
				assertTrue(msg, gvNodeEdges.contains(depNodeInternalName));
			}
		}
	}

	static Map<String, Set<String>> getEdges(org.cesta.parsers.dot.DotTree.Graph gvGraph) {
		Map<String, Set<String>> edges = new HashMap<String, Set<String>>();

		for (org.cesta.parsers.dot.DotTree.Node gvNode : gvGraph.getNodes()) {
			edges.put(gvNode.id, new HashSet<String>());
		}

		for (org.cesta.parsers.dot.DotTree.Edge edge : gvGraph.getEdges()) {
			String node1, node2;
			List<NodePair> nodePairs = edge.getNodePairs();
			NodePair nodePair = nodePairs.get(0);
			node1 = nodePair.x.id;
			node2 = nodePair.y.id;

			edges.get(node1).add(node2);
		}

		return edges;
	}
	
	static Map<String, org.cesta.parsers.dot.DotTree.Graph> subGraphsByLabel(org.cesta.parsers.dot.DotTree.Graph gvGraph) {
		Map<String, org.cesta.parsers.dot.DotTree.Graph> subGraphs = new HashMap<String, org.cesta.parsers.dot.DotTree.Graph>();
		
		for(org.cesta.parsers.dot.DotTree.Graph subGraph : gvGraph.subGraphs) {
			String label = subGraph.attributes.get("label").replace("\"", "");
			subGraphs.put(label, subGraph);
		}
		
		return subGraphs;
	}
}
