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
