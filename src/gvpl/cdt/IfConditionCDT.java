package gvpl.cdt;

import gvpl.common.IVar;
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

public class IfConditionCDT {

	static void loadIfCondition(IASTIfStatement ifStatement, InstructionLine instructionLine) {
		Map<IVar, PrevTrueFalseNode> mapPrevTrueFalse = new LinkedHashMap<IVar, PrevTrueFalseNode>();
		Map<IVar, PrevTrueFalseMemVar> mapPrevTrueFalseMV = new LinkedHashMap<IVar, PrevTrueFalseMemVar>();

		BoolValuePack trueBvp = null;
		BoolValuePack falseBvp = null;
		{
			BasicBlockCDT basicBlock = loadBasicBlock(ifStatement.getThenClause(), instructionLine);

			trueBvp = new trueClass(instructionLine, basicBlock, mapPrevTrueFalse,
					mapPrevTrueFalseMV);
		}
		{
			BasicBlockCDT basicBlock = loadBasicBlock(ifStatement.getElseClause(), instructionLine);
			if (basicBlock != null)
				falseBvp = new falseClass(instructionLine, basicBlock, mapPrevTrueFalse,
						mapPrevTrueFalseMV);
		}

		IASTExpression condition = ifStatement.getConditionExpression();
		GraphNode conditionNode = instructionLine.loadValue(condition);

		IfCondition.createIfNodes(mapPrevTrueFalse, trueBvp._ifMergedNodes,
				falseBvp._ifMergedNodes, conditionNode, instructionLine.getGraph());

		IfCondition.mergeIfMAV(mapPrevTrueFalseMV, conditionNode, trueBvp._inToExtVar,
				falseBvp._inToExtVar);
	}

	static BasicBlockCDT loadBasicBlock(IASTStatement clause, InstructionLine instructionLine) {
		if (clause == null)
			return null;

		AstLoaderCDT parentBasicBlock = instructionLine.getParentBasicBlock();
		BasicBlockCDT basicBlock = new BasicBlockCDT(parentBasicBlock,
				instructionLine.getAstInterpreter());
		basicBlock.load(clause);
		return basicBlock;
	}

}
