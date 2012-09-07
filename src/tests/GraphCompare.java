package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gvpl.graph.GraphNode;
import gvpl.graphviz.FileDriver.PropertyPair;
import gvpl.graphviz.Visualizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cesta.parsers.dot.DotTree;
import org.cesta.parsers.dot.DotTree.NodePair;

public class GraphCompare {
	
	/**
	 * Returns true if the graphs are equal
	 * 
	 * @param gvplGraph
	 * @param gvGraph
	 * @return True if the graphs are equal
	 */
	static boolean isEqual(gvpl.graph.Graph gvplGraph, DotTree.Graph gvGraph) {
		Map<String, Set<String>> gvEdges = getEdges(gvGraph);
		SubGraphNodes processedNodes = ProcessedNodes.process(gvGraph);
		List<NodeMatch> completeNodesMatch = analyseSubGraph(gvplGraph, processedNodes);
		compareEdges(gvplGraph, gvEdges, completeNodesMatch);
		return true;
	}

	static List<NodeMatch> analyseSubGraph(gvpl.graph.Graph gvplGraph, SubGraphNodes processedNodes) {
		List<NodeMatch> result = new ArrayList<NodeMatch>();
		
		assertEquals("Number of subgraphs of " + gvplGraph.getName(), gvplGraph._subgraphs.size(),
				processedNodes._subGraphs.size());
		for (gvpl.graph.Graph gvplSubGraph : gvplGraph._subgraphs) {
			IdLineKey key = new IdLineKey(gvplSubGraph.getName(), gvplSubGraph.getStartingLine());
			SubGraphNodes gvSubGraphNodes = processedNodes._subGraphs.get(key);
			List<NodeMatch> sgNodesMatch = analyseSubGraph(gvplSubGraph, gvSubGraphNodes);
			result.addAll(sgNodesMatch);
		}

		FileDriverTests fileDriver = new FileDriverTests();
		
		//assertEquals("Number of nodes", processedNodes._nodes.size(), gvplGraph.getNumNodes());
		
		List<NodeMatch> nodesMatch = matchNodes(processedNodes._nodes, gvplGraph);
		result.addAll(nodesMatch);
		for (NodeMatch nodeMatch : nodesMatch) {
			GraphNode gvplNode = nodeMatch._gvplNode;
			DotTree.Node gvNode = nodeMatch._gvNode;

			Visualizer.printNode(gvplNode, fileDriver);
			String gvNodeLabel = gvNode.getAttribute("label");

			gvNodeLabel = gvNodeLabel.replace("\"", "");
			assertEquals("Node label", gvplNode.getName(), gvNodeLabel);

			for (PropertyPair propertyPair : fileDriver._properties) {
				String value = gvNode.getAttribute(propertyPair._key);
				assertEquals("Property " + propertyPair._key, propertyPair._value, value);
			}
		}
		
		return result;
	}
	
	static void compareEdges(gvpl.graph.Graph gvplGraph, 
			Map<String, Set<String>> gvEdges, List<NodeMatch> completeNodesMatch) {
		
		Map<GraphNode, DotTree.Node> gvplToGv = new HashMap<GraphNode, DotTree.Node>();

		for (NodeMatch nodeMatch : completeNodesMatch) {
			gvplToGv.put(nodeMatch._gvplNode, nodeMatch._gvNode);
		}
		
		for (NodeMatch nodeMatch : completeNodesMatch) {
			GraphNode gvplNode = nodeMatch._gvplNode;
			DotTree.Node gvNode = nodeMatch._gvNode;	

			int numNodeEdges = gvplNode.getNumDependentNodes();
			Set<String> gvNodeEdges = gvEdges.get(gvNode.id);
			assertEquals("Number of edges of node " + gvNode.id, numNodeEdges,
					gvNodeEdges.size());
			
			for (GraphNode gvplDepNode : gvplNode.getDependentNodes()) {
				DotTree.Node gvDepNode = gvplToGv.get(gvplDepNode);

				assertTrue(gvNodeEdges.contains(gvDepNode.id));
			}
		}
		
	}
	
	static List<NodeMatch> matchNodes(Map<String, LinkedList<DotTree.Node>> gvNodes, gvpl.graph.Graph gvplGraph) {
		List<NodeMatch> result = new LinkedList<NodeMatch>();
		
		int numNodes = gvplGraph.getNumNodes();
		for (int i = 0; i < numNodes; i++) {
			GraphNode gvplNode = gvplGraph.getNode(i);
			String label = gvplNode.getName();
			
			LinkedList<DotTree.Node> list = gvNodes.get(label);
			DotTree.Node gvNode = list.getFirst();
			list.removeFirst();
			
			result.add(new NodeMatch(gvplNode, gvNode));
		}
		
		return result;
	}

	static Map<String, Set<String>> getEdges(DotTree.Graph gvGraph) {
		Map<String, Set<String>> edges = new HashMap<String, Set<String>>();

		for (DotTree.Node gvNode : gvGraph.getNodes()) {
			edges.put(gvNode.id, new HashSet<String>());
		}

		for (DotTree.Edge edge : gvGraph.getEdges()) {
			String node1, node2;
			List<NodePair> nodePairs = edge.getNodePairs();
			NodePair nodePair = nodePairs.get(0);
			node1 = nodePair.x.id;
			node2 = nodePair.y.id;

			edges.get(node1).add(node2);
		}

		return edges;
	}
}
