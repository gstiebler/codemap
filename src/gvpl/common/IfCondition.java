package gvpl.common;

import gvpl.cdt.InToExtVar;
import gvpl.cdt.PrevTrueFalseMemVar;
import gvpl.cdt.PrevTrueFalseNode;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;
import gvpl.graph.Graph.NodeType;

import java.util.Map;

public class IfCondition {

	/**
	 * Creates the If nodes to the written variables
	 * 
	 * @param ifStatement
	 *            The If statement from CDT
	 * @param mapPrevTrueFalse
	 *            Maps the external variable (the variable in the parent block)
	 *            to a structure that holds the node if the condition is true,
	 *            if the condition is false, and the previous GraphNode of this
	 *            variable
	 * @param ifTrueMergedNodes
	 *            Maps the nodes from the parent block to the True block
	 * @param ifFalseMergedNodes
	 *            Maps the nodes from the parent block to the True block
	 * @param conditionNode
	 *            The condition of the If statement
	 * @param Graph
	 *            The Graph
	 * @param startingLine
	 *            The number of the line in the source code
	 * 
	 */
	public static void createIfNodes(Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<GraphNode, GraphNode> ifTrueMergedNodes,
			Map<GraphNode, GraphNode> ifFalseMergedNodes, GraphNode conditionNode, Graph graph,
			int startingLine) {
		for (Map.Entry<IVar, PrevTrueFalseNode> entry : mapPrevTrueFalse.entrySet()) {
			IVar extVar = entry.getKey();
			PrevTrueFalseNode prevTrueFalse = entry.getValue();

			GraphNode trueNode = prevTrueFalse._true;
			GraphNode falseNode = prevTrueFalse._false;

			// if the variable was not written in the true block, then if the
			// condition is true,
			// the variable will hold it's previous value
			if (trueNode == null)
				trueNode = prevTrueFalse._prev;

			// if the variable was not written in the false block (else), then
			// if the condition is false,
			// the variable will hold it's previous value
			if (falseNode == null)
				falseNode = prevTrueFalse._prev;

			// get the nodes in the current graph, if necessary
			{
				GraphNode newNode = ifTrueMergedNodes.get(trueNode);
				if (newNode != null)
					trueNode = newNode;
			}
			{
				GraphNode newNode = ifFalseMergedNodes.get(falseNode);
				if (newNode != null)
					falseNode = newNode;
			}

			assert (trueNode != null);
			assert (falseNode != null);

			GraphNode ifOpNode = graph.addGraphNode("If", NodeType.E_OPERATION, startingLine);

			trueNode.addDependentNode(ifOpNode, startingLine);
			falseNode.addDependentNode(ifOpNode, startingLine);
			conditionNode.addDependentNode(ifOpNode, startingLine);

			extVar.setGraph(graph);
			extVar.receiveAssign(NodeType.E_VARIABLE, ifOpNode, startingLine);
		}
	}

	/**
	 * Creates if nodes for memory address variables (pointers and references)
	 * that have been referenced in the true and false blocks
	 * 
	 * @param mapPrevTrueFalseMV
	 *            Maps the variable to it's possible assigments. Maps to what
	 *            the variable would have been if the condition is true and if
	 *            the condition is false
	 * @param conditionNode
	 *            The condition of the If statement
	 * @param inToExtVarTrue
	 *            Maps the internal vars, created inside the True block, and the
	 *            correspondents variables in the parent block
	 * @param inToExtVarFalse
	 *            Maps the internal vars, created inside the False block, and the
	 *            correspondents variables in the parent block
	 */
	static public void mergeIfMAV(Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV,
			GraphNode conditionNode, InToExtVar inToExtVarTrue, InToExtVar inToExtVarFalse) {
		for (Map.Entry<IVar, PrevTrueFalseMemVar> entry : mapPrevTrueFalseMV.entrySet()) {
			MemAddressVar extVar = (MemAddressVar) entry.getKey();
			PrevTrueFalseMemVar prevTrueFalse = entry.getValue();

			MemAddressVar trueMAV = prevTrueFalse._true.updateInternalVars(inToExtVarTrue);
			MemAddressVar falseMAV = prevTrueFalse._false.updateInternalVars(inToExtVarFalse);

			if (trueMAV == null)
				trueMAV = prevTrueFalse._prev;

			if (falseMAV == null)
				falseMAV = prevTrueFalse._prev;

			extVar.setIf(conditionNode, trueMAV, falseMAV);
		}
	}
	
}
