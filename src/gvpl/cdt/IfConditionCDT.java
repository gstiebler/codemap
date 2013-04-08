package gvpl.cdt;

import gvpl.common.IVar;
import gvpl.common.InToExtVar;
import gvpl.common.ifclasses.BoolValuePack;
import gvpl.common.ifclasses.IfCondition;
import gvpl.common.ifclasses.PrevTrueFalseMemVar;
import gvpl.common.ifclasses.PrevTrueFalseNode;
import gvpl.common.ifclasses.falseClass;
import gvpl.common.ifclasses.trueClass;
import gvpl.graph.GraphNode;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

import debug.ExecTreeLogger;

public class IfConditionCDT {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition).getNode();
		loadIfCondition(conditionNode, ifStatement.getThenClause(), ifStatement.getElseClause(),
				instructionLine);
	}
	
	static void loadIfCondition(GraphNode conditionNode, IASTStatement thenClause,
			IASTStatement elseClause, InstructionLine instructionLine) {
		Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<IVar, PrevTrueFalseNode>();
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();

		BoolValuePack trueBvp = null;
		BoolValuePack falseBvp = null;
		{
			BasicBlockCDT basicBlock = loadBasicBlock(thenClause, instructionLine);

			trueBvp = new trueClass(instructionLine, basicBlock, mapPrevTrueFalse,
					mapPrevTrueFalseMV);
		}
		{
			BasicBlockCDT basicBlock = loadBasicBlock(elseClause, instructionLine);
			if (basicBlock != null)
				falseBvp = new falseClass(instructionLine, basicBlock, mapPrevTrueFalse,
						mapPrevTrueFalseMV);
		}

		Map<GraphNode, GraphNode> trueIfMergedNodes = null;
		Map<GraphNode, GraphNode> falseIfMergedNodes = null;
		InToExtVar trueInToExtVar = null;
		InToExtVar falseInToExtVar = null;
		if(trueBvp != null) {
			trueIfMergedNodes = trueBvp._ifMergedNodes;
			trueInToExtVar = trueBvp._inToExtVar;
		}
		if(falseBvp != null) {
			falseIfMergedNodes = falseBvp._ifMergedNodes;
			falseInToExtVar = falseBvp._inToExtVar;
		}
		
		IfCondition.createIfNodes(mapPrevTrueFalse, trueIfMergedNodes, falseIfMergedNodes,
				conditionNode, instructionLine.getGraph());

		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, trueInToExtVar, falseInToExtVar);
	}

	static BasicBlockCDT loadBasicBlock(IASTStatement clause, InstructionLine instructionLine) {
		if (clause == null)
			return null;

		ExecTreeLogger.log(clause.getRawSignature());

		AstLoaderCDT parentBasicBlock = instructionLine.getParentBasicBlock();
		BasicBlockCDT basicBlock = new BasicBlockCDT(parentBasicBlock,
				instructionLine.getAstInterpreter());
		basicBlock.load(clause);
		return basicBlock;
	}

}
