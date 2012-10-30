package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.IfCondition;
import gvpl.common.InExtVarPair;
import gvpl.common.MemAddressVar;
import gvpl.graph.GraphNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;


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

		AstLoaderCDT parentBasicBlock = instructionLine.getParentBasicBlock();
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

public class IfConditionCDT {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<IVar, PrevTrueFalseNode>();
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();

		BoolValuePack trueBvp = new trueClass(instructionLine, ifStatement.getThenClause(),
				mapPrevTrueFalse, mapPrevTrueFalseMV);
		BoolValuePack falseBvp = new falseClass(instructionLine, ifStatement.getElseClause(),
				mapPrevTrueFalse, mapPrevTrueFalseMV);

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		IfCondition.createIfNodes(mapPrevTrueFalse, trueBvp._ifMergedNodes, falseBvp._ifMergedNodes,
				conditionNode, instructionLine.getGraph(), ifStatement.getFileLocation()
						.getStartingLineNumber());

		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, trueBvp._inToExtVar, falseBvp._inToExtVar);
	}
	
}
