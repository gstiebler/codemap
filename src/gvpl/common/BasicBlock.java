package gvpl.common;

import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BasicBlock {
	
	/**
	 * Add the nodes of the internal graph to the external graph
	 * @param startingLine
	 * @return Maps the nodes that were merged with others. The nodes in the key
	 *         of the map no longer exists.
	 */
	public static Map<GraphNode, GraphNode> addToExtGraph(Graph extGraph, AstLoader astLoader, int startingLine) {
		Map<GraphNode, GraphNode> mergedNodes = new LinkedHashMap<GraphNode, GraphNode>();
		
		List<InExtVarPair> readVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> writtenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ignoredVars = new ArrayList<InExtVarPair>();
		astLoader.getAccessedVars(readVars, writtenVars, ignoredVars, new InToExtVar(extGraph), startingLine);
		extGraph.merge(astLoader.getGraph());

		// bind the vars from calling block to the internal read vars
		for(InExtVarPair readPair : readVars) {
			GraphNode intVarFirstNode = readPair._in.getFirstNode();
			// if someone read from internal var
			GraphNode extVarCurrNode = readPair._ext.getCurrentNode(startingLine);
			extGraph.mergeNodes(extVarCurrNode, intVarFirstNode, startingLine);
			// connect the var from the calling block to the correspodent var in this block
			mergedNodes.put(intVarFirstNode, extVarCurrNode);
		}
		
		// bind the vars from calling block to the internal written vars
		for (InExtVarPair writtenPair : writtenVars) {
			GraphNode intVarCurrNode = writtenPair._in.getCurrentNode(startingLine);
			// if someone has written in the internal var

			writtenPair._ext.initializeVar(NodeType.E_VARIABLE, extGraph,
					astLoader, astLoader.getAstInterpreter(), startingLine);
			GraphNode extVarCurrNode = writtenPair._ext
					.getCurrentNode(startingLine);
			// connect the var from the calling block to the correspodent var in this block
			extGraph.mergeNodes(extVarCurrNode, intVarCurrNode, startingLine);
			mergedNodes.put(intVarCurrNode, extVarCurrNode);
		}
		
		for(InExtVarPair ignoredPair : ignoredVars) {
			extGraph.removeNode(ignoredPair._in.getFirstNode());
		}
		
		return mergedNodes;
	}
}
