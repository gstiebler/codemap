package gvpl.cdt;

import gvpl.common.ifclasses.IfCondition;
import gvpl.graph.Graph;
import gvpl.graph.GraphNode;

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
		ExecTreeLogger.log(conditionNode.getName());
		
		BasicBlockCDT trueBasicBlock = loadBasicBlock(thenClause, instructionLine);
		BasicBlockCDT falseBasicBlock = loadBasicBlock(elseClause, instructionLine);

		Graph graph = instructionLine.getGraph();
		IfCondition.createIfNodes(trueBasicBlock, falseBasicBlock, conditionNode, graph);
	
	}

	static BasicBlockCDT loadBasicBlock(IASTStatement clause, InstructionLine instructionLine) {
		if (clause == null)
			return null;

		ExecTreeLogger.log(clause.getRawSignature());

		BaseScopeCDT parentBasicBlock = instructionLine.getParentBasicBlock();
		BasicBlockCDT basicBlock = new BasicBlockCDT(parentBasicBlock,
				instructionLine.getAstInterpreter(), instructionLine.getGraph());
		basicBlock.load(clause);
		return basicBlock;
	}

}
