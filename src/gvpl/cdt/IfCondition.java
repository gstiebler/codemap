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

public class IfCondition {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		AstLoader parentBasicBlock = instructionLine.getParentBasicBlock();

		Map<Var, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<Var, PrevTrueFalseNode>();
		Map<Var, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<Var, PrevTrueFalseMemVar>();

		BasicBlock ifTrueBB = new BasicBlock(parentBasicBlock, instructionLine.getAstInterpreter());
		BasicBlock ifFalseBB = null;

		List<InExtVarPair> ifTrueWrittenVars = new ArrayList<InExtVarPair>();
		List<InExtVarPair> ifFalseWrittenVars = null;

		Map<GraphNode, GraphNode> ifTrueMergedNodes = null;
		Map<GraphNode, GraphNode> ifFalseMergedNodes = null;
		{
			IASTStatement thenClause = ifStatement.getThenClause();
			int startingLine = thenClause.getFileLocation().getStartingLineNumber();

			ifTrueBB.load(thenClause);

			ifTrueBB.getAccessedVars(new ArrayList<InExtVarPair>(), ifTrueWrittenVars,
					new ArrayList<InExtVarPair>(), startingLine);
			for (InExtVarPair trueWrittenVarPair : ifTrueWrittenVars) {
				Var extVar = trueWrittenVarPair._ext;
				GraphNode currExtNode = trueWrittenVarPair._ext.getCurrentNode(startingLine);
				GraphNode currIntNode = trueWrittenVarPair._in.getCurrentNode(startingLine);
				PrevTrueFalseNode prevTrueFalse = new PrevTrueFalseNode();
				prevTrueFalse._prev = currExtNode;
				prevTrueFalse._true = currIntNode;
				mapPrevTrueFalse.put(extVar, prevTrueFalse);
			}

			List<InExtMAVarPair> trueAddressVars = ifTrueBB.getAccessedMemAddressVar();
			for (InExtMAVarPair pair : trueAddressVars) {
				PrevTrueFalseMemVar prevTrueFalse = new PrevTrueFalseMemVar();
				prevTrueFalse._prev = pair._ext;
				prevTrueFalse._true = pair._in;
				mapPrevTrueFalseMV.put(pair._ext, prevTrueFalse);
			}

			ifTrueMergedNodes = ifTrueBB.addToExtGraph(startingLine);
		}

		IASTStatement elseClause = ifStatement.getElseClause();
		if (elseClause != null) {
			int startingLine = elseClause.getFileLocation().getStartingLineNumber();

			ifFalseBB = new BasicBlock(parentBasicBlock, instructionLine.getAstInterpreter());
			ifFalseBB.load(elseClause);

			ifFalseWrittenVars = new ArrayList<InExtVarPair>();
			ifFalseBB.getAccessedVars(new ArrayList<InExtVarPair>(), ifFalseWrittenVars,
					new ArrayList<InExtVarPair>(), startingLine);
			for (InExtVarPair falseWrittenVarPair : ifFalseWrittenVars) {
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

			List<InExtMAVarPair> falseAddressVars = ifFalseBB.getAccessedMemAddressVar();
			for (InExtMAVarPair pair : falseAddressVars) {
				PrevTrueFalseMemVar prevTrueFalse = mapPrevTrueFalseMV.get(pair._ext);
				if (prevTrueFalse == null)
					prevTrueFalse = new PrevTrueFalseMemVar();
				prevTrueFalse._prev = pair._ext;
				prevTrueFalse._false = pair._in;
				mapPrevTrueFalseMV.put(pair._ext, prevTrueFalse);
			}

			ifFalseMergedNodes = ifFalseBB.addToExtGraph(startingLine);
		}

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		createIfNodes(ifStatement, mapPrevTrueFalse, ifTrueMergedNodes, ifFalseMergedNodes,
				conditionNode, instructionLine);
	
		mergeIfMAV(mapPrevTrueFalseMV, conditionNode);
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

	//TODO convert internal to external vars
	static void mergeIfMAV(Map<Var, PrevTrueFalseMemVar> mapPrevTrueFalseMV, GraphNode conditionNode) {
		for (Map.Entry<Var, PrevTrueFalseMemVar> entry : mapPrevTrueFalseMV.entrySet()) {
			MemAddressVar extVar = (MemAddressVar) entry.getKey();
			PrevTrueFalseMemVar prevTrueFalse = entry.getValue();

			MemAddressVar trueMAV = prevTrueFalse._true;
			MemAddressVar falseMAV = prevTrueFalse._false;

			if (trueMAV == null)
				trueMAV = prevTrueFalse._prev;

			if (falseMAV == null)
				falseMAV = prevTrueFalse._prev;

			extVar.setIf(conditionNode, trueMAV, falseMAV);
		}
	}
}
