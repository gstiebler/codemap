package gvpl.cdt;

import gvpl.common.MemAddressVar;
import gvpl.common.IVar;
import gvpl.graph.Graph;
import gvpl.graph.Graph.NodeType;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

class PrevTrueFalseNode {
	GraphNode _prev = null;
	GraphNode _true = null;
	GraphNode _false = null;
}

abstract class BoolValuePack {
	BasicBlock _ifBasicBlock = null;
	Map<GraphNode, GraphNode> _ifMergedNodes = null;
	/** includes all member vars */
	InToExtVar _inToExtVar = null;

	BoolValuePack(InstructionLine instructionLine, IASTStatement clause,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		if (clause == null)
			return;

		int startingLine = clause.getFileLocation().getStartingLineNumber();
		
		_inToExtVar = new InToExtVar(instructionLine.getGraph());

		AstLoader parentBasicBlock = instructionLine.getParentBasicBlock();
		_ifBasicBlock = new BasicBlock(parentBasicBlock, instructionLine.getAstInterpreter());
		_ifBasicBlock.load(clause);

		List<InExtVarPair> ifWrittenVars = new ArrayList<InExtVarPair>();
		// Get the accessed vars inside the block. This functions returns the variables created
		// inside the block, and the equivalent var from the calling block (external vars)
		_ifBasicBlock.getAccessedVars(new ArrayList<InExtVarPair>(), ifWrittenVars,
				new ArrayList<InExtVarPair>(), _inToExtVar, startingLine);
		for (InExtVarPair falseWrittenVarPair : ifWrittenVars) {
			IVar extVar = falseWrittenVarPair._ext;
			GraphNode currExtNode = falseWrittenVarPair._ext.getCurrentNode(startingLine);
			GraphNode currIntNode = falseWrittenVarPair._in.getCurrentNode(startingLine);

			PrevTrueFalseNode prevTrueFalse = mapPrevTrueFalse.get(extVar);
			if (prevTrueFalse == null)
				prevTrueFalse = new PrevTrueFalseNode();
			// the previous value is always the value that the variable was holding 
			// before the if and else blocks
			prevTrueFalse._prev = currExtNode;
			// the new value depends if it's an true or false (else) block
			insertBoolNode(prevTrueFalse, currIntNode);
			mapPrevTrueFalse.put(extVar, prevTrueFalse);
		}

		// the list of all pointers and reference variables
		List<InExtMAVarPair> addressVars = _ifBasicBlock.getAccessedMemAddressVar();
		for (InExtMAVarPair pair : addressVars) {
			PrevTrueFalseMemVar prevTrueFalse = mapPrevTrueFalseMV.get(pair._ext);
			if (prevTrueFalse == null)
				prevTrueFalse = new PrevTrueFalseMemVar();
			prevTrueFalse._prev = pair._ext;
			insertBoolVar(prevTrueFalse, pair._in);
			mapPrevTrueFalseMV.put(prevTrueFalse._prev, prevTrueFalse);
		}

		_ifMergedNodes = _ifBasicBlock.addToExtGraph(startingLine);
	}

	abstract void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node);
	abstract void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var);
}

class trueClass extends BoolValuePack {

	trueClass(InstructionLine instructionLine, IASTStatement clause,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		super(instructionLine, clause, mapPrevTrueFalse, mapPrevTrueFalseMV);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		prevTrueFalse._true = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		prevTrueFalse._true = var;
	}
}

class falseClass extends BoolValuePack {

	falseClass(InstructionLine instructionLine, IASTStatement clause,
			Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV) {
		super(instructionLine, clause, mapPrevTrueFalse, mapPrevTrueFalseMV);
	}

	void insertBoolNode(PrevTrueFalseNode prevTrueFalse, GraphNode node) {
		prevTrueFalse._false = node;
	}

	void insertBoolVar(PrevTrueFalseMemVar prevTrueFalse, MemAddressVar var) {
		prevTrueFalse._false = var;
	}
}

public class IfCondition {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<IVar, PrevTrueFalseNode>();
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();

		BoolValuePack trueBvp = new trueClass(instructionLine, ifStatement.getThenClause(),
				mapPrevTrueFalse, mapPrevTrueFalseMV);
		BoolValuePack falseBvp = new falseClass(instructionLine, ifStatement.getElseClause(),
				mapPrevTrueFalse, mapPrevTrueFalseMV);

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		createIfNodes(mapPrevTrueFalse, trueBvp._ifMergedNodes, falseBvp._ifMergedNodes,
				conditionNode, instructionLine.getGraph(), ifStatement.getFileLocation()
						.getStartingLineNumber());

		mergeIfMAV(mapPrevTrueFalseMV, conditionNode, trueBvp._inToExtVar, falseBvp._inToExtVar);
	}

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
	static void createIfNodes(Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse,
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
