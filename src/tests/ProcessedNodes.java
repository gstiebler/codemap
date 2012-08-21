package tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cesta.parsers.dot.DotTree;

public class ProcessedNodes {

	public static SubGraphNodes process(DotTree.Graph gvGraph) {
		Map<String, DotTree.Node> nodesByName = new HashMap<String, DotTree.Node>();
		nodesByName.putAll(gvGraph.nodes);

		Map<String, List<DotTree.SubGraph>> subGraphsByParent = processSubGraphsByParent(gvGraph.subGraphs);
		
		SubGraphNodes graphNodes = new SubGraphNodes();
		processRecursive(gvGraph, nodesByName, graphNodes, subGraphsByParent);

		return graphNodes;
	}

	/**
	 * Process the subgraphs recursively
	 * @param gvGraph The subgraph read from dot file
	 * @param nodesByName Hash map from node names to nodes. It gets reduced from each subgraph it time this function is called
	 * @param graphNodes The nodes from this subgraph. It is filled in this function
	 * @param subGraphsByParent Maps the name of the the graph to a list of it's children
	 */
	private static void processRecursive(DotTree.Graph gvGraph,
			Map<String, DotTree.Node> nodesByName, SubGraphNodes graphNodes, Map<String, List<DotTree.SubGraph>> subGraphsByParent) {
		processSubGraphs(gvGraph, nodesByName, graphNodes, subGraphsByParent);

		LinkedList<DotTree.Node> intersection = getIntersection(nodesByName, gvGraph.nodes);
		
		// at this point, the nodes in the graph contains only the nodes in this
		// current graph,
		// not containing nodes in the subgraphs.
		processNodesFromCurrentGraph(intersection, graphNodes);

		// Now we'll delete the nodes from this current graph
		for (Map.Entry<String, DotTree.Node> entry : gvGraph.nodes.entrySet()) {
			nodesByName.remove(entry.getKey());
		}
	}

	private static void processSubGraphs(DotTree.Graph gvGraph,
			Map<String, DotTree.Node> nodesByName, SubGraphNodes graphNodes,
			Map<String, List<DotTree.SubGraph>> subGraphsByParent) {
		List<DotTree.SubGraph> subGraphs = subGraphsByParent.get(gvGraph.getLabel());
		if(subGraphs == null)
			return;
		for (DotTree.SubGraph subGraph : subGraphs) {
			SubGraphNodes subGraphNodes = new SubGraphNodes();
			subGraphNodes._name = subGraph.getLabel();
			subGraphNodes._startingLine = subGraph.getStartingLine();
			processRecursive(subGraph, nodesByName, subGraphNodes, subGraphsByParent);
			NameLineKey key = new NameLineKey(subGraphNodes._name, subGraphNodes._startingLine);
			graphNodes._subGraphs.put(key, subGraphNodes);
		}
	}

	private static void processNodesFromCurrentGraph(LinkedList<DotTree.Node> nodesByName,
			SubGraphNodes graphNodes) {
		for (DotTree.Node gvNode : nodesByName) {
			String label = gvNode.attributes.get("label").replace("\"", "");

			if (!graphNodes._nodes.containsKey(label))
				graphNodes._nodes.put(label, new LinkedList<DotTree.Node>());

			graphNodes._nodes.get(label).add(gvNode);
		}

		orderNodesFromVariable(graphNodes);
	}

	@SuppressWarnings("unchecked")
	private static void orderNodesFromVariable(SubGraphNodes graphNodes) {
		for (Map.Entry<String, LinkedList<DotTree.Node>> entry : graphNodes._nodes.entrySet()) {
			List<DotTree.Node> list = entry.getValue();
			Collections.sort(list);
		}
	}

	private static Map<String, List<DotTree.SubGraph>> processSubGraphsByParent(Set<DotTree.SubGraph> allSubGraphs) {
		Map<String, List<DotTree.SubGraph>> result = new HashMap<String, List<DotTree.SubGraph>>();
		
		for (DotTree.SubGraph subGraph : allSubGraphs) {
			String parentName = subGraph.attributes.get("parent").replace("\"", "");
			
			if(!result.containsKey(parentName)) {
				result.put(parentName, new ArrayList<DotTree.SubGraph>());
			}
			
			result.get(parentName).add(subGraph);
		}
		
		for (List<DotTree.SubGraph> subGraphs : result.values()) {
			Collections.sort(subGraphs);
		}
		
		return result;
	}
	
	private static LinkedList<DotTree.Node> getIntersection(Map<String, DotTree.Node> nodesByName, Map<String, DotTree.Node> nodesInGraph) {
		LinkedList<DotTree.Node> result = new LinkedList<DotTree.Node>();
		
		Set<String> intersection = new HashSet<String>();
		intersection.addAll(nodesByName.keySet());
		Set<String> nodes2 = nodesInGraph.keySet();
		
		intersection.retainAll(nodes2);
		
		for(String nodeName : intersection)  {
			result.add(nodesByName.get(nodeName));
		}
		
		return result;
	}
}
