package gvpl.cdt;

import gvpl.cdt.AstLoader.InExtMAVarPair;
import gvpl.cdt.AstLoader.InExtVarPair;
import gvpl.common.MemAddressVar;
import gvpl.common.Var;
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

class PrevTrueFalseMemVar {
	MemAddressVar _prev = null;
	MemAddressVar _true = null;
	MemAddressVar _false = null;
}

class BoolValuePack {
	BasicBlock _ifBasicBlock = null;
	List<InExtVarPair> _ifWrittenVars = new ArrayList<InExtVarPair>();
	Map<GraphNode, GraphNode> _ifMergedNodes = null;
	Map<Var, Var> _inToExtVar = new LinkedHashMap<Var, Var>();
	
	BoolValuePack(InstructionLine instructionLine) {
		AstLoader parentBasicBlock = instructionLine.getParentBasicBlock();
		_ifBasicBlock = new BasicBlock(parentBasicBlock, instructionLine.getAstInterpreter());
	}
}

public class IfCondition {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		AstLoader parentBasicBlock = instructionLine.getParentBasicBlock();

		Map<Var, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<Var, PrevTrueFalseNode>();
		Map<Var, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<Var, PrevTrueFalseMemVar>();
		
		BoolValuePack trueBvp = new BoolValuePack(instructionLine);
		BoolValuePack falseBvp = new BoolValuePack(instructionLine);
		
		{
			IASTStatement thenClause = ifStatement.getThenClause();
			int startingLine = thenClause.getFileLocation().getStartingLineNumber();

			trueBvp._ifBasicBlock.load(thenClause);

			trueBvp._ifBasicBlock.getAccessedVars(new ArrayList<InExtVarPair>(), trueBvp._ifWrittenVars,
					new ArrayList<InExtVarPair>(), trueBvp._inToExtVar, startingLine);
			for (InExtVarPair trueWrittenVarPair : trueBvp._ifWrittenVars) {
				Var extVar = trueWrittenVarPair._ext;
				GraphNode currExtNode = trueWrittenVarPair._ext.getCurrentNode(startingLine);
				GraphNode currIntNode = trueWrittenVarPair._in.getCurrentNode(startingLine);
				PrevTrueFalseNode prevTrueFalse = new PrevTrueFalseNode();
				prevTrueFalse._prev = currExtNode;
				prevTrueFalse._true = currIntNode;
				mapPrevTrueFalse.put(extVar, prevTrueFalse);
			}

			List<InExtMAVarPair> trueAddressVars = trueBvp._ifBasicBlock.getAccessedMemAddressVar();
			for (InExtMAVarPair pair : trueAddressVars) {
				PrevTrueFalseMemVar prevTrueFalse = new PrevTrueFalseMemVar();
				prevTrueFalse._prev = pair._ext;
				prevTrueFalse._true = pair._in;
				mapPrevTrueFalseMV.put(pair._ext, prevTrueFalse);
			}

			trueBvp._ifMergedNodes = trueBvp._ifBasicBlock.addToExtGraph(startingLine);
		}

		IASTStatement elseClause = ifStatement.getElseClause();
		if (elseClause != null) {
			int startingLine = elseClause.getFileLocation().getStartingLineNumber();

			falseBvp._ifBasicBlock.load(elseClause);

			falseBvp._ifWrittenVars = new ArrayList<InExtVarPair>();
			falseBvp._ifBasicBlock.getAccessedVars(new ArrayList<InExtVarPair>(), falseBvp._ifWrittenVars,
					new ArrayList<InExtVarPair>(), falseBvp._inToExtVar, startingLine);
			for (InExtVarPair falseWrittenVarPair : falseBvp._ifWrittenVars) {
				Var extVar = falseWrittenVarPair._ext;
				GraphNode currExtNode = falseWrittenVarPair._ext.getCurrentNode(startingLine);
				GraphNode currIntNode = falseWrittenVarPair._in.getCurrentNode(startingLine);

				PrevTrueFalseNode prevTrueFalse = mapPrevTrueFalse.get(extVar);
				if (prevTrueFalse == null)
					prevTrueFalse = new PrevTrueFalseNode();
				prevTrueFalse._prev = currExtNode;
				prevTrueFalse._false = currIntNode;
				mapPrevTrueFalse.put(extVar, prevTrueFalse);
			}

			List<InExtMAVarPair> falseAddressVars = falseBvp._ifBasicBlock.getAccessedMemAddressVar();
			for (InExtMAVarPair pair : falseAddressVars) {
				PrevTrueFalseMemVar prevTrueFalse = mapPrevTrueFalseMV.get(pair._ext);
				if (prevTrueFalse == null)
					prevTrueFalse = new PrevTrueFalseMemVar();
				prevTrueFalse._prev = pair._ext;
				prevTrueFalse._false = pair._in;
				mapPrevTrueFalseMV.put(pair._ext, prevTrueFalse);
			}

			falseBvp._ifMergedNodes = falseBvp._ifBasicBlock.addToExtGraph(startingLine);
		}

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		createIfNodes(ifStatement, mapPrevTrueFalse, trueBvp._ifMergedNodes, falseBvp._ifMergedNodes,
				conditionNode, instructionLine);

		mergeIfMAV(mapPrevTrueFalseMV, conditionNode, trueBvp._inToExtVar, falseBvp._inToExtVar);
	}

	static void createIfNodes(IASTIfStatement ifStatement,
			Map<Var, PrevTrueFalseNode> mapPrevTrueFalse,
			Map<GraphNode, GraphNode> ifTrueMergedNodes,
			Map<GraphNode, GraphNode> ifFalseMergedNodes, GraphNode conditionNode,
			InstructionLine instructionLine) {

		int startingLine = ifStatement.getFileLocation().getStartingLineNumber();
		Graph graph = instructionLine.getGraph();
		for (Map.Entry<Var, PrevTrueFalseNode> entry : mapPrevTrueFalse.entrySet()) {
			Var extVar = entry.getKey();
			PrevTrueFalseNode prevTrueFalse = entry.getValue();

			GraphNode trueNode = prevTrueFalse._true;
			GraphNode falseNode = prevTrueFalse._false;

			if (trueNode == null)
				trueNode = prevTrueFalse._prev;

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

			extVar.receiveAssign(NodeType.E_VARIABLE, ifOpNode, startingLine);
		}
	}

	// TODO convert internal to external vars
	static void mergeIfMAV(Map<Var, PrevTrueFalseMemVar> mapPrevTrueFalseMV,
			GraphNode conditionNode, Map<Var, Var> inToExtVarTrue, Map<Var, Var> inToExtVarFalse) {
		for (Map.Entry<Var, PrevTrueFalseMemVar> entry : mapPrevTrueFalseMV.entrySet()) {
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
